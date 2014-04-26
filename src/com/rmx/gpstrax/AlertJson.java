
package com.rmx.gpstrax;

import org.json.JSONException;
import org.json.JSONObject;

public class AlertJson {

    public String type;

    public long ts;

    public double lat;

    public double lon;

    public long ts2;

    public double lat2;

    public double lon2;

    public float accel;

    public float maxSp;

    public float avgSp;

    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("type", type);
            json.put("ts", ts);
            json.put("lat", lat);
            json.put("lon", lon);

            if (ts2 != 0L) {
                json.put("ts2", ts2);
                json.put("lat2", lat2);
                json.put("lon2", lon2);
            }

            if (accel != 0f) {
                json.put("accel", accel);
            }

            if (maxSp != 0f) {
                json.put("maxSp", maxSp);
                json.put("avgSp", avgSp);
            }

            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
