package com.sendy.googlemaps;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class VisitedPlaces extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    //member variable for the recyclerview
    private RecyclerView mRecyclerView;
    private PlacesListAdapter mAdapter;
    private ArrayList<String> placeslist = new ArrayList<>();
    Context context;

    private static SQLiteDatabase db;
    private static SQLiteHandler sqLiteHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visited_places);

        ArrayList<String> placeslist = getIntent().getStringArrayListExtra("selectedPlaces");

        Log.d(TAG,"places list"+placeslist);



            //create a RecyclerView

            // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.recyclerview);
            // Create an adapter and supply the data to be displayed.
        mAdapter = new PlacesListAdapter(VisitedPlaces.this, placeslist);
        Log.d(TAG,"places list"+placeslist);
            // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
            // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        Log.d(TAG, placeslist.toString());
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            if (placeslist.size()>1){
                placeslist.remove(i);
                mAdapter.notifyItemRemoved(i);
                mAdapter.notifyItemRangeChanged(i, placeslist.size());
            }
            else {
                Toast.makeText(getApplicationContext(),"the list is less than 1",Toast.LENGTH_LONG);
            }
            mAdapter.notifyItemRemoved(i);
            mAdapter.notifyDataSetChanged();
        }
    };

}
