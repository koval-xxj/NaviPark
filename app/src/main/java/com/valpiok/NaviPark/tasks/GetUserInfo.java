package com.valpiok.NaviPark.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.valpiok.NaviPark.gn_classes.SaveDataSharPref;
import com.valpiok.NaviPark.gn_classes.VolleyApi;
import com.valpiok.NaviPark.interfaces.ActivityTasksCallback;
import com.valpiok.NaviPark.interfaces.VolleyResponse;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SERHIO on 22.09.2017.
 */

public class GetUserInfo extends AsyncTask implements VolleyResponse {

    private Context context;
    private ActivityTasksCallback ATC;
    private VolleyApi Api;
    private int TaskID;

    public GetUserInfo(Context context, int TaskID) {
        this.context = context;
        this.ATC = (ActivityTasksCallback) context;
        this.TaskID = TaskID;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        this.get_user_info();
        return null;
    }

    private void get_user_info() {
        SaveDataSharPref data = new SaveDataSharPref(this.context);
        String userJson = data.read_string("user_info");
        try {
            JSONObject json = new JSONObject(userJson);
            this.Api = new VolleyApi(this.context, this);
            String method = "user_get_info";
            Map<String, String> params = new HashMap<>();
            params.put("user_id", json.getString("user_id"));
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
