package com.sendy.googlemaps;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface Polyline {
    String BASE_URL = "https://maps.googleapis.com/maps/api/directions/";
    //declare http request
    @GET(/*""+ "?\" + parameters+\"&key=AIzaSyBTpq2aXpU-MsCALXcmCWpNE6-hNZ11mZI"*/)
    //define a method
    Call<JsonObject> getpoints(@Url String url);
    //Declare type of request POST
    @POST()
    //declare method
    Call<JsonObject> postOrder(@Url String url, @Body JsonObject body);

}
