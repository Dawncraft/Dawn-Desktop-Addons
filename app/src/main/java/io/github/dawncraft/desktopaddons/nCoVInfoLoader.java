package io.github.dawncraft.desktopaddons;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class nCoVInfoLoader
{
    public static final String NCOV_QQ_NEWS = "https://news.qq.com/zt2020/page/feiyan.htm";
    public static final String NCOV_QQ_NEWS_API = "https://view.inews.qq.com/g2/getOnsInfo?name=wuwei_ww_global_vars";
    public static final String NCOV_QQ_NEWS_API2 = "https://view.inews.qq.com/g2/getOnsInfo?name=disease_h5";

    private static boolean updating = false;
    private static Map<String, String> NCOV_DATA_CACHE = new HashMap<>();
    {
        NCOV_DATA_CACHE.put("confirm", "-");
        NCOV_DATA_CACHE.put("suspect", "-");
        NCOV_DATA_CACHE.put("cure", "-");
        NCOV_DATA_CACHE.put("dead", "-");
        NCOV_DATA_CACHE.put("date", "XXXX-XX-XX XX:XX:XX");
        NCOV_DATA_CACHE.put("update_time", "XXXX-XX-XX XX:XX:XX");
    }

    public static boolean loadnCoVData(Context context)
    {
        if (Utils.isNetworkAvailable(context))
        {
            if (!updating)
            {
                updating = true;
                try
                {
                    // String content = Utils.getUrl(NCOV_QQ_NEWS_API);
                    String content = Utils.getUrl(NCOV_QQ_NEWS_API2);
                    JSONObject json = new JSONObject(content);
                    // readnCovJSON(json);
                    readnCovJSON2(json);
                    updating = false;
                    return true;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Utils.toast(context, "无法获取数据, 请与作者联系以解决这个问题");
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    Utils.toast(context, "无法解析JSON, 请与作者联系以解决这个问题");
                }
            }
            else
            {
                Utils.toast(context, context.getString(R.string.ncov_app_widget_updating));
            }
        }
        else
        {
            Utils.toast(context, "网络不可用, 无法获取nCoV-2019的最新数据");
        }
        return false;
    }

    private static void readnCovJSON(JSONObject json) throws JSONException
    {
        JSONArray array = new JSONArray(json.getString("data"));
        JSONObject data = array.getJSONObject(0);
        NCOV_DATA_CACHE.put("confirm", String.valueOf(data.getInt("confirmCount")));
        NCOV_DATA_CACHE.put("suspect", String.valueOf(data.getInt("suspectCount")));
        NCOV_DATA_CACHE.put("cure", String.valueOf(data.getInt("cure")));
        NCOV_DATA_CACHE.put("dead", String.valueOf(data.getInt("deadCount")));
        NCOV_DATA_CACHE.put("date", data.getString("recentTime"));
        NCOV_DATA_CACHE.put("update_time", Utils.getFormattedDate());
    }

    private static void readnCovJSON2(JSONObject json) throws JSONException
    {
        JSONObject data = new JSONObject(json.getString("data"));
        NCOV_DATA_CACHE.put("date", data.getString("lastUpdateTime"));
        JSONObject stats = data.getJSONObject("chinaTotal");
        NCOV_DATA_CACHE.put("confirm", String.valueOf(stats.getInt("confirm")));
        NCOV_DATA_CACHE.put("suspect", String.valueOf(stats.getInt("suspect")));
        NCOV_DATA_CACHE.put("cure", String.valueOf(stats.getInt("heal")));
        NCOV_DATA_CACHE.put("dead", String.valueOf(stats.getInt("dead")));
        NCOV_DATA_CACHE.put("update_time", Utils.getFormattedDate());
    }

    public static Map<String, String> getCachednCoVData()
    {
        return NCOV_DATA_CACHE;
    }
}
