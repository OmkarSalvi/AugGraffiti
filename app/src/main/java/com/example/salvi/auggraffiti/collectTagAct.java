/**
 * This activity is created in order to implement the capture feature of application.
 * This activity presents user with the tag which user wants to capture and the camera view of the current surroundings.
 * The tag is embedded on top of camera view to give the feel of augmented reality.
 * Tag is placed by owner at specific angle and at specific altitude.
 * In order to collect the tag, user will have to rotate his device to match azimuth(rotation around z-axis and altitude of tag).
 * These parameters are displayed on top of screen using the location service and the previous activities intent.
 * On successful matching of these values, Current picture of the camera is taken and the presented tag is placed
 * on the camera picture. Matched(text) is displaced on both corners and then collect tag request is sent from the user
 * which uses user’s email, tag id and collected image with tag which is base-64 compressed.
 * On successful creation of collect tag request and it’s response, user is presented with the toast which gives information of the response.
 * User’s score is increased on success, otherwise user will be notified with the error/issue.
 */
package com.example.salvi.auggraffiti;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.FrameLayout;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.example.salvi.auggraffiti.locationService.LocationBinder;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static com.example.salvi.auggraffiti.Activity2.locationManager;
import static com.example.salvi.auggraffiti.R.layout.activity_collect_tag;

public class collectTagAct extends AppCompatActivity implements LocationListener, SensorEventListener{


    locationService objLocService = new locationService();
    Boolean isBound= false;

    public int value;
    public int buffer = 10;
    private static final String TAG = "debugCollectTag";
    public static final String actStopTag = "actCollectTag";
    final String collectTagURL = "http://roblkw.com/msa/collecttag.php";
    private String strTagId;
    private String strUserEmail;
    private String strtag_img_URL;
    private double orient_azimuth;
    private double orient_altitude;
    private String strCollect_img;
    private String strCollectTagResponse;

    /**
     * Camera and UI variables
     */
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imgPreview;
    private Camera objCamera;
    private CameraPreview objCamPreview;
    private Bitmap resizedBitmap = null;
    private Bitmap bit = null;
    private Bitmap finalMergedBitmap = null;

    /**
     * Sensor variables
     */
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
    public double global_azimuth;
    public double global_curr_long;
    public double global_curr_lat;
    public double global_curr_alt;
    public LatLng CurrentPosition;
    public boolean setFlag = true;
    public Button btnAzimuth;
    public Button btnLatitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(activity_collect_tag);
        Log.i("logCollect", "Entering into Collect tag activity");

        /**
         * Binding the service to current activity
         */
        //Intent i = new Intent(this, locationService.class);
        //bindService(i,locConnection, Context.BIND_AUTO_CREATE );

