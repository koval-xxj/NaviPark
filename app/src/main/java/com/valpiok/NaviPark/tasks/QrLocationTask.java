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
 * Created by SERHIO on 07.09.2017.
 */

public class QrLocationTask extends AsyncTask<Void, Void, Boolean> implements VolleyResponse {

    private Activity QrActivity;
    private String QR;
    private Context context;
    private VolleyApi Api;

    private Integer TaskID;

    ActivityTasksCallback ATC;

    public QrLocationTask(Context context, String qr, Integer TaskId) {
        this.ATC = (ActivityTasksCallback) context;
        this.QrActivity = (Activity) context;
        this.QR = qr;
        this.context = context;
        this.TaskID = TaskId;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        this.Api = new VolleyApi(context, this);
        this.get_qr_parking();
        return true;
    }

    private void result_handler(final boolean success, final String json_resp) {
        this.ATC.onTaskFinish(success, json_resp, this.TaskID);
    }

    private void get_qr_parking() {
        String method = "qr_parking";
        Map<String, String> params = new HashMap<>();
        params.put("qr", this.QR);
        this.Api.send_request(method, params);
    }

    @Override
    public void VolleyResponseData(Integer header, String json_response, String method) {
        boolean success;
        switch (header) {
            case 200:
                success = true;
                break;
            default:
                success = false;
        }
        this.result_handler(success, json_response);
    }

}
