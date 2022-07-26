package io.github.dawncraft.desktopaddons.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.github.dawncraft.desktopaddons.entity.SentenceAppWidgetID;

/**
 * 一言桌面小部件的DAO层
 *
 * @author QingChenW
 */
@Dao
public interface SentenceAppWidgetDAO
{
    @Query("SELECT * FROM sentence_app_widget_id")
    List<SentenceAppWidgetID> getAll();

    @Query("SELECT * FROM sentence_app_widget_id WHERE id == :id")
    SentenceAppWidgetID findById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SentenceAppWidgetID item);

    @Query("DELETE FROM sentence_app_widget_id WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM sentence_app_widget_id")
    void deleteAll();
}
