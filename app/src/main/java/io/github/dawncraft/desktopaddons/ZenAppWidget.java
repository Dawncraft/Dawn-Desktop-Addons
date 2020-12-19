package io.github.dawncraft.desktopaddons;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/**
 * 开启/关闭勿扰模式的锁屏小部件
 *
 * @author QingChenW (Wu Chen)
 */
public class ZenAppWidget extends AppWidgetProvider
{
    private static final String TAG = "ZenAppWidget";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        String action = intent.getAction();

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }
}
