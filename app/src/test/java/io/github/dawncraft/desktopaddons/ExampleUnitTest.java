package io.github.dawncraft.desktopaddons;

import org.junit.Before;
import org.junit.Test;

import io.github.dawncraft.desktopaddons.model.NCPInfoModel;
import io.github.dawncraft.desktopaddons.util.HttpUtils;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    @Before
    public void init()
    {
        HttpUtils.testInit();
    }

    // NOTE 单元测试老子不写了, 略略略
    @Test
    public void NCPInfoTest()
    {
        NCPInfoModel ncpInfoModel = new NCPInfoModel();
        ncpInfoModel.getAllInfo((result, infoTree) ->
        {
            System.out.println(result);
        });
        ncpInfoModel.getRegionInfo("中国", (result, info) ->
        {
            System.out.println(result);
        });
    }
}
