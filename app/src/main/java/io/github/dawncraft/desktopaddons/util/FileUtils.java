package io.github.dawncraft.desktopaddons.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FileUtils
{
    private FileUtils() {}

    public static String getFormatSize(double size)
    {
        double kiloByte = size / 1024;
        if (kiloByte < 1)
        {
            return size + "Byte";
        }
        double megaByte = kiloByte / 1024;
        if (megaByte < 1)
        {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, RoundingMode.HALF_UP).toPlainString() + "KB";
        }
        double gigaByte = megaByte / 1024;
        if (gigaByte < 1)
        {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, RoundingMode.HALF_UP).toPlainString() + "MB";
        }
        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1)
        {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, RoundingMode.HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, RoundingMode.HALF_UP).toPlainString() + "TB";
    }
}
