/*
  Copyright 2017 Valpio-K. All Rights Reserved.
 */

package com.valpiok.NaviPark;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.valpiok.NaviPark.activities.StartTarriffActivity;
import com.valpiok.NaviPark.fragments.DialogWindow;
import com.valpiok.NaviPark.fragments.Timer;
import com.valpiok.NaviPark.gn_classes.ParkingInfoParsing;
import com.valpiok.NaviPark.interfaces.ActivityTasksCallback;
import com.valpiok.NaviPark.tasks.QrLocationTask;

import java.util.HashMap;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class LocationQrActivity extends AppCompatActivity implements View.OnClickListener, ActivityTasksCallback {

    // use a compound button so either checkbox or switch widgets work.
    //private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private AutoCompleteTextView barcodeView;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";
    private static final int TARRIFF_ACTIVITY = 9002;

    private QrLocationTask QrLocTask = null;
    private static final int QrLocTaskID = 1;
    private long parking_max_time;
    private LinearLayout base_layout;

    private ParkingInfoParsing parkResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_location);

        statusMessage = (TextView)findViewById(R.id.status_message);
        barcodeView = (AutoCompleteTextView) findViewById(R.id.barcode_value);

        //autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);

        findViewById(R.id.read_barcode).setOnClickListener(this);
        findViewById(R.id.check_parking).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.base_layout = (LinearLayout) findViewById(R.id.base);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        boolean return_flag = true;

        switch (v.getId()) {
            case R.id.read_barcode:

                // launch barcode activity.
                Intent intent = new Intent(this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                return_flag = false;
                break;
            case R.id.check_parking:
                this.start_qr_task();
                return_flag = false;
                break;
        }

        if (!return_flag) {
            return;
        }

        String[] elemData = v.getTag().toString().split(";");
        switch (elemData[0]) {
            case "tarriff_btn":

                Intent intent = new Intent(this, StartTarriffActivity.class);
                HashMap TarriffData =  this.parkResult.Tarrifs.get(Integer.parseInt(elemData[1])).get(Integer.parseInt(elemData[2]));
                intent.putExtra("TarriffIds", elemData);
                intent.putExtra("ValidTarriffs", this.parkResult.ValidTarrifs);
                intent.putExtra("TarriffData", TarriffData);

                startActivityForResult(intent, TARRIFF_ACTIVITY);

                break;
        }

    }

    private void start_qr_task() {
        String QR = barcodeView.getText().toString();
        if (TextUtils.isEmpty(QR)) {
            barcodeView.setError(getString(R.string.error_field_required));
            return;
        }

        statusMessage.setText("process");
        this.QrLocTask = new QrLocationTask(this, QR, this.QrLocTaskID);
        this.QrLocTask.execute();
    }

    @Override
    public void onTaskFinish(boolean success, String json_message, Integer task) {

        switch (task) {
            case QrLocTaskID:

                this.parkResult = new ParkingInfoParsing(json_message, success);

                statusMessage.setText("Finished");

                if (success) {

                    for (Integer i = 0; i < this.parkResult.ParkTitls.length; i++) {
                        this.add_park_elem_to_layout(this.parkResult.ParkTitls[i]);
                        String ParkingText = "Address: " + this.parkResult.ParkAddress[i] + "\n" + "Places quantity: " + this.parkResult.ParkPlacesQtv[i];
                        this.add_park_elem_to_layout(ParkingText);
                        this.add_park_tarrif_elem(i);
                    }

                } else {
                    this.showDialog("Message", parkResult.error);
                }

                break;
        }

    }

    private void add_park_elem_to_layout(String text) {
        TextView myText = new TextView(new ContextThemeWrapper(this, R.style.textViewParams));
        myText.setText(text);
        this.base_layout.addView(myText);
    }

    private void add_park_tarrif_elem(Integer parkID) {

        HashMap ParkingTarrifs = this.parkResult.Tarrifs.get(parkID);
        Integer parkRealID = this.parkResult.ParkIds[parkID];

        for (Object tariffID : ParkingTarrifs.keySet()) {
            HashMap Tarriff = (HashMap) ParkingTarrifs.get(tariffID);
            Integer id = (Integer) tariffID;
            boolean validTarriff = (this.parkResult.ValidTarrifs.containsKey(id)) ? true : false;
            Integer style = (validTarriff) ? R.style.LayoutValidTarriff : R.style.LayoutNoValidTarriff;
            LinearLayoutCompat TarCont = new LinearLayoutCompat(new ContextThemeWrapper(this, style));

            TextView myText = new TextView(new ContextThemeWrapper(this, R.style.textViewTarrifsParamsTitle));
            myText.setText(Tarriff.get("tariff_title").toString());
            TarCont.addView(myText);

            LinearLayoutCompat ContentLayout = new LinearLayoutCompat(new ContextThemeWrapper(this, R.style.LinearLayoutTContent));

            myText = new TextView(new ContextThemeWrapper(this, R.style.textViewTarrifsParamsContent));
            String tContext = "Valid From: " + Tarriff.get("validity_from") + "\n" + "Valid To: " + Tarriff.get("validity_to");
            myText.setText(tContext);
            ContentLayout.addView(myText);

            if (validTarriff) {
                myText = new TextView(new ContextThemeWrapper(this, R.style.textViewTarrifsParamsBottom));
                myText.setText("More");
                String TagInfo = "tarriff_btn;" + parkID + ";" + id + ";" + parkRealID;
                myText.setTag(TagInfo);
                myText.setOnClickListener(this);
                ContentLayout.addView(myText);
            }

            TarCont.addView(ContentLayout);
            this.base_layout.addView(TarCont);
        }

    }

    private void add_fragment_timer() {
        Timer fragmTimer = new Timer();

        Bundle args = new Bundle();
        args.putLong("parking_max_time", this.parking_max_time);
        fragmTimer.setArguments(args);
        getFragmentManager().beginTransaction()
                .add(R.id.base, fragmTimer)
                .commit();
    }

    public void showDialog(String title, String message) {
        DialogWindow dialog = new DialogWindow();
        dialog.set_title(title);
        dialog.set_message(message);
        dialog.show(getSupportFragmentManager(), "custom");
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    barcodeView.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
