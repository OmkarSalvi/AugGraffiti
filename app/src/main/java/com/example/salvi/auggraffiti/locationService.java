/**
 * The use of location service enables the background communication for the current activity with
 * the location and device parameters. Service is used for continuous updation of required variables which
 * interacts with the user to implement necessary features. The location service basically runs a part of current activity.
 * The location service is being used in several activities for the smooth working of an application.
 * This service is binded with given activity on its creation and it’s disconnected on its interruption.
 * This leads to efficient use of memory and bind service allows the continuous interaction with the user interface.
 * This service is used to get following parameters.
 *      Longitude, Latitude, Altitude : 	User’s current location parameters
 *      Pitch, Roll and Azimuth:	        Rotation around X, Y and Z coordinates respectively
 */
package com.example.salvi.auggraffiti;


import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Binder;
import android.util.Log;

import java.util.Date;
import java.util.Locale;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;;

public class locationService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SensorEventListener {

    private double currentLatitude;
    private double currentLongitude;
    private double currentAltitude;

    //private final String TAG = locationService.class.getSimpleName();
    private final String TAG = "debugService";
    private Intent intent;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    //String orientation;
    public SensorManager mSensorManager;
    public Sensor accelerometer;
    public Sensor magnetometer;
    public Sensor gyrometer;
    public double azimuth,pitch,roll;
    float[] mGravity;
    float[] mGeomagnetic;
    public float orientation[] = new float[3];

    boolean ready;
    public float[] accelValues = new float[3];
    public float[] compassValues =  new float[3];
    public float prefValues[] = new float[3];
    public float inR[] = new float[9];
    public float inclineMatrix[] = new float[9];

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)        // 1 seconds, in milliseconds
                .setFastestInterval(100); // 0.1 second, in milliseconds

        mGoogleApiClient.connect();

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyrometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this,gyrometer , SensorManager.SENSOR_DELAY_NORMAL );
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d(TAG,"onSensorChanged..");
        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:
                //Log.d(TAG,"on Accelerometer..");
                for(int i=0; i<3; i++){
                    accelValues[i] =  event.values[i];
                }
                if(compassValues[0] != 0) {
                    ready = true;
                }
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                //Log.d(TAG,"onMagnetic Field..");
                for(int i=0; i<3; i++){
                    compassValues[i] = event.values[i];
                }
                if(accelValues[2] != 0){
                    ready = true;
                }
                break;
        }

        if(!ready){
            return;
        }

        boolean cek = SensorManager.getRotationMatrix(inR, inclineMatrix, accelValues, compassValues);

        if(cek){
            SensorManager.getOrientation(inR, prefValues);

            azimuth = Math.round(Math.toDegrees(prefValues[0]));
            pitch = Math.round(Math.toDegrees(prefValues[1]));
            roll = Math.round(Math.toDegrees(prefValues[2]));
            //Log.d(TAG,"onSensor Changed Suceess " + Double.toString(azimuth));
            //Log.d(TAG,"onSensor Changed Suceess " + Double.toString(pitch));
            //Log.d(TAG,"onSensor Changed Suceess " + Double.toString(roll));

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Location services connected.");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, location.toString());
        currentLatitude = location.getLatitude();
        Log.d("place", "in onconnected : "+Double.toString(currentLatitude)+ " | "+ currentLatitude);
        currentLongitude = location.getLongitude();
        currentAltitude = location.getAltitude();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "GoogleApiClient connection has failed: " + connectionResult.getErrorCode());
        stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("place", "in service onlocationchanged");
        Log.d(TAG, "onLocationChanged()");
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        currentAltitude = location.getAltitude();
        Log.d("place", "onlocation changed"+Double.toString(currentLatitude)+ " | "+ currentLatitude);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    public String getLatitude(){
        Log.d("place", Double.toString(currentLatitude)+ " | "+ currentLatitude);
        return Double.toString(currentLatitude);
    }

    public String getLongitude(){
        return Double.toString(currentLongitude);
    }

    public String getAlti(){
        return Double.toString(currentAltitude);
    }

    public String getAzimuth(){
        return Double.toString(azimuth);
    }

    public String getPitch(){
        return Double.toString(pitch);
    }

    public String getRoll(){
        return Double.toString(roll);
    }


    //---------------
    public final IBinder objLocationBinder = new locationService.LocationBinder();
    public locationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return objLocationBinder;
    }

    public String getCurrentTime(){
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("HH:mm:ss", Locale.US);
        return  df.format(new Date());
    }



    public class LocationBinder extends Binder{

        locationService getService(){
            return locationService.this;
        }
    }
    //----------









}
