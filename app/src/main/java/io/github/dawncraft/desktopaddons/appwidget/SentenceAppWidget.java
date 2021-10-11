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

import org.json.JSONException;

import java.io.IOException;

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.entity.Sentence;
import io.github.dawncraft.desktopaddons.model.SentenceModel;

public class SentenceAppWidget extends AppWidgetProvider
{
    public static final String ACTION_REFRESH = "desktopaddons.intent.action.REFRESH";
    private static final String TAG = "SentenceAppWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // 主线程上不能执行耗费时间过长的操作, 也不能执行网络等操作, 除非开启严格模式, 但正式环境不应使用严格模式
        // 应该开新线程或者用协程, AsyncTask, Service, WorkManager等, 这里因为比较简单就直接开了新线程
        final PendingResult pendingResult = goAsync();
        SentenceModel sentenceModel = new SentenceModel();
        boolean useHitokoto = DAApplication.getPreferences().getBoolean("sentence_source", false);
        for (int appWidgetId : appWidgetIds)
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Sentence sentence = null;
                    try
                    {
                        sentence = useHitokoto ? sentenceModel.getHitokoto() : sentenceModel.getSentence();
                    }
                    catch (IOException | JSONException e)
                    {
                        e.printStackTrace();
                    }
                    updateAppWidget(context, appWidgetManager, appWidgetId, sentence);
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
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_REFRESH.equals(action))
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

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Sentence sentence)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget_sentence);
        Intent intent = new Intent(context, SentenceAppWidget.class);
        intent.setAction(ACTION_REFRESH);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.layoutSentenceWidget, pendingIntent);
        if (sentence != null)
        {
            views.setTextViewText(R.id.textViewSentence, sentence.getSentence());
            StringBuilder sb = new StringBuilder();
            if (sentence.getAuthor() != null)
                sb.append(sentence.getAuthor());
            if (sentence.getFrom() != null)
            {
                if (sentence.getAuthor() != null)
                    sb.append(", ");
                sb.append(sentence.getFrom());
            }
            sb.insert(0, "——");
            views.setTextViewText(R.id.textViewFrom, sb.toString());
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void notifyUpdate(Context context, int[] appWidgetIds)
    {
        Intent intent = new Intent(context, SentenceAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(intent);
    }

    public static void notifyUpdateAll(Context context)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, SentenceAppWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        notifyUpdate(context, appWidgetIds);
    }
}
