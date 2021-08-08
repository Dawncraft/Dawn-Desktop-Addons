package io.github.dawncraft.desktopaddons.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.github.dawncraft.desktopaddons.entity.NCPAppWidgetID;

@Dao
public interface NCPAppWidgetDAO
{
    @Query("SELECT * FROM ncp_app_widget_id")
    List<NCPAppWidgetID> getAll();

    @Query("SELECT * FROM ncp_app_widget_id WHERE id == :id")
    NCPAppWidgetID findById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NCPAppWidgetID item);

    // NOTE @Delete 只能根据Entity删除指定行
    @Query("DELETE FROM ncp_app_widget_id WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM ncp_app_widget_id")
    void deleteAll();
}
