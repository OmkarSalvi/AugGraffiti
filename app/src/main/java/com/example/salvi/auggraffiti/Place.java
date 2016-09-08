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
//project
/**
 * Created by salvi on 9/7/2016.
 */
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

        this.image = (ImageView)this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.Camera_Button);
        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(photo);
        }
    }
}
