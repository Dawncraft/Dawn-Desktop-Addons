package io.github.dawncraft.desktopaddons;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * 监控新型冠状病毒的桌面小工具
 *
 * @author QingChenW (Wu Chen)
 */
public class NCPAppWidget extends AppWidgetProvider
{
    public static final String ACTION_DETAILS = "desktopaddons.intent.action.DETAILS";
    public static final String ACTION_REFRESH = "desktopaddons.intent.action.REFRESH";
    
    private static final String TAG = "NCPAppWidget";
    
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
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_DETAILS.equals(action))
        {
            Log.d(TAG, "Action open");
            // TODO 自己写详情页
            int id = Integer.parseInt(DAApplication.getSharedPreferences().getString("ncp_source", "0"));
            Utils.openUrl(context, NCPInfoModel.getSourceUrl(id));
        }
        else if (ACTION_REFRESH.equals(action))
        {
            Log.d(TAG, "Action refresh");
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID))
            {
                int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                onUpdate(context, AppWidgetManager.getInstance(context), new int[] { appWidgetId });
            }
        }
    }
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // 主线程上不能执行耗费时间过长的操作, 也不能执行网络等操作, 除非开启严格模式, 但正式环境不应使用严格模式
        PendingResult pendingResult = goAsync();
        final NCPInfoModel.EnumResult[] result = new NCPInfoModel.EnumResult[] { NCPInfoModel.EnumResult.UNKNOWN };
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    result[0] = NCPInfoModel.loadData();
                }
            });
            thread.start();
            thread.join();
        }
        catch (InterruptedException ignored) {}
        switch (result[0])
        {
            case SUCCESS:
            case CACHED:
                break;
            case UPDATING: Utils.toast(context, context.getString(R.string.ncp_app_widget_updating)); break;
            case NO_NETWORK: Utils.toast(context, context.getString(R.string.no_network)); break;
            case IO_ERROR: Utils.toast(context, context.getString(R.string.ncp_app_widget_no_data)); break;
            case JSON_ERROR: Utils.toast(context, context.getString(R.string.ncp_app_widget_no_json)); break;
            case UNKNOWN:
            default: Utils.toast(context, context.getString(R.string.unknown_error)); break;
        }
        for (int appWidgetId : appWidgetIds)
        {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        pendingResult.finish();
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        NCPInfoItem item = NCPInfoModel.getInfoItem("");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ncp_app_widget);
        // FIXME Android 3.0起点击小部件默认会跳转至应用主Activity
        // 详见 https://developer.android.google.cn/guide/topics/appwidgets/host#which-version-are-you-targeting
        // 详见 android.appwidget.AppWidgetHostView#onDefaultViewClicked
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, -666, new Intent(), 0);
        views.setOnClickPendingIntent(R.id.ncp_widget_layout, pendingIntent);
        if (item.isVaild)
        {
            views.setTextViewText(R.id.textViewTime, item.date);
            views.setTextViewText(R.id.textViewUpdate, item.updateTime);
            views.setTextViewText(R.id.textViewConfirm, String.valueOf(item.confirm));
            views.setTextViewText(R.id.textViewSuspect, String.valueOf(item.suspect));
            views.setTextViewText(R.id.textViewCure, String.valueOf(item.cure));
            views.setTextViewText(R.id.textViewDead, String.valueOf(item.dead));
        }
        // FIXME Android 8.0 后台限制, 隐式广播无法正常工作
        // 详见 https://www.jianshu.com/p/5283ebc225d5
        Intent intentOpen = new Intent(ACTION_DETAILS);
        intentOpen.setClass(context, NCPAppWidget.class);
        PendingIntent pendingIntentOpen = PendingIntent.getBroadcast(context, -1, intentOpen, 0);
        views.setOnClickPendingIntent(R.id.imageButtonOpen, pendingIntentOpen);
        Intent intentRefresh = new Intent(ACTION_REFRESH);
        intentRefresh.setClass(context, NCPAppWidget.class);
        intentRefresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntentRefresh = PendingIntent.getBroadcast(context, appWidgetId, intentRefresh, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.imageButtonRefresh, pendingIntentRefresh);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
