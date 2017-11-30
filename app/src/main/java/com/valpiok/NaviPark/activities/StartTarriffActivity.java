package com.valpiok.NaviPark.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.valpiok.NaviPark.R;
import com.valpiok.NaviPark.fragments.DialogWindow;
import com.valpiok.NaviPark.fragments.Timer;
import com.valpiok.NaviPark.gn_classes.General;
import com.valpiok.NaviPark.gn_classes.SaveDataSharPref;
import com.valpiok.NaviPark.gn_classes.TarriffTimeControl;
import com.valpiok.NaviPark.interfaces.ActivityTasksCallback;
import com.valpiok.NaviPark.tasks.GetUserInfo;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by SERHIO on 21.09.2017.
 */

public class StartTarriffActivity extends AppCompatActivity implements View.OnClickListener, ActivityTasksCallback, View.OnTouchListener {

    private boolean ValidTarriff;
    private HashMap TarriffData;
    private LinearLayout base_layout;
    private Integer parkID;
    private Integer TarriffID;
    private Toolbar mToolbar;
    private Long max_tarriff_time;

    private int timer_hour = 0;
    private int timer_min = 0;
    //private int timer_sec = 0;
    private AppCompatTextView DisplayTimerHour;
    private AppCompatTextView DisplayTimerMin;
    //private AppCompatTextView DisplayTimerSec;

    private Integer TimerButtons[] = {
            R.id.tarriff_hour_up,
            R.id.tarriff_hour_down,
            R.id.tarriff_min_up,
            R.id.tarriff_min_down,
//            R.id.tarriff_sec_up,
//            R.id.tarriff_sec_down
    };

    AppCompatTextView calcTarriffRes;
    private TarriffTimeControl TTControl;
    private AppCompatButton startTimer;
    private Double TarriffTotal = 0.00;
    private Double UserBalance;
    private Double TarriffPriceMin;

