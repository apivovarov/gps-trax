
package com.rmx.gpstrax.loc;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.rmx.gpstrax.C;
import com.rmx.gpstrax.GpsTrax;
import com.rmx.gpstrax.R;

public class LocationService extends Service {

    Notification.Builder notifBuilder;

    static final int NOTIF_ID = 1;

    long adjFreq;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int freq = intent.getIntExtra(C.GPS_FREQ, 5000);

        boolean highFreq;
        int realFreq;
        if (freq <= 10000) {
            // if freq is high - track gsp constantly (every 1 sec)
            realFreq = 1000;
            adjFreq = freq - 200;
            highFreq = true;
        } else {
            // if freq is low then give gps 7 sec to fix gps after long sleep
            // adjFreq related logic should always return true
            realFreq = freq - 7000;
            adjFreq = 0;
            highFreq = false;
        }

        Log.i("gpstrax", "LocationService onStartCommand, freq: " + freq + " adjFreq: " + adjFreq
                + " realFreq: " + realFreq);

        if (GpsTrax.gpsLocListener != null) {
            removeSpeedListener();
            removeLocationListener();
            try {
                // sleep needed to switch from 3 min to 5 sec gps listener
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
        if (highFreq) {
            addSpeedListener(realFreq);
        }
        addLocationListener(realFreq);

        notifBuilder.setContentTitle("GPS update freq: " + freq / 1000 + " sec");
        getNotifMngr().notify(NOTIF_ID, notifBuilder.getNotification());

        GpsTrax.gpsFreq = freq;

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i("gpstrax", "LocationService onCreate: " + this);
        super.onCreate();

        notifBuilder = new Notification.Builder(GpsTrax.context)
                .setContentTitle("GPS update freq: -").setContentText("")
                .setSmallIcon(R.drawable.pin_map_gps);

        startForeground(NOTIF_ID, notifBuilder.getNotification());
        Log.i("gpstrax", "started service in foreground");
    }

    @Override
    public void onDestroy() {
        removeSpeedListener();
        removeLocationListener();
        Log.i("gpstrax", "LocationService onDestroy: " + this);
        super.onDestroy();
    }

    protected void addLocationListener(int freq) {
        try {
            LocationManager lm = getLocationManager();
            GpsTrax.gpsLocListener = new GpsLocationListener();
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, freq, 0f,
                    GpsTrax.gpsLocListener);
            Log.i("gpstrax", "LocationListener added, freq: " + freq);
        } catch (RuntimeException e) {
            Log.e("gpstrax", e.getMessage(), e);
        }
    }

    protected void removeLocationListener() {
        try {
            if (GpsTrax.gpsLocListener != null) {
                getLocationManager().removeUpdates(GpsTrax.gpsLocListener);
                GpsTrax.gpsLocListener = null;
                GpsTrax.gpsFreq = -1;
                Log.i("gpstrax", "LocationListener removed");
            }
        } catch (RuntimeException e) {
            Log.e("gpstrax", e.getMessage(), e);
        }
    }

    protected void addSpeedListener(int freq) {
        try {
            LocationManager lm = getLocationManager();
            GpsTrax.speedListener = new SpeedListener();
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, freq, 0f, GpsTrax.speedListener);
            Log.i("gpstrax", "SpeedListener added, freq: " + freq);
        } catch (RuntimeException e) {
            Log.e("gpstrax", e.getMessage(), e);
        }
    }

    protected void removeSpeedListener() {
        try {
            if (GpsTrax.speedListener != null) {
                getLocationManager().removeUpdates(GpsTrax.speedListener);
                GpsTrax.speedListener = null;
                Log.i("gpstrax", "SpeedListener removed");
            }
        } catch (RuntimeException e) {
            Log.e("gpstrax", e.getMessage(), e);
        }
    }

    public class GpsLocationListener implements LocationListener {

        long lastUpdate;

        @Override
        public void onLocationChanged(Location location) {
            try {
                if (location != null) {
                    long now = location.getTime();
                    if (now >= C.MIN_LOC_TS && now < C.MAX_LOC_TS) {
                        long diff = now - lastUpdate;
                        Log.i("gpstrax", "onLocationChanged. gps time: " + location.getTime()
                                + " diff: " + diff);

                        if (diff >= adjFreq) {
                            lastUpdate = now;
                            logLocation(location);

                            GpsTrax.locationDao.saveLocation(location);
                            GpsTrax.gpsCnt++;
                            GpsTrax.lastLocation = location;

                            String datetime = GpsTrax.sdfHhmmss
                                    .format(new Date(location.getTime()));
                            notifBuilder.setContentText(location.getLatitude() + ","
                                    + location.getLongitude() + " " + datetime);
                            getNotifMngr().notify(NOTIF_ID, notifBuilder.getNotification());
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
    }

    protected LocationManager getLocationManager() {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        return lm;
    }

    protected NotificationManager getNotifMngr() {
        NotificationManager notifMngr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return notifMngr;
    }

    protected void logLocation(Location loc) {
        Log.i("gpstrax",
                "latlon: " + loc.getLatitude() + "," + loc.getLongitude() + " alt: "
                        + loc.getAltitude() + " time: " + loc.getTime());
    }
}
