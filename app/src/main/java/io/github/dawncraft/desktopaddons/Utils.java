package io.github.dawncraft.desktopaddons;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Utils
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static OkHttpClient client = new OkHttpClient();

    private Utils() {}

    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static String getUrl(String url) throws IOException
    {
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        if (response.body() != null)
        {
            return response.body().string();
        }
        else
        {
            return "";
        }
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

    public static String getFormattedDate()
    {
        return DATE_FORMAT.format(new Date(System.currentTimeMillis()));
    }

    public static void toast(final String msg)
    {
        toast(DAApplication.getInstance(), msg);
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
