package com.valpiok.NaviPark.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.valpiok.NaviPark.gn_classes.SaveDataSharPref;
import com.valpiok.NaviPark.gn_classes.TarriffTimeControl;
import com.valpiok.NaviPark.gn_classes.VolleyApi;
import com.valpiok.NaviPark.interfaces.ActivityTasksCallback;
import com.valpiok.NaviPark.interfaces.VolleyResponse;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SERHIO on 22.09.2017.
 */

public class RecordingStartParkTime extends AsyncTask implements VolleyResponse {

    private Context context;
    private ActivityTasksCallback ATC;
    private VolleyApi Api;
    private int TaskID;
    private String TaskParams[];

    public RecordingStartParkTime(Context context, int TaskID, String[] params, ActivityTasksCallback ATC) {
        this.context = context;
        this.ATC = ATC;
        this.TaskID = TaskID;
        this.TaskParams = params;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        this.recording_car_parking();
        return null;
    }

    private void recording_car_parking() {

        TarriffTimeControl timeControl = new TarriffTimeControl();
        SaveDataSharPref data = new SaveDataSharPref(this.context);
        String userJson = data.read_string("user_info");
        try {
            JSONObject json = new JSONObject(userJson);
            this.Api = new VolleyApi(this.context, this);
            String method = "car_parking_start";
            Map<String, String> params = new HashMap<>();
            params.put("car_id", json.getString("car_id"));
            params.put("parking_id", this.TaskParams[0]);
            params.put("tariff_id", this.TaskParams[1]);
            params.put("user_id", json.getString("user_id"));
            params.put("park_from", timeControl.getCurrentDateFormat("yyyy-MM-dd HH:mm:ss"));
            long time_park_to = timeControl.getCurrentTimeMilisec() + Long.valueOf(this.TaskParams[3]);
            params.put("park_to", timeControl.getDateFormat("yyyy-MM-dd HH:mm:ss", time_park_to));
            params.put("amount", this.TaskParams[2]);
            this.Api.send_request(method, params);

        } catch (Exception e) {
            Log.e("GetUserInfo Json", e.getMessage());
        }
    }

    @Override
    public void VolleyResponseData(Integer header, String json_response, String method) {
        boolean success = (header == 200) ? true : false;
        this.ATC.onTaskFinish(success, json_response, this.TaskID);
    }

}
