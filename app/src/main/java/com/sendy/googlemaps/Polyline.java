package com.sendy.googlemaps;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface Polyline {

    String BASE_URL = "https://maps.googleapis.com/directions/";
    //declare http request
    @GET(/*"maps/api/directions/"+ "?\" + parameters+\"&key=AIzaSyBTpq2aXpU-MsCALXcmCWpNE6-hNZ11mZI"*/)

    //define a method
    Call<List<JsonObject>> getpoints(@Url String url);
}
