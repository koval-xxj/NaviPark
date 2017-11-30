package com.valpiok.NaviPark.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.valpiok.NaviPark.gn_classes.VolleyApi;
import com.valpiok.NaviPark.interfaces.ActivityTasksCallback;
import com.valpiok.NaviPark.interfaces.VolleyResponse;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by KS on 09.09.2017.
 */

public class UserRegisterTask extends AsyncTask implements VolleyResponse {

    private Context context;
    private Activity activity;
    private ActivityTasksCallback ATC;
    private VolleyApi Api;

    private Map api_params;

    public UserRegisterTask(Context context, Map params) {
        this.api_params = params;
        this.context = context;
        this.activity = (Activity) context;
        this.ATC = (ActivityTasksCallback) context;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        this.Api = new VolleyApi(this.context, this);
        this.add_user();
        return null;
    }

    private void add_user() {
        this.api_params.put("user_role", "customer");
        this.api_params.put("balance", "0");
        this.api_params.put("user_status", "1");
        this.api_params.put("owner_id", "null");

        String method = "user_register";
        this.Api.send_request(method, this.api_params);
    }

    public void VolleyResponseData(Integer header, String json_response, String method) {
        boolean success = (header == 200) ? true : false;
        if (success) {
            json_response = this.user_info_to_string();
        }
        this.ATC.onTaskFinish(success, json_response, null);
    }

    private String user_info_to_string() {
        String json_user_info = "";
        try {
            JSONObject json = new JSONObject();

            for (Object entry : this.api_params.entrySet()) {
                String [] explode_values = entry.toString().split("=");
                json.put(explode_values[0], explode_values[1]);
            }
            json_user_info = json.toString();

        } catch (Exception e) {
            Log.d("set_user_info_JSON", e.getMessage());
        }

        return json_user_info;
    }

}
