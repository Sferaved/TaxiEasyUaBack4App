package com.taxieasyua.back4app.ui.open_map.visicom;

import retrofit2.Retrofit;

public class GeocodeApiClient {

    private GeocodeApiService apiService;

    public GeocodeApiClient() {
        Retrofit retrofit = RetrofitClientInstance.getRetrofitInstance();
        apiService = retrofit.create(GeocodeApiService.class);
    }

    public GeocodeApiService getApiService() {
        return apiService;
    }
}
