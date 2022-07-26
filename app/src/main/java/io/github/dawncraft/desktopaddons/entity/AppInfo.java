package io.github.dawncraft.desktopaddons.entity;

import java.util.Date;

/**
 * 用于检查更新的应用信息
 *
 * @author QingChenW
 */
public class AppInfo
{
    String version;
    String message;
    String releaseTime;
    int size;
    String downloadUrl;

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getReleaseTime()
    {
        return releaseTime;
    }

    public void setReleaseTime(String releaseTime)
    {
        this.releaseTime = releaseTime;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }
}
