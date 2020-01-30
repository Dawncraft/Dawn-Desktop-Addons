package io.github.dawncraft.desktopaddons;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import java.util.Map;

/**
 * 监控新型冠状病毒的桌面小工具
 *
 * @author QingChenW (Wu Chen)
 */
public class nCoVAppWidget extends AppWidgetProvider
{
    public static final String ACTION_REFRESH = "desktopaddons.intent.action.REFRESH";
    public static final String ACTION_CLICK = "desktopaddons.intent.action.CLICK";

    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context)
    {
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        final PendingResult result = goAsync();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (nCoVInfoLoader.loadnCoVData(context))
                    {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (int appWidgetId : appWidgetIds)
                                {
                                    updateAppWidget(context, appWidgetManager, appWidgetId);
                                }
                                result.finish();
                            }
                        });
                    }
                    else
                    {
                        result.finish();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    result.finish();
                }
            }
        }).start();
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_REFRESH.equals(action))
        {
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID))
            {
                final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                this.onUpdate(context, AppWidgetManager.getInstance(context), new int[] { appWidgetId });
            }
        }
        else if (ACTION_CLICK.equals(action))
        {
            // 打开
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        Map<String, String> data = nCoVInfoLoader.getCachednCoVData();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ncov_app_widget);
        views.setTextViewText(R.id.textViewTime, data.get("date"));
        views.setTextViewText(R.id.textViewUpdate, data.get("update_time"));
        views.setTextViewText(R.id.textViewConfirm, data.get("confirm"));
        views.setTextViewText(R.id.textViewSuspect, data.get("suspect"));
        views.setTextViewText(R.id.textViewCure, data.get("cure"));
        views.setTextViewText(R.id.textViewDead, data.get("dead"));
        Intent intent = new Intent(ACTION_REFRESH);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.imageButtonRefresh, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
