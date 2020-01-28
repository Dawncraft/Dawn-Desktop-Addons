package io.github.dawncraft.desktopaddons;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Utils
{
    static final public String NCOV_QQ_NEWS = "https://news.qq.com/zt2020/page/feiyan.htm";
    static final public String NCOV_QQ_NEWS_API = "https://view.inews.qq.com/g2/getOnsInfo?name=wuwei_ww_global_vars";
    static final private Map<String, String> NCOV_DATA_CACHE = new HashMap<>();
    {
        NCOV_DATA_CACHE.put("confirm", "-");
        NCOV_DATA_CACHE.put("suspect", "-");
        NCOV_DATA_CACHE.put("cure", "-");
        NCOV_DATA_CACHE.put("dead", "-");
        NCOV_DATA_CACHE.put("time", "XXXX-XX-XX XX:XX");
    }

    static private OkHttpClient client = new OkHttpClient();

    static public void loadnCoVData() throws Exception
    {
        String content = getUrl(NCOV_QQ_NEWS_API);
        JSONObject json = new JSONObject(content);
        if (json.has("data"))
        {
            JSONArray array = new JSONArray(json.getString("data"));
            JSONObject data = array.getJSONObject(0);
            NCOV_DATA_CACHE.put("confirm", String.valueOf(data.getInt("confirmCount")));
            NCOV_DATA_CACHE.put("suspect", String.valueOf(data.getInt("suspectCount")));
            NCOV_DATA_CACHE.put("cure", String.valueOf(data.getInt("cure")));
            NCOV_DATA_CACHE.put("dead", String.valueOf(data.getInt("deadCount")));
            NCOV_DATA_CACHE.put("time", data.getString("recentTime"));
        }
    }

    static public Map<String, String> getCachednCoVData()
    {
        return NCOV_DATA_CACHE;
    }

    static public String getUrl(String url) throws IOException
    {
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    static public void openUrl(Context context, String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
