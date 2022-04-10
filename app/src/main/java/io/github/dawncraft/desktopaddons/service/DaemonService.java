package io.github.dawncraft.desktopaddons.service;

import android.annotation.SuppressLint;
import android.app.Notification;
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
    private static final int NOTIFICATION_ID = 666;

    @Override
    public void onCreate()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            startForeground(NOTIFICATION_ID, new Notification());
        }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1)
        {
            // 如果API大于等于18, 需要弹出一个可见通知
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.drawable.ic_notification);
            startForeground(NOTIFICATION_ID, builder.build());
            Intent intent = new Intent(this, InternalService.class);
            startService(intent);
        }
        // API大于等于25就别尝试保活了, 没意义的
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1)
        {
            stopForeground(true);
        }
    }

    public static void startService(Context context)
    {
        Intent intent = new Intent(context, DaemonService.class);
        context.startService(intent);
    }

    public static void stopService(Context context)
    {
        Intent intent = new Intent(context, DaemonService.class);
        context.stopService(intent);
    }

    public static class InternalService extends Service
    {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            {
                Notification.Builder builder = new Notification.Builder(this);
                builder.setSmallIcon(R.drawable.ic_notification);
                startForeground(NOTIFICATION_ID, builder.build());
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        SystemClock.sleep(1000);
                        stopForeground(true);
                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        manager.cancel(NOTIFICATION_ID);
                        stopSelf();
                    }
                }).start();
                return super.onStartCommand(intent, flags, startId);
            }
            return START_NOT_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent)
        {
            return null;
        }
    }
}
