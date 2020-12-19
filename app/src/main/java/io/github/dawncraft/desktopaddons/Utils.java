package io.github.dawncraft.desktopaddons;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Utils
{

    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
    private static final SimpleDateFormat UTC_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z", Locale.ROOT);

    private Utils() {}

    public static String formatDate(Date date)
    {
        return SimpleDateFormat.getDateTimeInstance().format(date);
    }

    public static String formatDefaultDate(String str)
    {
        try
        {
            Date date = DEFAULT_DATE_FORMAT.parse(str);
            return formatDate(date);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public static String formatUTCDate(String str)
    {
        try
        {
            str = str.replace("Z", " UTC");
            Date date = UTC_DATE_FORMAT.parse(str);
            return formatDate(date);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public static String getCurrentFmtDate()
    {
        return formatDate(new Date());
    }

    public static void toast(final String msg)
    {
        toast(DAApplication.getInstance(), msg);
    }

    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static String getUrl(String url) throws IOException
    {
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        if (response.body() != null)
        {
            return response.body().string();
        }
        return "";
    }

    public static void openUrl(Context context, String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // FIXME 踩坑了! 在Activity之外startActivity时必须用FLAG_ACTIVITY_NEW_TASK参数
        // 详见android.app.ContextImpl#startActivity(android.content.Intent, android.os.Bundle)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndNormalize(Uri.parse(url));
        context.startActivity(intent);
    }

    /*
    android.provider.Settings.Global#ZEN_MODE_OFF = 0;
    android.provider.Settings.Global#ZEN_MODE_IMPORTANT_INTERRUPTIONS = 1;
    android.provider.Settings.Global#ZEN_MODE_NO_INTERRUPTIONS = 2;
    android.provider.Settings.Global#ZEN_MODE_ALARMS = 3;
    */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setZenMode(int zenMode, Uri conditionId, String reason)
    {
        NotificationManager notificationManager = (NotificationManager) DAApplication.getInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager.isNotificationPolicyAccessGranted())
        {
            Log.e("desktopaddon", notificationManager.getAutomaticZenRules().toString());
            /*try
            {
                Class<NotificationManager> clazz = NotificationManager.class;
                Method method = clazz.getMethod("setZenMode", int.class, Uri.class, String.class);
                method.invoke(notificationManager, zenMode, conditionId, reason);
            }
            catch (NoSuchMethodException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch (InvocationTargetException e)
            {
                e.printStackTrace();
            }*/
        }
        else
        {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            DAApplication.getInstance().startActivity(intent);
        }

    }

    public static void toast(final Context context, final String msg)
    {
        runOnUIThread(new Runnable()
        {
            @Override
            public void run()
            {
                int duration = msg.length() < 15 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;
                Toast.makeText(context, msg, duration).show();
            }
        });
    }

    public static boolean runOnUIThread(Runnable runnable)
    {
        Handler handler = new Handler(Looper.getMainLooper());
        return handler.post(runnable);
    }
}
