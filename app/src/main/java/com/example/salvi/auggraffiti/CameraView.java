/**
 * This class is a holder for holding the camera view and providing continuous live camera feedback on screen.
 */
package com.example.salvi.auggraffiti;


import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import java.io.IOException;


/**
 * Created by salvi on 9/24/2016.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder myHolder;
    private static Camera myCamera = null;
    private static final String TAG = "place CamerView";

    public CameraView(Context context, Camera camera){
        super(context);

        myCamera = camera;
        myCamera.setDisplayOrientation(90);
        //get the holder and set this class as the callback, so we can get camera data here
        myHolder = getHolder();
        myHolder.addCallback(this);
        myHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        Log.d(TAG, "in constructor Got the surface holder");
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try{
            //when the surface is created, we can set the camera to draw images in this surfaceholder
            myCamera.setPreviewDisplay(surfaceHolder);
            myCamera.startPreview();
            Log.d(TAG, "surface created");
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceCreated " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        //before changing the application orientation, you need to stop the preview, rotate and then start it again
        Log.d(TAG, "surfacechanged");
        if(myHolder.getSurface() == null)//check if the surface is ready to receive camera data
            return;

        try{
            myCamera.stopPreview();
        } catch (Exception e){
            //this will happen when you are trying the camera if it's not running
        }

        //now, recreate the camera preview
        try{
            myCamera.setPreviewDisplay(myHolder);
            myCamera.startPreview();
            Log.d(TAG, "recreated camera preview");
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceChanged " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //our app has only one screen, so we'll destroy the camera in the surface
        //if you are unsing with more screens, please move this code your activity
        myCamera.stopPreview();
        myCamera.release();
        Log.d(TAG, "destroyed surface");
    }
}
