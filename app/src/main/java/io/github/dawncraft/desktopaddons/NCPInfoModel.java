package io.github.dawncraft.desktopaddons;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 用于获取并处理新型冠状病毒数据的Model类
 *
 * @author QingChenW (Wu Chen)
 */
public class NCPInfoModel
{
    public static final String NCP_QQ_NEWS = "https://news.qq.com/zt2020/page/feiyan.htm";
    public static final String NCP_QQ_NEWS_API_GLOBAL = "https://view.inews.qq.com/g2/getOnsInfo?name=wuwei_ww_global_vars";
    public static final String NCP_QQ_NEWS_API_CHINA = "https://view.inews.qq.com/g2/getOnsInfo?name=disease_h5";
    public static final String NCP_QQ_NEWS_API_FOREIGN = "https://view.inews.qq.com/g2/getOnsInfo?name=disease_foreign";

    private static final String TAG = "NCPInfoModel";

    private static boolean isUpdating = false;
    private static long lastUpdateTime = 0;
    private static JSONObject cache = null;

    public static EnumResult loadData()
    {
        if (!isUpdating)
        {
            if (cache == null || System.currentTimeMillis() - lastUpdateTime > 60000)
            {
                if (Utils.isNetworkAvailable(DAApplication.getInstance()))
                {
                    Log.i(TAG, "Start to update.");
                    isUpdating = true;
                    lastUpdateTime = System.currentTimeMillis();
                    try
                    {
                        String content = Utils.getUrl(NCP_QQ_NEWS_API_GLOBAL);
                        cache = new JSONObject(content);
                    }
                    catch (IOException e)
                    {
                        Log.e(TAG, "Can't get data.");
                        e.printStackTrace();
                        return EnumResult.IO_ERROR;
                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, "Can't analyse JSON.");
                        e.printStackTrace();
                        return EnumResult.JSON_ERROR;
                    }
                    finally
                    {
                        isUpdating = false;
                    }
                    Log.i(TAG, "Update successfully.");
                    return EnumResult.SUCCESS;
                }
                else
                {
                    Log.e(TAG, "No network connection.");
                    return EnumResult.NO_NETWORK;
                }
            }
            else
            {
                Log.i(TAG, "Load from cache.");
                return EnumResult.CACHED;
            }
        }
        else
        {
            Log.e(TAG, "Updating.");
            return EnumResult.UPDATING;
        }
    }

    public static NCPInfoItem getInfoItem(String region)
    {
        NCPInfoItem item = new NCPInfoItem();
        if (cache == null) return item;
        item.region = region;
        item.updateTime = Utils.getCurrentFmtDate();
        try
        {
            if (region == null || region.equals(""))
            {
                getInfoItemDefault(item);
                item.isVaild = true;
            }
        }
        catch (JSONException e)
        {
            Log.e(TAG, "Can't read region data: " + item.region + ".");
            e.printStackTrace();
        }
        return item;
    }

    private static void getInfoItemDefault(NCPInfoItem item) throws JSONException
    {
        JSONArray array = new JSONArray(cache.getString("data"));
        JSONObject data = array.getJSONObject(0);
        item.confirm = data.getInt("nowConfirm");
        item.suspect = data.getInt("suspectCount");
        item.cure = data.getInt("cure");
        item.dead = data.getInt("deadCount");
        item.date = Utils.formatUTCDate(data.getString("update_time"));
    }

    private static void getInfoItemChina(NCPInfoItem item) throws JSONException
    {
        JSONObject data = new JSONObject(cache.getString("data"));
        JSONObject stats = data.getJSONObject("chinaTotal");
        item.confirm = stats.getInt("confirm");
        item.suspect = stats.getInt("suspect");
        item.cure = stats.getInt("heal");
        item.dead = stats.getInt("dead");
        item.date = Utils.formatDefaultDate(data.getString("lastUpdateTime"));
    }

    public enum EnumResult
    {
        SUCCESS,
        UPDATING,
        CACHED,
        NO_NETWORK,
        IO_ERROR,
        JSON_ERROR
    }
}
