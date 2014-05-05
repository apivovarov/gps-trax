
package com.rmx.gpstrax.accel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.rmx.gpstrax.GpsTrax;
import com.rmx.gpstrax.R;

public class AccelService extends Service {

    Notification.Builder notifBuilder;

    static final int NOTIF_ID = 2;

    long adjFreq;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("gpstrax", "AccelService onStartCommand");

        if (GpsTrax.accelListener != null) {
            removeAccelListener();
            try {
                // sleep needed to switch from 3 min to 5 sec gps listener
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
        addAccelListener();

        notifBuilder.setContentTitle(getHbAcParams());
        getNotifMngr().notify(NOTIF_ID, notifBuilder.getNotification());

        return START_STICKY;
    }

    String getHbAcParams() {
        return "hb:" + GpsTrax.zAccelTh + "/" + GpsTrax.zAboveThCntTh + "; ac:" + GpsTrax.zAccelTh2
                + "/" + GpsTrax.zAboveThCntTh2;
    }

    @Override
    public void onCreate() {
        Log.i("gpstrax", "AccelService onCreate: " + this);
        super.onCreate();

        notifBuilder = new Notification.Builder(GpsTrax.context).setContentTitle(getHbAcParams())
                .setContentText("").setSmallIcon(R.drawable.brakes2);

        startForeground(NOTIF_ID, notifBuilder.getNotification());
        Log.i("gpstrax", "started AccelService in foreground");
    }

    @Override
    public void onDestroy() {
        removeAccelListener();
        Log.i("gpstrax", "AccelService onDestroy: " + this);
        super.onDestroy();
    }

    private void addAccelListener() {
        try {
            SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
            Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            if (accelSensor != null) {
                Log.i("gpstrax", "Accel sensor found");
                int minDelay = accelSensor.getMinDelay();
                Log.i("gpstrax", "minDelay: " + minDelay);
                Log.i("gpstrax", "res: " + accelSensor.getResolution());
                Log.i("gpstrax", "max.range: " + accelSensor.getMaximumRange());
                Log.i("gpstrax", "power: " + accelSensor.getPower());
                Log.i("gpstrax", "ver: " + accelSensor.getVersion());
                Log.i("gpstrax", "vendor: " + accelSensor.getVendor());
                Log.i("gpstrax",
                        "list.size: "
                                + sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION)
                                        .size());

                if (GpsTrax.accelListener == null) {
                    GpsTrax.accelListener = new AccelListener();
                    sensorManager.registerListener(GpsTrax.accelListener, accelSensor,
                            SensorManager.SENSOR_DELAY_NORMAL);
                    Log.i("gpstrax", "accelListener registered");
                } else {
                    Log.i("gpstrax", "accelListener was already registered");
                }
            } else {
                Log.w("gpstrax", "No Accel sensor");
            }
        } catch (RuntimeException e) {
            Log.e("gpstrax", e.getMessage(), e);
        }
    }

    protected void removeAccelListener() {
        try {
            SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
            if (GpsTrax.accelListener != null) {
                sensorManager.unregisterListener(GpsTrax.accelListener);
                GpsTrax.accelListener = null;
                Log.i("gpstrax", "accelListener unregistered");
            }
        } catch (RuntimeException e) {
            Log.e("gpstrax", e.getMessage(), e);
        }
    }

    protected NotificationManager getNotifMngr() {
        NotificationManager notifMngr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return notifMngr;
    }
}
