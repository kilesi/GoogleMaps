package com.sendy.googlemaps;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity<points> extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final String TAG = "MainActivity";
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private static ArrayList<String> places = new ArrayList<>();
    List<LatLng> latlng;
    //PolylineOptions lineOptions = new PolylineOptions();
    ArrayList<LatLng> MarkerPoints;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Marker vehicleOnMotion;
    LocationRequest mLocationRequest;
    Place place;
    SQLiteDatabase db;
    SQLiteHandler sqLiteHandler;
    SessionManager sessionManager;
    String lat, longi;
    String pathroute;
    private int at = 0;
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

//        FloatingActionButton dialog = findViewById(R.id.fab_visitedPlaces);
//
//        dialog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ShowDialog();
//            }
//        });

        FloatingActionButton recyclerView = findViewById(R.id.fab_recyclerView);

        recyclerView.setOnClickListener(v -> {
            //Toast.makeText(MapsActivity.this, "Places yet to be loaded", Toast.LENGTH_LONG).show();
            recyclerViewPlaces();
            //mMap.clear();
            //latlng.clear();
        });
        Intent intent = getIntent();
        sessionManager = new SessionManager(getApplicationContext());

//        FloatingActionButton orders = findViewById(R.id.fab_postOrder);
//
//        orders.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //postOrders()
//            }
//        });

        Places.initialize(getApplicationContext(), "AIzaSyBTpq2aXpU-MsCALXcmCWpNE6-hNZ11mZI");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        sqLiteHandler = new SQLiteHandler(getApplicationContext());

        haveNetworkConnection();
        getLocationPermission();

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

        capturePoint(intent);


//        if (intent != null) {
//           if  (intent.getStringExtra("Current") != null) {
//                Log.d("Current", intent.getStringExtra("Current"));
//                String placeName = intent.getStringExtra("Current");
//                db = sqLiteHandler.getReadableDatabase();
//
//                Cursor cursor = db.rawQuery("SELECT * FROM " + "places" + " WHERE place_name = " + "'" + placeName + "'", null);
//
//                //ArrayList<String> places = new ArrayList<>();
//
//                if (cursor.getCount() > 0) {
//                    for (int i = 0; i < cursor.getCount(); i++) {
//                        if (cursor.moveToNext()) {
//                            lat = cursor.getString(2);
//                            longi = cursor.getString(3);
//                        }
//                       Log.d(TAG, places.toString());
//                    }
//                }
//                cursor.close();
//                db.close();
//
//                Log.d("Orig", sessionManager.getOrigin());
//                Log.d("Origi", lat);
//                Log.d("Origin", longi);
//
//                //create Retrofit object
//                Retrofit retrofit = new Retrofit.Builder()
//                        .baseUrl(Polyline.BASE_URL)
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .build();
//
//                //Build Api
//                polyline = retrofit.create(Polyline.class);
//
//                getPolyline(sessionManager.getOrigin(), lat, longi);
//            }
//        }

