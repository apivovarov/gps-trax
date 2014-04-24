
package com.rmx.gpstrax.accel;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.util.Log;

import com.rmx.gpstrax.GpsTrax;

public class AccelListener implements SensorEventListener {

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("gpstrax", "new accel accuracy: " + accuracy);
    }

    public void onSensorChanged(android.hardware.SensorEvent event) {
        boolean aboveTh = false;
        boolean above1mssTh = false;
        for (float v : event.values) {
            if (Math.abs(v) >= GpsTrax.accelTh) {
                aboveTh = true;
                above1mssTh = true;
                break;
            }
            if (Math.abs(v) >= 1.0f) {
                above1mssTh = true;
            }
        }

        if (aboveTh) {
            StringBuilder sb = new StringBuilder();
            for (float v : event.values) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(v);
            }
            Log.i("gpstrax", sb.toString());
            GpsTrax.playNotif();
        }
        if (above1mssTh) {
            long eventMs = System.currentTimeMillis() + (event.timestamp - System.nanoTime())
                    / 1000000L;
            Log.i("gpstrax", "eventMs: " + eventMs);
            GpsTrax.accelDao.saveAccel(eventMs, event.values);
        }
    }
}
