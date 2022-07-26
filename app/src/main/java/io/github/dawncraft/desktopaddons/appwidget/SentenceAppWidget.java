package io.github.dawncraft.desktopaddons.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.dao.SentenceAppWidgetDAO;
import io.github.dawncraft.desktopaddons.entity.Sentence;
import io.github.dawncraft.desktopaddons.entity.SentenceAppWidgetID;
import io.github.dawncraft.desktopaddons.model.SentenceModel;
import io.github.dawncraft.desktopaddons.util.Utils;

/**
 * 一言桌面小部件
 *
 * @author QingChenW
 */
public class SentenceAppWidget extends AppWidgetProvider
{
    public static final String ACTION_REFRESH = "desktopaddons.intent.action.REFRESH";
    public static final String ACTION_PINNED = "desktopaddons.intent.action.PINNED";
    public static final String EXTRA_SENTENCE_SOURCE = "sentenceSource";
    public static final String EXTRA_SENTENCE_ID = "sentenceId";
    private static final String TAG = "SentenceAppWidget";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final SentenceAppWidgetDAO sentenceAppWidgetDAO = DAApplication.getDatabase().sentenceAppWidgetDAO();
    private final SentenceModel sentenceModel = new SentenceModel();

    @Override
    public void onDisabled(Context context)
    {
        super.onDisabled(context);
        executorService.shutdown();
        sentenceAppWidgetDAO.deleteAll();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // 主线程上不能执行耗费时间过长的操作, 也不能执行网络等操作, 除非开启严格模式, 但正式环境不应使用严格模式
        // 应该开新线程或者用协程, AsyncTask, Service, WorkManager等, 这里因为比较简单就直接开了新线程
        PendingResult pendingResult = goAsync();
        List<Callable<Void>> taskList = new ArrayList<>();
        for (int appWidgetId : appWidgetIds)
        {
            SentenceAppWidgetID sentenceAppWidgetID = sentenceAppWidgetDAO.findById(appWidgetId);
            if (sentenceAppWidgetID == null) continue;
            taskList.add(new Callable<Void>()
            {
                @Override
                public Void call() throws JSONException, IOException
                {
                    Sentence sentence = null;
                    switch (sentenceAppWidgetID.source)
                    {
                        case Hitokoto:
                            sentence = sentenceModel.getHitokoto();
                            break;
                        case Dawncraft:
                            if (TextUtils.isEmpty(sentenceAppWidgetID.sid))
                                sentence = sentenceModel.getSentence();
                            else
                                sentence = sentenceModel.getSentence(Integer.parseInt(sentenceAppWidgetID.sid));
                            break;
                    }
                    updateAppWidget(context, appWidgetManager, appWidgetId, sentence);
                    return null;
                }
            });
        }
        try
        {
            executorService.invokeAll(taskList);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        finally
        {
            pendingResult.finish();
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds)
        {
            sentenceAppWidgetDAO.deleteById(appWidgetId);
        }
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
        else if (ACTION_PINNED.equals(action))
        {
            Log.d(TAG, "Action pinned");
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID))
            {
                int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                SentenceAppWidgetID sentenceAppWidgetID = new SentenceAppWidgetID();
                sentenceAppWidgetID.id = appWidgetId;
                sentenceAppWidgetID.source = Sentence.Source.valueOf(extras.getString(EXTRA_SENTENCE_SOURCE));
                sentenceAppWidgetID.sid = extras.getString(EXTRA_SENTENCE_ID);
                sentenceAppWidgetDAO.insert(sentenceAppWidgetID);
                onUpdate(context, AppWidgetManager.getInstance(context), new int[] { appWidgetId });
            }
        }
    }

    public static RemoteViews createViews(Context context, Sentence sentence)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget_sentence);
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
        return views;
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Sentence sentence)
    {
        RemoteViews views = createViews(context, sentence);
        Intent intent = new Intent(context, SentenceAppWidget.class);
        intent.setAction(ACTION_REFRESH);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent,
                Utils.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.layoutSentenceWidget, pendingIntent);
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

    public static boolean requestPin(Context context, Sentence sentence)
    {
        if (!Utils.isPinAppWidgetSupported(context)) return false;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, SentenceAppWidget.class);
        Bundle extras = new Bundle();
        extras.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, createViews(context, sentence));
        Intent intent = new Intent(context, SentenceAppWidget.class);
        intent.setAction(ACTION_PINNED);
        if (sentence != null)
        {
            intent.putExtra(EXTRA_SENTENCE_SOURCE, sentence.getSource().name());
            intent.putExtra(EXTRA_SENTENCE_ID, sentence.getUUID());
        }
        // NOTE 又踩坑啦, 这个Intent是会变的, 所以应该用FLAG_MUTABLE
        PendingIntent successCallback = PendingIntent.getBroadcast(context, 0, intent,
                Utils.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return appWidgetManager.requestPinAppWidget(componentName, extras, successCallback);
    }
}
