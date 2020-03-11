package io.github.dawncraft.desktopaddons;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NCPInfoLoader
{
    public static final String NCP_QQ_NEWS = "https://news.qq.com/zt2020/page/feiyan.htm";
    public static final String NCP_QQ_NEWS_API = "https://view.inews.qq.com/g2/getOnsInfo?name=wuwei_ww_global_vars";
    public static final String NCP_QQ_NEWS_API2 = "https://view.inews.qq.com/g2/getOnsInfo?name=disease_h5";

    private static final String TAG = "NCPInfoLoader";

    // 非线程安全,先凑合用,我还没学多线程呢
    private static boolean updating = false;
    private static Map<String, String> NCP_DATA_CACHE = new HashMap<>();
    {
        NCP_DATA_CACHE.put("confirm", "-");
        NCP_DATA_CACHE.put("suspect", "-");
        NCP_DATA_CACHE.put("cure", "-");
        NCP_DATA_CACHE.put("dead", "-");
        NCP_DATA_CACHE.put("date", "XXXX-XX-XX XX:XX:XX");
        NCP_DATA_CACHE.put("update_time", "XXXX-XX-XX XX:XX:XX");
    }

    public static int loadNCPData(Context context)
    {
        if (Utils.isNetworkAvailable(context))
        {
            if (!updating)
            {
                updating = true;
                try
                {
                    Log.i(TAG, "Start to update.");
                    String content = Utils.getUrl(NCP_QQ_NEWS_API);
                    // String content = Utils.getUrl(NCP_QQ_NEWS_API2);
                    JSONObject json = new JSONObject(content);
                    readNCPJSON(json);
                    // readNCPJSON2(json);
                    updating = false;
                    Log.i(TAG, "Update successfully.");
                    return 1;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Log.e(TAG, "Can't get data.");
                    return -3;
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    Log.e(TAG, "Can't analyse JSON.");
                    return -4;
                }
            }
            else
            {
                Log.e(TAG, "Updating.");
                return -2;
            }
        }
        else
        {
            Log.e(TAG, "No network connection.");
            return -1;
        }
    }

    private static void readNCPJSON(JSONObject json) throws JSONException
    {
        JSONArray array = new JSONArray(json.getString("data"));
        JSONObject data = array.getJSONObject(0);
        NCP_DATA_CACHE.put("confirm", String.valueOf(data.getInt("nowConfirm")));
        NCP_DATA_CACHE.put("suspect", String.valueOf(data.getInt("suspectCount")));
        NCP_DATA_CACHE.put("cure", String.valueOf(data.getInt("cure")));
        NCP_DATA_CACHE.put("dead", String.valueOf(data.getInt("deadCount")));
        NCP_DATA_CACHE.put("date", data.getString("update_time"));
        NCP_DATA_CACHE.put("update_time", Utils.getFormattedDate());
    }

    private static void readNCPJSON2(JSONObject json) throws JSONException
    {
        JSONObject data = new JSONObject(json.getString("data"));
        NCP_DATA_CACHE.put("date", data.getString("lastUpdateTime"));
        JSONObject stats = data.getJSONObject("chinaTotal");
        NCP_DATA_CACHE.put("confirm", String.valueOf(stats.getInt("confirm")));
        NCP_DATA_CACHE.put("suspect", String.valueOf(stats.getInt("suspect")));
        NCP_DATA_CACHE.put("cure", String.valueOf(stats.getInt("heal")));
        NCP_DATA_CACHE.put("dead", String.valueOf(stats.getInt("dead")));
        NCP_DATA_CACHE.put("update_time", Utils.getFormattedDate());
    }

    public static Map<String, String> getCachedNCPData()
    {
        return NCP_DATA_CACHE;
    }
}
