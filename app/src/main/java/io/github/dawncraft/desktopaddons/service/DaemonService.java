package io.github.dawncraft.desktopaddons.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DaemonService extends Service
{
    @Override
    public void onCreate()
    {
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
    }
}
