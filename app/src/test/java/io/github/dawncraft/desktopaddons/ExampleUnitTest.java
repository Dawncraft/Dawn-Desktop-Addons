package io.github.dawncraft.desktopaddons;

import android.util.Log;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    @Test
    public void get_nCoV_data()
    {
        Map data = Utils.getnCoVData();
        System.out.println(data);
    }
}
