/**
 * This class provides the interface to draw on the screen on response to touch events
 */
package com.example.salvi.auggraffiti;

import android.content.Context;
import android.graphics.Path;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
public class PlaceDrawing extends View {

    private Bitmap myBitmap;
    private Canvas myCanvas;
    private Path myPath;
    private Paint myBitmapPaint;
    Context context;
    private Paint myPaint2;
    private Path myPath2;
    private static Paint mPaint;

    /**
     * Constructor of the class
     * @param c : conext object of the activity where canvas is being drawn
     * @param mPaint : Paint object used to draw on canvas
     */
    public PlaceDrawing(Context c, Paint mPaint) {
        super(c);
        context=c;
        PlaceDrawing.mPaint = mPaint;
        myPath = new Path();
        myBitmapPaint = new Paint(Paint.DITHER_FLAG);
        myPaint2 = new Paint();
        myPath2 = new Path();
        myPaint2.setAntiAlias(true);
        myPaint2.setColor(Color.BLUE);
        myPaint2.setStyle(Paint.Style.STROKE);
        myPaint2.setStrokeJoin(Paint.Join.ROUND);
        myPaint2.setStrokeWidth(50);
    }

    /**
     * Getter for the bitmap member of class
     * @return Bitmap object
     */
    public Bitmap getmyBitmap(){
        return this.myBitmap;
    }

    /**
     * Callback function on change in the size of the View
     * @param w : new width of th View
     * @param h : new height of the View
     * @param oldw : old width of th View
     * @param oldh : old height of the View
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        myBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        myCanvas = new Canvas(myBitmap);

    }

    /**
     * This Method is called via invalidate method call
     * @param canvas : canvas to be drawn on
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap( myBitmap, 0, 0, myBitmapPaint);
        canvas.drawPath( myPath,  mPaint);
        canvas.drawPath( myPath2,  myPaint2);

    }

    private float oldx, oldy;
    private static final float threshold = 5;

    /**
     * On touchListerner for listening to touch events
     * @param event : Motionevent object
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float newx = event.getX();
        float newy = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                myPath.reset();
                myPath.moveTo(newx, newy);
                oldx = newx;
                oldy = newy;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(newx - oldx);
                float dy = Math.abs(newy - oldy);
                if (dx >= threshold || dy >= threshold) {
                    myPath.quadTo(oldx, oldy, (newx + oldx)/2, (newy + oldy)/2);
                    oldx = newx;
                    oldy = newy;
                    myPath2.reset();
                    myPath2.addCircle(oldx, oldy, 30, Path.Direction.CW);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                myPath.lineTo(oldx, oldy);
                myPath2.reset();
                // commit the path to our offscreen
                myCanvas.drawPath(myPath,  mPaint);
                // kill this so we don't double draw
                myPath.reset();
                invalidate();
                break;
        }
        return true;
    }
}
