package io.github.dawncraft.desktopaddons.util;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.service.wallpaper.WallpaperService;
import android.widget.Toast;

import androidx.annotation.StringRes;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public final class Utils
{
    public static final int FLAG_IMMUTABLE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
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
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                new Intent().setComponent(componentName), PackageManager.MATCH_DEFAULT_ONLY);
        return !list.isEmpty();
    }

    public static void setComponentEnabled(Context context, ComponentName componentName, boolean enabled)
    {
        if (isComponentEnabled(context, componentName) == enabled) return;
        PackageManager packageManager = context.getPackageManager();
        packageManager.setComponentEnabledSetting(componentName,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean isZenMode(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager.isNotificationPolicyAccessGranted())
            {
                int mode = notificationManager.getCurrentInterruptionFilter();
                return mode > NotificationManager.INTERRUPTION_FILTER_ALL;
            }
            else
            {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
        else
        {
            AudioManager audioManager = (AudioManager)
                    context.getSystemService(Context.AUDIO_SERVICE);
            int mode = audioManager.getRingerMode();
            return mode > AudioManager.RINGER_MODE_SILENT;
        }
        return false;
    }

    // Android 5.0后直接操作勿扰模式的方法被设为hide, 可能是因为勿扰模式改成了NotificationManager
    // 详见 https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
    public static void setZenMode(Context context, boolean flag)
    {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (notificationManager.isNotificationPolicyAccessGranted())
            {
                // TODO 使用NotificationManager.Policy切换勿扰模式
                // 另见 ZenPolicy, 在Android 6.0后ZenPolicy改为了NotificationManager.Policy
                // NotificationManager.Policy policy = new NotificationManager.Policy();
                // notificationManager.setNotificationPolicy();
                int newMode = flag ? NotificationManager.INTERRUPTION_FILTER_PRIORITY : NotificationManager.INTERRUPTION_FILTER_ALL;
                notificationManager.setInterruptionFilter(newMode);
            }
            else
            {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
        else
        {
            AudioManager audioManager = (AudioManager)
                    context.getSystemService(Context.AUDIO_SERVICE);
            int newMode = flag ? AudioManager.RINGER_MODE_SILENT : AudioManager.RINGER_MODE_NORMAL;
            audioManager.setRingerMode(newMode);
        }
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

    public static String getProperty(String key)
    {
        try
        {
            InputStream inputStream = Runtime.getRuntime()
                    .exec("/system/bin/getprop " + key)
                    .getInputStream();
            Scanner in = new Scanner(inputStream);
            String value = in.next();
            in.close();
            return value;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void setProperty(String key, String value)
    {
        try
        {
            Runtime.getRuntime().exec("/system/bin/setprop " + key + " " + value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
