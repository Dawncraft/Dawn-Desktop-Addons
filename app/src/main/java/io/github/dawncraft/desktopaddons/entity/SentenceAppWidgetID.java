package io.github.dawncraft.desktopaddons.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 存储在数据库中的一言桌面小部件实体
 *
 * @author QingChenW
 */
@Entity(tableName = "sentence_app_widget_id")
public class SentenceAppWidgetID
{
    @PrimaryKey
    public int id;
    @ColumnInfo
    public Sentence.Source source;
    @ColumnInfo
    public String sid;
}
