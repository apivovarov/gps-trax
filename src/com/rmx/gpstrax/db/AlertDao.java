
package com.rmx.gpstrax.db;

import java.util.List;

import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.rmx.gpstrax.AlertJson;
import com.rmx.gpstrax.GpsTrax;

public class AlertDao {

    public static final String TABLE_NAME = "alert";

    public static final String COLUMN_NAME_KEY = "k";

    public static final String COLUMN_NAME_VALUE = "v";

    public synchronized void saveAlert(AlertJson alert) {
        JSONObject o = alert.toJson();

        String json = o.toString();
        Log.d("gpstrax", "alert json: " + json);
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_KEY, alert.ts);
        values.put(COLUMN_NAME_VALUE, json);

        SQLiteDatabase db = GpsTrax.dbHelper.getWritableDatabase();
        try {
            long rowId = db.insert(TABLE_NAME, null, values);
            Log.i("gpstrax", "alert rowId: " + rowId);
        } finally {
            db.close();
            Log.d("gpstrax", "db closed");
        }
    }

    public synchronized int getCount() {
        SQLiteDatabase db = GpsTrax.dbHelper.getReadableDatabase();
        try {
            Log.i("gpstrax", "select alerts count");
            Cursor c = db.rawQuery("select " + COLUMN_NAME_KEY + " from " + TABLE_NAME, null);
            Log.i("gpstrax", "" + c);
            if (c != null) {
                int cnt = c.getCount();
                Log.i("gpstrax", "alerts count: " + cnt);
                return cnt;
            }
            return 0;
        } finally {
            db.close();
            Log.d("gpstrax", "db closed");
        }
    }

    public synchronized boolean getFirstNAlerts(List<String> res, int n) {
        Log.i("gpstrax", "getFirstNAlerts, n: " + n);
        SQLiteDatabase db = GpsTrax.dbHelper.getReadableDatabase();
        try {
            String[] columns = new String[] {
                COLUMN_NAME_VALUE
            };

            Cursor c = db.query(TABLE_NAME, columns, null, null, null, null, COLUMN_NAME_KEY);
            boolean exist = c.moveToFirst();
            if (exist) {
                Log.i("gpstrax", "first row exists");
            }
            while (exist && res.size() < n) {
                String v = c.getString(0);
                res.add(v);
                exist = c.moveToNext();
            }
            return exist;
        } finally {
            db.close();
            Log.d("gpstrax", "db closed");
        }
    }

    public synchronized int delAlerts(long firstId, long lastId) {
        SQLiteDatabase db = GpsTrax.dbHelper.getWritableDatabase();
        String firstIdStr = String.valueOf(firstId);
        String lastIdStr = String.valueOf(lastId);
        String where = COLUMN_NAME_KEY + " >= ? and " + COLUMN_NAME_KEY + " <= ?";
        String[] whereValues = new String[] {
                firstIdStr, lastIdStr
        };
        try {
            int cnt = db.delete(TABLE_NAME, where, whereValues);
            Log.i("gpstrax", "alerts deleted " + cnt);
            return cnt;
        } finally {
            db.close();
            Log.d("gpstrax", "db closed");
        }
    }
}
