package com.example.salvi.auggraffiti;

/**
 * Created by Abhishek on 10/10/2016.
 */

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder objHolder;
    private Camera objCamera;
    private Context context;
    private static final String TAG = "debugCamera";
    public byte bytearray[];
    public CameraPreview(Context context, Camera camera) {
        super(context);
        objCamera = camera;
        objCamera.setDisplayOrientation(90);
        // Install a SurfaceHolder callback to get notified when the underlying surface is created and destroyed
        objHolder = getHolder();
        objHolder.addCallback(this);
        objHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    /**
     * Surface has been created, now tell the camera where to draw the preview
     * @param holder: Surface holder
     */
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if(objCamera == null || holder == null ){
                Log.d(TAG, "Please check the Permissions");
            }
            objCamera.setPreviewDisplay(holder);
            objCamera.startPreview();

        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
        try{

            objCamera.setPreviewCallback(new Camera.PreviewCallback() {

                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Parameters parameters = camera.getParameters();
                    int width = parameters.getPreviewSize().width;
                    int height = parameters.getPreviewSize().height;
                    ByteArrayOutputStream outstr = new ByteArrayOutputStream();
                    Rect rect = new Rect(0, 0, width, height);
                    YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21,width,height,null);
                    yuvimage.compressToJpeg(rect, 100, outstr);
                    bytearray = outstr.toByteArray();
                    //Bitmap bmp = BitmapFactory.decodeByteArray(outstr.toByteArray(), 0, outstr.size());
                    //imgView1.setImageBitmap(bmp);
                }
            });
        }
        catch (Exception e) {

            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
    }

    /**
     * Helper to take care of releasing the Camera preview in activity
     * @param holder : Surface holder
     */
    public void surfaceDestroyed(SurfaceHolder holder) {

        if(objCamera != null) {
            objCamera.stopPreview();
            objCamera.setPreviewCallback(null);
            objCamera.release();
            objCamera = null;
        }
    }

    /**
     * Helper to get captured image byte array data
     * @return : returns bytearray of captured image
     */
    public byte[] getFinalBytes(){
        return bytearray;
    }

    /**
     * Helper to take care of change or rotate camera
     * Stops the preview before resizing or reformatting it
     * @param holder : Surface holder
     * @param format : defines format
     * @param w : width in integer
     * @param h : height in integer
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (objHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            objCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        /**
         * Set preview size and make any resize, rotate or reformatting changes
         * start preview with new settings
         */
        try {
            objCamera.setPreviewDisplay(objHolder);
            objCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}
