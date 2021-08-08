package io.github.dawncraft.desktopaddons.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils
{
    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
    private static final SimpleDateFormat UTC_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z", Locale.ROOT);

    private DateUtils() {}

    public static String formatDate(Date date)
    {
        return SimpleDateFormat.getDateTimeInstance().format(date);
    }

    public static String formatDefaultDate(String str)
    {
        try
        {
            return formatDate(DEFAULT_DATE_FORMAT.parse(str));
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatUTCDate(String str)
    {
        try
        {
            str = str.replace("Z", " UTC");
            return formatDate(UTC_DATE_FORMAT.parse(str));
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatCurrentDate()
    {
        return formatDate(new Date());
    }
}
