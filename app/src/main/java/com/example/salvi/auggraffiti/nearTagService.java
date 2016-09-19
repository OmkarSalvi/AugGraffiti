package com.example.salvi.auggraffiti;

/* This class is created for phase-2 of the application
*
* */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class nearTagService extends Service {

    public final IBinder objIBinder = new MyLocalBinder();
    String finalResponse;
    public nearTagService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return objIBinder;
    }


    public String getCurrentTime(){
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.US);
        //return date object
        return  df.format(new Date());
    }

    public String getNearTagsResponse(){
        return finalResponse;
    }


    public void getNearTags(final String strEmail,final String TAG, final String loc_long, final String loc_lang){
        // Instantiate the tagQueue
        RequestQueue tagQueue = Volley.newRequestQueue(this);
        final String nearTagsURL = "http://roblkw.com/msa/neartags.php";
        // Request a string response from the provided URL.
        StringRequest tagsRequest = new StringRequest(Request.Method.POST, nearTagsURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, "Response is : " + response);
                        finalResponse = response;
                        //handleNearTagsResult(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error occurred : That didn't work!");
                return;
            }
        }
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params_map = new HashMap<String, String>();
                params_map.put("email", strEmail);
                params_map.put("loc_long", loc_long);
                params_map.put("loc_lang", loc_lang);
                return params_map;
            }
        };
        // Add the request to the tagQueue.
        tagQueue.add(tagsRequest);
        //------------------
    }

    public class MyLocalBinder extends Binder{

        nearTagService getService(){
            return nearTagService.this;
        }
    }

}