    private final int GET_USER_INFO_TASK = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_tarriff_activity);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String[] Ids = extras.getStringArray("TarriffIds");
            this.parkID = Integer.parseInt(Ids[3]);
            this.TarriffID = Integer.parseInt(Ids[2]);
            HashMap ValidTarrifs = (HashMap) extras.get("ValidTarriffs");
            this.ValidTarriff = (ValidTarrifs.containsKey(this.TarriffID)) ? true : false;
            this.TarriffData = (HashMap) extras.get("TarriffData");
        }

        Log.d("park_id", "" + this.parkID);

        this.mToolbar =  (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(this.mToolbar);

        this.base_layout = (LinearLayout) findViewById(R.id.base);
        this.start_task(GET_USER_INFO_TASK);
        this.calcTarriffRes = (AppCompatTextView) findViewById(R.id.tarriff_calculate_result);
        this.prepare_timer_listener();
        this.TTControl = new TarriffTimeControl();
    }

    private void prepare_timer_listener() {
        this.DisplayTimerHour = (AppCompatTextView) findViewById(R.id.display_timer_hour);
        this.DisplayTimerMin = (AppCompatTextView) findViewById(R.id.display_timer_min);
        //this.DisplayTimerSec = (AppCompatTextView) findViewById(R.id.display_timer_sec);

        this.startTimer = (AppCompatButton) findViewById(R.id.start_timer);
        this.startTimer.setOnClickListener(this);

        for (int id : this.TimerButtons) {
            AppCompatImageView elem = (AppCompatImageView) findViewById(id);
            elem.setOnClickListener(this);
            //elem.setOnTouchListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.set_info();
    }

//    private void setTimerOnTouch() {
//        AppCompatButton button = (AppCompatButton) findViewById(R.id.test_button);
//        button.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    try {
//                        Thread.sleep(500);
//
//
//
//                    } catch (Exception e) {
//                        Log.e("Sleep Exception", e.getMessage());
//                    }
//
////                    Toast.makeText(getApplicationContext(),
////                            "Молодой человек, не прикасайтесь ко мне!",
////                            Toast.LENGTH_SHORT).show();
//                }
//                return false;
//            }
//        });
//    }

    private void set_info() {
        AppCompatTextView TarTitle = (AppCompatTextView) findViewById(R.id.tarriff_title);
        TarTitle.setText(this.TarriffData.get("tariff_title").toString());
        AppCompatTextView TarContent = (AppCompatTextView) findViewById(R.id.tarriff_content);

        Double price = Double.valueOf(this.TarriffData.get("price").toString());
        String FormatPrice_15min;
        String FormatPrice_1hour;

        if (this.TarriffData.get("unit_type").toString().equals("hour")) {
            this.TarriffPriceMin = price / 60;
            FormatPrice_15min = String.format("%.2f", price / 4);
            FormatPrice_1hour = price.toString();
        } else {
            this.TarriffPriceMin = price;
            FormatPrice_15min = String.format("%.2f", price * 15);
            FormatPrice_1hour = String.format("%.2f", price * 60);
        }

        this.max_tarriff_time = Long.parseLong(this.TarriffData.get("max_time").toString());
        String FormatMaxTime = General.TimeFormat(this.max_tarriff_time);

        String TarInfo = "Valid From: " + this.TarriffData.get("validity_from") + "\nValid To: " + this.TarriffData.get("validity_to") + "\nPrice: 15 Min = " +
                FormatPrice_15min + " CHF " + "\nPrice: 1 Hour = " + FormatPrice_1hour + " CHF " + "\nMax Time: " + FormatMaxTime;

        TarContent.setText(TarInfo);

    }

    @Override
    public void onClick(View v) {
        Integer elemId = v.getId();
        switch (elemId) {
            case R.id.start_timer:
                this.startTimer.setVisibility(View.GONE);
                LinearLayout set_timer_layout = (LinearLayout) findViewById(R.id.set_timer_layout);
                set_timer_layout.setVisibility(View.GONE);
                this.add_fragment_timer();
                break;
            case R.id.tarriff_hour_up:
                this.timer_hour = (this.timer_hour == 24) ? 0 : this.timer_hour + 1;
                this.set_timer_values(this.DisplayTimerHour, this.timer_hour);
                break;
            case R.id.tarriff_hour_down:
                this.timer_hour = (this.timer_hour == 0) ? 24 : this.timer_hour - 1;
                this.set_timer_values(this.DisplayTimerHour, this.timer_hour);
                break;
            case R.id.tarriff_min_up:
                this.timer_min = (this.timer_min == 60) ? 0 : this.timer_min + 1;
                this.set_timer_values(this.DisplayTimerMin, this.timer_min);
                break;
            case R.id.tarriff_min_down:
                this.timer_min = (this.timer_min == 0) ? 60 : this.timer_min - 1;
                this.set_timer_values(this.DisplayTimerMin, this.timer_min);
                break;
//            case R.id.tarriff_sec_up:
//                this.timer_sec = (this.timer_sec == 60) ? 0 : this.timer_sec + 1;
//                this.set_timer_values(this.DisplayTimerSec, this.timer_sec);
//                break;
//            case R.id.tarriff_sec_down:
//                this.timer_sec = (this.timer_sec == 0) ? 60 : this.timer_sec - 1;
//                this.set_timer_values(this.DisplayTimerSec, this.timer_sec);
//                break;
        }

        if (Arrays.asList(this.TimerButtons).contains(elemId)) {
            Integer set_timer_btn = (this.timer_control()) ? View.VISIBLE : View.GONE;
            this.startTimer.setVisibility(set_timer_btn);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            GregorianCalendar current = new GregorianCalendar();
//            long currentTimeInMillis = current.getTimeInMillis();
//            switch(v.getId()) {
//
//            }
//            try {
//                for (int i = 0; i <= 10; i++) {
//                    Thread.sleep(500);
//                    this.DisplayTimerHour.setText(String.valueOf(i));
//                }
//
//            } catch (Exception e) {
//                Log.e("Sleep Exception", e.getMessage());
//            }
//
////                    Toast.makeText(getApplicationContext(),
////                            "Молодой человек, не прикасайтесь ко мне!",
////                            Toast.LENGTH_SHORT).show();
//        }

        return false;
    }

    private void set_timer_values(AppCompatTextView view, int numb) {
        String numb_value = (numb < 10) ? "0" + numb : String.valueOf(numb);
        view.setText(numb_value);
    }

    @Override
    public void onTaskFinish(boolean success, String json_message, Integer task) {
        switch (task) {
            case GET_USER_INFO_TASK:
                Log.d("taskinfo", success + " ; " +  json_message);
                try {
                    JSONObject json = new JSONObject(json_message);
                    this.UserBalance = Double.valueOf(json.getString("balance"));
                    this.mToolbar.setTitle("Balance: " + this.UserBalance);
                    SaveDataSharPref data = new SaveDataSharPref(this);
                    data.put_string("user_info", json_message);
                } catch (Exception e) {
                    Log.e("StartTariff Json", e.getMessage());
                }
                break;
        }
    }

    private void start_task(int TaskID) {
        switch (TaskID) {
            case GET_USER_INFO_TASK:
                GetUserInfo task = new GetUserInfo(this, GET_USER_INFO_TASK);
                task.execute();
                break;
        }
    }

    private void add_fragment_timer() {
        Timer fragmTimer = new Timer();
        Bundle args = new Bundle();
        long client_time = (this.timer_hour * 60 * 60 * 1000) + (this.timer_min * 60 * 1000);
        args.putLong("timer_time", client_time);
        args.putInt("park_id", this.parkID);
        args.putInt("tarriff_id", this.TarriffID);
        args.putDouble("total", this.TarriffTotal);
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

    private boolean timer_control () {

        if (this.timer_hour == 0 && this.timer_min == 0) {
            this.calcTarriffRes.setText("Please select time.");
            return false;
        }

        Integer aTime[] = {this.timer_hour, this.timer_min};

        if (!this.TTControl.valid_tarriff_time(this.TarriffData.get("validity_to").toString(), aTime, this.max_tarriff_time)) {
            this.calcTarriffRes.setText("You can't be here during this time.");
            return false;
        }

        this.TarriffTotal = ((Double.valueOf(this.timer_hour) * 60) + Double.valueOf(this.timer_min)) * this.TarriffPriceMin;
        if (this.UserBalance < this.TarriffTotal) {
            this.calcTarriffRes.setText("Not enough money.");
            return false;
        } else {
            String display_price = String.format(Locale.US, "%.2f", this.TarriffTotal);
            this.TarriffTotal = Double.valueOf(display_price);
            this.calcTarriffRes.setText("Total: " + display_price);
        }

        return true;
    }
}
