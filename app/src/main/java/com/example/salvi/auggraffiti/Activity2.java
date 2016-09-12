package com.example.salvi.auggraffiti;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
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
public class Activity2 extends FragmentActivity implements
        LocationListener,
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener
        {

            nearTagService objNearTagService;
            boolean isBound = false;


    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private static final String TAG = Activity2.class.getName();
    private static final int RC_SIGN_IN = 9001;
    //private Button SignOut_Button;
    GoogleMap googleMap;
    LatLng myPosition;
    Marker marker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2);

        //-------------
        Intent objIntent = new Intent(this, nearTagService.class);
        bindService(objIntent, objServiceConnection, Context.BIND_AUTO_CREATE);
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
        Log.d(TAG, "message received : " + value);

        Button SignOut_Button = (Button) findViewById(R.id.sign_out_button);
        //findViewById(R.id.sign_out_button).setOnClickListener(this);
        mStatusTextView = (TextView) findViewById(R.id.Points_textView);
        mStatusTextView.setText("email :" + value);
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

        /*SignInButton signInButton = (SignInButton) findViewById(R.id.sign_out_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());*/
        SignOut_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        //googleMap.setMyLocationEnabled(true);

        //---------


        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


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

            //myPosition = new LatLng(latitude, longitude);

            //googleMap.addMarker(new MarkerOptions().position(myPosition).title("Start"));
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 2000, 0, this);

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {

            @Override
            public boolean onMarkerClick(Marker arg0) {
                //if(arg0.getTitle().equals("MyHome")) // if marker source is clicked
                Toast.makeText(Activity2.this, "marker clicked", Toast.LENGTH_SHORT).show();// display toast
                Intent myIntent = new Intent(Activity2.this, Place.class);
                myIntent.putExtra("key", "Omkar clicked the marker"); //Optional parameters
                //finish();
                Activity2.this.startActivity(myIntent);
                return true;
            }

        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if(marker != null){
            marker.remove();
        }
        TextView locationTv = (TextView) findViewById(R.id.status);
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        marker = googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        locationTv.setText("Latitude:" + latitude + ", Longitude:" + longitude);

        //----
        String tagsResonse;
        final String strEmail = "abhishekpatil369@gmail.com";
        objNearTagService.getNearTags(strEmail,TAG,String.valueOf(longitude),String.valueOf(latitude));
        tagsResonse= objNearTagService.getNearTagsResponse();
        tagsResonse = "ID1,"+(longitude+1)+","+(latitude+1);
        if(tagsResonse !=""){
            handleNearTagsResult(tagsResonse);
        }
        //----

    }
    private void handleNearTagsResult(String response){
        String objResponse[] = response.split(",");
        int i=0;
        for(i = 0; i< objResponse.length; i = i+2){
            Log.d(TAG, "Tag id: " + objResponse[i]+ " With Longitude: "+objResponse[i+1] + " With Latitude: "+objResponse[i+2]);
            LatLng latLng = new LatLng(Double.parseDouble(objResponse[i + 1]), Double.parseDouble(objResponse[i + 2]));
            marker = googleMap.addMarker(new MarkerOptions().position(latLng).title("ID"));
        }
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

        /*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */
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


            public ServiceConnection objServiceConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //create binder to get access to service class
            MyLocalBinder binder = (MyLocalBinder) service;
            // access class to get access
            objNearTagService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    }; // end Service Connection
}
