package io.github.dawncraft.desktopaddons;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import io.github.dawncraft.desktopaddons.dao.NCPAppWidgetDAO;
import io.github.dawncraft.desktopaddons.entity.NCPAppWidgetID;

@Database(entities = { NCPAppWidgetID.class }, version = 1)
public abstract class DADatabase extends RoomDatabase
{
    public abstract NCPAppWidgetDAO ncpAppWidgetDAO();
}
