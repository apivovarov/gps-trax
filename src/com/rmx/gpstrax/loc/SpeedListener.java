
package com.rmx.gpstrax.loc;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.rmx.gpstrax.AlertJson;
import com.rmx.gpstrax.GpsTrax;

public class SpeedListener implements LocationListener {

    long startTs;

    double startLat;

    double startLon;

    float maxSpeed;

    double sumSpeed;

    int speedCnt;

    boolean speeding;

    @Override
    public void onLocationChanged(Location location) {
        try {
            if (location != null) {
                float speed = location.getSpeed();
                if (speed > GpsTrax.speedTh) {
                    // mark start point
                    if (!speeding) {
                        startTs = location.getTime();
                        startLat = location.getLatitude();
                        startLon = location.getLongitude();

                        maxSpeed = speed;
                        sumSpeed = speed;
                        speedCnt++;
                        speeding = true;
                    } else {
                        // continue speeding. update max speed
                        if (maxSpeed < speed) {
                            maxSpeed = speed;
                        }
                        sumSpeed += speed;
                        speedCnt++;
                        GpsTrax.playNotif();
                    }
                } else {
                    // mark stop point
                    if (speeding) {
                        AlertJson alert = new AlertJson();
                        alert.type = "sp";
                        alert.ts = startTs;
                        alert.lat = startLat;
                        alert.lon = startLon;

                        alert.ts2 = location.getTime();
                        alert.lat2 = location.getLatitude();
                        alert.lon2 = location.getLongitude();
                        alert.maxSp = maxSpeed;
                        alert.avgSp = (float)(sumSpeed / speedCnt);

                        maxSpeed = 0f;
                        sumSpeed = 0D;
                        speedCnt = 0;
                        speeding = false;

                        saveAlert(alert);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("gpstrax", e.getMessage(), e);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("gpstrax", "gps disabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("gpstrax", "gps enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("gpstrax", "gps status: " + status);
    }

    protected void saveAlert(AlertJson alert) {
        GpsTrax.alertDao.saveAlert(alert);
    }
}
