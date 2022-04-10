package io.github.dawncraft.desktopaddons.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuSystemProperties;

/**
 * 非系统应用访问系统属性
 * <br/>
 * 此类以Apache License 2.0协议开源
 *
 * @author QingChenW
 */
@SuppressLint("PrivateApi")
public final class SystemPropertyUtils
{
    public static boolean isDevelopmentSettingsEnabled(Context context)
    {
        try
        {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED) == 1;
        }
        catch (Settings.SettingNotFoundException ignored) {}
        return false;
    }

    public static String getProperty(String key, String def)
    {
        if (Shizuku.pingBinder())
        {
            try
            {
                return ShizukuSystemProperties.get(key, def);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getMethod("get", String.class, String.class);
            return (String) method.invoke(null, key, def);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void setProperty(String key, String value)
    {
        if (Shizuku.pingBinder())
        {
            try
            {
                ShizukuSystemProperties.set(key, value);
                return;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getMethod("set", String.class, String.class);
            method.invoke(null, key, value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void pokeSystemProps()
    {
        new PokerTask().execute();
    }

    // 来自 com.android.settingslib.development.SystemPropPoker
    // 这个类大括号不换行是因为这是从AOSP里直接复制出来的
    private static class PokerTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "PokerTask";
        // 来自 android.os.IBinder.SYSPROPS_TRANSACTION
        private static final int SYSPROPS_TRANSACTION = ('_'<<24)|('S'<<16)|('P'<<8)|'R';

        private String[] listServices() {
            try {
                Class<?> clazz = Class.forName("android.os.ServiceManager");
                Method listServicesMethod = clazz.getMethod("listServices");
                return (String[]) listServicesMethod.invoke(null);
            } catch (Exception ignored) {}
            return null;
        }

        private IBinder checkService(String service) {
            try {
                Class<?> clazz = Class.forName("android.os.ServiceManager");
                Method method = clazz.getMethod("checkService", String.class);
                return (IBinder) method.invoke(null, service);
            } catch (Exception ignored) {}
            return null;
        }

        @Override
        protected Void doInBackground(@NonNull Void... params) {
            String[] services = listServices();
            if (services == null) {
                Log.e(TAG, "There are no services, how odd");
                return null;
            }
            for (String service : services) {
                IBinder obj = checkService(service);
                if (obj != null) {
                    Parcel data = Parcel.obtain();
                    try {
                        obj.transact(SYSPROPS_TRANSACTION, data, null, 0);
                    } catch (RemoteException e) {
                        // Ignore
                    } catch (Exception e) {
                        Log.i(TAG, "Someone wrote a bad service '" + service
                                + "' that doesn't like to be poked", e);
                    }
                    data.recycle();
                }
            }
            return null;
        }
    }
}
