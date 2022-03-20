package io.github.dawncraft.desktopaddons;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

import io.github.dawncraft.desktopaddons.broadcast.ZenModeBroadcastReceiver;
import io.github.dawncraft.desktopaddons.model.NCPDataSource;
import io.github.dawncraft.desktopaddons.util.HttpUtils;

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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        database = Room.databaseBuilder(getApplicationContext(), DADatabase.class, "db")
                .allowMainThreadQueries()
                // .enableMultiInstanceInvalidation()
                .build();
        NCPDataSource.loadNamesFromRes(this);
        HttpUtils.init(this);
        if (sharedPreferences.getBoolean("zen_mode_switch", false))
        {
            ZenModeBroadcastReceiver.register(this);
        }
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
