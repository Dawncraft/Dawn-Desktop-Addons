package io.github.dawncraft.desktopaddons.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.StringRes;

import java.util.List;

public final class Utils
{
    public static final int FLAG_IMMUTABLE = PendingIntent.FLAG_IMMUTABLE;
    public static final int FLAG_MUTABLE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0;

    private Utils() {}

    public static PackageInfo getAppInfo(Context context)
    {
        try
        {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.getPackageInfo(context.getPackageName(), 0);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isComponentEnabled(Context context, ComponentName componentName)
    {
        PackageManager packageManager = context.getPackageManager();
        int state = packageManager.getComponentEnabledSetting(componentName);
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    public static void setComponentEnabled(Context context, ComponentName componentName, boolean enabled)
    {
        if (isComponentEnabled(context, componentName) == enabled) return;
        PackageManager packageManager = context.getPackageManager();
        packageManager.setComponentEnabledSetting(componentName,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static boolean isZenModeGranted(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.isNotificationPolicyAccessGranted();
    }

    public static boolean isZenMode(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        int mode = notificationManager.getCurrentInterruptionFilter();
        return mode > NotificationManager.INTERRUPTION_FILTER_ALL;
    }

    public static void setZenMode(Context context, boolean flag)
    {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        // TODO 使用NotificationManager.Policy切换勿扰模式
        // NotificationManager.Policy policy = new NotificationManager.Policy();
        // notificationManager.setNotificationPolicy();
        int mode = flag ? NotificationManager.INTERRUPTION_FILTER_PRIORITY
                : NotificationManager.INTERRUPTION_FILTER_ALL;
        notificationManager.setInterruptionFilter(mode);
    }

    public static boolean isFifthGSupported()
    {
        return FifthGHelper.instance != null && FifthGHelper.instance.isFifthGSupported();
    }

    public static boolean isFifthGEnabled()
    {
        return FifthGHelper.instance.isFifthGEnabled();
    }

    public static void setFifthGEnabled(boolean enable)
    {
        FifthGHelper.instance.setFifthGEnabled(enable);
    }

    public static boolean isPinAppWidgetSupported(Context context)
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported();
    }

    public static void openUrl(Context context, String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // NOTE 踩坑了! 在Activity之外startActivity时必须用FLAG_ACTIVITY_NEW_TASK参数
        // 详见android.app.ContextImpl#startActivity(android.content.Intent, android.os.Bundle)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndNormalize(Uri.parse(url));
        context.startActivity(intent);
    }

    public static void copyToClipboard(Context context, String text)
    {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(context.getPackageName(), text);
        clipboardManager.setPrimaryClip(clipData);
    }

    public static void toast(Context context, String msg)
    {
        int duration = msg.length() < 15 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;
        Toast.makeText(context, msg, duration).show();
    }

    public static void toast(Context context, @StringRes int resId)
    {
        toast(context, context.getString(resId));
    }
}
