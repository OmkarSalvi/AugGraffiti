/**
 * This is the class to hold indivitual items which needs to be displayed in gridview.
 * We are displaying Image and a text in each of the gridview.
 */
package com.example.salvi.auggraffiti;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by salvi on 10/10/2016.
 */

/**
 * This class is an abstraction for each of the item inside GridView
 */
public class ImageItem {
    private Bitmap image;
    private String title;

    /**
     * Constructor for this class
     * @param image : bitmap object which is the image itself to be shown
     * @param title : String object which is the title of the image
     */
    public ImageItem(Bitmap image, String title) {
        super();
        Log.d("gallery","ImageItem");

        this.image = image;
        this.title = title;
    }

    /**
     * Getter for the image
     * @return Bitmap of the image
     */
    public Bitmap getImage() {
        Log.d("gallery","getImage");

        return image;
    }

    /**
     * Setter for the image
     * @param image : Bitmap object to be set
     */
    public void setImage(Bitmap image) {
        Log.d("gallery","setImage");

        this.image = image;
    }

    /**
     * Getter for the image title
     * @return : String which is the title of the image
     */
    public String getTitle() {

        Log.d("gallery","gettitle");
        return title;
    }

    /**
     * Setter for the image title
     * @param title : String which is to be set as title of the image
     */
    public void setTitle(String title) {
        Log.d("gallery","setTitle");

        this.title = title;
    }
}
