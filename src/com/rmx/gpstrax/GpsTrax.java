
package com.rmx.gpstrax;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.rmx.gpstrax.accel.AccelListener;
import com.rmx.gpstrax.db.AccelDao;
import com.rmx.gpstrax.db.AlertDao;
import com.rmx.gpstrax.db.LocationDao;
import com.rmx.gpstrax.db.PlateDbHelper;
import com.rmx.gpstrax.loc.LocationService.GpsLocationListener;
import com.rmx.gpstrax.loc.SpeedListener;

public class GpsTrax extends Application {

    public static Context context;

    public static int gpsFreq;

    public static int gpsCnt;

    public static int sendCnt;

    public static int saveAccelsCnt;

    public static int sendAlertCnt;

    public static String plateNo;

    public static Location lastLocation;

    public static GpsLocationListener gpsLocListener;

    public static SpeedListener speedListener;

    public static AccelListener accelListener;

    public static float speedTh;

    public static float zAccelTh;

    public static int zAboveThCntTh;

    public static float zAccelTh2;

    public static int zAboveThCntTh2;

    public static PlateDbHelper dbHelper;

    public static LocationDao locationDao;

    public static AccelDao accelDao;

    public static AlertDao alertDao;

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static SimpleDateFormat sdfHhmmss = new SimpleDateFormat("HH:mm:ss", Locale.US);

    static ShowErrorHandler showErrorHandler;

    public static PrintWriter pw;

    public static GpsTrax me;

    /*
     * (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        context = getApplicationContext();

        dbHelper = new PlateDbHelper(context);
        locationDao = new LocationDao();
        accelDao = new AccelDao();
        alertDao = new AlertDao();

        sdf.setTimeZone(TimeZone.getDefault());
        sdfHhmmss.setTimeZone(TimeZone.getDefault());

        showErrorHandler = new ShowErrorHandler();
        me = this;
    }

    public static Context getAppContext() {
        return GpsTrax.context;
    }

    public static void resetCounters() {
        gpsCnt = 0;
        sendCnt = 0;
    }

    public static class ShowErrorHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            if (b != null) {
                String errMsg = b.getString(C.ERROR_KEY);
                if (errMsg != null) {
                    Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /*
     * Method can be called from other threads
     */
    public static void showErrorMsg(String msg) {
        Message mymsg = new Message();
        mymsg.getData().putString(C.ERROR_KEY, msg);
        showErrorHandler.sendMessage(mymsg);
    }

    public static void playNotif() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LocationManager getLocationManager() {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        return lm;
    }
}
