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
 * Created by KS on 09.09.2017.
 */

public class UserLoginTask extends AsyncTask <Void, Void, Boolean> implements VolleyResponse {

    private final String mLogin;
    private final String mPassword;

    private Context context;
    private Activity activity;
    private ActivityTasksCallback ATC;
    private VolleyApi Api;

    public UserLoginTask(Context context, String login, String password) {
        this.mLogin = login;
        this.mPassword = password;
        this.context = context;
        this.activity = (Activity) context;
        this.ATC = (ActivityTasksCallback) context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        this.Api = new VolleyApi(this.context, this);
        this.sign_in();
        return true;
    }

    private void sign_in() {
        String method = "user_login";

        Map<String, String> params = new HashMap<>();
        params.put("user_login", this.mLogin);
        params.put("user_pass", this.mPassword);

        this.Api.send_request(method, params);
    }

    public void VolleyResponseData(Integer header, String json_response, String method) {
        boolean success = (header == 200) ? true : false;
        this.ATC.onTaskFinish(success, json_response, null);
    }

}
