package io.github.dawncraft.desktopaddons.entity;

/**
 * 用于展示的新冠肺炎数据
 *
 * @author QingChenW
 */
public class NCPInfo
{
    private String region;
    private int confirm;
    private int suspect;
    private int cure;
    private int dead;
    private String date;
    private String updateTime;

    public String getRegion()
    {
        return region;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    public int getConfirm()
    {
        return confirm;
    }

    public void setConfirm(int confirm)
    {
        this.confirm = confirm;
    }

    public int getSuspect()
    {
        return suspect;
    }

    public void setSuspect(int suspect)
    {
        this.suspect = suspect;
    }

    public int getCure()
    {
        return cure;
    }

    public void setCure(int cure)
    {
        this.cure = cure;
    }

    public int getDead()
    {
        return dead;
    }

    public void setDead(int dead)
    {
        this.dead = dead;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(String updateTime)
    {
        this.updateTime = updateTime;
    }
}
