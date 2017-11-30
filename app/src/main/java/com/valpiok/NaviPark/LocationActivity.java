/*
  Copyright 2017 Valpio-K. All Rights Reserved.
 */

package com.valpiok.NaviPark;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.valpiok.NaviPark.activities.StartTarriffActivity;
import com.valpiok.NaviPark.fragments.DialogWindow;
import com.valpiok.NaviPark.fragments.Timer;
import com.valpiok.NaviPark.gn_classes.ParkingInfoParsing;
import com.valpiok.NaviPark.interfaces.ActivityTasksCallback;
import com.valpiok.NaviPark.tasks.GPSLocationTask;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * step1 :buildGoogleApiClient
 * step2: createLocationRequest
 * step3: buildLocationSettingsRequest
 * step 4:on click of button detect location check the location is on/off using checkLocationSettings method.
 * step 5:on click of the options in location dialog
 * step 6 :depending on the action taken on dialog (startResolution for result and check the action in onActivityResult
 */
public class LocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>,
        ActivityTasksCallback,
        View.OnClickListener {

    //UI
    private Button btn_location;
    private TextView txt_location;

    boolean mRepeat;

    //New views for city and pincode
    //protected TextView tv_pincode, tv_city;

    protected static final String TAG = "LocationActivity";

    //Any random number you can take
    public static final int REQUEST_PERMISSION_LOCATION = 10;

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";
    protected final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;


    // Labels.
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    int RQS_GooglePlayServices = 0;

    private GPSLocationTask GPSTask = null;

    private long parking_max_time;
    private ParkingInfoParsing parkResult;
    private LinearLayout base_layout;
    private static final int TARRIFF_ACTIVITY = 9002;

    @Override
    protected void onStart() {
        super.onStart();

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
        } else {
            googleAPI.getErrorDialog(this, resultCode, RQS_GooglePlayServices);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            //  Toast.makeText(FusedLocationWithSettingsDialog.this, "location was already on so detecting location now", Toast.LENGTH_SHORT).show();
            startLocationUpdates();
        }
        this.base_layout = (LinearLayout) findViewById(R.id.base);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);

        btn_location = (Button) findViewById(R.id.btn_detect_fused_location);

        //total six textviews
        txt_location = (TextView) findViewById(R.id.txt_location);

        //tv_city = (TextView) findViewById(R.id.tv_city);
        //tv_pincode = (TextView) findViewById(R.id.tv_pincode);

        // Set labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        ////
        mRepeat = true;

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.

        //step 1
        buildGoogleApiClient();

        //step 2
        createLocationRequest();

        //step 3
        buildLocationSettingsRequest();
        btn_location.setOnClickListener(this);

//        btn_location.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mGoogleApiClient.connect();
//                checkLocationSettings();
//            }
//        });

    }

    @Override
    public void onClick(View v) {
        boolean return_flag = true;

        switch (v.getId()) {
            case R.id.btn_detect_fused_location:
                mGoogleApiClient.connect();
                checkLocationSettings();
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

    //step 1
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    //step 2
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //step 3
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    //step 4
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        } else {
            goAndDetectLocation();
        }

    }

    public void goAndDetectLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = true;
                //     setButtonsEnabledState();
            }
        });
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
                //   setButtonsEnabledState();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goAndDetectLocation();
                }
                break;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            // LocationActivity.this.stopLocationUpdates();
            // updateLocationUI(mRepeat);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateLocationUI();
        Toast.makeText(this, getResources().getString(R.string.location_updated_message),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    /**
     * Invoked when settings dialog is opened and action taken
     *
     * @param locationSettingsResult This below OnResult will be used by settings dialog actions.
     */

    //step 5
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {

        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");

                Toast.makeText(LocationActivity.this, "Location is already on.", Toast.LENGTH_SHORT).show();
                startLocationUpdates();
                // stopLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    Toast.makeText(LocationActivity.this, "Location dialog will be open", Toast.LENGTH_SHORT).show();
                    //

                    //move to step 6 in onActivityResult to check what action user has taken on settings dialog
                    status.startResolutionForResult(LocationActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }


    /**
     * This OnActivityResult will listen when
     * case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: is called on the above OnResult
     */
    //step 6:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        // if (TextUtils.isEmpty(LocationActivity.this.api_token_name) || TextUtils.isEmpty(LocationActivity.this.api_token_value)) {
        // }
        if (mCurrentLocation != null) {
            //TextView data_text = (TextView) findViewById(R.id.txt_data);
            //data_text.setText(Double.toString(mCurrentLocation.getLongitude()) + " $ " + Double.toString(mCurrentLocation.getLatitude()));
            this.txt_location.setText("Process");

            //this.txt_location.setText(Double.toString(mCurrentLocation.getLongitude()) + " $ " + Double.toString(mCurrentLocation.getLatitude()));
            this.GPSTask = new GPSLocationTask(this, Double.toString(mCurrentLocation.getLatitude()), Double.toString(mCurrentLocation.getLongitude()));
            //this.GPSTask = new GPSLocationTask(this, "49.88959", "36.4545873");
            this.GPSTask.execute();


            LocationActivity.this.stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onTaskFinish(boolean success, String json_message, Integer task) {

        Log.d("Tarriff result", json_message);

        this.parkResult = new ParkingInfoParsing(json_message, success);
        this.txt_location.setText("Finished");

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

    /**
     * This updateCityAndPincode method uses Geocoder api to map the latitude and longitude into city location or pincode.
     * We can retrieve many details using this Geocoder class.
     * <p>
     * And yes the Geocoder will not work unless you have data connection or wifi connected to internet.
     */

    /*
    private void updateCityAndPincode(double latitude, double longitude) {
        try {
            Geocoder gcd = new Geocoder(LocationActivity.this, Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                tv_city.setText("City=" + addresses.get(0).getLocality());
                tv_pincode.setText("Pincode=" + addresses.get(0).getPostalCode());
                //  System.out.println(addresses.get(0).getLocality());
            }

        } catch (Exception e) {
            Log.e(TAG, "exception:" + e.toString());
        }

    }
    */

}
