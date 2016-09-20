/* This file includes class MySingleton. It encapsulates RequestQueue and other volley functionality.
*  we are setting up only single instance of the request queue using this class, that will last the lifetime of our application.
 */

package com.example.salvi.auggraffiti;

/**
 * Created by Abhishek on 9/12/2016.
 */

import android.app.Activity;
import android.content.Context;
import android.nfc.Tag;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/* This class has been created to avoid unnecessary object of the request queue
* If one queue is already created for current activity then the same instance is used
* So, unnecessary creation is avoided and application performance is enhanced
* */
public class MySingleton {
    private static MySingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private MySingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

    }
    /* This function is used to get the handle of the current activity's queue
    * If the queue is not created then new queue is created
    * Otherwise, the same instance of queue is returned
    * */
    public static synchronized MySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MySingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            /* getApplicationContext() is key, it keeps you from leaking the
            * Activity or BroadcastReceiver if someone passes one in.
            * */
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    /* Adds the request to the created queue
    * */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


}
