package com.valpiok.NaviPark.gn_classes;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by SERHIO on 26.09.2017.
 */

public class TarriffTimeControl {

    private GregorianCalendar objCalendar;

    private Integer cur_year;
    private Integer cur_month;
    private Integer cur_day;

    public TarriffTimeControl() {

        this.objCalendar = new GregorianCalendar();
        this.cur_year = this.objCalendar.get(GregorianCalendar.YEAR);
        this.cur_month = this.objCalendar.get(GregorianCalendar.MONTH) + 1;
        this.cur_day = this.objCalendar.get(GregorianCalendar.DAY_OF_MONTH);

    }

    public boolean valid_tarriff_time(String TarriffTimeTo, Integer ClientTime[], Long tMaxTime) {

        boolean result = false;

        long client_time = (ClientTime[0] * 60 * 60 * 1000) + (ClientTime[1] * 60 * 1000);
        if (client_time > tMaxTime) {
            return result;
        }

        Date dTarValTo = this.parce_time(TarriffTimeTo);

        long TimeTarValTo = dTarValTo.getTime();
        long currentTimeInMillis = this.objCalendar.getTimeInMillis();
        client_time = client_time + currentTimeInMillis;

        if (TimeTarValTo > client_time) {
            result = true;
        }

        return result;
    }

    private Date parce_time (String time) {
        Date parce_date = null;
        String Date = cur_year + "-" + cur_month + "-" + cur_day + " " + time;

        try {
            parce_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(Date);
        } catch (Exception e) {
            Log.e("TarriffTimeControl", e.getMessage());
        }

        return parce_date;
    }

    public String getCurrentDateFormat(String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(this.objCalendar.getTime());
    }

    public boolean check_valid_tarrif(String TValidFrom, String TValidTo) {

        boolean result = false;

        Date dValFrom = this.parce_time(TValidFrom);
        Date dValTo = this.parce_time(TValidTo);

        long TimeValFromMS = dValFrom.getTime();
        long TimeValToMS = dValTo.getTime();
        long currentTimeInMillis = this.objCalendar.getTimeInMillis();

        if (TimeValFromMS < currentTimeInMillis && currentTimeInMillis < TimeValToMS) {
            result = true;
        }

        return result;
    }

    public Long getCurrentTimeMilisec() {
        return this.objCalendar.getTimeInMillis();
    }

    public String getDateFormat(String dateFormat, long TimeMilisec) {

        Date date = new Date(TimeMilisec);
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(date);

    }

}