        /**
         * Accessing and registering sensor
         */
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyrometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this,gyrometer , SensorManager.SENSOR_DELAY_NORMAL );
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        /**
         * GetSystemService with LOCATION_SERVICE is used to retrieve a LocationManager instance for controlling location updates
         * Using LocationManager- bestprovider is obtained, last known loacation is obtained to mask the delay corresponding to
         * obtaining first location information.
         * GetBestProvider returns the name of the provider that best meets the given criteria.
         *
         */
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String bestProvider = locationManager.getBestProvider(criteria, true);

        /**
         * API 23 Check
         * Check if ACCESS_FINE_LOCATION and/or ACCESS_COARSE_LOCATION permission are granted
         */
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {

            /**
             * Getting longitude of the current location
             * Creating a LatLng object for the current location
             * Placing the Marker on the map to display the current location of user.
             * set the method onlocationchanged with argument as current location.
             * Thus when current location changes onLocationChanged method will be called.
             */
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            CurrentPosition = new LatLng(latitude, longitude);

            onLocationChanged(location);
        }
        /**
         * Requesting location updates from locationManager every 2 seconds using best location provider.
         */
        locationManager.requestLocationUpdates(bestProvider, 2000, 0, this);
        /**
         * Creating a request queue to perform nearTag service
         */
        RequestQueue collectTagQueue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        Intent intent = getIntent();

        strTagId = intent.getStringExtra("tag_id");
        strUserEmail = intent.getStringExtra("email");
        strtag_img_URL = intent.getStringExtra("tag_img_URL");
        orient_azimuth = Double.parseDouble(intent.getStringExtra("orient_azimuth"));
        orient_altitude = Double.parseDouble(intent.getStringExtra("orient_altitude"));
        Log.i(TAG, "tag id is "+strTagId + " for user's email "+strUserEmail);
        Log.i(TAG, "tag azimuth : "+String.valueOf(orient_azimuth) + " altitude : "+String.valueOf(orient_altitude));

        btnAzimuth = (Button) findViewById(R.id.btnAzimuth);
        btnLatitude = (Button) findViewById(R.id.btnLatitude);
        btnAzimuth.setText(String.valueOf((int)orient_azimuth)+"<=>"+String.valueOf((int)global_azimuth));
        btnLatitude.setText(String.valueOf((int)orient_altitude)+"<=>"+String.valueOf((int)global_curr_alt));

        fillBitmap(strtag_img_URL);

        final FrameLayout rootFrame = (FrameLayout)findViewById(R.id.rootFrame);

        // Create an instance of Camera
        objCamera = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
        objCamPreview = new CameraPreview(this, objCamera);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(1000, 1000);//1000, 1000
        params.leftMargin = 50;
        params.topMargin  = 300;
        ImageLoader objImgLoader;

        /**
         * Get the NetworkImageView that will display the image.
         * Get the ImageLoader through your singleton class.
         * Set the URL of the image that should be loaded into this view, and
         * specify the ImageLoader that will be used to make the request.
         */
        final NetworkImageView vwNetImg = new NetworkImageView(this);
        objImgLoader = MySingleton.getInstance(this).getImageLoader();
        vwNetImg.setImageUrl(strtag_img_URL, objImgLoader);

        /**
         * Rotating obtained Tag
         */
        /**
        Matrix matrix = new Matrix();
        vwNetImg.setScaleType(ImageView.ScaleType.MATRIX);
        matrix.postRotate(-45,0,0);
        vwNetImg.setImageMatrix(matrix);
        */
        rootFrame.addView(objCamPreview);
        rootFrame.addView(vwNetImg,params);

    }

    /**
     * Helper to create base64 encoded string from Bitmap
     * Encoded string is used in collect tag request paramters
     */
    public void encodeBitmap(){

        strCollect_img = encodeToBase64(finalMergedBitmap, Bitmap.CompressFormat.JPEG ,5);
        Log.i(TAG,"Size of encoded image is "+ String.valueOf(strCollect_img.getBytes()));
        funcCollectTag();
    }

    /**
     * Helper to make collect tag request & passes reponse to handleCollectTagResult method
     * String URL, User email, Tag Id is obtained from intent variables from earlier activity
     *
     */
    public void funcCollectTag() {

        Log.i(TAG, "Collect tag URL is " + collectTagURL);
        StringRequest collecttagsRequest = new StringRequest(Request.Method.POST, collectTagURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "CollectTag Response is :" + response);
                        //Setting response to global variable
                        strCollectTagResponse = response;
                        handleCollectTagResult(response);
                    } //----- Finish OnResponse
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error occurred in collectTagsRequest!!!");
                Toast.makeText(collectTagAct.this, "Collect tag failed..Please try again!!", Toast.LENGTH_LONG).show();
                return;
            }// OnErrorResponse function finished
        } // ErrorListener function finished
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params_map = new HashMap<String, String>();
                params_map.put("email", strUserEmail);
                params_map.put("tag_id", String.valueOf(strTagId));
                params_map.put("collect_img", strCollect_img);
                return params_map;

            } // getParams function finished
        };

        /**
         * Adding the tag request to the collectTagQueue & setting the flag on the collectTagQueue request
         */
        MySingleton.getInstance(this).addToRequestQueue(collecttagsRequest);
        collecttagsRequest.setTag(actStopTag);
    }

    /**
     * Helper to display the toast after getting response of collect tag request
     * @param response : String - stores the collect tag response
     */
    public void handleCollectTagResult(String response){

        if (response.equals("0")){
            Toast.makeText(collectTagAct.this, "Tag collected..!!Press back", Toast.LENGTH_LONG).show();
            Log.d("logCollect", "Success");
        }else{
            Toast.makeText(collectTagAct.this, "Collect tag failed..Please try again!!", Toast.LENGTH_LONG).show();
            Log.d("logCollect", "Failure");
        }
    }

    /**
     * Helper to create the bitmap using image URL
     * Retrieves an image specified by the URL, displays it in the UI
     * @param url : String - global image url obtained from find tag request
     */
    public void fillBitmap(String url){

        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        bit = bitmap;
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d("logCollect","Error occured in fillBitmap");
                        //viewforBit.setImageResource(R.drawable.image_load_error);
                    }
                });
        // Access the RequestQueue through your request queue class.
        MySingleton.getInstance(this).addToRequestQueue(request);

    }

    /**
     * Helper to create the base 64 encoded string using bitmap
     *
     * @param mergeBit : Merged bitmap using canvas which is strtured using canvas, camera and tag view
     * @param jpeg : Bitmap compress format
     * @param quality : quality of the image ranging from 0-100
     * @return  base64 encoded string constructed from byte array
     */
    public static String encodeToBase64(Bitmap mergeBit, Bitmap.CompressFormat jpeg, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        mergeBit.compress(jpeg, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.NO_WRAP);
    }

    /**
     * Camera callback function to get the byte array
     * Creates bitmap using the byte array & Merges Tag bitmap and Camera bitmap using canvas
     * Updates and passes combined bitmap to encodeBitmap
     */
    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Log.d(TAG, "Capturing Tag");
            BitmapFactory.Options options = new BitmapFactory.Options();
            Matrix mat = new Matrix();
            resizedBitmap = BitmapFactory.decodeByteArray(data, 0,
                    data.length, options);
            Bitmap mergeBitmap = Bitmap.createBitmap(resizedBitmap.getWidth(), resizedBitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mergeBitmap);
            canvas.drawBitmap(resizedBitmap, mat, null);
            canvas.drawBitmap(bit, 0, 0, null);
            finalMergedBitmap = mergeBitmap;
            Log.d("logCollect", "Bitmaps merged");
            encodeBitmap();

        }
    };

    /**
     * Helper to create the camera instance
     * @return Camera instance
     */
    public static Camera getCameraInstance(){
        Camera cam  = null;
        try {
            cam = Camera.open();
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return cam;
    }

    /**
     * Helper to bind the service to the current activity
     * access the service variables using getService
     */
    private ServiceConnection locConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationBinder binder = (LocationBinder) service;
            objLocService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    /**
     * Helper to update user's device parameters used in the collect tag request
     * @param event : event to get the sensor type
     */
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
            global_azimuth = azimuth;
            /**
             * Conditional check to continuously update the device paramters in user interface
             * Sets the value of text to matched based on flag
             */
            if(setFlag){
                btnAzimuth.setText(String.valueOf((int)orient_azimuth)+"<AZIMIUTH>"+String.valueOf((int)global_azimuth));
                btnLatitude.setText(String.valueOf((int)orient_altitude)+"<ALTITUDE>"+String.valueOf((int)global_curr_alt));
            }else{
                btnAzimuth.setText("MATCHED..!!");
                btnLatitude.setText("MATCHED..!!");
            }
            Log.d("logCollectParams", "current azimuth: "+global_azimuth+"| current altitude: "+global_curr_alt);
            boolean cond1 = ((orient_azimuth-buffer) <= global_azimuth) && (global_azimuth <= (orient_azimuth+buffer));
            boolean cond2 = ((orient_altitude-buffer) <= global_curr_alt) && (global_curr_alt <= (orient_altitude+buffer));
            /**
             * Conditional check to compare the device parameters and obtained tag parameters
             * Calls camera instance callback function when parameters are matched
             */
            if(cond1 && cond2 && setFlag){
                setFlag = false;
                Log.d("logCollect", "Current azimuth: "+global_azimuth+"| current altitude: "+global_curr_alt);
                objCamera.takePicture(null, null,mPicture);
                Toast.makeText(collectTagAct.this, "Capturing Tag In Progress..!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Updating location parameters on location change using location listener
     * @param location : location object
     */
    @Override
    public void onLocationChanged(Location location) {

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        final double altitude = location.getAltitude();
        CurrentPosition = new LatLng(latitude, longitude);
        global_curr_long =longitude;
        global_curr_lat = latitude;
        global_curr_alt = altitude;
        //Log.i(TAG, "Current altitude: "+String.valueOf(altitude));
    }

    /**
     * OnStop gets called when the activity is stopped
     * Clears all request from the queue for this activity
     */
    @Override
    protected void onStop(){
        super.onStop();
        if( MySingleton.getInstance(this)!= null){
            Log.d(TAG,"onStop collect tag activity");
            MySingleton.getInstance(this).getRequestQueue().cancelAll(actStopTag);
        }
        mSensorManager.unregisterListener(this);
    }

    /**
     * Unregisters sensor listeners on pause for this activity
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPaused collect tag activity");
        mSensorManager.unregisterListener(this);
    }

    /**
     * Registers sensor listeners on resume for this activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume collect tag activity");
        mSensorManager.registerListener(this,gyrometer , SensorManager.SENSOR_DELAY_NORMAL );
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}//------------end Class
