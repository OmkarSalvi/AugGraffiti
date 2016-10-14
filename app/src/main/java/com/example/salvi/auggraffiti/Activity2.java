/* This file contains the Activity2 class. This is the second activity of the application.
*  It displays the Map and user's current location on the screen.
*  It informs user of his score, nearby tags from his current location.
*  It allows user to do google sign-out. Also user can view gallery.
*  User can collect tag placed by other artists which are in vicinty, shown on the map by blue color.
*  To collect these tags user needs to click on that marker.
 */
package com.example.salvi.auggraffiti;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by salvi on 8/28/2016.
 */
public class Activity2 extends FragmentActivity implements LocationListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener
{
    locationService objLocService = new locationService();
    Boolean isBound= false;

    /**
     * Defined global parameters to store response obtained from post requests
     * Defining debug tag for Activity2
     * Defining variable to get the login email ID
     */
    private static final String TAG = "debugActivity2";
    private static final int RC_SIGN_IN = 9001;
    String strGetNearTagsResponse;
    String strGetScoreResponse;
    static String Email_used = "";

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private Button Gallery;
    GoogleMap googleMap;
    LatLng CurrentPosition;

    /**
     * Defined marker for the User's location and near tags
     * Defined service object to get near tags
     * Defined list for tags and nearTagMarker for response
     * Defined tag to stop the nearTag calls on closing activity
     */
    Marker marker, nearTagMarker;

    //Arraylist of the markers which are placed on map for nearby tags
    List<Marker> tagList = new ArrayList<Marker>();
    public static final String actStopTag = "tagActivity2";

    /* Location variables
    *  Defined Location variable to store current location of the user
    *  Defined LocationManager variable to request location update from location provider
    * */
    Location currentLocation;
    public static LocationManager locationManager;

    /**
     * Polyline variables to draw lines joining current location to nearby tags
     */
    public PolylineOptions polylines = new PolylineOptions();
    public static List<Polyline> Listpolylines = new ArrayList<Polyline>();

