package io.github.dawncraft.desktopaddons;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;

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

    private ZenModeBroadcastReceiver zenModeBroadcastReceiver;

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
        createNotificationChannel();
        boolean zenMode = sharedPreferences.getBoolean("zen_mode_switch", false);
        if (zenMode)
        {
            zenModeBroadcastReceiver = new ZenModeBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_USER_PRESENT);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            // filter.addAction(ScreenBroadcastReceiver.ACTION_SWITCH);
            registerReceiver(zenModeBroadcastReceiver, filter);
        }
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        if (zenModeBroadcastReceiver != null)
        {
            unregisterReceiver(zenModeBroadcastReceiver);
            zenModeBroadcastReceiver = null;
        }
        sharedPreferences = null;
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            CharSequence name = getString(R.string.zen_mode_channel_name);
            String description = getString(R.string.zen_mode_channel_desc);
            NotificationChannel channel = new NotificationChannel(
                    ZenModeBroadcastReceiver.CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setSound(null, null);
            channel.setVibrationPattern(null);
            channel.enableLights(false);
            channel.setBypassDnd(true);
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static SharedPreferences getPreferences()
    {
        return sharedPreferences;
    }

    public static DADatabase getDatabase()
    {
        return database;
    }
}
