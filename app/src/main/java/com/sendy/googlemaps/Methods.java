package com.sendy.googlemaps;

import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Methods {
    private static final String TAG = "MainActivity";
    Location mLastLocation;
    Place place;
    private Polyline polyline;
    private GoogleMap mMap;

    public void getPolyline() {
        String str_origin = "origin=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
        String str_dest = "destination=" + place.getLatLng().latitude + "," + place.getLatLng().longitude;
        String output = "json";
        String parameters = str_origin + "&" + str_dest;
        final String url = output + "?" + parameters + "&key=AIzaSyBTpq2aXpU-MsCALXcmCWpNE6-hNZ11mZI";

        //call Api method
        Call<JsonObject> call = polyline.getpoints(url);
        Log.w(TAG, "url: " + url);
        //call api using empty method
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.w(TAG, "url is okay");
                List<LatLng> latlng = new ArrayList<>(0);
                PolylineOptions lineOptions = new PolylineOptions();
                JsonObject response1 = response.body();
                Log.w(TAG, "response: " + response1.toString());
                JsonArray routes = response1.get("routes").getAsJsonArray();
                JsonArray legs = routes.get(0).getAsJsonObject().get("legs").getAsJsonArray();
                JsonArray steps = legs.get(0).getAsJsonObject().get("steps").getAsJsonArray();

                JsonObject distance = legs.get(0).getAsJsonObject().get("distance").getAsJsonObject();
                String parseDistance = distance.get("text").getAsString();

                JsonObject duration = legs.get(0).getAsJsonObject().get("duration").getAsJsonObject();
                String parseTime = duration.get("text").getAsString();
                for (int i = 0; i < steps.size(); i++) {
                    JsonObject step = steps.get(i).getAsJsonObject();
                    JsonObject polyline = step.get("polyline").getAsJsonObject();
                    String encoded = polyline.get("points").getAsString();
                    latlng.addAll(PolyUtil.decode(encoded));
                }
                // Toast.makeText(getApplicationContext(), "The distance is" + parseDistance + " and travel time is" + parseTime, Toast.LENGTH_LONG).show();
                lineOptions.addAll(latlng);
                mMap.addPolyline(lineOptions);
                Log.w(TAG, "points not passed correctly");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "url:" + url);

            }

        });

        //create Retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Polyline.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //Build Api
        polyline = retrofit.create(Polyline.class);
    }

}
