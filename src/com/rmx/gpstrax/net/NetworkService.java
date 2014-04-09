
package com.rmx.gpstrax.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.rmx.gpstrax.C;
import com.rmx.gpstrax.GpsTrax;

public class NetworkService extends IntentService {

    public static final String locationUrlStr = "http://172.31.60.250:8080/dlp-proxy-server/rest/location/save";

    public static final Charset utf8 = Charset.forName("UTF-8");

    public NetworkService() {
        super("NetwotkServiceWorker");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("gpstrax", "NetworkService th: " + Thread.currentThread());
        readAndSendLocations();
    }

    @Override
    public void onDestroy() {
        Log.i("gpstrax", "NetworkService onDestroy");
        super.onDestroy();
    }

    protected void readAndSendLocations() {
        try {
            boolean more = true;
            int cnt = 0;
            while (more) {
                List<String> res = new ArrayList<String>();
                more = GpsTrax.locationDao.getFirstNLocations(res, C.SEND_BATCH_SIZE);
                more = false;

                if (res.size() == 0) {
                    Log.i("gpstrax", "no data were read from DB");
                    break;
                }

                JSONObject locList = new JSONObject();
                JSONArray dlpLocList = new JSONArray();
                long firstId = 0L;
                long lastId = 0L;

                JSONObject dlpLoc = null;
                long dlpLocTs = 0L;
                for (String s : res) {
                    dlpLoc = new JSONObject(s);
                    dlpLocTs = dlpLoc.getLong("ts");
                    // valid time 01/01/2014 - 01/01/2050
                    if (dlpLocTs >= C.MIN_LOC_TS && dlpLocTs < C.MAX_LOC_TS) {
                        dlpLocList.put(dlpLoc);
                    }
                    if (firstId == 0L) {
                        firstId = dlpLocTs;
                    }
                }
                lastId = dlpLocTs;

                if (dlpLocList.length() > 0) {
                    locList.put("ll", dlpLocList);
                    locList.put("plNo", GpsTrax.plateNo);
                    locList.put("ts", System.currentTimeMillis());
                    // send data
                    cnt++;
                    String locListJson = locList.toString();
                    Log.i("gpstrax", "batch " + cnt + ": " + locListJson);

                    sendLocationList(locListJson);
                }
                Log.i("gpstrax", "deleting batch " + cnt + "; firstId: " + firstId + ", lastId: "
                        + lastId);
                GpsTrax.locationDao.delLocations(firstId, lastId);
                GpsTrax.sendCnt++;
            }
        } catch (Exception e) {
            Log.e("gpstrax", e.getMessage(), e);
            GpsTrax.showErrorMsg(e.getMessage());
        }
    }

    public void sendLocationList(String json) throws IOException {
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            throw new IOException("networkInfo is not connected");
        }
        HttpURLConnection urlConnection = null;
        try {
            URL locationUrl = new URL(locationUrlStr);
            urlConnection = (HttpURLConnection)locationUrl.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setConnectTimeout(3000);
            urlConnection.addRequestProperty("Content-Type", "application/json");

            byte[] jsonBytes = json.getBytes(utf8);
            Log.i("gpstrax", "body length: " + jsonBytes.length);
            urlConnection.setFixedLengthStreamingMode(jsonBytes.length);
            // Log.i("gpstrax", "connecting");
            // urlConnection.connect();
            Log.i("gpstrax", "getting output stream");
            OutputStream out = urlConnection.getOutputStream();
            Log.i("gpstrax", "out: " + out);
            out.write(jsonBytes);

            int responseCode = urlConnection.getResponseCode();
            Log.i("gpstrax", "resp code: " + responseCode);
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("bad response code: " + responseCode);
            }
            byte[] respMsg = new byte[100];
            int respLen = urlConnection.getInputStream().read(respMsg);
            String resp = new String(respMsg, 0, respLen);
            Log.i("gpstrax", "resp msg: " + resp);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
