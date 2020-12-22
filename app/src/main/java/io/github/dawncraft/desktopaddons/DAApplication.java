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

/**
 * 曙光桌面小工具APP
 *
 * @author QingChenW (Wu Chen)
 */
public class DAApplication extends Application
{
    private static Context instance;

    private ScreenBroadcastReceiver screenReceiver;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = getApplicationContext();

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        boolean zenMode = sharedPreferences.getBoolean("zen_mode", false);

        createNotificationChannel();
        if (zenMode)
        {
            screenReceiver = new ScreenBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            // filter.addAction(ScreenBroadcastReceiver.ACTION_SWITCH);
            instance.registerReceiver(screenReceiver, filter);
        }
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        if (screenReceiver != null)
        {
            instance.unregisterReceiver(screenReceiver);
            screenReceiver = null;
        }
        instance = null;
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ScreenBroadcastReceiver.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setSound(null, null);
            channel.setVibrationPattern(null);
            channel.enableLights(false);
            channel.setBypassDnd(true);
            channel.setShowBadge(false);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static Context getInstance()
    {
        return instance;
    }
}
