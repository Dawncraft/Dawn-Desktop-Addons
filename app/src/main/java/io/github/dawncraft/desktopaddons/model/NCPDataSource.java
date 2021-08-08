package io.github.dawncraft.desktopaddons.model;

import android.content.Context;

import io.github.dawncraft.desktopaddons.R;

/**
 * 新冠肺炎疫情数据来源页面地址
 * <br/>
 * 此类以MIT协议开源
 *
 * @author QingChenW
 */
public enum NCPDataSource {
    // 腾讯新闻(我为啥要起名叫QQ NEWS啊(手动笑哭))
    QQ_NEWS("https://news.qq.com/zt2020/page/feiyan.htm"),
    // 丁香园
    DXY("http://ncov.dxy.cn/ncovh5/view/pneumonia"),
    // 百度
    BAIDU("https://voice.baidu.com/act/newpneumonia/newpneumonia"),
    // 搜狗
    SOGOU("https://sa.sogou.com/new-weball/page/sgs/epidemic"),
    // 新浪微博
    SINA("https://news.sina.cn/zt_d/yiqing0121"),
    // 知乎
    ZHIHU("https://www.zhihu.com/special/19681091/trends"),
    // 高德地图
    GAODE("https://cache.gaode.com/activity/2020plague/index.html"),
    // 人民网
    PEOPLE("http://health.people.com.cn/GB/26466/431463/431576/index.html"),
    // 网易新闻
    NETEASE("https://wp.m.163.com/163/page/news/virus_report/index.html"),
    // 凤凰网
    IFENG("https://news.ifeng.com/c/specialClient/7tPlDSzDgVk"),
    // 今日头条
    TOUTIAO("https://i.snssdk.com/ugc/hotboard_fe/hot_list/template/hot_list/forum_tab.html"),
    // 阿里健康
    ALI("https://alihealth.taobao.com/medicalhealth/influenzamap"),
    // UC
    UC("https://iflow.uc.cn/webview/article/newspecial.html?aid=3804775841868884355&feiyan=1"),
    // GITHUB
    GITHUB("https://github.com/canghailan/Wuhan-2019-nCoV");

    // 从各页面逆向出来的API
    public static final String QQ_NEWS_GLOBAL = "https://view.inews.qq.com/g2/getOnsInfo?name=wuwei_ww_global_vars";
    public static final String QQ_NEWS_CHINA = "https://view.inews.qq.com/g2/getOnsInfo?name=disease_h5";
    public static final String QQ_NEWS_FOREIGN = "https://view.inews.qq.com/g2/getOnsInfo?name=disease_foreign";
    public static final String NETEASE_TOTAL = "https://c.m.163.com/ug/api/wuhan/app/data/list-total";
    public static final String NETEASE_AREA = "https://c.m.163.com/ug/api/wuhan/app/data/list-by-area-code?areaCode=";
    public static final String TOUTIAO_DATA = "https://i.snssdk.com/forum/home/v1/info/?forum_id=1656784762444839";
    public static final String ALI_DATA = "https://cdn.mdeer.com/data/yqstaticdata.js";

    private static String[] NAMES_CACHE;
    private final String url;

    NCPDataSource(String url)
    {
        this.url = url;
    }

    public String getName()
    {
        return NAMES_CACHE[ordinal()];
    }

    public String getUrl()
    {
        return url;
    }

    public static void loadNamesFromRes(Context context)
    {
        NAMES_CACHE = context.getResources().getStringArray(R.array.ncp_data_sources);
    }

    public static String getSourceUrl(int id)
    {
        if (id >= 0 && id < NCPDataSource.values().length)
            return NCPDataSource.values()[id].getUrl();
        return null;
    }
}
