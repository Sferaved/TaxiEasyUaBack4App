package com.taxieasyua.back4app.cities.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CityService {
    @GET("city/maxPayValue/{city}")
    Call<CityResponse> getMaxPayValues(@Path("city") String city);
}

