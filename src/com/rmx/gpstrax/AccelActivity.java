
package com.rmx.gpstrax;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.rmx.gpstrax.accel.AccelService;
import com.rmx.gpstrax.net.NetworkService;

public class AccelActivity extends Activity {

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

        setContentView(R.layout.accel);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        GpsTrax.zAccelTh = sharedPref.getFloat(C.ACCEL_TH, 3.3f);
        GpsTrax.zAboveThCntTh = sharedPref.getInt(C.ABOVE_TH_CNT_TH, 2);

        GpsTrax.zAccelTh2 = sharedPref.getFloat(C.ACCEL_TH_2, 2.0f);
        GpsTrax.zAboveThCntTh2 = sharedPref.getInt(C.ABOVE_TH_CNT_TH_2, 2);

        EditText editAccelTh = (EditText)findViewById(R.id.editAccelTh);
        EditText editAboveThCntTh = (EditText)findViewById(R.id.editAboveThCntTh);
        editAccelTh.setText(Float.toString(GpsTrax.zAccelTh));
        editAboveThCntTh.setText(Integer.toString(GpsTrax.zAboveThCntTh));

        EditText editAccelTh2 = (EditText)findViewById(R.id.editAccelTh2);
        EditText editAboveThCntTh2 = (EditText)findViewById(R.id.editAboveThCntTh2);
        editAccelTh2.setText(Float.toString(GpsTrax.zAccelTh2));
        editAboveThCntTh2.setText(Integer.toString(GpsTrax.zAboveThCntTh2));

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

    public void showShortToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public void showToast(String msg, int duration) {
        Toast.makeText(getApplicationContext(), msg, duration).show();
    }

    public void buttonSendAlertsClick(View view) {
        Log.d("gpstrax", "buttonSendAlertsClick");
        Intent networkServIntent = new Intent(this, NetworkService.class);
        networkServIntent.putExtra(C.NET_OBJ, "ALERT");
        startService(networkServIntent);
    }

    public void buttonSaveAccelsToCsvClick(View view) {
        Log.d("gpstrax", "buttonSendAccelsClick");
        Intent networkServIntent = new Intent(this, NetworkService.class);
        networkServIntent.putExtra(C.NET_OBJ, "ACELL_CSV");
        startService(networkServIntent);
    }

    public void buttonStartAccelClick(View view) {
        startAccelService();
    }

    public void buttonStopAccelClick(View view) {
        stopAccelService();
    }

    public void buttonRefreshCountClick(View view) {
        refreshAll();
    }

    public void buttonResetCountClick(View view) {
        GpsTrax.saveAccelsCnt = 0;
        GpsTrax.sendAlertCnt = 0;
        updateTextAccelSaveCnt();
        updateTextAlertSendCnt();
    }

    public void buttonDeleteAllAccelsClick(View view) {
        deleteAllAccels();
        updateTextDbAccelCount();
    }

    public void buttonDeleteAllAlertsClick(View view) {
        deleteAllAlerts();
        updateTextDbAlertCount();
    }

    protected void refreshAll() {
        updateTextAccelSaveCnt();
        updateTextAlertSendCnt();
        updateTextDbAccelCount();
        updateTextDbAlertCount();
    }

    public void buttonSaveAccelThClick(View view) {
        saveAccelTh();
    }

    protected void startAccelService() {
        Intent accelServIntent = new Intent(this, AccelService.class);
        startService(accelServIntent);
        Log.i("gpstrax", "called startAccelService");
    }

    protected void stopAccelService() {
        Intent accelServIntent = new Intent(this, AccelService.class);
        stopService(accelServIntent);
        Log.i("gpstrax", "called stopLocationService");
    }

    protected void deleteAllAccels() {
        GpsTrax.accelDao.delAccels(0L, Long.MAX_VALUE);
    }

    protected void deleteAllAlerts() {
        GpsTrax.alertDao.delAlerts(0L, Long.MAX_VALUE);
    }

    protected void saveAccelTh() {
        EditText editAccelTh = (EditText)findViewById(R.id.editAccelTh);
        EditText editAboveThCntTh = (EditText)findViewById(R.id.editAboveThCntTh);
        String accelThStr = editAccelTh.getText().toString();
        String aboveThCntThStr = editAboveThCntTh.getText().toString();

        EditText editAccelTh2 = (EditText)findViewById(R.id.editAccelTh2);
        EditText editAboveThCntTh2 = (EditText)findViewById(R.id.editAboveThCntTh2);
        String accelThStr2 = editAccelTh2.getText().toString();
        String aboveThCntThStr2 = editAboveThCntTh2.getText().toString();

        if (accelThStr == null || (accelThStr = accelThStr.trim()).isEmpty()) {
            showShortToast("accelTh is empty");
            return;
        }

        if (aboveThCntThStr == null || (aboveThCntThStr = aboveThCntThStr.trim()).isEmpty()) {
            showShortToast("aboveTcCntTh is empty");
            return;
        }

        if (accelThStr2 == null || (accelThStr2 = accelThStr2.trim()).isEmpty()) {
            showShortToast("accelTh2 is empty");
            return;
        }

        if (aboveThCntThStr2 == null || (aboveThCntThStr2 = aboveThCntThStr2.trim()).isEmpty()) {
            showShortToast("aboveTcCntTh2 is empty");
            return;
        }

        float accelTh;
        float accelTh2;
        int aboveThCntTh;
        int aboveThCntTh2;
        try {
            accelTh = Float.parseFloat(accelThStr);
            aboveThCntTh = Integer.parseInt(aboveThCntThStr);
            accelTh2 = Float.parseFloat(accelThStr2);
            aboveThCntTh2 = Integer.parseInt(aboveThCntThStr2);
        } catch (NumberFormatException e) {
            showShortToast(e.getMessage());
            return;
        }

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(C.ACCEL_TH, accelTh);
        editor.putInt(C.ABOVE_TH_CNT_TH, aboveThCntTh);
        editor.putFloat(C.ACCEL_TH_2, accelTh2);
        editor.putInt(C.ABOVE_TH_CNT_TH_2, aboveThCntTh2);
        boolean res = editor.commit();
        showShortToast("commited: " + res);
        if (res) {
            GpsTrax.zAccelTh = accelTh;
            GpsTrax.zAboveThCntTh = aboveThCntTh;
            GpsTrax.zAccelTh2 = accelTh2;
            GpsTrax.zAboveThCntTh2 = aboveThCntTh2;
            hideSoftInput(editAccelTh.getWindowToken());
            LinearLayout topPanel = (LinearLayout)findViewById(R.id.topPanel);
            topPanel.requestFocus();
        }
    }

    protected void updateTextAccelSaveCnt() {
        TextView textSendAccelCnt = (TextView)findViewById(R.id.textSaveAccelsCnt);
        textSendAccelCnt.setText(String.valueOf(GpsTrax.saveAccelsCnt));
    }

    protected void updateTextAlertSendCnt() {
        TextView textSendAccelCnt = (TextView)findViewById(R.id.textSendAlertCnt);
        textSendAccelCnt.setText(String.valueOf(GpsTrax.sendAlertCnt));
    }

    protected void updateTextDbAccelCount() {
        int cnt = GpsTrax.accelDao.getCount();
        TextView tv = (TextView)findViewById(R.id.textDbAccelCount);
        tv.setText(String.valueOf(cnt));
    }

    protected void updateTextDbAlertCount() {
        int cnt = GpsTrax.alertDao.getCount();
        TextView tv = (TextView)findViewById(R.id.textDbAlertCount);
        tv.setText(String.valueOf(cnt));
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
