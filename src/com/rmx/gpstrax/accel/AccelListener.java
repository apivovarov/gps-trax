
package com.rmx.gpstrax.accel;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.rmx.gpstrax.AlertJson;
import com.rmx.gpstrax.C;
import com.rmx.gpstrax.GpsTrax;

public class AccelListener implements SensorEventListener {

    int zAboveThCnt;

    int zBelowThCnt;

    int zAboveThCnt2;

    boolean hb = false;

    boolean ac = false;

    public AccelListener() {
        Log.i(C.LOG_TAG, "GpsTrax.zAccelTh: " + GpsTrax.zAccelTh);
        Log.i(C.LOG_TAG, "GpsTrax.zAccelTh2: " + GpsTrax.zAccelTh2);

        Log.i(C.LOG_TAG, "GpsTrax.zAboveThCntTh: " + GpsTrax.zAboveThCntTh);
        Log.i(C.LOG_TAG, "GpsTrax.zAboveThCntTh2: " + GpsTrax.zAboveThCntTh2);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("gpstrax", "new accel accuracy: " + accuracy);
    }

    public void onSensorChanged(android.hardware.SensorEvent event) {
        // Log.i(C.LOG_TAG, "" + event.values[2]);
        if (event.values[2] >= GpsTrax.zAccelTh) {
            zAboveThCnt++;
            zBelowThCnt = 0;
            Log.i(C.LOG_TAG, "zAboveThCnt: " + zAboveThCnt);

            // hardbrake detected
            if (zAboveThCnt == GpsTrax.zAboveThCntTh && !hb) {
                hb = true;
                Log.i(C.LOG_TAG, "hb=true");
                long eventMs = System.currentTimeMillis() + (event.timestamp - System.nanoTime())
                        / 1000000L;
                saveAlert(eventMs, "hb", event.values[2]);
                GpsTrax.playNotif();
            }

            // // write accel to DB
            // StringBuilder sb = new StringBuilder();
            // for (float v : event.values) {
            // if (sb.length() > 0) {
            // sb.append(" ");
            // }
            // sb.append(v);
            // }
            // long eventMs = System.currentTimeMillis() + (event.timestamp -
            // System.nanoTime())
            // / 1000000L;
            // // Log.i("gpstrax", "eventMs: " + eventMs);
            // GpsTrax.accelDao.saveAccel(eventMs, event.values);
            // Log.i("gpstrax", sb.toString());
        } else if (event.values[2] <= -GpsTrax.zAccelTh2) {
            zAboveThCnt2++;
            zBelowThCnt = 0;
            Log.i(C.LOG_TAG, "zAboveThCnt2: " + zAboveThCnt2);

            // hard acceleration detected
            if (zAboveThCnt2 == GpsTrax.zAboveThCntTh2 && !ac) {
                ac = true;
                Log.i(C.LOG_TAG, "ac=true");
                long eventMs = System.currentTimeMillis() + (event.timestamp - System.nanoTime())
                        / 1000000L;
                saveAlert(eventMs, "ac", event.values[2]);
                GpsTrax.playNotif();
            }
        } else {
            if (zAboveThCnt > 0) {
                zAboveThCnt = 0;
            }
            if (zAboveThCnt2 > 0) {
                zAboveThCnt2 = 0;
            }
            if (zBelowThCnt < 2) {
                zBelowThCnt++;
                Log.i(C.LOG_TAG, "zBelowThCnt: " + zBelowThCnt);
                if (zBelowThCnt == 2) {
                    hb = false;
                    ac = false;
                    Log.i(C.LOG_TAG, "hb=false, ac=false");
                }
            }
        }

    }

    protected void saveAlert(long ts, String type, float accel) {
        AlertJson alert = new AlertJson();
        alert.type = type;
        alert.ts = ts;

        Location loc = GpsTrax.me.getLocationManager().getLastKnownLocation(
                LocationManager.PASSIVE_PROVIDER);

        if (loc != null) {
            alert.lat = loc.getLatitude();
            alert.lon = loc.getLongitude();
        }

        alert.accel = accel;

        GpsTrax.alertDao.saveAlert(alert);
    }
}
