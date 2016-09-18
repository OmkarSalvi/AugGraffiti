package com.example.salvi.auggraffiti;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//-----importing classes to perform service
import android.os.IBinder;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ServiceConnection;

//----importing package.superClass.binderClass
import com.example.salvi.auggraffiti.nearTagService.MyLocalBinder;


/**
 * Created by salvi on 8/28/2016.
 */
public class Activity2 extends FragmentActivity implements LocationListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener
    {
        nearTagService objNearTagService;
        boolean isBound = false;
        static String Email_used = "";

        private GoogleApiClient mGoogleApiClient;
        private TextView mStatusTextView;
        //private static final String TAG = Activity2.class.getName();
        private static final String TAG = "debug";
        private static final int RC_SIGN_IN = 9001;
        //private Button SignOut_Button;
        GoogleMap googleMap;
        LatLng myPosition;
        Marker marker, nearTagMarker;
        Location currentLocation;
        public static final String actStopTag = "tagActivity2";

        public static LocationManager locationManager;
        public static LocationListener mylocListener;
        //Defining list for tags and nearTagMarker for response
        List<Marker> tagList = new ArrayList<Marker>();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity2);
            //-------------

            // Globally defined queue
            // Create Get a RequestQueue
            RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
                    getRequestQueue();

            //Intent objIntent = new Intent(this, nearTagService.class);
            //bindService(objIntent, objServiceConnection, Context.BIND_AUTO_CREATE);
            //-------------

            if (!isGooglePlayServicesAvailable()) {
                finish();
            }

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            googleMap = mapFragment.getMap();
            Intent intent = getIntent();
            String value = intent.getStringExtra("key"); //if it's a string you stored.
            Email_used = value; // static vaiable to store email used to login into the app
            Log.d(TAG, "message received : " + value);

            Button SignOut_Button = (Button) findViewById(R.id.sign_out_button);
            //findViewById(R.id.sign_out_button).setOnClickListener(this);
            mStatusTextView = (TextView) findViewById(R.id.Points_textView);
            // Configure sign-in to request the user's ID, email address, and basic profile. ID and
            // basic profile are included in DEFAULT_SIGN_IN.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            SignOut_Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signOut();
                }
            });

            //googleMap.setMyLocationEnabled(true);

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String bestProvider = locationManager.getBestProvider(criteria, true);

            // API 23 Check: we have to check if ACCESS_FINE_LOCATION and/or ACCESS_COARSE_LOCATION permission are granted
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                double latitude = location.getLatitude();

                // Getting longitude of the current location
                double longitude = location.getLongitude();
                // Creating a LatLng object for the current location
                LatLng latLng = new LatLng(latitude, longitude);
                myPosition = new LatLng(33.419351, -111.938083);
                googleMap.addMarker(new MarkerOptions().position(myPosition).title("Start"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(bestProvider, 2000, 0, this);//this);

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
            {
                @Override
                public boolean onMarkerClick(Marker arg0) {
                    //if(arg0.getTitle().equals("MyHome")) // if marker source is clicked
                    Toast.makeText(Activity2.this, "marker clicked", Toast.LENGTH_SHORT).show();// display toast
                    Intent myIntent = new Intent(Activity2.this, Place.class);
                    myIntent.putExtra("key", "you clicked the marker"); //Optional parameters
                    Activity2.this.startActivity(myIntent);
                    return true;
                }
            });// Finish OnMarker Listener
            mStatusTextView.setText(strGetScoreResponse);
    } // FIninsh OnCreate

        // Function which will be called when device location is changed
        @Override
        public void onLocationChanged(Location location) {

            if(marker != null){
                marker.remove();
            }
            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            //coor hall location
            myPosition = new LatLng(33.419351, -111.938083);
            marker = googleMap.addMarker(new MarkerOptions().position(myPosition).title("Start"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            currentLocation = new Location("start point");
            currentLocation.setLatitude(33.419351);
            currentLocation.setLongitude(-111.938083);
            Log.d(TAG,"location = "+ latLng );
            //----

            Log.d(TAG,"Email= "+ Email_used );

            getScore(Email_used);
             if(strGetScoreResponse != ""){
                Log.d(TAG,"Received GetScore Response : "+ strGetScoreResponse);
            }


            /* getting response using service--not used now
            //for next phase use
            //objNearTagService.getNearTags(strEmail,TAG,String.valueOf(longitude),String.valueOf(latitude));
            //objNearTagService.getNearTags(strEmail,"Inside service",32.00,-111.00);
            //tagsResonse= objNearTagService.getNearTagsResponse();
            */

            getNearTags(Email_used,TAG,longitude,latitude);
            /*
            if(tagsResonse !=""){
                Log.d(TAG,"After getNearTags call");
                handleNearTagsResult(tagsResonse);
            }*/
            //mStatusTextView.setText(strGetScoreResponse);
            //----
        } //----Finish OnLocationChanged

        String strGetNearTagsResponse;
        String strGetScoreResponse;
        public void getNearTags(final String strEmail,final String TAG, final Double loc_long, final Double loc_lang){
            // Instantiate the tagQueue
            Log.d(TAG,"Inside NearTags Function");
            //RequestQueue tagQueue = Volley.newRequestQueue(this);
            final String nearTagsURL = "http://roblkw.com/msa/neartags.php";
            //Log.d(TAG, "URL");


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
                        }//---finish OnErrorResponse
                    } //---finish ErrorListener
                ){
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params_map = new HashMap<String, String>();
                        params_map.put("email", strEmail);
                        //params_map.put("loc_long", String.valueOf(loc_long));
                        //params_map.put("loc_lang", String.valueOf(loc_lang));

                        // for testing we are sending location of Latti COOR Hall
                        params_map.put("loc_long", String.valueOf(-111.938083));
                        params_map.put("loc_lat", String.valueOf(33.419351));
                        return params_map;
                    } // ---finish getParams
                };
            // Add the request to the tagQueue.
            MySingleton.getInstance(this).addToRequestQueue(tagsRequest);
            //Set tag on the request
            tagsRequest.setTag(actStopTag);
            //tagQueue.add(tagsRequest);
            //------------------
        }


        /*handleNearTagsResult Method
       // This Function retrieves places the marker on the current map based on the getNearTags's response
       */
        private void handleNearTagsResult(String response){


            //Clearing Markers
            if(!tagList.isEmpty()){
                Log.d(TAG,"Inside Tag List " );
                for (Marker m: tagList){
                    m.remove();
                    Log.d(TAG,m.getTitle());
                }
               // tagList.removeAll(tagList);
                Log.d(TAG,"Empty Tag List" ); 
            }

            Log.d(TAG, "HandleNearTag  :"+response);
            if(response != "") {
                String objResponse[] = response.split(",");
                int i=0;
                Log.d(TAG, "hello");

                for (i = 0; i < objResponse.length; i = i + 3) {
                    Log.d(TAG, "Tag id: " + objResponse[i] + " With Longitude: " + objResponse[i + 1] + " With Latitude: " + objResponse[i + 2]);
                    LatLng latLng = new LatLng(Double.parseDouble(objResponse[i + 2]), Double.parseDouble(objResponse[i + 1]));
                    nearTagMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(objResponse[i]));
                    tagList.add(nearTagMarker);
                    Location locationA = new Location("point A");
                    locationA.setLatitude(Double.parseDouble(objResponse[i + 2]));
                    locationA.setLongitude(Double.parseDouble(objResponse[i + 1]));

                    float distance = locationA.distanceTo(currentLocation);
                    Log.d(TAG,"distance : "+distance);
                }
            }else{
                Log.d(TAG, "null response");
            }
        }// finish handleNearTagsResult method


        /*GetScore.php
        // This Function retrieves the current score of the user based on user's email ID
        */
        public void getScore(final String strEmail){
            //Log.d(TAG,"Inside getScore Function");
            final String nearTagsURL = "http://roblkw.com/msa/getscore.php";
            // Request a string response from the provided URL.
            StringRequest getScoreRequest = new StringRequest(Request.Method.POST, nearTagsURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "GetScore response is : " + response);
                            strGetScoreResponse = String.valueOf(response);
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
        }



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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            //findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "in updateUI");
           // mStatusTextView.setText(R.string.signed_out);

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



        /*For Future USe
        *  Binding with the service request
        *

        public ServiceConnection objServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                //create binder to get access to service class
                MyLocalBinder binder = (MyLocalBinder) service;
                // access class to get access
                objNearTagService = binder.getService();
                Log.d(TAG,"isBOund:"+String.valueOf(isBound));
                isBound = true;
                Log.d(TAG,"isBOund:"+String.valueOf(isBound));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
            }
        }; // end Service Connection

        */


        @Override
        protected void onStop(){
            super.onStop();
            if( MySingleton.getInstance(this)!= null){
                Log.d(TAG,"Inside onstop and inside queue activity2");
                MySingleton.getInstance(this).getRequestQueue().cancelAll(actStopTag);
            }
        }
    }
