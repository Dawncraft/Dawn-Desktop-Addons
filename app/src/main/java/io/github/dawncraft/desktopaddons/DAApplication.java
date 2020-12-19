package io.github.dawncraft.desktopaddons;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

/**
 * 曙光桌面小工具APP
 *
 * @author QingChenW (Wu Chen)
 */
public class DAApplication extends Application
{
    private static Context instance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = getApplicationContext();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .permitAll()
                // .penaltyLog()
                .build());
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        instance = null;
    }

    public static Context getInstance()
    {
        return instance;
    }
}
