package com.valpiok.NaviPark.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.valpiok.NaviPark.gn_classes.VolleyApi;
import com.valpiok.NaviPark.interfaces.ActivityTasksCallback;
import com.valpiok.NaviPark.interfaces.VolleyResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by KS on 10.09.2017.
 */

public class GPSLocationTask extends AsyncTask<Void, Void, Boolean> implements VolleyResponse {

    private Activity GPSActivity;
    private String Latitude;
    private String Longitude;
    private Context context;
    private VolleyApi Api;

    ActivityTasksCallback ATC;

    public GPSLocationTask(Context context, String latitude, String longitude) {
        this.ATC = (ActivityTasksCallback) context;
        this.GPSActivity = (Activity) context;
        this.Latitude = latitude;
        this.Longitude = longitude;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        this.Api = new VolleyApi(context, this);
        this.get_gps_parking();
        return true;
    }

    private void result_handler(final boolean success, final String json_resp) {
        this.ATC.onTaskFinish(success, json_resp, null);
    }

    private void get_gps_parking() {
        String method = "gps_parking";
        Map<String, String> params = new HashMap<>();
        params.put("latitude", this.Latitude);
        params.put("longitude", this.Longitude);
        this.Api.send_request(method, params);
    }

    @Override
    public void VolleyResponseData(Integer header, String json_response, String method) {
        boolean success = (header == 200) ? true : false;
        this.result_handler(success, json_response);
    }

}
