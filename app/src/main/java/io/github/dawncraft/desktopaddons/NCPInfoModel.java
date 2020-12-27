package io.github.dawncraft.desktopaddons;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * 用于获取并处理新型冠状病毒数据的Model类
 *
 * @author QingChenW (Wu Chen)
 */
public class NCPInfoModel
{
    // 丁香园(似乎停止更新)
    public static final String NCP_DXY = "http://ncov.dxy.cn/ncovh5/view/pneumonia";
    // 腾讯新闻(为啥要起名叫QQ NEWS啊(手动笑哭))
    public static final String NCP_QQ_NEWS = "https://news.qq.com/zt2020/page/feiyan.htm";
    public static final String NCP_QQ_NEWS_API_GLOBAL = "https://view.inews.qq.com/g2/getOnsInfo?name=wuwei_ww_global_vars";
    public static final String NCP_QQ_NEWS_API_CHINA = "https://view.inews.qq.com/g2/getOnsInfo?name=disease_h5";
    public static final String NCP_QQ_NEWS_API_FOREIGN = "https://view.inews.qq.com/g2/getOnsInfo?name=disease_foreign";
    // 百度
    public static final String NCP_BAIDU = "https://voice.baidu.com/act/newpneumonia/newpneumonia";
    // 搜狗
    public static final String NCP_SOGOU = "https://sa.sogou.com/new-weball/page/sgs/epidemic";
    // 新浪微博
    public static final String NCP_SINA = "https://news.sina.cn/zt_d/yiqing0121";
    // 知乎
    public static final String NCP_ZHIHU = "https://www.zhihu.com/special/19681091/trends";
    // 高德地图
    public static final String NCP_GAODE = "https://cache.gaode.com/activity/2020plague/index.html";
    // 人民网
    public static final String NCP_PEOPLE = "http://health.people.com.cn/GB/26466/431463/431576/index.html";
    // 网易新闻
    public static final String NCP_NETEASE = "https://wp.m.163.com/163/page/news/virus_report/index.html";
    public static final String NCP_NETEASE_TOTAL = "https://c.m.163.com/ug/api/wuhan/app/data/list-total";
    public static final String NCP_NETEASE_AREA = "https://c.m.163.com/ug/api/wuhan/app/data/list-by-area-code?areaCode=";
    // 凤凰网
    public static final String NCP_IFENG = "https://news.ifeng.com/c/specialClient/7tPlDSzDgVk";
    // 今日头条
    public static final String NCP_TOUTIAO = "https://i.snssdk.com/feoffline/hot_list/template/hot_list/forum_share.html?forum_id=1656388947394568";
    // 阿里健康
    public static final String NCP_ALI = "https://alihealth.taobao.com/medicalhealth/influenzamap";
    public static final String NCP_ALI_DATA = "https://cdn.mdeer.com/data/yqstaticdata.js";
    // UC
    public static final String NCP_UC = "https://iflow.uc.cn/webview/article/newspecial.html?aid=3804775841868884355&feiyan=1";
    // GITHUB
    public static final String NCP_GITHUB = "https://github.com/canghailan/Wuhan-2019-nCoV";

    private static final String TAG = "NCPInfoModel";

    private static boolean isUpdating = false;
    private static long lastUpdateTime = 0;
    private static JSONObject cache = null;

    public static String getSourceUrl(int id)
    {
        switch (id)
        {
            default:
            case 0: return NCP_QQ_NEWS;
            case 1: return NCP_DXY;
            case 2: return NCP_BAIDU;
            case 3: return NCP_SOGOU;
            case 4: return NCP_SINA;
            case 5: return NCP_ZHIHU;
            case 6: return NCP_GAODE;
            case 7: return NCP_PEOPLE;
            case 8: return NCP_NETEASE;
            case 9: return NCP_IFENG;
            case 10: return NCP_TOUTIAO;
            case 11: return NCP_ALI;
            case 12: return NCP_UC;
            case 13: return NCP_GITHUB;
        }
    }

    public static JSONObject getDataJSON(int id) throws IOException, JSONException
    {
        // 啥, 你问我id有啥用, 我说你这个没用
        // TODO 序列化JSON
        String content = Utils.getUrl(NCP_QQ_NEWS_API_GLOBAL);
        return new JSONObject(content);
    }

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
                        int id = Integer.parseInt(DAApplication.getSharedPreferences().getString("ncp_source", "0"));
                        cache = getDataJSON(id);
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

    public static List<String> getRegions()
    {
        // TODO 获取地区列表
        return null;
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
        JSON_ERROR,
        UNKNOWN
    }
}
