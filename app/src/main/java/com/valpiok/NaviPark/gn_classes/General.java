package com.valpiok.NaviPark.gn_classes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by SERHIO on 22.09.2017.
 */

public class General {

    public static String TimeFormat(long millisUntilFinished) {

        Date date = new Date(millisUntilFinished);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateFormatted = formatter.format(date);

        return dateFormatted;
    }

}
