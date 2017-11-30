package com.valpiok.NaviPark.fragments;

/**
 * Created by SERHIO on 12.09.2017.
 */

import android.app.Fragment;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.valpiok.NaviPark.R;
import com.valpiok.NaviPark.gn_classes.General;
import com.valpiok.NaviPark.interfaces.ActivityTasksCallback;
import com.valpiok.NaviPark.tasks.RecordingStartParkTime;


public class Timer extends Fragment implements ActivityTasksCallback {

    final String LOG_TAG = "myLogs";

    private View v;
    private long timer_time;
    private static final int NOTIFY_ID = 101;
    private Context context;
    private final int RECORDING_START_PARK_TIME = 102;
    public String TaskParams[] = new String[4];

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        this.context = context;
//        if (this.context == null) {
        this.context = getActivity().getApplicationContext();
        Log.d("this.context45", String.valueOf(this.context));
        //}
        //this.activity = (AppCompatActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.context = getActivity().getApplicationContext();

        v = inflater.inflate(R.layout.fragment_timer, null);
        Bundle params = this.getArguments();
        this.timer_time = params.getLong("timer_time");
        this.TaskParams[0] = String.valueOf(params.getInt("park_id"));
        this.TaskParams[1] = String.valueOf(params.getInt("tarriff_id"));
        this.TaskParams[2] = String.valueOf(params.getDouble("total"));
        this.TaskParams[3] = String.valueOf(this.timer_time);
        this.start_task();
        this.start_timer();

//        Button button = (Button) v.findViewById(R.id.start_timer);
//        button.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Timer.this.start_task();
//                Timer.this.start_timer();
//            }
//        });

        return v;
    }

    private void start_timer() {

        final TextView timer_data = (TextView) v.findViewById(R.id.timer_display);
        timer_data.setText("");
        timer_data.setVisibility(View.VISIBLE);

        new CountDownTimer(this.timer_time, 1000) {

            public void onTick(long millisUntilFinished) {
                timer_data.setText(General.TimeFormat(millisUntilFinished));
            }

            public void onFinish() {
                Timer.this.send_Notification();
                timer_data.setText("Time is up");
            }

        }.start();

    }

    private void send_Notification() {
        Context context = this.context;


        Intent notificationIntent = new Intent(context, getActivity().getClass());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
        // оставим только самое необходимое
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Notigication")
                .setContentText("Time is up."); // Текст уведомления

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND |
                Notification.DEFAULT_VIBRATE;
//        long[] vibrate = new long[] { 1000, 1000, 1000, 1000, 1000 };
//        notification.vibrate = vibrate;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        notificationManager.notify(NOTIFY_ID, notification);
    }

    private void start_task() {
        RecordingStartParkTime task = new RecordingStartParkTime(this.context, RECORDING_START_PARK_TIME, this.TaskParams, this);
        task.execute();
    }

    @Override
    public void onTaskFinish(boolean success, String json_message, Integer task) {
        if (!success) {
            Log.d("onTaskFinish", json_message);
        }
    }

}
