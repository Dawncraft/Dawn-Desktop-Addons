package io.github.dawncraft.desktopaddons;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

import io.github.dawncraft.desktopaddons.broadcast.ZenModeBroadcastReceiver;
import io.github.dawncraft.desktopaddons.model.NCPDataSource;
import io.github.dawncraft.desktopaddons.service.DaemonService;
import io.github.dawncraft.desktopaddons.util.HttpUtils;
import io.github.dawncraft.desktopaddons.worker.NCPInfoWorker;

/**
 * 曙光桌面小部件APP
 *
 * @author QingChenW (Wu Chen)
 */
public class DAApplication extends Application
{
    private static SharedPreferences sharedPreferences;
    private static DADatabase database;

    @Override
    public void onCreate()
    {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!getProcessName().equals(getPackageName()))
            {
                return;
            }
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        database = Room.databaseBuilder(this, DADatabase.class, "db")
                .allowMainThreadQueries()
                // .enableMultiInstanceInvalidation()
                .build();
        NCPDataSource.loadNamesFromRes(this);
        HttpUtils.init(this);
        if (sharedPreferences.getBoolean("zen_mode_switch", false))
        {
            ZenModeBroadcastReceiver.register(this);
        }
        int interval = Integer.parseInt(sharedPreferences
                .getString("ncp_update_interval", "360"));
        NCPInfoWorker.startSyncWork(this, interval);
        //Intent intent = new Intent(this, DaemonService.class);
        //startService(intent);
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        ZenModeBroadcastReceiver.unregister(this);
        sharedPreferences = null;
    }

    public static SharedPreferences getPreferences()
    {
        return sharedPreferences;
    }

    public static DADatabase getDatabase()
    {
        return database;
    }

    public static boolean hasToken()
    {
        return sharedPreferences.contains("token");
    }

    public static String getToken()
    {
        return sharedPreferences.getString("token", null);
    }

    public static void setToken(String token)
    {
        sharedPreferences.edit()
                .putString("token", token)
                .putLong("refresh_timer", System.currentTimeMillis())
                .apply();
    }

    public static void removeToken()
    {
        sharedPreferences.edit()
                .remove("token")
                .remove("refresh_timer")
                .apply();
    }

    public static boolean needRefresh()
    {
        long refreshTimer = sharedPreferences.getLong("refresh_timer", 0);
        return hasToken() && System.currentTimeMillis() - refreshTimer > 86400 * 1000;
    }
}
