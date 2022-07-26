package io.github.dawncraft.desktopaddons.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 存储在数据库中的新冠肺炎桌面小部件实体
 *
 * @author QingChenW
 */
@Entity(tableName = "ncp_app_widget_id")
public class NCPAppWidgetID
{
    @PrimaryKey
    public int id;
    @ColumnInfo
    public String region;
}
