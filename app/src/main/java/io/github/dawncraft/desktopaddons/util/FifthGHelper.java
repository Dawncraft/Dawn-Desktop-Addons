/*
 * MIUI部分代码来自 https://github.com/ysy950803/FiveGSwitcher
 * 原作者代码以 GPLv3 协议开源
 */
package io.github.dawncraft.desktopaddons.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import rikka.shizuku.Shizuku;
import rikka.shizuku.SystemServiceHelper;

public abstract class FifthGHelper
{
    public static final FifthGHelper instance;

    static
    {
        switch (Build.BRAND)
        {
            case "Xiaomi":
            case "Redmi":
                instance = new MIUI();
                break;
            case "Huawei":
            case "HONOR":
                instance = new EMUI();
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

    // 注: 华为的开关5G需要 android.permission.READ_PHONE_STATE 权限, 且此权限需要动态申请
    @SuppressLint("PrivateApi")
    private static class EMUI extends FifthGHelper
    {
        private static final String CLASS_NAME = "android.telephony.HwTelephonyManager";
        private static final int NETWORK_TYPE_4G = 0;
        private static final int NETWORK_TYPE_5G = 1;
        // public static final boolean IS_SUPPORT_5G = (HwTelephonyManager.getDefault().getModemMaxCapability(0) >= 4);

        public static void PackageManager_grantRuntimePermission(String packageName, String permission, int user)
        {
            Parcel data = SystemServiceHelper.obtainParcel("package", "android.content.pm.IPackageManager", "grantRuntimePermission");
            Parcel reply = Parcel.obtain();
            data.writeString(packageName);
            data.writeString(permission);
            data.writeInt(user);
            try
            {
                Shizuku.transactRemote(data, reply, 0);
                reply.readException();
            }
            catch (Exception e)
            {
                Log.e("EMUI", "PackageManager#grantRuntimePermission", e);
            }
            finally
            {
                data.recycle();
                reply.recycle();
            }
        }

        private Object getTelephonyManager(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
        {
            Method method = clazz.getMethod("getDefault");
            return method.invoke(null);
        }

        private int getDefaultSlotId(Class<?> clazz, Object telephonyManager) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
        {
            Method method = clazz.getMethod("getDefault4GSlotId");
            return (int) Objects.requireNonNull(method.invoke(telephonyManager));
        }

        @Override
        public boolean isFifthGSupported()
        {
            // 华为操作5G的API需要READ_PHONE_STATE和MODIFY_PHONE_STATE权限, 这里用Shizuku获取权限
            if (!Shizuku.pingBinder() || Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
                return false;
            try
            {
                Class<?> clazz = Class.forName(CLASS_NAME);
                Object telephonyManager = getTelephonyManager(clazz);
                Method method = clazz.getMethod("isNrSupported");
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
            PackageManager_grantRuntimePermission("io.github.dawncraft.desktopaddons", Manifest.permission.READ_PHONE_STATE, 0);
            try
            {
                Class<?> clazz = Class.forName(CLASS_NAME);
                Object telephonyManager = getTelephonyManager(clazz);
                Method method = clazz.getMethod("getServiceAbility", Integer.TYPE, Integer.TYPE);
                int slotId = getDefaultSlotId(clazz, telephonyManager);
                return (int) Objects.requireNonNull(method.invoke(telephonyManager, slotId, NETWORK_TYPE_5G)) == NETWORK_TYPE_5G;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return false;
        }

        /*
        at com.android.internal.telephony.IHwTelephony$Stub$Proxy.setServiceAbilityForSlotId(IHwTelephony.java:2514)
        at android.telephony.HwTelephonyManagerInner.setServiceAbility(HwTelephonyManagerInner.java:1085)
        at android.telephony.HwTelephonyManager.setServiceAbility(HwTelephonyManager.java:562)
        不过估计也没用
         */
        @Override
        public void setFifthGEnabled(boolean enable)
        {
            // MODIFY_PHONE_STATE 需要应用与系统签名相同或者位于系统应用目录下
            PackageManager_grantRuntimePermission("io.github.dawncraft.desktopaddons", Manifest.permission.MODIFY_PHONE_STATE, 0);
            try
            {
                Class<?> clazz = Class.forName(CLASS_NAME);
                Object telephonyManager = getTelephonyManager(clazz);
                Method method = clazz.getMethod("setServiceAbility", Integer.TYPE, Integer.TYPE, Integer.TYPE);
                int slotId = getDefaultSlotId(clazz, telephonyManager);
                method.invoke(telephonyManager, slotId, NETWORK_TYPE_5G, enable ? 1 : 0);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
