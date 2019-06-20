package com.sendy.googlemaps;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class VisitedPlacesActivity extends AppCompatActivity {
    String rPlaces;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visited_places);

        textView = findViewById(R.id.textView_places);

        rPlaces = getIntent().getStringExtra("vPlaces");

        textView.setText(rPlaces);

    }
}
