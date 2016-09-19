package com.example.salvi.auggraffiti;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
//project

public class Place extends Activity {
    private static final int CAMERA_REQUEST = 1888;
    private TextView status;
    private ImageView image;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_layout);

        Intent intent = getIntent();
        String value = intent.getStringExtra("key"); //if it's a string you stored.
        Log.d("message received : ", value);
        status = (TextView) findViewById(R.id.test);
        status.setText(value);

        /* set onclicklistener for button. This listener will listen to the click event on photo button.
        * When button is clicked camera will be launched and user can take a picture.
        * */
        this.image = (ImageView)this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.Camera_Button);
        photoButton.setOnClickListener(new View.OnClickListener() {

            /* This method will create a camera intent and launch the camera activity.
            * when camera is launched, user can take a picture.
            * */
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

    }

    /* when camera ctivity is finished, this method will be called to handle the result of activity.
    * The image captured by the user will be displayed on the screen.
    * */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(photo);
        }
    }
}
