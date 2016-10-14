/**
 * This is the activity for displaying all the images of the collected tags.
 * This activity is triggered by clicking the button “Gallery” on Activity2.
 * In this Activity we send a POST request to the server asking for URLs of the images collected by the user.
 * On obtaining the image URLs, this activity will display those images in gridview.
 * It will also allocate title  name to each image and display the title also along with image in gridview.
 * If there is any error in obtaining the image from server, it will be indicated by the toast.
 * We can go back to Activity2 from this activity by clicking the back button.
 */
package com.example.salvi.auggraffiti;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by salvi on 10/10/2016.
 */
public class GalleryActivity extends AppCompatActivity {

    public static final String actStopTag = "taggallery";
    private static final String TAG = "galleryAct";

    static String Email_used = "";
    static String strGetGalleryResponse = "";

    private GridView gridView;
    private GridViewAdapter gridAdapter;

    final ArrayList<ImageItem> imageItems = new ArrayList<>();
    Context context;

    public int image_index;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_layout);

        Intent intent = getIntent();
        Email_used = intent.getStringExtra("Email");

        context = this;
        gridView = (GridView) findViewById(R.id.gridView);

        /* defined request queue used to perform getgallery service
          * defined bindService function to bind the current activity with the service
          * */
        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();

        /* Setting the URL used to create getgallery post request
            * Logging the value of URL and testing the function
            * */
        final String getGalleryURL = "http://roblkw.com/msa/getgallery.php";
        Log.d(TAG,"URL is "+ getGalleryURL);

            /* Creating the post tags request to get all collected tags
            * Input: user's email
            * Output: output obtained from getgallery.php is Comma‐delimited list of image urls
            * */
        StringRequest tagsRequest = new StringRequest(Request.Method.POST, getGalleryURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "getGallery Response is :" + response);
                        //Setting response to global variable
                        strGetGalleryResponse = response;
                        //method to handle the response obtained from server
                        handleGetGallerysResult(strGetGalleryResponse);
                    } //----- Finish OnResponse
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error occurred in getGallery!!!");
                return;
            }// OnErrorResponse function finished
        } // ErrorListener function finished
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params_map = new HashMap<String, String>();
                params_map.put("email", Email_used);

                return params_map;
            } // getParams function finished
        };

            /* Adding the tag request to the tagQueue
            * Setting the flag on the getgallery request
            * */
        MySingleton.getInstance(this).addToRequestQueue(tagsRequest);
        tagsRequest.setTag(actStopTag);

    }

    /**
     * This method handles the response obtained from getgallery post request
     * @param response : String object which is the response obtained from server
     */
    private void handleGetGallerysResult(String response) {

        // Logging response obtained from the neartag request
        Log.d(TAG, "strGetGalleryResponse  :" + response);

        if(response.contains(",")) {
                /* Parsing the response into various image URLS
                * */
            String objResponse[] = response.split(",");
            int i;
            //image index to identify individual image obtained
            image_index = 0;
            //Iterate over all URLs obtained in the response
            for (i = 0; i < objResponse.length; i++) {
                Log.d(TAG, "Image URL" + i + ": " + objResponse[i]);
                // Retrieves an image specified by the URL, displays it in the UI.
                ImageRequest request = new ImageRequest(objResponse[i],
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap1) {
                                image_index++;
                                //Add the bitmap obtained in response to the Arraylist of ImageItem
                                imageItems.add(new ImageItem(bitmap1, "Image#" + image_index));
                                //Create a new GridViewAdapter and connect it to the layout and this activity
                                gridAdapter = new GridViewAdapter(context, R.layout.grid_item_layout, imageItems);
                                //Set the GridViewAdapter created above to the GridView object
                                gridView.setAdapter(gridAdapter);
                                //null -> Bitmap.Config.ARGB_8888
                            }
                        }, 0, 0, Bitmap.Config.ARGB_8888,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                image_index++;
                                Log.d("gallery", "error in get image"+(image_index));
                                Log.d("gallery", ""+error);
                                //Toast to indicate user about any error in the getting image from server
                                Toast.makeText(context,"error in get image"+image_index, Toast.LENGTH_LONG).show();
                            }
                        });
                // Access the RequestQueue through your singleton class.
                MySingleton.getInstance(this).addToRequestQueue(request);
            }
        }else{
            Log.d(TAG, "No image URLS to show");
        }
    }

}

