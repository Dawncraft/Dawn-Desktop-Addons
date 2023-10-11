package io.github.dawncraft.desktopaddons;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.StrictMode;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.pluto.Pluto;
import com.pluto.plugins.datastore.pref.PlutoDatastorePreferencesPlugin;
import com.pluto.plugins.exceptions.PlutoExceptionsPlugin;
import com.pluto.plugins.layoutinspector.PlutoLayoutInspectorPlugin;
import com.pluto.plugins.logger.PlutoLoggerPlugin;
import com.pluto.plugins.network.PlutoNetworkPlugin;
import com.pluto.plugins.preferences.PlutoSharePreferencesPlugin;
import com.pluto.plugins.rooms.db.PlutoRoomsDBWatcher;
import com.pluto.plugins.rooms.db.PlutoRoomsDatabasePlugin;

import io.github.dawncraft.desktopaddons.broadcast.ZenModeBroadcastReceiver;
import io.github.dawncraft.desktopaddons.model.NCPDataSource;
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
        new Pluto.Installer(this)
                .addPlugin(new PlutoNetworkPlugin())
                .addPlugin(new PlutoExceptionsPlugin())
                .addPlugin(new PlutoLoggerPlugin())
                .addPlugin(new PlutoSharePreferencesPlugin())
                .addPlugin(new PlutoRoomsDatabasePlugin())
                .addPlugin(new PlutoDatastorePreferencesPlugin())
                .addPlugin(new PlutoLayoutInspectorPlugin())
                .install();
        initializeStrictMode();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        database = Room.databaseBuilder(this, DADatabase.class, DADatabase.DB_NAME)
                .allowMainThreadQueries()
                // .enableMultiInstanceInvalidation()
                .build();
        PlutoRoomsDBWatcher.INSTANCE.watch(DADatabase.DB_NAME, DADatabase.class);
        NCPDataSource.loadNamesFromRes(this);
        HttpUtils.init(this);
        if (sharedPreferences.getBoolean("zen_mode_switch", false))
        {
            ZenModeBroadcastReceiver.register(this);
        }
        int interval = Integer.parseInt(sharedPreferences
                .getString("ncp_update_interval", "360"));
        NCPInfoWorker.startSyncWork(this, interval);
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        ZenModeBroadcastReceiver.unregister(this);
        sharedPreferences = null;
    }

    private void initializeStrictMode()
    {
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build()
        );
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build()
        );
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
