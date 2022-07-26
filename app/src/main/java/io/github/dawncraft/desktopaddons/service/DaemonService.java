package io.github.dawncraft.desktopaddons.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import io.github.dawncraft.desktopaddons.R;

/**
 * 用空的后台服务稍微提高进程的优先级, 不至于在内存足够时也被杀死<br/>
 * Android 旧版本也可使用此服务进行保活, 新版本则应引导用户允许自启和后台运行<br/>
 * 参考自 https://cloud.tencent.com/developer/article/1784046 及 QQ 的 CoreService
 *
 * @author QingChenW
 */
@SuppressLint("ObsoleteSdkInt")
public class DaemonService extends Service
{
    public static final String CHANNEL_ID = "FOREGROUND";
    public static final int NOTIFICATION_ID = 666;

    @Override
    public void onCreate()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            // API小于等于18可以用空通知启动前台服务
            startForeground(NOTIFICATION_ID, new Notification());
        }
        else
        {
            // API大于等于18需要弹出一个可见通知
            startForeground(NOTIFICATION_ID, createNotification(this));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1)
            {
                // API小于等于24可以利用bug把通知去掉
                Intent intent = new Intent(this, InternalService.class);
                startService(intent);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy()
    {
        stopForeground(true);
    }

    public static void startService(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = context.getString(R.string.foreground_channel_name);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(context, DaemonService.class);
        context.startService(intent);
    }

    public static void stopService(Context context)
    {
        Intent intent = new Intent(context, DaemonService.class);
        context.stopService(intent);
    }

    private static Notification createNotification(Context context)
    {
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(context.getString(R.string.is_running))
                .setShowWhen(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId(CHANNEL_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        return builder.build();
    }

    public static class InternalService extends Service
    {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId)
        {
            startForeground(NOTIFICATION_ID, createNotification(this));
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    SystemClock.sleep(1000);
                    stopForeground(true);
                    NotificationManager manager = (NotificationManager) getSystemService(
                            Context.NOTIFICATION_SERVICE);
                    manager.cancel(NOTIFICATION_ID);
                    stopSelf();
                }
            }).start();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent)
        {
            return null;
        }
    }
}
