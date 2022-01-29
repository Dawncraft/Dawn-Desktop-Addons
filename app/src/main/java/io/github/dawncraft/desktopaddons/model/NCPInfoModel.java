package io.github.dawncraft.desktopaddons.model;

import static io.github.dawncraft.desktopaddons.model.NCPDataSource.QQ_NEWS_CHINA;
import static io.github.dawncraft.desktopaddons.model.NCPDataSource.QQ_NEWS_FOREIGN;
import static io.github.dawncraft.desktopaddons.model.NCPDataSource.QQ_NEWS_GLOBAL;

import androidx.core.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import io.github.dawncraft.desktopaddons.entity.NCPInfo;
import io.github.dawncraft.desktopaddons.entity.QQNewsNCPInfo;
import io.github.dawncraft.desktopaddons.util.DateUtils;
import io.github.dawncraft.desktopaddons.util.HttpUtils;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 用于获取并处理新冠肺炎疫情数据的Model
 *
 * @author QingChenW
 */
public class NCPInfoModel
{
    private static final Map<String, Pair<Long, NCPInfo>> CACHE = new ConcurrentHashMap<>();

    @Deprecated
    private JSONObject getGlobalInfo() throws IOException, JSONException
    {
        Request request = new Request.Builder()
                .url(QQ_NEWS_GLOBAL)
                .get()
                .build();
        Response response = HttpUtils.getClient().newCall(request).execute();
        String content = Objects.requireNonNull(response.body()).string();
        JSONObject json = new JSONObject(content);
        JSONArray data = new JSONArray(json.getString("data"));
        return data.getJSONObject(0);
    }

    private JSONObject getChinaInfo() throws IOException, JSONException
    {
        Request request = new Request.Builder()
                .url(QQ_NEWS_CHINA)
                .get()
                .build();
        Response response = HttpUtils.getClient().newCall(request).execute();
        String content = Objects.requireNonNull(response.body()).string();
        JSONObject json = new JSONObject(content);
        JSONObject data = new JSONObject(json.getString("data"));
        JSONObject area = data.getJSONArray("areaTree").getJSONObject(0);
        area.put("lastUpdateTime", data.getString("lastUpdateTime"));
        return area;
    }

    private JSONObject getForeignInfo() throws IOException, JSONException
    {
        Request request = new Request.Builder()
                .url(QQ_NEWS_FOREIGN)
                .get()
                .build();
        Response response = HttpUtils.getClient().newCall(request).execute();
        String content = Objects.requireNonNull(response.body()).string();
        JSONObject json = new JSONObject(content);
        JSONObject data = new JSONObject(json.getString("data"));
        return data;
    }

    public void getRegionInfo(String region, OnRegionDataListener listener)
    {
        Pair<Long, NCPInfo> pair = CACHE.get(region);
        if (pair != null && System.currentTimeMillis() - pair.first <= 60_000)
        {
            pair.second.setUpdateTime(DateUtils.formatCurrentDate());
            listener.onResponse(Result.CACHED, pair.second);
            return;
        }
        try
        {
            String[] areas = region.split(",");
            JSONObject data = getChinaInfo();
            String updateTime = data.getString("lastUpdateTime");
            for (String area : areas)
            {
                JSONArray children = data.getJSONArray("children");
                for (int i = 0; i < children.length(); i++)
                {
                    JSONObject child = children.getJSONObject(i);
                    if (area.equals(child.get("name")))
                    {
                        data = child;
                        break;
                    }
                }
            }
            JSONObject total = data.getJSONObject("total");
            NCPInfo info = new NCPInfo();
            info.setRegion(region);
            info.setConfirm(total.getInt("nowConfirm"));
            // suspect 字段已被移除 total.getInt("suspect")
            info.setSuspect(0);
            info.setCure(total.getInt("heal"));
            info.setDead(total.getInt("dead"));
            info.setDate(DateUtils.formatDefaultDate(updateTime));
            info.setUpdateTime(DateUtils.formatCurrentDate());
            CACHE.put(region, Pair.create(System.currentTimeMillis(), info));
            listener.onResponse(Result.SUCCESS, info);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            listener.onResponse(Result.IO_ERROR, null);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            listener.onResponse(Result.JSON_ERROR, null);
        }
    }

    public void getAllInfo(OnAllDataListener listener)
    {
        try
        {
            JSONObject data = getChinaInfo();
            List<QQNewsNCPInfo> root = new ArrayList<>();
            root.add(QQNewsNCPInfo.fromJson(data));
            listener.onResponse(Result.SUCCESS, root);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            listener.onResponse(Result.IO_ERROR, null);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            listener.onResponse(Result.JSON_ERROR, null);
        }
    }

    public enum Result
    {
        SUCCESS,
        CACHED,
        IO_ERROR,
        JSON_ERROR,
        UNKNOWN_ERROR
    }

    public interface OnRegionDataListener
    {
        void onResponse(Result result, NCPInfo info);
    }

    public interface OnAllDataListener
    {
        void onResponse(Result result, List<QQNewsNCPInfo> infoTree);
    }
}
