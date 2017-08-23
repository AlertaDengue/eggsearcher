package dinidiniz.eggsearcher.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Handler;

import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.SQL.DBHelper;
import dinidiniz.eggsearcher.activity.TelaResultado;

/**
 * Created by leon on 08/04/17.
 */
public class Coordinates implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = Coordinates.class.getName();

    // The minimum distance to change updates in metters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10
    // metters

    // The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    public double lat;
    public double lng;
    private Context context;
    GoogleApiClient mGoogleApiClient;

    private String code;
    private int eggs;
    private String description;
    private long date;
    private int sampleNumber;
    private int areatotal;
    private int height;
    private int resolutionHeight;
    private int resolutionWidth;
    private int[] areas;
    private String userId;
    private String userEmail;

    public Coordinates(Context context, String code, int eggs, String description, long date,
                       int samplenumber, int areatotal, int height, int resolutionHeight,
                       int resolutionWidth, int[] areas, String userId, String userEmail) {
        this.context = context;
        this.code = code;
        this.eggs = eggs;
        this.description = description;
        this.date = date;
        this.sampleNumber = samplenumber;
        this.areatotal = areatotal;
        this.height = height;
        this.resolutionHeight = resolutionHeight;
        this.resolutionWidth = resolutionWidth;
        this.areas = areas;
        this.userId = userId;
        this.userEmail = userEmail;

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        /*
        // Create the LocationRequest object
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        //Make Request to make sure Google api have last location
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        */

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            lat = mLastLocation.getLatitude();
            lng = mLastLocation.getLongitude();
        } else {
            lat = 0;
            lng = 0;
            Toast.makeText(context, context.getResources().getText(R.string.cant_access_location), Toast.LENGTH_LONG).show();
        }

        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);

        mGoogleApiClient.disconnect();

        Log.i(TAG, "lat: " + lat + " ;lng: " + lng);

        DBHelper db = new DBHelper(context);
        db.insertSample(code,eggs,description, lng, lat, date, areatotal,
                resolutionHeight, resolutionWidth, height, areas, userId, userEmail);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("numberOfEggsSamples", new HashSet<String>(1));
        editor.putInt("sampleNumber", sampleNumber + 1);
        editor.apply();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    /***
     * Return the full address given the lat and lng
     * @param context
     * @param lat latitute
     * @param lng longitude
     * @return string of the full address
     */
    public static String getAdressFromLatLng(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context);
        String address;

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses != null && addresses.size() > 0) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("Endereço:\n");
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                address = strReturnedAddress.toString();
            } else {
                address = "latitude: " + lat + " ;longitude: " + lng;
            }
        } catch (IOException e) {
            e.printStackTrace();
            address = "latitude: " + lat + " ;longitude: " + lng;
        }

        return address;
    }

}
