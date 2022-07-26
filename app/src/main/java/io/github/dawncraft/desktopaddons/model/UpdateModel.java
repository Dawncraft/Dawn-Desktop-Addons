package io.github.dawncraft.desktopaddons.model;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import io.github.dawncraft.desktopaddons.entity.AppInfo;
import io.github.dawncraft.desktopaddons.util.DateUtils;
import io.github.dawncraft.desktopaddons.util.HttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 用于检查更新的Model
 *
 * @author QingChenW
 */
public class UpdateModel
{
    private static boolean isChecking = false;
    private static AppInfo cache;

    public void getGitHubRelease(String id, OnAppInfoListener listener)
    {
        Request request = new Request.Builder()
                .url("https://api.github.com/repos/Dawncraft/Dawn-Desktop-Addons/releases/" + id)
                .get()
                .build();
        HttpUtils.getClient().newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                listener.onResponse(Result.ERROR, null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                String content = Objects.requireNonNull(response.body()).string();
                try
                {
                    JSONObject json = new JSONObject(content);
                    AppInfo appInfo = new AppInfo();
                    appInfo.setVersion(json.getString("name"));
                    appInfo.setMessage(json.getString("body"));
                    appInfo.setReleaseTime(DateUtils.formatUTCDate(json.getString("published_at")));
                    JSONObject asset = json.getJSONArray("assets").getJSONObject(0);
                    appInfo.setSize(asset.getInt("size"));
                    appInfo.setDownloadUrl(json.getString("html_url"));
                    listener.onResponse(Result.SUCCESS, appInfo);
                }
                catch (JSONException e)
                {
                    listener.onResponse(Result.ERROR, null);
                }
            }
        });
    }

    public void checkUpdate(boolean useCache, OnAppInfoListener listener)
    {
        if (useCache && cache != null)
        {
            listener.onResponse(Result.CACHE, cache);
            return;
        }
        if (isChecking)
        {
            listener.onResponse(Result.BUSY, null);
            return;
        }
        isChecking = true;
        getGitHubRelease("latest", new OnAppInfoListener()
        {
            @Override
            public void onResponse(Result result, AppInfo appInfo)
            {
                cache = appInfo;
                listener.onResponse(result, appInfo);
                isChecking = false;
            }
        });
    }

    public enum Result
    {
        SUCCESS,
        CACHE,
        BUSY,
        ERROR;
    }

    public interface OnAppInfoListener
    {
        void onResponse(Result result, AppInfo appInfo);
    }
}
