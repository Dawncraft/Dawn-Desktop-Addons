/*
 * MIUI部分代码来自 https://github.com/ysy950803/FiveGSwitcher
 * 原作者代码以 GPLv3 协议开源
 */
package io.github.dawncraft.desktopaddons.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public abstract class FifthGHelper
{
    public static final FifthGHelper instance;

    static
    {
        //noinspection SwitchStatementWithTooFewBranches
        switch (android.os.Build.BRAND)
        {
            case "Xiaomi":
            case "Redmi":
                instance = new MIUI();
                break;
            default:
                instance = null;
                break;
        }
    }

    public abstract boolean isFifthGSupported();
    public abstract boolean isFifthGEnabled();
    public abstract void setFifthGEnabled(boolean enable);

    private static class MIUI extends FifthGHelper
    {
        private static final String CLASS_NAME = "miui.telephony.TelephonyManager";

        private Object getTelephonyManager(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
        {
            Method method = clazz.getMethod("getDefault");
            return method.invoke(null);
        }

        @Override
        public boolean isFifthGSupported()
        {
            try
            {
                Class<?> clazz = Class.forName(CLASS_NAME);
                Object telephonyManager = getTelephonyManager(clazz);
                Method method = clazz.getMethod("isFiveGCapable");
                return (boolean) Objects.requireNonNull(method.invoke(telephonyManager));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean isFifthGEnabled()
        {
            try
            {
                Class<?> clazz = Class.forName(CLASS_NAME);
                Object telephonyManager = getTelephonyManager(clazz);
                Method method = clazz.getMethod("isUserFiveGEnabled");
                return (boolean) Objects.requireNonNull(method.invoke(telephonyManager));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public void setFifthGEnabled(boolean enable)
        {
            try
            {
                Class<?> clazz = Class.forName(CLASS_NAME);
                Object telephonyManager = getTelephonyManager(clazz);
                Method method = clazz.getMethod("setUserFiveGEnabled", Boolean.TYPE);
                method.invoke(telephonyManager, enable);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
