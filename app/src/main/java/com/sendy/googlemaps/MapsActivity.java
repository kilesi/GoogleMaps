package com.sendy.googlemaps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class MapsActivity<points> extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "MainActivity";
    private GoogleMap mMap;
    ArrayList<LatLng> MarkerPoints;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    Place place;
    Location location;
    Status status;
    TextView textView;
    private String encodedRoute;
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Places.initialize(getApplicationContext(), "AIzaSyBTpq2aXpU-MsCALXcmCWpNE6-hNZ11mZI");
       //PlacesClient placesClient = Places.createClient(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Initializing
        MarkerPoints = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place1) {
                //TODO: place marker on selected place.
//                ArrayList<Place> places = new ArrayList<>();
//                ArrayList<Object> placeObjects = new ArrayList<>();
                place = place1;
                //places.add(place);
//                placeObjects.addAll(places);
//                TinyDB tinyDB = new TinyDB(getApplicationContext());
//                tinyDB.putListObject("places", placeObjects);
//
//                // Convert PlaceObjects to PlaceList
//                ArrayList<Object> retrievedObject = new ArrayList<>();
//                retrievedObject = tinyDB.getListObject("places", Object.class);
//
//                ArrayList<Place> retrievedPlaces = new ArrayList<>();
//                for (Object object : retrievedObject) {
//                    retrievedPlaces.add((Place)  object);
//                }
//
//                // on click of action button
//                String placeName = "";
//                for (Place place : retrievedPlaces) {
//                    placeName = place.getName();
//                }
                Log.d(TAG, "place:" + place.toString());
                if (place != null) {
                    mMap.clear();
                }
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
                getPolyline();
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        //create Retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Polyline.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //Build Api
        polyline = retrofit.create(Polyline.class);
        postOrders();
    }

    public void getPolyline() {
        String str_origin = "origin=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
        String str_dest = "destination="+place.getLatLng().latitude + "," + place.getLatLng().longitude;
        // Sensor enabled
        String sensor = "sensor=false";
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
                //JsonArray distance = legs.get(0).getAsJsonObject().get("distance").getAsJsonArray();
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
                Toast.makeText(getApplicationContext(), "The distance is" + parseDistance + " and travel time is" + parseTime, Toast.LENGTH_LONG).show();
                lineOptions.addAll(latlng);
                mMap.addPolyline(lineOptions);
                Log.w(TAG, "points not passed correctly");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "url:" + url);

            }

        });

    }

    public void postOrders() {
        //declare variable url and initialize with the url provided
        String url = "https://orderstest.sendyit.com/orders/query_order?apikey=4RNNeyATKN6B6S6XiOyJdPMEJ3oLRKBT";
        //declare variable body and perform POST operation
        JsonObject body = new JsonObject();
        body.addProperty("order_no", "AC57XI813-41B");
        Call<JsonObject> call1 = polyline.postOrder(url, body);
        call1.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject response2 = response.body();
                    Log.i(TAG, "response2: " + response2);
                    JsonArray result = response2.get("result").getAsJsonArray();
                    JsonObject element1 = result.get(0).getAsJsonObject();
                    String order_no = element1.get("order_no").getAsString();
                    Log.d(TAG, "this is the order number: " + order_no);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "the error is: " + t.getMessage());
            }
        });

    }

    /**
     * Manipulates the map once available.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }

    /**
     * A class to parse the Google Places in JSON format
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    public void postResponse(View view) {
            //declare variable url and initialize with the url provided
            String url = "https://orderstest.sendyit.com/orders/query_order?apikey=4RNNeyATKN6B6S6XiOyJdPMEJ3oLRKBT";
            //declare variable body and perform POST operation
            JsonObject body = new JsonObject();
            body.addProperty("order_no", "AC57XI813-41B");
            Call<JsonObject> call1 = polyline.postOrder(url, body);
            call1.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        JsonObject response2 = response.body();
                        Log.i(TAG, "response2: " + response2);
                        JsonArray result = response2.get("result").getAsJsonArray();
                        JsonObject element1 = result.get(0).getAsJsonObject();
                        String order_no = element1.get("order_no").getAsString();
                        Log.d(TAG, "this is the order number: " + order_no);

                        Intent intent = new Intent(MapsActivity.this, PostActivity.class);
                        intent.putExtra("order_no", order_no);
                        startActivity(intent);
                        textView =  findViewById(R.id.post_view);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "the error is: " + t.getMessage());
                }
            });
        Log.d(TAG,"Post button clicked");
    }

//    public void getResponse(View view) {
//        String msg="";
//        String returnFinalDistanceFee="";
//        String waiting_time_cost_per_min="";
//        String finalDistanceFee="";
//        Call<JsonObject> call2 = polyline.priceDetails(msg,returnFinalDistanceFee,waiting_time_cost_per_min,finalDistanceFee);
//
//        call2.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                JsonObject response3 = response.body();
//                JsonArray result = response3.get("result").getAsJsonArray();
//                JsonObject element2 = result.get(0).getAsJsonObject();
//
//                Log.d(TAG,"this is the price details:"+element2);
//
//                Intent intent = new Intent(MapsActivity.this,GetActivity.class);
//                intent.putExtra("msg", msg);
//                intent.putExtra("returnFinalDistanceFee", returnFinalDistanceFee);
//                intent.putExtra("waiting_time_cost_per_min", waiting_time_cost_per_min);
//                intent.putExtra("finalDistanceFee", finalDistanceFee);
//
//                startActivity(intent);
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.d(TAG,"the error is:"+t.getMessage());
//
//            }
//        });
//    }
}