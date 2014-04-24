
package com.rmx.gpstrax;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rmx.gpstrax.loc.LocationService;
import com.rmx.gpstrax.net.NetworkService;

public class MainActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        refreshAll();
        Log.d("gpstrax", "onResume");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("gpstrax", "onCreate");

        setContentView(R.layout.main);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        GpsTrax.plateNo = sharedPref.getString(C.PLATE_NO, "6YIT551");
        EditText editPlateNo = (EditText)findViewById(R.id.editPlateNo);
        editPlateNo.setText(GpsTrax.plateNo);

        Log.d("gpstrax", "onCreate done");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    protected void openSettings() {

    }

    protected void updateTextLatLon(Location loc) {
        if (loc != null) {
            TextView textLatLon = (TextView)findViewById(R.id.textLatLon);
            if (textLatLon != null) {
                String alt = "" + loc.getAltitude();
                if (alt.length() > 10) {
                    alt = alt.substring(0, 10);
                }
                textLatLon.setText(loc.getLatitude() + "," + loc.getLongitude() + " alt: " + alt);
            }
            String dateTime = GpsTrax.sdf.format(new Date(loc.getTime()));
            TextView textLastTime = (TextView)findViewById(R.id.textLastTime);
            textLastTime.setText(dateTime);
        }
    }

    public void showShortToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public void showToast(String msg, int duration) {
        Toast.makeText(getApplicationContext(), msg, duration).show();
    }

    public void buttonSendLocationsClick(View view) {
        Intent networkServIntent = new Intent(this, NetworkService.class);
        networkServIntent.putExtra(C.NET_OBJ, "L");
        startService(networkServIntent);
    }

    public void buttonOpenAccelClick(View view) {
        Log.d("gpstrax", "buttonOpenAccelClick");
        Intent accelIntent = new Intent(this, AccelActivity.class);
        startActivity(accelIntent);
    }

    public void buttonListenGps5(View view) {
        startLocationService(5000);
    }

    public void buttonListenGps180(View view) {
        startLocationService(180000);
    }

    public void buttonStopGpsClick(View view) {
        stopLocationService();
    }

    public void buttonRefreshCountClick(View view) {
        refreshAll();
    }

    public void buttonResetCountClick(View view) {
        GpsTrax.resetCounters();
        updateTextGpsCnt();
        updateTextSentCnt();
    }

    protected void refreshAll() {
        updateTextGspUpdateStatus();
        updateTextGpsCnt();
        updateTextSentCnt();
        updateTextLatLon(GpsTrax.lastLocation);
        updateTextDbLocationCount();
    }

    public void buttonSavePlateNo(View view) {
        savePlateNo();
    }

    protected void startLocationService(int gpsFreq) {
        Intent locationServIntent = new Intent(this, LocationService.class);
        locationServIntent.putExtra(C.GPS_FREQ, gpsFreq);
        startService(locationServIntent);
        Log.i("gpstrax", "called startLocationService");
    }

    protected void stopLocationService() {
        Intent locationServIntent = new Intent(this, LocationService.class);
        stopService(locationServIntent);
        Log.i("gpstrax", "called stopLocationService");
    }

    protected void savePlateNo() {
        EditText editPlateNo = (EditText)findViewById(R.id.editPlateNo);
        String plateNo0 = editPlateNo.getText().toString();

        if (plateNo0 == null || (plateNo0 = plateNo0.trim()).isEmpty()) {
            showShortToast("plateNo is empty");
            return;
        }

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(C.PLATE_NO, plateNo0);
        boolean res = editor.commit();
        showShortToast("commited: " + res);
        if (res) {
            GpsTrax.plateNo = plateNo0;
            hideSoftInput(editPlateNo.getWindowToken());
            LinearLayout topPanel = (LinearLayout)findViewById(R.id.topPanel);
            topPanel.requestFocus();
        }
    }

    protected void updateTextGpsCnt() {
        TextView textGpsCnt = (TextView)findViewById(R.id.textGpsCnt);
        textGpsCnt.setText(String.valueOf(GpsTrax.gpsCnt));
    }

    protected void updateTextSentCnt() {
        TextView textSendCnt = (TextView)findViewById(R.id.textSendCnt);
        textSendCnt.setText(String.valueOf(GpsTrax.sendCnt));
    }

    protected void updateTextDbLocationCount() {
        int cnt = GpsTrax.locationDao.getCount();
        TextView textSelectCount = (TextView)findViewById(R.id.textDbLocationCount);
        textSelectCount.setText(String.valueOf(cnt));
    }

    protected void updateTextGspUpdateStatus() {
        TextView textGpsStatus = (TextView)findViewById(R.id.textGpsUpdatesStatus);
        String status = GpsTrax.gpsFreq > 0 ? (GpsTrax.gpsFreq / 1000 + " sec") : "-";
        textGpsStatus.setText(status);
    }

    protected void toggleSoftInput() {
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    protected void hideSoftInput(IBinder windowToken) {
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
