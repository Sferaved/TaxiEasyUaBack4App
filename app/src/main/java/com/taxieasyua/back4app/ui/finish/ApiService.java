package com.taxieasyua.back4app.ui.finish;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiService {
    @GET
    Call<Status> cancelOrder(@Url String url);

    @GET
    Call<OrderResponse> statusOrder(@Url String url);


    @GET("/ip/city")
    Call<City> cityOrder();

    @GET()
    Call<List<RouteResponse>> getRoutes(@Url String url);
}

