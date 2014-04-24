
package com.rmx.gpstrax.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlateDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "plate.db";

    private static final String SQL_CREATE_LOCATION = "create table " + LocationDao.TABLE_NAME
            + " (" + LocationDao.COLUMN_NAME_KEY + " INTEGER PRIMARY KEY, "
            + LocationDao.COLUMN_NAME_VALUE + " TEXT " + ")";

    private static final String SQL_CREATE_ACCEL = "create table " + AccelDao.TABLE_NAME + " ("
            + AccelDao.COLUMN_NAME_KEY + " INTEGER PRIMARY KEY, " + AccelDao.COLUMN_NAME_VALUE
            + " TEXT " + ")";

    private static final String SQL_DROP_LOCATION = "DROP TABLE IF EXISTS "
            + LocationDao.TABLE_NAME;

    private static final String SQL_DROP_ACCEL = "DROP TABLE IF EXISTS " + AccelDao.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_LOCATION);
        db.execSQL(SQL_CREATE_ACCEL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_LOCATION);
        db.execSQL(SQL_DROP_ACCEL);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public PlateDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
