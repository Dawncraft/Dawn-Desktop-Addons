package io.github.dawncraft.desktopaddons.broadcast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.util.Utils;

/**
 * 切换勿扰模式的<s>锁屏小部件</s>锁屏通知
 *
 * @author QingChenW
 */
// NOTE 为防止流氓软件后台自启, 从Android 3.1起未启动的应用无法接收广播
// 详见 https://developer.android.com/about/versions/android-3.1.html#launchcontrols
public class ZenModeBroadcastReceiver extends BroadcastReceiver
{
    public static final String ACTION_SWITCH = "desktopaddons.intent.action.SWITCH_ZEN_MODE";
    public static final String CHANNEL_ID = "ZEN_MODE";
    public static final int NOTIFICATION_ID = 233;
    private static final String TAG = "ZenModeBroadcast";

    private static ZenModeBroadcastReceiver instance;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (Intent.ACTION_USER_PRESENT.equals(action))
        {
            Log.d(TAG, "Action user present");
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.cancel(NOTIFICATION_ID);
            return;
        }
        boolean isZenMode = Utils.isZenMode(context);
        if (ACTION_SWITCH.equals(action))
        {
            Log.d(TAG, "Action switch zen mode");
            Utils.setZenMode(context, !isZenMode);
            sendNotification(context, !isZenMode);
        }
        // 有些系统可以灭屏解锁, 所以这里接收屏幕关闭的广播而不是屏幕开启的
        else if (Intent.ACTION_SCREEN_OFF.equals(action))
        {
            Log.d(TAG, "Action screen off");
            sendNotification(context, isZenMode);
        }
    }

    private static void sendNotification(Context context, boolean checked)
    {
        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_zen_mode);
        notificationLayout.setImageViewResource(R.id.imageButtonZen, R.drawable.ic_zen_mode);
        if (checked)
        {
            notificationLayout.setTextViewText(R.id.textViewZen, context.getString(R.string.zen_mode_open));
            TypedValue typedValue = new TypedValue();
            int color;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                color = context.getTheme().resolveAttribute(R.attr.colorControlActivated, typedValue, true)
                        ? typedValue.data : context.getColor(android.R.color.holo_blue_light);
            }
            else
            {
                color = context.getResources().getColor(android.R.color.holo_blue_light);
            }
            notificationLayout.setInt(R.id.imageButtonZen, "setColorFilter", color);
        }
        Intent intent = new Intent(context, ZenModeBroadcastReceiver.class);
        intent.setAction(ACTION_SWITCH);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, Utils.FLAG_IMMUTABLE);
        notificationLayout.setOnClickPendingIntent(R.id.imageButtonZen, pendingIntent);
        // NOTE Android 5.0 之后, 谷歌为了使通知栏图标更加统一, 小图标必须是背景镂空只包含黑白两色的透明图片, 否则出错会变成小白块
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setShowWhen(false)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContent(notificationLayout)
                .setCustomContentView(notificationLayout)
                .setSound(null)
                .setVibrate(null)
                .setLights(0, 0, 0)
                .setOngoing(true)
                // .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(NOTIFICATION_ID, notification);
    }

    public static void register(Context context)
    {
        if (instance != null)
        {
            Log.e(TAG, "Receiver has been registered");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = context.getString(R.string.zen_mode_channel_name);
            String description = context.getString(R.string.zen_mode_channel_desc);
            NotificationChannel channel = new NotificationChannel(
                    ZenModeBroadcastReceiver.CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            channel.setSound(null, null);
            channel.setVibrationPattern(null);
            channel.enableLights(false);
            channel.setShowBadge(false);
            channel.setBypassDnd(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.createNotificationChannel(channel);
        }
        instance = new ZenModeBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        // filter.addAction(ZenModeBroadcastReceiver.ACTION_SWITCH);
        context.getApplicationContext().registerReceiver(instance, filter);
    }

    public static void unregister(Context context)
    {
        if (instance != null)
        {
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.cancel(NOTIFICATION_ID);
            context.getApplicationContext().unregisterReceiver(instance);
            instance = null;
        }
    }
}
