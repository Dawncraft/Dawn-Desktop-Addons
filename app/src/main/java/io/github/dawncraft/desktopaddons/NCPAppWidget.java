package io.github.dawncraft.desktopaddons;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Map;

/**
 * 监控新型冠状病毒的桌面小工具
 *
 * @author QingChenW (Wu Chen)
 */
public class NCPAppWidget extends AppWidgetProvider
{
    public static final String ACTION_OPEN = "desktopaddons.intent.action.OPEN";
    public static final String ACTION_REFRESH = "desktopaddons.intent.action.REFRESH";
    
    private static final String TAG = "NCPAppWidget";
    
    private int lastResult = 0;
    
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
        if (ACTION_OPEN.equals(action))
        {
            Log.i(TAG, "Action open");
            Utils.openUrl(context, NCPInfoLoader.NCP_QQ_NEWS);
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
        PendingResult result = goAsync();
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                lastResult = NCPInfoLoader.loadNCPData(DAApplication.getInstance());
            }
        });
        thread.start();
        try
        {
            // 我没别的招了
            thread.join();
        }
        catch (InterruptedException ignored)
        {
            lastResult = -233;
        }
        if (lastResult > 0)
        {
            for (int appWidgetId : appWidgetIds)
            {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
        else
        {
            switch (lastResult)
            {
                case 0: break;
                case -1: Utils.toast(context, "网络不可用, 无法获取新冠肺炎的最新数据"); break;
                case -2: Utils.toast(context, context.getString(R.string.ncp_app_widget_updating)); break;
                case -3: Utils.toast(context, "无法获取数据, 请与作者联系以解决这个问题"); break;
                case -4: Utils.toast(context, "无法解析JSON, 请与作者联系以解决这个问题"); break;
                default: Utils.toast(context, "发生了未知错误, 请与作者联系以解决这个问题" + lastResult); break;
            }
        }
        result.finish();
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        Map<String, String> data = NCPInfoLoader.getCachedNCPData();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ncp_app_widget);
        views.setTextViewText(R.id.textViewTime, data.get("date"));
        views.setTextViewText(R.id.textViewUpdate, data.get("update_time"));
        views.setTextViewText(R.id.textViewConfirm, data.get("confirm"));
        views.setTextViewText(R.id.textViewSuspect, data.get("suspect"));
        views.setTextViewText(R.id.textViewCure, data.get("cure"));
        views.setTextViewText(R.id.textViewDead, data.get("dead"));
        // TODO Android 8.0 后台服务限制,似乎无法正常工作
        // 将目标平台改为7.0,先凑合着
        Intent intentOpen = new Intent(ACTION_OPEN);
        PendingIntent pendingIntentOpen = PendingIntent.getBroadcast(context, -233, intentOpen, 0);
        views.setOnClickPendingIntent(R.id.imageButtonOpen, pendingIntentOpen);
        Intent intentRefresh = new Intent(ACTION_REFRESH);
        // intentRefresh.setComponent(new ComponentName(context, NCPAppWidget.class));
        intentRefresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntentRefresh = PendingIntent.getBroadcast(context, appWidgetId, intentRefresh, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.imageButtonRefresh, pendingIntentRefresh);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
