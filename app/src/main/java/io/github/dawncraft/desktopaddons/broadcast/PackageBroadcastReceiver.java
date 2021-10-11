package io.github.dawncraft.desktopaddons.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.github.dawncraft.desktopaddons.appwidget.NCPAppWidget;
import io.github.dawncraft.desktopaddons.appwidget.SentenceAppWidget;

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
            NCPAppWidget.notifyUpdateAll(context);
            SentenceAppWidget.notifyUpdateAll(context);
        }
    }
}
