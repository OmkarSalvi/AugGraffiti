/* This file contains the MainActivity class. This is the first activity of the application.
*  It allows user to do google sign-in and register user on the web server using login.php
 */
package com.example.salvi.auggraffiti;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// Importing the volley parameter's
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.server.converter.StringToIntConverter;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "mainactivity";
    private static final int RC_SIGN_IN = 9001;
    //private Button SignIn_Button;

    /* Defined variables for user's email
    * Defined tag to clear all requests of the main activity queue
    * */
    public static String User_email;
    public boolean boolLogin = false;
    public static final String mainStopTag = "tagMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* defined request queue used to perform login service
        * */
        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

		/*setOnClickListener for listening to click event on sign-in button
		* */
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        /* Configure sign-in to request the user's ID, email address, and basic profile. ID and
        * basic profile are included in DEFAULT_SIGN_IN.
        * */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
		.requestServerAuthCode("888442725796-edvqlaf4lbu5ntp1buab19lv7qphpahi.apps.googleusercontent.com")
                .requestEmail()
                .build();

        /* Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        *
        * */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());

    }

    /* onConnectionFailed is called when the connection is failed
    * */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
    /* This method will handle the activity result
    *  Input: int requestCode, int resultCode, Intent data
	*  Output: If sign-in successful, call handleSignInResult method to register usr on the web server.
	*          Else toast will be displayed informing user that sign in attempt failed.
    * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            GoogleSignInAccount acct = result.getSignInAccount();
            if(acct != null) {
                String personName = acct.getDisplayName();
                User_email = acct.getEmail();
                handleSignInResult(result);
            }
            else{
                Toast.makeText(this, "SignIn failed!!",Toast.LENGTH_LONG).show();
            }
        }
    }

    /* updateUI method is used to update the user interface depending on its argument value.
    *  If argument is true, then this method will make sign-in button invisible, create intent of current activity,
    *  terminate the current activity and launch the new activity, i.e. Activity2
    *  If argument is false, then it will keep sign-in button visible
    *  Input: boolean signedIn
    *  Output: Start new activity or be in current activity
    * */
    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            Intent myIntent = new Intent(MainActivity.this, Activity2.class);
            myIntent.putExtra("key", User_email);
            finish();
            MainActivity.this.startActivity(myIntent);
        }
        else {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }

    /* handleSignInResult is called when user logs in using google account credentials
    * Input: googleSignInResult result
    * Output: update the user's interface based on the boolean value obtained from login request
    * */
    private void handleSignInResult(GoogleSignInResult result) {
        //Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d(TAG, "userEmail : "+User_email);
            final String loginURL = "http://roblkw.com/msa/login.php";
            //
            /* Creating a login request
            * User get registered if he/she is loging for the first time
            * Otherwise, user gets his profile paramets such as score on the next page
            * Request a string response from the provided URL.
            * Input: user's email Id
            * Output: boolean value obtained from request
            * */
            StringRequest stringRequest = new StringRequest(Request.Method.POST, loginURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if(Integer.parseInt(response) == 0){
                                Log.d(TAG, "Response is : " + response);
                                boolLogin = true;
                                updateUI(boolLogin);
                            }else{
                                updateUI(boolLogin);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Error occurred in Login request!! Please try later!");
                }
            }
            ){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params_map = new HashMap<String, String>();
                    params_map.put("email", User_email);
                    return params_map;
                }
            }; // getParamrs finished
            /* Adding the tag request to the stringRequest queue
            * Setting the flag on the stringRequest
            * */
            MySingleton.getInstance(this).addToRequestQueue(stringRequest);
            stringRequest.setTag(mainStopTag);
        }
        else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    /* signIn method gets called when sign-in button is clicked
    *  This method starts the sign-in intent.
    *  The user is prompted to select a Google account to sign in with.
    * */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /* this is on click method which will be called when sign-in button is clicked
	*  inside this method call to signin() method will be made and in that method sign-in request will be handled.
    * */
    @Override
    public void onClick(View v) {
        signIn();
    }

    /* onStop gets called when the activity is stopped
    * Clears all request from the queue for this activity
    * */
    @Override
    protected void onStop(){
        super.onStop();
        if( MySingleton.getInstance(this)!= null){
            Log.d(TAG,"Inside onstop and inside queue main");
            MySingleton.getInstance(this).getRequestQueue().cancelAll(mainStopTag);
        }
    }

}
