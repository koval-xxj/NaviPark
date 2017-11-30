package com.valpiok.NaviPark.gn_classes;

/**
 * Created by SERHIO on 29.09.2017.
 */

import android.app.Application;

import com.valpiok.NaviPark.R;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(mailTo = "koval.xxj@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)

public class ErrorRepSender extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }

}
