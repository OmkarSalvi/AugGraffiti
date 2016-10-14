/**
 * This is the Class for providing adapter to the gridview.
 * Adapter acts as a bridge between data source and adapter view i.e. GridView.
 * Adapter iterates through the data set from beginning till the end and generate Views for each item in the list.
 * Android SDK provides three different Adapter implementation, that includes ArrayAdapter, CursorAdapterand SimpleAdapter.
 * An ArrayAdapter expects an Array or an List as input.
 * We are directly using ArrayAdapter by passing array as input.
 */
package com.example.salvi.auggraffiti;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by salvi on 10/10/2016.
 */
public class GridViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<ImageItem> data = new ArrayList<ImageItem>();


    /**
     * This is the constructor for this class to set the members of the class
     * @param context : Context of the activity
     * @param layoutResourceId : resource id of the layout in which grid item defined
     * @param data : Arraylist of the items to be displaced in gridview
     */
    public GridViewAdapter(Context context, int layoutResourceId, ArrayList data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        Log.d("gallery","GridViewAdapter");
    }

    /*
    * The getView() method implementation is necessary, it is responsible for creating a new View for each grid item.
    * When this is called, a View is passed in, which is normally a recycled object, so there’s a check to see if the object is null.
    * If it is null, an ViewHolder is instantiated and configured for holding an ImageView and a TextView.
    * ViewHolder design patterns are efficient while using composite layouts.
    *
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        Log.d("gallery","getview");

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            //object of the class viewholder which will hold the objects of the components inside each item in gridview
            holder = new ViewHolder();
            //Setting components of the item in gridview
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        //Get the item in gridview at certain position and set the components of the item from arraylist
        ImageItem item = data.get(position);
        holder.imageTitle.setText(item.getTitle());
        holder.image.setImageBitmap(item.getImage());
        return row;
    }

    /**
     * Class to hold the components of each item inside gridview
     * compnents : TextView and ImageView
     */
    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}
