package io.github.dawncraft.desktopaddons;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
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
            Log.i(TAG, "Action open");
            Utils.openUrl(context, NCPInfoModel.NCP_QQ_NEWS);
            // 小米android4.4会崩溃
            /*
            Utils.runOnUIThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Context context = DAApplication.getInstance();

                    LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View view = layoutInflater.inflate(R.layout.ncp_popup_window, new FrameLayout(context));

                    final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                    windowManager.addView(view, layoutParams);

                    ImageButton imageButtonClose = view.findViewById(R.id.imageButtonClose);
                    imageButtonClose.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            windowManager.removeView(view);
                        }
                    });

                    WebView webView = view.findViewById(R.id.webView);
                    webView.loadUrl(NCPInfoLoader.NCP_QQ_NEWS);

                    TextView textViewTitle = view.findViewById(R.id.textViewTitle);
                    textViewTitle.setText(webView.getTitle());
                }
            });
            */
        }
        else if (ACTION_REFRESH.equals(action))
        {
            Log.i(TAG, "Action refresh");
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
        PendingResult pendingResult = goAsync();
        NCPInfoModel.EnumResult result = NCPInfoModel.loadData();
        switch (result)
        {
            case SUCCESS:
            case CACHED:
                break;
            case UPDATING: Utils.toast(context, context.getString(R.string.ncp_app_widget_updating)); break;
            case NO_NETWORK: Utils.toast(context, "无网络连接, 无法获取新冠肺炎的最新数据"); break;
            case IO_ERROR: Utils.toast(context, "无法获取数据, 请联系作者以解决这个问题"); break;
            case JSON_ERROR: Utils.toast(context, "无法解析JSON, 请联系作者以解决这个问题"); break;
            default: Utils.toast(context, "发生了未知错误, 请联系作者: " + result.toString()); break;
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
        views.setTextViewText(R.id.textViewTime, item.isVaild ? item.date : "XXXX-XX-XX XX:XX:XX");
        views.setTextViewText(R.id.textViewUpdate, item.isVaild ? item.updateTime : "XXXX-XX-XX XX:XX:XX");
        views.setTextViewText(R.id.textViewConfirm, item.isVaild ? String.valueOf(item.confirm) : "-");
        views.setTextViewText(R.id.textViewSuspect, item.isVaild ? String.valueOf(item.suspect) : "-");
        views.setTextViewText(R.id.textViewCure, item.isVaild ? String.valueOf(item.cure) : "-");
        views.setTextViewText(R.id.textViewDead, item.isVaild ? String.valueOf(item.dead) : "-");
        // FIXME Android 8.0 后台限制, 隐式广播无法正常工作
        // 详见 https://www.jianshu.com/p/5283ebc225d5
        Intent intentOpen = new Intent(ACTION_DETAILS);
        intentOpen.setComponent(new ComponentName(context, NCPAppWidget.class));
        PendingIntent pendingIntentOpen = PendingIntent.getBroadcast(context, -233, intentOpen, 0);
        views.setOnClickPendingIntent(R.id.imageButtonOpen, pendingIntentOpen);
        Intent intentRefresh = new Intent(ACTION_REFRESH);
        intentRefresh.setComponent(new ComponentName(context, NCPAppWidget.class));
        intentRefresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntentRefresh = PendingIntent.getBroadcast(context, appWidgetId, intentRefresh, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.imageButtonRefresh, pendingIntentRefresh);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
