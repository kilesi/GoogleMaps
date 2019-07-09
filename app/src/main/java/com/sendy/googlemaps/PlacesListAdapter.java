package com.sendy.googlemaps;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.PlacesViewHolder> {

    private static final String TAG = "MainActivity";
    //String Arraylist to hold data in the Adapter
    private ArrayList<String> placesList;
    //private LayoutInflater mInflater;
    private Context context;

    public PlacesListAdapter(Context context, ArrayList<String> placesList) {
       // mInflater = LayoutInflater.from(context);
        this.placesList = placesList;
        this.context = context;
    }


    @NonNull
    @Override
    public PlacesListAdapter.PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.placeslist_item, parent, false);
        return new PlacesViewHolder(view);

    }

    @Override
        public void onBindViewHolder(@NonNull PlacesListAdapter.PlacesViewHolder placesViewHolder, int i) {
            //connects data to the viewHolder.
            String mCurrent = placesList.get(i);
            placesViewHolder.placeItemView.setText(mCurrent);

            placesViewHolder.placeItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, mCurrent,Toast.LENGTH_LONG).show();

//                Methods methods = new Methods();
//                methods.getPolyline();

                    Intent intent = new Intent(context,MapsActivity.class);
                    intent.putExtra("Current", mCurrent);

                    context.startActivity(intent);
                }
            });
    }

    @Override
    public int getItemCount() {
        //getting the size of the Arraylist.
        return placesList.size();
    }

    //create a ViewHolder for the adapter
    public class PlacesViewHolder extends RecyclerView.ViewHolder {
        final TextView placeItemView;
        final LinearLayout placesLinearLayout;

        public PlacesViewHolder(@NonNull View view) {
            super(view);
            placesLinearLayout = itemView.findViewById(R.id.placesLinearLayout);
            placeItemView = itemView.findViewById(R.id.place);
            context = itemView.getContext();
        }
    }

}
