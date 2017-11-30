/*
  Copyright 2017 Valpio-K. All Rights Reserved.
 */

package com.valpiok.NaviPark;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.valpiok.NaviPark.gn_classes.SaveDataSharPref;
import com.valpiok.NaviPark.payment.activities.TransactionOverviewActivity;

import org.json.JSONObject;


/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class MainActivity extends AppCompatActivity {

    int REQUEST_READ_PHONE_STATE = 999;

    String phone_number;

    private final int ACTIVITY_LOGIN = 1;
    private final int ACTIVITY_REGISTER = 2;
    private final int ACTIVITY_LOCATION = 3;
    private final int ACTIVITY_QR_LOCATION = 4;
    private final int ACTIVITY_PAYMENT = 5;

    private Toolbar mToolbar;
    private SaveDataSharPref fileSettings;

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_page);

        //this.check_phone_permissions();

        this.mToolbar =  (Toolbar) findViewById(R.id.toolbar);
        this.mToolbar.setTitle("Navi Park");
        setSupportActionBar(this.mToolbar);

        this.creat_activity_listeners();

        fileSettings = new SaveDataSharPref(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String user_info = this.fileSettings.read_string("user_info");
        if (!TextUtils.isEmpty(user_info)) {
            this.set_user_info(user_info);
        } else {
            this.set_reg_user_buttons(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void creat_activity_listeners() {
        final Button mDetectLocationButton = (Button) findViewById(R.id.location_button);
        mDetectLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.go_to_activity(ACTIVITY_LOCATION);
            }
        });

        final Button mRegisterNewUser = (Button) findViewById(R.id.register_button);
        mRegisterNewUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.go_to_activity(ACTIVITY_REGISTER);
            }
        });

        final Button mLogin = (Button) findViewById(R.id.login_button);
        mLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.go_to_activity(ACTIVITY_LOGIN);
            }
        });

        final Button mQrLocation = (Button) findViewById(R.id.location_qr_button);
        mQrLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.go_to_activity(ACTIVITY_QR_LOCATION);
            }
        });

        final Button mPayLocation = (Button) findViewById(R.id.payment_button);
        mPayLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.go_to_activity(ACTIVITY_PAYMENT);
            }
        });
    }

    public String getPhoneNumber() {
        Context mContext = this.getApplicationContext();
        String mPhoneNumber = "null";

        try {
            TelephonyManager tMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneNumber = tMgr.getLine1Number().toString();
        } catch (Exception e) {
            Log.e("Phone Exception", e.getMessage().toString());
        }

        this.phone_number = mPhoneNumber;

        return mPhoneNumber;
    }

    private void check_phone_permissions() {
        Context mContext = this.getApplicationContext();

        int permissionCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE);

        Log.d("test log", Integer.toString(permissionCheck));
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            this.getPhoneNumber();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_READ_PHONE_STATE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("READ_PHONE_STATE", "true");
                this.getPhoneNumber();



                //Intent intent = new Intent(this, RegisterActivity.class);

                //startActivity(intent);

            } else {
                Log.d("READ_PHONE_STATE", "false");
            }
        }

    }

    private void go_to_activity(final int activity) {
        switch (activity) {
            case ACTIVITY_LOGIN:
                Intent LogIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(LogIntent, ACTIVITY_LOGIN);
                break;
            case ACTIVITY_LOCATION:

                Intent LocIntent = new Intent(this, LocationActivity.class);
                startActivity(LocIntent);

                break;
            case ACTIVITY_REGISTER:
                Intent RegIntent = new Intent(this, RegisterActivity.class);
                startActivityForResult(RegIntent, ACTIVITY_REGISTER);
                break;

            case ACTIVITY_QR_LOCATION:
                Intent QrLoc = new Intent(this, LocationQrActivity.class);
                startActivity(QrLoc);
                break;
            case ACTIVITY_PAYMENT:
                Intent PayIntent = new Intent(this, TransactionOverviewActivity.class);
                startActivity(PayIntent);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        switch (requestCode) {
            case ACTIVITY_REGISTER:
            case ACTIVITY_LOGIN:
                if (resultCode == RESULT_OK) {
                    Log.d("MainResp149", data.getStringExtra("json_resp"));
                    this.set_user_info(data.getStringExtra("json_resp"));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void set_user_info(final String json_str) {
        this.fileSettings.put_string("user_info", json_str);
        this.set_reg_user_buttons(true);
        try {
            JSONObject json = new JSONObject(json_str);
            this.mToolbar.setTitle("Hello " + json.getString("user_name"));
        } catch (Exception e) {
            Log.d("set_user_info_JSON", e.getMessage());
        }
    }

    private void set_reg_user_buttons(boolean user_exists) {
        final Button mGpsPark = (Button) findViewById(R.id.location_button);
        final Button mQRPark = (Button) findViewById(R.id.location_qr_button);
        final Button mRegisterNewUser = (Button) findViewById(R.id.register_button);
        final Button mLogin = (Button) findViewById(R.id.login_button);
        final Button mPayment = (Button) findViewById(R.id.payment_button);

        if (user_exists) {
            mGpsPark.setVisibility(View.VISIBLE);
            mQRPark.setVisibility(View.VISIBLE);
            mPayment.setVisibility(View.VISIBLE);
            mRegisterNewUser.setVisibility(View.GONE);
            mLogin.setVisibility(View.GONE);
        } else {
            mGpsPark.setVisibility(View.GONE);
            mQRPark.setVisibility(View.GONE);
            mPayment.setVisibility(View.GONE);
            mRegisterNewUser.setVisibility(View.VISIBLE);
            mLogin.setVisibility(View.VISIBLE);
        }
    }

}