    final String findTagURL = "http://roblkw.com/msa/findtag.php";
    private String strTagId;
    private String strUserEmail;
    private String findTagResponse;
    private String tag_img_URL;
    private Double orient_azimuth;
    private Double orient_altitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2);

        Intent i = new Intent(this, locationService.class);
        bindService(i,locConnection, Context.BIND_AUTO_CREATE );

            /* defined request queue used to perform findtag service
            * defined bindService function to bind the current activity with the service
            * */
        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

			/* Setting variables for providing Google map
            *  getMapAsync initializes the maps system and the view.
			*  getMap method returns an instance of the map class.
            * */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Get the instance of the google map
        googleMap = mapFragment.getMap();

            /*
            * Defined intent object to get handle of the activity
            * Getting the value of the key from login service of main activity
            * Storing the key value which is email of the user, in the variable used to perform various functions
            *
            * */
        Intent intent = getIntent();
        String value = intent.getStringExtra("key");
        Email_used = value;
        Log.d(TAG, "Email Id received : " + value);

			/* Sign out button to let user sign out from the application
			* */
        Button SignOut_Button = (Button) findViewById(R.id.sign_out_button);
        mStatusTextView = (TextView) findViewById(R.id.Points_textView);
        Gallery = (Button) findViewById(R.id.GalleryButton);

        // Configure sign-in to request the user's ID, email address, and basic profile. ID and
        // basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

            /* Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
            * --------------
            * */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        /**
         * On Click listener for sign out button
         * Helper to listen for click event on the sign out button
         * Calls signout method defined in this code to handle sign out activity
         */
        SignOut_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        /* on Click listener for Gallery button
        *  this method will listen for click event on the gallery button.
		*  then it will put the user email in intent and start the gallery activity to display the collected tags.
        * */
        Gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(Activity2.this, GalleryActivity.class);
                myIntent.putExtra("Email", Email_used); //Optional parameters
                Activity2.this.startActivity(myIntent);
            }
        });

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

            /* API 23 Check
            * Check if ACCESS_FINE_LOCATION and/or ACCESS_COARSE_LOCATION permission are granted
            * */
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        /**
         * Till GPS obtains the current location display the last known location on the provider
         */
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
            //Set the marker for the user's location
            marker = googleMap.addMarker(new MarkerOptions().position(CurrentPosition).title("Start"));

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(CurrentPosition));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(20));
            onLocationChanged(location);
        }
			/* Requesting location updates from locationManager every 2 seconds using best location provider.
			* */
        locationManager.requestLocationUpdates(bestProvider, 2000, 0, this);

			/* this is a listener which will listen to click event on the marker placed on the map.
			*  It will create an intent of this activity and start a new activity called Place.
			* */
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker arg0) {

                strTagId = arg0.getTitle();
                Log.d(TAG, "title of the marker clicked = " + arg0.getTitle());
                /**
                 * Check the title of the marker being clicked
                 * if it is current loction marrker which is being clicked then start place activity to place the tag
                 * Otherwise start the findtag activity to find the tag_id of tag placed by other artist at that location.
                 */
                if(arg0.getTitle().equals("Start")) {
                    Toast.makeText(Activity2.this, "Marker clicked", Toast.LENGTH_SHORT).show();// display toast
                    Log.d(TAG, "email: "+Email_used+"loc_long: "+CurrentPosition.longitude+"loc_lat: "+CurrentPosition.latitude+"loc_alt :"+currentLocation.getAltitude());
                    Intent myIntent = new Intent(Activity2.this, Place.class);
                    myIntent.putExtra("key", "You clicked marker"); //Optional parameters
                    myIntent.putExtra("Email", ""+Email_used);
                    myIntent.putExtra("loc_long", ""+CurrentPosition.longitude);
                    myIntent.putExtra("loc_lat", ""+CurrentPosition.latitude);
                    myIntent.putExtra("loc_alt", ""+currentLocation.getAltitude());

                    Activity2.this.startActivity(myIntent);
                }else{
                    Log.d(TAG,"Going into collect activity");
                    findTag();
                }
                return true;

            }
        });//  OnMarker Listener finished
        mStatusTextView.setText(strGetScoreResponse);
    } // OnCreate function finished

    /**
     * Returns tag image (URL) and orientation as comma separated pattern in response
     * Both parameters are passed to handleFindTag method
     */
    public void findTag(){
        Log.d(TAG, "FindTag URL is " + findTagURL);
        StringRequest findTagRequest = new StringRequest(Request.Method.POST, findTagURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "FindTag Response is :" + response);
                        //Setting response to global variable
                        findTagResponse = response;
                        handleFindTag(findTagResponse);
                    } //----- Finish OnResponse
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error occurred in Findtag request!!!");
                return;
            }// OnErrorResponse function finished
        } // ErrorListener function finished
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params_map = new HashMap<String, String>();
                params_map.put("tag_id", strTagId);
                //params_map.put("tag_id", String.valueOf(294));
                return params_map;
            } // getParams function finished
        };
        MySingleton.getInstance(this).addToRequestQueue(findTagRequest);
        findTagRequest.setTag(actStopTag);

    }


    public void handleFindTag(String response){

        if(!(response.isEmpty() || response == null) && response.contains(",")){
            Log.d(TAG, "findTagResponse is "+response);
            tag_img_URL = response.split(",")[0];
            orient_azimuth = Double.parseDouble(response.split(",")[1]);
            orient_altitude = Double.parseDouble(response.split(",")[2]);
            Log.d(TAG, "TagImage is "+tag_img_URL);
            Log.d(TAG, "TagAzimuth is "+orient_azimuth);
            Log.d(TAG, "TagAltitude is "+orient_altitude);
            Intent collectIntent = new Intent(Activity2.this, collectTagAct.class);
            collectIntent.putExtra("tag_id", strTagId);
            collectIntent.putExtra("email", Email_used);
            collectIntent.putExtra("tag_img_URL", tag_img_URL);
            collectIntent.putExtra("orient_azimuth", String.valueOf(orient_azimuth));
            collectIntent.putExtra("orient_altitude", String.valueOf(orient_altitude));
            Activity2.this.startActivity(collectIntent);

        }else{
            Log.d(TAG, "findTagResponse is "+response);
        }

    }

    /**
     * Updating location parameters on location change using location listener
     * Based on  current location is changed this method will update the marker location on map to represent new location.
     * @param location : location object
     */
    @Override
    public void onLocationChanged(Location location) {

        Log.d("place", "in activity2 onlocationchanged");
        Log.d("altitude", objLocService.getAlti());
        if(marker != null){
            marker.remove();
        }
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        final double altitude = location.getAltitude();
        CurrentPosition = new LatLng(latitude, longitude);

        marker = googleMap.addMarker(new MarkerOptions().position(CurrentPosition).title("Start"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(CurrentPosition));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(20));
        currentLocation = new Location("start point");
        currentLocation.setLatitude(latitude);
        currentLocation.setLongitude(longitude);
        currentLocation.setAltitude(altitude);

            /* Calling the getScore function to get the user's current score
            * Logging the respose obtained from method
            * */
        getScore(Email_used);
        if(strGetScoreResponse != ""){
            Log.d(TAG,"Received GetScore Response : "+ strGetScoreResponse);
        }

        /**
         * Calling the getNearTags function which will fetched tags near user's location
         */
        getNearTags(Email_used,TAG,longitude,latitude);
    } // OnLocationChanged fucntion finished

    /* GetNearTags function is called to create and get response from nearTag.php
    * Input: user's email Id obtained from main activity, debug tag, user's coordinates
    * Output: calls the handleNearTag function to handle response obtained
    * */
    public void getNearTags(final String strEmail,final String TAG, final Double loc_long, final Double loc_lat){

            /* Setting the URL used to create nearTag post request
            * Logging the value of URL and testing the function
            * */
        final String nearTagsURL = "http://roblkw.com/msa/neartags.php";
        Log.d(TAG,"URL is "+ nearTagsURL);

            /* Creating the post tags request to get all nearby tags
            * Input: user's email, user's coordinates i.e longitude and latitude
            * Output: output obtained from neartag.php is in the form of triplet
            * Triplet format: tag_id,loc_logitude,loc_latitude......continues for all tags
            * */
        StringRequest tagsRequest = new StringRequest(Request.Method.POST, nearTagsURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Neartag Response is :" + response);
                        //Setting response to global variable
                        strGetNearTagsResponse = response;
                        handleNearTagsResult(response);
                    } //----- Finish OnResponse
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error occurred in tagsRequest!!!");
                return;
            }// OnErrorResponse function finished
        } // ErrorListener function finished
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params_map = new HashMap<String, String>();
                params_map.put("email", strEmail);
                String cur_long = objLocService.getLongitude();
                String cur_lat = objLocService.getLatitude();
                params_map.put("loc_long", cur_long);
                params_map.put("loc_lat", cur_lat);

                return params_map;
            } // getParams function finished
        };

            /* Adding the tag request to the tagQueue
            * Setting the flag on the nearTag request
            * */
        MySingleton.getInstance(this).addToRequestQueue(tagsRequest);
        tagsRequest.setTag(actStopTag);
    }

    /* HandleNearTagsResult function is called on getting the response from the neartag.php
    * Input: response from neartag.php URL
    * Output: prints markers on the map with tags obtained from neartag request
    * Output obtained from neartag.php is in the form of triplet
    * Triplet format: tag_id,loc_logitude,loc_latitude......continues for all tags
    *
    * */
    private void handleNearTagsResult(String response){

        // Logging response obtained from the neartag request
        Log.d(TAG, "HandleNearTag  :"+response);
        //Clearing earlier markers from the tag list
        if(!tagList.isEmpty()){
            for (Marker m: tagList){
                m.remove();
            }
            Log.d(TAG,"Empty Tag List" );
        }

        if(response.contains(",")){
                /* Parsing the response into various parameters
                * Creating the object of latlng class which is further used to create marker
                * Adding the marker to taglist so that on the next call we can clear earlier markers
                *
                * */
            String objResponse[] = response.split(",");
            int i=0;
            for(Polyline line : Listpolylines)
            {
                line.remove();
                Log.d("remove","polyline removed");
            }
            Listpolylines.clear();
            polylines = new PolylineOptions();

            for (i = 0; i < objResponse.length; i = i + 3) {
                Log.d(TAG, "Tag Id: " + objResponse[i] + " with Longitude: " + objResponse[i + 1] + " with Latitude: " + objResponse[i + 2]);
                /*
                LatLng latLng = new LatLng(Double.parseDouble(objResponse[i + 2]), Double.parseDouble(objResponse[i + 1]));
                nearTagMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(objResponse[i]));
                tagList.add(nearTagMarker);
                */
                Location locationA = new Location("Point A");
                locationA.setLatitude(Double.parseDouble(objResponse[i + 2]));
                locationA.setLongitude(Double.parseDouble(objResponse[i + 1]));

                LatLng latLng = new LatLng(Double.parseDouble(objResponse[i + 2]), Double.parseDouble(objResponse[i + 1]));
                //Draw arrow line between current location and nearby tag location
                LatLng from = CurrentPosition;
                LatLng to = latLng;
                polylines.add(from, to).color(Color.CYAN).width(5);// green : 0xff00ff00

                Listpolylines.add(googleMap.addPolyline(polylines));
                //newline.remove();
                Log.d("remove", "size : " + Listpolylines.size());

                float distance = locationA.distanceTo(currentLocation);
                Log.d(TAG,"distance : "+distance);
                nearTagMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(objResponse[i]));
                tagList.add(nearTagMarker);

            }

        }else{
            Log.d(TAG, "null response");
        }
    } // handleNearTagsResult method finished


    /**
     * GetScore.php : Function retrieves the current score of the user based on user's email ID
     * GetScore function is called to get user's current score
     * Called from onCreate activity
     * @param strEmail : String Input: user's email Id (strEmail)
     * Output: user's current score
     */
    public void getScore(final String strEmail){
        // Definig the nearTag URL
        final String nearTagsURL = "http://roblkw.com/msa/getscore.php";
        // Request a string response from the provided URL.
        StringRequest getScoreRequest = new StringRequest(Request.Method.POST, nearTagsURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "GetScore response is : " + response);
                        strGetScoreResponse = String.valueOf(response);
                        //Setting the text as resposne/score obtained
                        mStatusTextView.setText("SCORE : "+response);
                    } // Finish OnResponse
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error occurred in GetScore!!!");
                return;
            }//---finish OnErrorResponse
        } //---finish ErrorListener
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params_map = new HashMap<String, String>();
                params_map.put("email", strEmail);
                return params_map;
            } // ---finish getParams
        };
        // Add the request to the tagQueue.
        MySingleton.getInstance(this).addToRequestQueue(getScoreRequest);
    } // getScore finished

    /**
     * onConnectionFailed is called when the connection is failed
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    /**
     * Helper to updates the user interface according to its argument
     * If the boolean value is false, it will close current activity and start MainActivity
     * @param signedIn: Boolean variable
     */
    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            //findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            Intent myIntent = new Intent(Activity2.this, MainActivity.class);
            finish();
            Activity2.this.startActivity(myIntent);
            //findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }


    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        Log.d(TAG, "in signout");
                        stopUsingGPS();
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }


    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }


    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(Activity2.this);
            Log.d(TAG,"stopping GPS");
        }
    }

    /* onStop gets called when the activity is stopped
    * Clears all request from the queue for this activity
    * */
    @Override
    protected void onStop(){
        super.onStop();
        if( MySingleton.getInstance(this)!= null){
            Log.d(TAG,"Inside onstop and inside queue activity2");
            MySingleton.getInstance(this).getRequestQueue().cancelAll(actStopTag);
        }
        //stopUsingGPS();
    }

    /**
     * Helper to bind the service to the current activity
     * access the service variables using getService
     */
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
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap = googleMap;

    }

} //---end main class
