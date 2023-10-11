package io.github.dawncraft.desktopaddons;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import io.github.dawncraft.desktopaddons.dao.NCPAppWidgetDAO;
import io.github.dawncraft.desktopaddons.dao.SentenceAppWidgetDAO;
import io.github.dawncraft.desktopaddons.entity.NCPAppWidgetID;
import io.github.dawncraft.desktopaddons.entity.SentenceAppWidgetID;

@Database(
        version = 2,
        entities = { NCPAppWidgetID.class, SentenceAppWidgetID.class },
        autoMigrations = { @AutoMigration(from = 1, to = 2) }
)
public abstract class DADatabase extends RoomDatabase
{
    public static final String DB_NAME = "db";
    public abstract NCPAppWidgetDAO ncpAppWidgetDAO();
    public abstract SentenceAppWidgetDAO sentenceAppWidgetDAO();
}
