
package com.rmx.gpstrax.accel;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.rmx.gpstrax.AlertJson;
import com.rmx.gpstrax.GpsTrax;

public class AccelListener implements SensorEventListener {

    int zAboveThCnt;

    int zBelowThCnt;

    boolean hb = false;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("gpstrax", "new accel accuracy: " + accuracy);
    }

    public void onSensorChanged(android.hardware.SensorEvent event) {

        if (Math.abs(event.values[2]) >= GpsTrax.zAccelTh) {
            zAboveThCnt++;
            zBelowThCnt = 0;

            // hardbrake detected
            if (zAboveThCnt == GpsTrax.zAboveThCntTh && !hb) {
                hb = true;
                long eventMs = System.currentTimeMillis() + (event.timestamp - System.nanoTime())
                        / 1000000L;
                saveAlert(eventMs, event.values[2]);
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
        } else {
            zAboveThCnt = 0;
            zBelowThCnt++;
            if (zBelowThCnt == 2) {
                hb = false;
            }
        }

    }

    protected void saveAlert(long ts, float accel) {
        AlertJson alert = new AlertJson();
        alert.type = "hb";
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
