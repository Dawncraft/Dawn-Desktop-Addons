package io.github.dawncraft.desktopaddons.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.github.dawncraft.desktopaddons.appwidget.NCPAppWidget;
import io.github.dawncraft.desktopaddons.appwidget.SentenceAppWidget;

/**
 * 应用更新广播接收器, 用于在更新应用后刷新桌面小部件
 * <br/>
 * 小部件在应用更新后不工作可能是Android Studio导致的, 详见: https://stackoverflow.com/a/64579518/17528745
 *
 * @author QingChenW
 */
public class PackageBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "PackageBroadcast";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_MY_PACKAGE_REPLACED))
        {
            Log.d(TAG, "Action my package replaced");
//            NCPAppWidget.notifyUpdateAll(context);
//            SentenceAppWidget.notifyUpdateAll(context);
        }
    }
}
