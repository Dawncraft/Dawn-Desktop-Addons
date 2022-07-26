package io.github.dawncraft.desktopaddons.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.dao.NCPAppWidgetDAO;
import io.github.dawncraft.desktopaddons.entity.NCPAppWidgetID;
import io.github.dawncraft.desktopaddons.entity.NCPInfo;
import io.github.dawncraft.desktopaddons.model.NCPDataSource;
import io.github.dawncraft.desktopaddons.util.HttpUtils;
import io.github.dawncraft.desktopaddons.util.Utils;
import io.github.dawncraft.desktopaddons.worker.NCPInfoWorker;

/**
 * 监控新型冠状病毒肺炎疫情数据的桌面小部件
 *
 * @author QingChenW
 */
public class NCPAppWidget extends AppWidgetProvider
{
    public static final String ACTION_DETAILS = "desktopaddons.intent.action.DETAILS";
    public static final String ACTION_REFRESH = "desktopaddons.intent.action.REFRESH";
    public static final String ACTION_PINNED = "desktopaddons.intent.action.PINNED";
    public static final String EXTRA_NCP_REGION = "ncpRegion";
    private static final String TAG = "NCPAppWidget";

    private final NCPAppWidgetDAO ncpAppWidgetDAO = DAApplication.getDatabase().ncpAppWidgetDAO();

    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
        int interval = Integer.parseInt(DAApplication.getPreferences()
                        .getString("ncp_update_interval", "360"));
        NCPInfoWorker.startSyncWork(context, interval);
    }

    @Override
    public void onDisabled(Context context)
    {
        super.onDisabled(context);
        NCPInfoWorker.stopAllWorks(context);
        ncpAppWidgetDAO.deleteAll();
    }

    // NOTE 在onUpdate中使用WorkManager会导致小部件无限更新, 因此现在仅由WorkManager更新
    // 原因: https://commonsware.com/blog/2018/11/24/workmanager-app-widgets-side-effects.html
    // NOTE 在启用/禁用组件时系统会收到ACTION_PACKAGE_CHANGED广播, 进而向AppWidgetProvider发送ACTION_APPWIDGET_UPDATE广播
    // 所以onUpdate这个方法还不能删(捂脸
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds)
        {
            NCPInfoWorker.requestWork(context, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds)
        {
            ncpAppWidgetDAO.deleteById(appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_DETAILS.equals(action))
        {
            Log.d(TAG, "Action open");
            int id = Integer.parseInt(DAApplication.getPreferences().getString("ncp_data_source", "0"));
            String url = NCPDataSource.getSourceUrl(id);
            HttpUtils.openUrl(context, url, true);
        }
        else if (ACTION_REFRESH.equals(action))
        {
            Log.d(TAG, "Action refresh");
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID))
            {
                int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                NCPInfoWorker.requestWork(context, appWidgetId);
            }
        }
        else if (ACTION_PINNED.equals(action))
        {
            Log.d(TAG, "Action pinned");
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID))
            {
                int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                NCPAppWidgetID ncpAppWidgetID = new NCPAppWidgetID();
                ncpAppWidgetID.id = appWidgetId;
                ncpAppWidgetID.region = extras.getString(EXTRA_NCP_REGION);
                ncpAppWidgetDAO.insert(ncpAppWidgetID);
                NCPInfoWorker.requestWork(context, appWidgetId);
            }
        }
    }

    public static RemoteViews createViews(Context context, NCPInfo ncpInfo)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget_ncp_info);
        if (ncpInfo != null)
        {
            String[] areas = ncpInfo.getRegion().split(",");
            if (areas.length > 1)
            {
                String title = context.getString(R.string.ncp_app_widget_region, areas[areas.length - 1]);
                views.setTextViewText(R.id.textViewTitle, title);
            }
            else
            {
                views.setTextViewText(R.id.textViewTitle, context.getString(R.string.ncp_app_widget_title));
            }
            views.setTextViewText(R.id.textViewTime, ncpInfo.getDate());
            views.setTextViewText(R.id.textViewUpdate, ncpInfo.getUpdateTime());
            views.setTextViewText(R.id.textViewConfirm, String.valueOf(ncpInfo.getConfirm()));
            views.setTextViewText(R.id.textViewSuspect, String.valueOf(ncpInfo.getSuspect()));
            views.setTextViewText(R.id.textViewCure, String.valueOf(ncpInfo.getCure()));
            views.setTextViewText(R.id.textViewDead, String.valueOf(ncpInfo.getDead()));
        }
        return views;
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, NCPInfo ncpInfo)
    {
        RemoteViews views = createViews(context, ncpInfo);
        // NOTE Android 3.0 起点击小部件默认会跳转至应用主Activity
        // 详见 https://developer.android.google.cn/guide/topics/appwidgets/host#which-version-are-you-targeting
        // 详见 android.appwidget.AppWidgetHostView#onDefaultViewClicked
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(), Utils.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.layoutNCPWidget, pendingIntent);
        // NOTE Android 8.0 后台限制, 隐式广播无法正常工作
        // 详见 https://www.jianshu.com/p/5283ebc225d5
        Intent intentOpen = new Intent(context, NCPAppWidget.class);
        intentOpen.setAction(ACTION_DETAILS);
        PendingIntent pendingIntentOpen = PendingIntent.getBroadcast(context, -1, intentOpen, Utils.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.imageButtonOpen, pendingIntentOpen);
        Intent intentRefresh = new Intent(context, NCPAppWidget.class);
        intentRefresh.setAction(ACTION_REFRESH);
        intentRefresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntentRefresh = PendingIntent.getBroadcast(context, appWidgetId, intentRefresh,
                Utils.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.imageButtonRefresh, pendingIntentRefresh);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void notifyUpdate(Context context, int[] appWidgetIds)
    {
        Intent intent = new Intent(context, NCPAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(intent);
    }

    public static void notifyUpdateAll(Context context)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, NCPAppWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        notifyUpdate(context, appWidgetIds);
    }

    public static boolean requestPin(Context context, NCPInfo ncpInfo)
    {
        if (!Utils.isPinAppWidgetSupported(context)) return false;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, NCPAppWidget.class);
        Bundle extras = new Bundle();
        extras.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, createViews(context, ncpInfo));
        Intent intent = new Intent(context, NCPAppWidget.class);
        intent.setAction(ACTION_PINNED);
        if (ncpInfo != null)
        {
            intent.putExtra(EXTRA_NCP_REGION, ncpInfo.getRegion());
        }
        PendingIntent successCallback = PendingIntent.getBroadcast(context, 0, intent,
                Utils.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return appWidgetManager.requestPinAppWidget(componentName, extras, successCallback);
    }
}