//        autocompleteFragment.setText("KISUMU");
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place1) {
                //TODO: place marker on selected place.
                // ArrayList<Place> places = new ArrayList<>();
                place = place1;

                Log.d(TAG, "place:" + place.getName() + place.getLatLng());

                if (place != null) {
                    mMap.clear();
                }
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()));

                db = sqLiteHandler.getWritableDatabase();

                Log.d(TAG, "latlngs " + latlng);
                String origin = "origin=" + mMap.getMyLocation().getLatitude() + "," + mMap.getMyLocation().getLongitude();
                sessionManager.setOrigin(origin);

                getPolyline(origin, String.valueOf(place.getLatLng().latitude), String.valueOf(
                        place.getLatLng().longitude
                ));
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

        //postOrders();
    }

    private void capturePoint(Intent intent) {
        if (intent != null) {
            final String placeName;
            if ((placeName = intent.getStringExtra("Current")) != null) {
                db = sqLiteHandler.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT * FROM " + "places" + " WHERE place_name =" + "'" + placeName + "'", null);
                if (cursor.getCount() > 0 && cursor.moveToNext()) {
                    pathroute = cursor.getString(cursor.getColumnIndexOrThrow(sqLiteHandler.KEY_ROUTE));
                    Log.d(TAG, "list" + pathroute);
                }
                drawOnMap();

            }
        }
    }

    private void drawOnMap() {
        Log.d(TAG, "ROUTE" + pathroute);
        if (pathroute == null) {
            return;
        }
//        mMap.clear();
        PolylineOptions lineOptions = new PolylineOptions();

        latlng = PolyUtil.decode(pathroute);
        lineOptions.addAll(latlng);
        Log.d(TAG, "pathroute is " + pathroute);
        Log.d(TAG, "decoded pathroute is " + latlng);
        // mMap.clear();
        mMap.addPolyline(lineOptions);
        if (!latlng.isEmpty()) {
            mMap.addMarker(new MarkerOptions().position(latlng.get(latlng.size() - 1)));
            viewRoute();

        }
    }


    private ArrayList<String> displayPlaces() {

        if (place == null) {

        } else
            Log.d(TAG, "place is not selected");
        db = sqLiteHandler.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + "places", null);

        //ArrayList<String> places = new ArrayList<>();
        Log.d(TAG, "place is not selected");
        Log.d("Cursor Count", Integer.toString(cursor.getCount()));


        if (cursor.getCount() > 0) {
            places.clear();
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToNext()) {
                    places.add(cursor.getString(1));
                }
                Log.d(TAG, places.toString());
            }
        }
        cursor.close();
        return places;
    }

    public void ShowDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Choose  visited places");
        builder.setItems(displayPlaces().toArray(new CharSequence[0]), null);
        builder.setPositiveButton("OK", (dialog, selectedPlace) -> {

//                getPolyline();

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        Log.d(TAG, "places lists" + displayPlaces());

        dialog.show();
    }

    private void recyclerViewPlaces() {
        displayPlaces();

        Intent intent = new Intent(MapsActivity.this, VisitedPlaces.class);

        intent.putStringArrayListExtra("selectedPlaces", places);
        startActivityForResult(intent, 76);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 76) {
            if (resultCode == Activity.RESULT_OK) {
                mMap.clear();
                capturePoint(data);

                // viewRoute();
                Log.d(TAG, "onActivityResult: viewRoute");
                Log.d(TAG, "onActivityResult: OK");
            } else {
                drawOnMap();
                Log.d(TAG, "onActivityResult: Not okay");
            }
        }
    }


    public void getPolyline(String origin, String latitude, String longitude) {
        String str_dest = "destination=" + latitude + "," + longitude;
        String output = "json";
        String parameters = origin + "&" + str_dest;
        final String url = output + "?" + parameters + "&key=AIzaSyBTpq2aXpU-MsCALXcmCWpNE6-hNZ11mZI";
        Log.w(TAG, "url: " + url);
        //call Api method
        Call<JsonObject> call = polyline.getpoints(url);

        //call api using empty method
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.w(TAG, "url is okay");
                latlng = new ArrayList<>(0);
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
                    String route = polyline.get("points").getAsString();
                    latlng.addAll(PolyUtil.decode(route));
                }

                //saving the Latlng Arraylist in db
                String str = PolyUtil.encode(latlng);
                sqLiteHandler.addPlace(place.getName(), str);

                Toast.makeText(getApplicationContext(), "The distance is" + parseDistance + " and travel time is" + parseTime, Toast.LENGTH_LONG).show();
                lineOptions.addAll(latlng);
                mMap.addPolyline(lineOptions);
                Log.w(TAG, "points not passed correctly");


                Log.w(TAG, "viewRoute not executed correctly");
                viewRoute();

                ////////////////////////////////////
