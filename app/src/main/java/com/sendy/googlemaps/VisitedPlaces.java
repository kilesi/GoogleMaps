package com.sendy.googlemaps;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class VisitedPlaces extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    //member variable for the recyclerview
    private RecyclerView mRecyclerView;
    private PlacesListAdapter mAdapter;


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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, placeslist.toString());
    }


}
