package io.github.dawncraft.desktopaddons.worker;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.appwidget.NCPAppWidget;
import io.github.dawncraft.desktopaddons.dao.NCPAppWidgetDAO;
import io.github.dawncraft.desktopaddons.entity.NCPAppWidgetID;
import io.github.dawncraft.desktopaddons.entity.NCPInfo;
import io.github.dawncraft.desktopaddons.model.NCPInfoModel;

/**
 * 在后台更新新冠肺炎疫情数据的Worker
 * <br />
 * 将所有的小部件更新逻辑都交由Worker处理
 *
 * @author QingChenW
 */
public class NCPInfoWorker extends Worker implements NCPInfoModel.OnRegionDataListener
{
    private static final String TAG = "NCPInfoWorker";
    private static final String WORK_TAG = "ncp";

    private final AppWidgetManager appWidgetManager;
    private final NCPAppWidgetDAO ncpAppWidgetDAO;
    private final NCPInfoModel ncpInfoModel;
    private NCPInfo ncpInfo;

    public NCPInfoWorker(Context context, WorkerParameters workerParams)
    {
        super(context, workerParams);
        appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        ncpAppWidgetDAO = DAApplication.getDatabase().ncpAppWidgetDAO();
        ncpInfoModel = new NCPInfoModel();
    }

    @NonNull
    @Override
    public Result doWork()
    {
        Log.d(TAG, "Start to update");
        int appWidgetId = getInputData().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        List<NCPAppWidgetID> widgets = appWidgetId < 0 ? ncpAppWidgetDAO.getAll()
                : Collections.singletonList(ncpAppWidgetDAO.findById(appWidgetId));
        for (NCPAppWidgetID ncpAppWidgetID : widgets)
        {
            ncpInfoModel.getRegionInfo(ncpAppWidgetID.region, this);
            NCPAppWidget.updateAppWidget(getApplicationContext(), appWidgetManager, appWidgetId, ncpInfo);
            if (ncpInfo == null) return Result.retry();
        }
        Log.d(TAG, "Update successfully");
        return Result.success();
    }

    @Override
    public void onResponse(NCPInfoModel.Result result, NCPInfo info)
    {
        switch (result)
        {
            case CACHED:
                Log.d(TAG, "Load from cache");
            case SUCCESS:
                ncpInfo = info;
                break;
            case IO_ERROR:
                Log.e(TAG, "Can't get data");
                break;
            case JSON_ERROR:
                Log.e(TAG, "Can't analyse JSON");
                break;
            case UNKNOWN_ERROR:
                Log.e(TAG, "An unknown error occurred");
                break;
        }
    }

    public static Operation startSyncWork(Context context, int updateInterval)
    {
        if (updateInterval <= 0)
        {
            // Log.w(TAG, "Update interval is lower than zero, can't start work");
            return null;
        }
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                NCPInfoWorker.class, updateInterval, TimeUnit.MINUTES)
                .addTag(WORK_TAG)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build())
                .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        OneTimeWorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                        TimeUnit.MILLISECONDS)
                .build();
        return WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }

    public static Operation requestWork(Context context, int appWidgetId)
    {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NCPInfoWorker.class)
                .addTag(WORK_TAG)
                .setConstraints(new Constraints.Builder()
                        // NOTE 如果wifi连接受限的话, 即便是能连上网, 该Worker也不会执行
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .setInputData(new Data.Builder()
                        .putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        .build())
                // 此Worker似乎不需要成为加急任务
                // .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
                .build();
        return WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_TAG + appWidgetId, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public static Operation stopAllWorks(Context context)
    {
        return WorkManager.getInstance(context)
                .cancelAllWorkByTag(WORK_TAG);
    }
}
