package io.github.dawncraft.desktopaddons.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ncp_app_widget_id")
public class NCPAppWidgetID
{
    @PrimaryKey
    public int id;
    @ColumnInfo
    public String region;
}
