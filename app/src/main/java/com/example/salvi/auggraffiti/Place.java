/* This file contains the Place class. This will be used for placing the tag at current location.
 *  It allows user to place tag on current location and register this tag on the web server.
 *  This is the activity to place the tag at users current location. On the map in Activity2,
 *  when user touches the current location marker (i.e. marker with Red color) this activity is triggered.
 *  In this activity live camera view is shown on the screen. User can draw the tag he want to place
 *  on the screen by touching and dragging the finger on the touchscreen. Once user is done drawing the tag
 *  he should click the “placetag” button provided in the upper right corner of the screen.
 *  Clicking on this button sends the POST request to the server to store this created tag
 *  along with its location and orientation. On success a toast will be displayed on the screen
 *  saying that place tag has been successful and user can now click back button.
 *  On any error toast will be displayed asking user to try again placing the tag.
 *  We can go back to Activity2 from this activity by clicking the back button.
 */
package com.example.salvi.auggraffiti;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
//project

public class Place extends AppCompatActivity {

    locationService objLocService = new locationService();
    Boolean isBound= false;

    //variables used for drawing the tag
    public static Bitmap bitmap;
    public Canvas canvas;
    private Paint mPaint;
    private PlaceDrawing dv;
    private OutputStream outputStream = null;

    //variables used for camera View
    private Camera myCamera = null;
    private CameraView myCameraView = null;
    private static final String TAG = "place";
    //public static final int MY_CAMERA_REQUEST_CODE = 105;
    public static final String actStopTag = "tagPlace";

    // To pass intent to placetag activity
    static String Email_used = "";
    static String loc_lat = "";
    static String loc_long = "";
    static String loc_alt = "";
    static String azimuth = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_layout);

        //Bind the service to the activity to get location information
        Intent i = new Intent(this, locationService.class);
        bindService(i,locConnection, Context.BIND_AUTO_CREATE );

        //Set paint variable
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        //object of PlaceDrawing class to paint on the camera view
        dv = new PlaceDrawing(this, mPaint);


         /* defined request queue used to perform neartag service
          * defined bindService function to bind the current activity with the service
          * */
        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        final Context context = this;
        Log.d(TAG, "oncreate of place");

        Intent intent = getIntent();
        Email_used = intent.getStringExtra("Email");


        Log.d(TAG, "azi "+objLocService.getAzimuth());

        //button to place the tag at current location
        Button imgClose = (Button) findViewById(R.id.imgClose);

        //OnclickListerner to start process of placing the tag
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get location
                Log.d(TAG, "on click listerner");
                // get the bitmap of the tag drawn by user and save it in variable "bitmap"
                bitmap = dv.getmyBitmap();
                ByteArrayOutputStream stream =  new ByteArrayOutputStream();
                //convert the captured bitmap into jpeg format
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                //Create a new file named "placetagImage.jpg" in pictures directory on external storage of the device.
                //tag image will be saved in this file
                File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "placetagImage.jpg");
                //path: /storage/emulated/0/Pictures/placetagImage.jpg
                try{
                    //base64 Encoding the output stream of the compressed bitmap into a String
                    final String base64EncodedImage = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
                    //Log.i("urlbase64EncodedImage",base64EncodedImage);
                    outputStream = new BufferedOutputStream(new FileOutputStream(f));
                    //Write the output stream of the compressed bitmap into the file ceated before
                    outputStream.write(stream.toByteArray());

                    Log.d(TAG, "path: "+f.getAbsolutePath());
                    if(outputStream != null){
                        outputStream.close();
                    }

                    /* Setting the URL used to create placetag post request
                    * Logging the value of URL and testing the function
                    * */
                    final String placeTagsURL = "http://roblkw.com/msa/placetag.php";
                    Log.d(TAG,"URL is "+ placeTagsURL);

            /* Creating the post tags request to place the tag user created
            * Input: user's email, base64 encoded jpg file (less than 100 KB), current longitude, current latitude,
            *        azimuth of placement, altitude of placement
            * Output: output obtained from placetag.php is an integer : ‐1 if error, 0 if success
            * */
                    StringRequest tagsRequest = new StringRequest(Request.Method.POST, placeTagsURL,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d(TAG, "placetag Response is 1:" + response);
                                    //calling toastmaker function to inform user about status of the request made by him
                                    toastmaker(response);
                                } //----- Finish OnResponse
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "Error occurred in tagsRequest!!!");
                            toastmaker("1");
                            return;
                        }// OnErrorResponse function finished
                    } // ErrorListener function finished
                    ){
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params_map = new HashMap<String, String>();
                            params_map.put("email", Email_used);

                            loc_long = objLocService.getLongitude();
                            params_map.put("loc_long", loc_long);
                            loc_lat = objLocService.getLatitude();
                            params_map.put("loc_lat", loc_lat);

                            azimuth = objLocService.getAzimuth();
                            params_map.put("orient_azimuth", azimuth);
                            loc_alt = objLocService.getAlti();
                            params_map.put("orient_altitude", loc_alt);
                            params_map.put("tag_img", base64EncodedImage);

                            Log.d(TAG, "long "+loc_long+" lat "+loc_lat+" alt "+loc_alt+" azimuth "+azimuth);
                            return params_map;
                        } // getParams function finished
                    };

            /* Adding the tag request to the tagQueue
            * Setting the flag on the placeTag request
            * */
                    MySingleton.getInstance(context).addToRequestQueue(tagsRequest);
                    tagsRequest.setTag(actStopTag);
                }catch(Exception e){

                }
                //System.exit(0);
            }
        });

    }

    /**
     * This function creates the toast to give user feedback regarding the success of the placetag request made by him
     * @param response : this is the String which is obtained from server in response to the post request made
     */
    public void toastmaker(String response){
        if(response.equals("0")) {
            Toast.makeText(Place.this, "Success!!Tag has been placed! Now press back button", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(Place.this, "Placetag failed! please try again...", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onstart");
        // Create an instance of Camera
        myCamera = getCameraInstance();
        if (myCamera != null) {
            // Create our Preview view and set it as the content of our activity.
            myCameraView = new CameraView(this, myCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_view);
            if(preview.getChildCount()>0){
                preview.removeAllViews();
            }
            preview.addView(myCameraView);
            preview.addView(dv);
        }

    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Camera c = null;
        Toast.makeText(this, "onRequestPermissionsResult, requestCode: " + requestCode, Toast.LENGTH_SHORT).show();
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                c = Camera.open();
                myCamera = c;
                myCameraView = new CameraView(this, myCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_view);
                preview.addView(myCameraView);
            }
        }
    }*/

    /**
     * This method craetes an instance of camera by opening camera on device
     * @return camera object if success or return null
     */
    public Camera getCameraInstance() {
        Camera c = null;
        Log.d(TAG, "getcamerainstance");
        try {
                c = Camera.open(); // attempt to get a Camera instance

        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "error : "+e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }

    private ServiceConnection locConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            locationService.LocationBinder binder = (locationService.LocationBinder) service;
            objLocService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        //objLocService.onDestroy();
    }
}
