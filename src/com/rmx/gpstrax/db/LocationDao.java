
package com.rmx.gpstrax.db;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.rmx.gpstrax.GpsTrax;

public class LocationDao {

    public static final String TABLE_NAME = "location";

    public static final String COLUMN_NAME_KEY = "k";

    public static final String COLUMN_NAME_VALUE = "v";

    public synchronized void saveLocation(Location loc) {
        JSONObject o = getDlpLocationJson(loc);

        String json = o.toString();
        Log.d("gpstrax", "json: " + json);
        Log.i("gpstrax", "loc.time: " + loc.getTime());
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_KEY, loc.getTime());
        values.put(COLUMN_NAME_VALUE, json);

        SQLiteDatabase db = GpsTrax.dbHelper.getWritableDatabase();
        try {
            long rowId = db.insert(TABLE_NAME, null, values);
            Log.i("gpstrax", "rowId: " + rowId);
        } finally {
            db.close();
            Log.d("gpstrax", "db closed");
        }
    }

    /**
     * Adds up to N rown to ArrayList.
     * 
     * @param res
     * @param n
     * @return true is more data available
     */
    public synchronized boolean getFirstNLocations(List<String> res, int n) {
        Log.i("gpstrax", "getFirstNLocations, n: " + n);
        SQLiteDatabase db = GpsTrax.dbHelper.getWritableDatabase();
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

    public synchronized int delLocations(long firstId, long lastId) {
        SQLiteDatabase db = GpsTrax.dbHelper.getWritableDatabase();
        String firstIdStr = String.valueOf(firstId);
        String lastIdStr = String.valueOf(lastId);
        String where = COLUMN_NAME_KEY + " >= ? and " + COLUMN_NAME_KEY + " <= ?";
        String[] whereValues = new String[] {
                firstIdStr, lastIdStr
        };
        try {
            int cnt = db.delete(TABLE_NAME, where, whereValues);
            Log.i("gpstrax", "deleted " + cnt);
            return cnt;
        } finally {
            db.close();
            Log.d("gpstrax", "db closed");
        }
    }

    JSONObject getDlpLocationJson(Location loc) {
        // DlpLocation l = new DlpLocation();
        // l.prv = "gps";
        // l.ts = loc.getTime();
        // l.lat = loc.getLatitude();
        // l.lon = loc.getLongitude();
        // l.alt = loc.getAltitude();
        // l.acc = loc.getAccuracy();
        // l.brng = loc.getBearing();
        // l.spd = loc.getSpeed();

        JSONObject o = new JSONObject();
        try {
            o.put("prv", "gps");
            o.put("ts", loc.getTime());
            o.put("lat", loc.getLatitude());
            o.put("lon", loc.getLongitude());
            o.put("alt", loc.getAltitude());
            o.put("acc", loc.getAccuracy());
            o.put("brng", loc.getBearing());
            o.put("spd", loc.getSpeed());
            return o;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized int getCount() {
        SQLiteDatabase db = GpsTrax.dbHelper.getReadableDatabase();
        try {
            Log.i("gpstrax", "select count");
            Cursor c = db.rawQuery("select " + COLUMN_NAME_KEY + " from " + TABLE_NAME, null);
            Log.i("gpstrax", "" + c);
            if (c != null) {
                int cnt = c.getCount();
                Log.i("gpstrax", "count: " + cnt);
                return cnt;
            }
            return 0;
        } finally {
            db.close();
            Log.d("gpstrax", "db closed");
        }
    }
}
