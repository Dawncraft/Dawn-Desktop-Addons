package io.github.dawncraft.desktopaddons.worker;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import io.github.dawncraft.desktopaddons.appwidget.NCPAppWidget;
import io.github.dawncraft.desktopaddons.entity.NCPInfo;
import io.github.dawncraft.desktopaddons.model.NCPInfoModel;

/**
 * 在后台更新新冠肺炎疫情数据的Worker
 * <br />
 * 有bug, 会导致桌面小部件无限更新, 暂时不使用
 *
 * @author QingChenW
 */
public class NCPInfoWorker extends Worker implements NCPInfoModel.OnRegionDataListener
{
    private static final String TAG = "NCPInfoWorker";
    private final AppWidgetManager appWidgetManager;
    private final NCPInfoModel ncpInfoModel;
    private NCPInfo ncpInfo;

    public NCPInfoWorker(Context context, WorkerParameters workerParams)
    {
        super(context, workerParams);
        appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        ncpInfoModel = new NCPInfoModel();
    }

    @NonNull
    @Override
    public Result doWork()
    {
        Log.i(TAG, "Start to update.");
        int appWidgetId = getInputData().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        String region = getInputData().getString("region");
        if (appWidgetId == -1 || region == null) return Result.failure();
        ncpInfoModel.getRegionInfo(region, this);
        NCPAppWidget.updateAppWidget(getApplicationContext(), appWidgetManager, appWidgetId, ncpInfo);
        if (ncpInfo == null) return Result.retry();
        Log.i(TAG, "Update successfully.");
        return Result.success();
    }

    @Override
    public void onResponse(NCPInfoModel.Result result, NCPInfo info)
    {
        switch (result)
        {
            case CACHED:
                Log.i(TAG, "Load from cache.");
            case SUCCESS:
                ncpInfo = info;
                break;
            case IO_ERROR:
                Log.e(TAG, "Can't get data.");
                break;
            case JSON_ERROR:
                Log.e(TAG, "Can't analyse JSON.");
                break;
            case UNKNOWN_ERROR:
                Log.e(TAG, "An unknown error occurred.");
                break;
        }
    }

    public static OneTimeWorkRequest requestWork(int appWidgetId, String region)
    {
        return new OneTimeWorkRequest.Builder(NCPInfoWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        OneTimeWorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                        TimeUnit.MILLISECONDS)
                .setInputData(new Data.Builder()
                        .putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        .putString("region", region)
                        .build())
                .build();
    }
}
