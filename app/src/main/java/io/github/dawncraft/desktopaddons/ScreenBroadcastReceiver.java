package io.github.dawncraft.desktopaddons;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ScreenBroadcastReceiver extends BroadcastReceiver
{
    public static final String ACTION_SWITCH = "desktopaddons.intent.action.SWITCH_ZEN_MODE";

    public static final String CHANNEL_ID = "ZEN";
    public static final int NOTIFICATION_ID = 233;

    private static final String TAG = "ScreenBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (Intent.ACTION_USER_PRESENT.equals(action))
        {
            Log.d(TAG, "Action user present");
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(NOTIFICATION_ID);
            return;
        }
        boolean isZenMode = Utils.isZenMode(context);
        if (ACTION_SWITCH.equals(action))
        {
            Log.d(TAG, "Action switch zen mode");
            Utils.setZenMode(context, !isZenMode);
            sendNotification(context, !isZenMode);
        }
        else if (Intent.ACTION_SCREEN_OFF.equals(action))
        {
            Log.d(TAG, "Action screen off");
            sendNotification(context, isZenMode);
        }
        else if (Intent.ACTION_SCREEN_ON.equals(action))
        {
            Log.d(TAG, "Action screen on");
            sendNotification(context, isZenMode);
        }
    }

    private void sendNotification(Context context, boolean checked)
    {
        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.zen_notification);
        notificationLayout.setImageViewResource(R.id.imageButtonZen, R.drawable.ic_zen_mode);
        if (checked)
        {
            notificationLayout.setTextViewText(R.id.textViewZen, context.getString(R.string.zen_mode_open));
            int color;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                color = context.getColor(android.R.color.holo_blue_light);
            else
                color = context.getResources().getColor(android.R.color.holo_blue_light);
            notificationLayout.setInt(R.id.imageButtonZen, "setColorFilter", color);
        }
        Intent intent = new Intent(ACTION_SWITCH);
        intent.setClass(context, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, -233, intent, 0);
        notificationLayout.setOnClickPendingIntent(R.id.imageButtonZen, pendingIntent);
        Notification customNotification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setShowWhen(false)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContent(notificationLayout)
                .setCustomContentView(notificationLayout)
                .setSound(null)
                .setVibrate(null)
                .setLights(0, 0, 0)
                .setOngoing(true)
                //.setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, customNotification);
    }
}
