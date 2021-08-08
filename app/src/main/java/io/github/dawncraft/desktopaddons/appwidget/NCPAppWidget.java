package io.github.dawncraft.desktopaddons.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
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
import io.github.dawncraft.desktopaddons.model.NCPInfoModel;
import io.github.dawncraft.desktopaddons.ui.WebViewActivity;

/**
 * 监控新型冠状病毒肺炎疫情数据的桌面小工具
 *
 * @author QingChenW
 */
public class NCPAppWidget extends AppWidgetProvider
{
    public static final String ACTION_DETAILS = "desktopaddons.intent.action.DETAILS";
    public static final String ACTION_REFRESH = "desktopaddons.intent.action.REFRESH";
    private static final String TAG = "NCPAppWidget";

    private final NCPAppWidgetDAO ncpAppWidgetDAO = DAApplication.getDatabase().ncpAppWidgetDAO();
    
    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context)
    {
        super.onDisabled(context);
//        WorkManager.getInstance(context)
//                .cancelAllWork();
        ncpAppWidgetDAO.deleteAll();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        final PendingResult pendingResult = goAsync();
        NCPInfoModel ncpInfoModel = new NCPInfoModel();
        for (int appWidgetId : appWidgetIds)
        {
            NCPAppWidgetID ncpAppWidgetID = ncpAppWidgetDAO.findById(appWidgetId);
            if (ncpAppWidgetID == null) continue;
//            OneTimeWorkRequest workRequest = NCPInfoWorker.requestWork(appWidgetId, ncpAppWidgetID.region);
//            WorkManager.getInstance(context)
//                    .beginUniqueWork("ncp-" + appWidgetId, ExistingWorkPolicy.KEEP, workRequest)
//                    .enqueue();
            // FIXME 使用WorkManager会导致小部件无限更新, 暂时回退到创建新线程
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    ncpInfoModel.getRegionInfo(ncpAppWidgetID.region, new NCPInfoModel.OnRegionDataListener()
                    {
                        @Override
                        public void onResponse(NCPInfoModel.Result result, NCPInfo info)
                        {
                            RemoteViews views = createRemoteViews(context, appWidgetId, info);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        }
                    });
                }
            });
            thread.start();
            try
            {
                thread.join();
            }
            catch (InterruptedException ignored) {}
        }
        pendingResult.finish();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds)
        {
//            WorkManager.getInstance(context)
//                    .cancelUniqueWork("ncp-" + appWidgetId);
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
            Intent newIntent = new Intent(context, WebViewActivity.class);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.putExtra("url", url);
            context.startActivity(newIntent);
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

    public static RemoteViews createRemoteViews(Context context, int appWidgetId, NCPInfo ncpInfo)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget_ncp_info);
        // NOTE Android 3.0起点击小部件默认会跳转至应用主Activity
        // 详见 https://developer.android.google.cn/guide/topics/appwidgets/host#which-version-are-you-targeting
        // 详见 android.appwidget.AppWidgetHostView#onDefaultViewClicked
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
        views.setOnClickPendingIntent(R.id.layoutNCPWidget, pendingIntent);
        // NOTE Android 8.0 后台限制, 隐式广播无法正常工作
        // 详见 https://www.jianshu.com/p/5283ebc225d5
        Intent intentOpen = new Intent(context, NCPAppWidget.class);
        intentOpen.setAction(ACTION_DETAILS);
        PendingIntent pendingIntentOpen = PendingIntent.getBroadcast(context, -1, intentOpen, 0);
        views.setOnClickPendingIntent(R.id.imageButtonOpen, pendingIntentOpen);
        Intent intentRefresh = new Intent(context, NCPAppWidget.class);
        intentRefresh.setAction(ACTION_REFRESH);
        intentRefresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntentRefresh = PendingIntent.getBroadcast(context, appWidgetId, intentRefresh, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.imageButtonRefresh, pendingIntentRefresh);
        if (ncpInfo != null)
        {
            String[] areas = ncpInfo.getRegion().split(",");
            if (areas.length > 1)
            {
                String title = String.format(context.getString(R.string.ncp_app_widget_region), areas[areas.length - 1]);
                views.setTextViewText(R.id.textViewTitle, title);
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
}