//                boolean hasPoints = false;
//                Double maxLat = null, minLat = null, minLon = null, maxLon = null;
//
//                if (polyline !=null && polyline.getpoints("url") !=null
//                        //polyline != null && polyline.getPoints() != null
//                ) {
//                    for (LatLng coordinate : latlng) {
//                        // Find out the maximum and minimum latitudes & longitudes
//                        // Latitude
//                        maxLat = maxLat != null ? Math.max(coordinate.latitude, maxLat) : coordinate.latitude;
//                        minLat = minLat != null ? Math.min(coordinate.latitude, minLat) : coordinate.latitude;
//
//                        // Longitude
//                        maxLon = maxLon != null ? Math.max(coordinate.longitude, maxLon) : coordinate.longitude;
//                        minLon = minLon != null ? Math.min(coordinate.longitude, minLon) : coordinate.longitude;
//
//                        hasPoints = true;
//                    }
//                }
//
//                if (hasPoints) {
//                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                    builder.include(new LatLng(maxLat, maxLon));
//                    builder.include(new LatLng(minLat, minLon));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));
//                    Log.d(TAG,"CAMERA MOVED "+latlng.toString());
//                }
                /////////////////////
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "url:" + url);

            }

        });

    }

    public void viewRoute() {

        boolean hasPoints = false;
        Double maxLat = null, minLat = null, minLon = null, maxLon = null;

        if (polyline != null && polyline.getpoints("url") != null
            //polyline != null && polyline.getPoints() != null
        ) {
            for (LatLng coordinate : latlng) {
                // Find out the maximum and minimum latitudes & longitudes
                // Latitude
                maxLat = maxLat != null ? Math.max(coordinate.latitude, maxLat) : coordinate.latitude;
                minLat = minLat != null ? Math.min(coordinate.latitude, minLat) : coordinate.latitude;

                // Longitude
                maxLon = maxLon != null ? Math.max(coordinate.longitude, maxLon) : coordinate.longitude;
                minLon = minLon != null ? Math.min(coordinate.longitude, minLon) : coordinate.longitude;

                hasPoints = true;
            }
        }

        if (hasPoints) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(maxLat, maxLon));
            builder.include(new LatLng(minLat, minLon));
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));
            Log.d(TAG, "CAMERA MOVED " + latlng.toString());
            moveObject();
        }

    }

    /**
     * Manipulates the map once available.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: " + googleMap);
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

        locationManager();

        Toast.makeText(this, "Map is ready", Toast.LENGTH_LONG).show();
        //  getLocationPermission();

        if (pathroute != null) {
//            drawOnMap();
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
        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION)
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
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++)
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;

                            return;
                        }
                }
                mLocationPermissionGranted = true;
                Toast.makeText(this, "Permission Granted : OnRequest", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onRequestPermissionsResult: calling init");

            }
        }

    }


    private void getLocationPermission() {

        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                // Show rationale and request permission.

                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void locationManager() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Not Active");
            builder.setMessage("Please enable Location Services and GPS");
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                // Show location settings when the user acknowledges the alert dialog
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
//    public void postOrders() {
//        //declare variable url and initialize with the url provided
//        String url = "https://orderstest.sendyit.com/orders/query_order?apikey=4RNNeyATKN6B6S6XiOyJdPMEJ3oLRKBT";
//        //declare variable body and perform POST operation
//        JsonObject body = new JsonObject();
//        body.addProperty("order_no", "AC29C3177-D4R");
//        Call<JsonObject> call1 = polyline.postOrder(url, body);
//        call1.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                if (response.isSuccessful()) {
//                    JsonObject response2 = response.body();
//                    Log.i(TAG, "response2: " + response2);
//                    JsonArray result = response2.get("result").getAsJsonArray();
//                    JsonObject element1 = result.get(0).getAsJsonObject();
//                    String order_no = element1.get("order_no").getAsString();
//                    Log.d(TAG, "this is the order number: " + order_no);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.d(TAG, "the error is: " + t.getMessage());
//            }
//        });
//
//    }

    private void moveObject() {

//        if (latlng.size() < 1)
//            return;
        if (at >= latlng.size())
            //at = 0;
            return;


        if (vehicleOnMotion != null) {
            vehicleOnMotion.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng.get(at));
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        vehicleOnMotion = mMap.addMarker(markerOptions);


         at=at+(latlng.size()/50);
        new Handler().postDelayed(this::moveObject, 500);

//        if (vehicleOnMotion == latlng.size()){
//            Toast.makeText(getApplicationContext(),"You have arrived",Toast.LENGTH_SHORT).show();
//        }

    }


}

