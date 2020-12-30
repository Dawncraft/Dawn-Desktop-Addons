package io.github.dawncraft.desktopaddons;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class WidgetIDProvider extends ContentProvider
{
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        SQLiteOpenHelper dbHelper = new SQLiteOpenHelper(getContext(), "APPWidget", null, 1)
        {
            @Override
            public void onCreate(SQLiteDatabase db)
            {

            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
            {

            }
        };
        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    @Override
    public String getType(Uri uri)
    {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        return null;
    }
}
