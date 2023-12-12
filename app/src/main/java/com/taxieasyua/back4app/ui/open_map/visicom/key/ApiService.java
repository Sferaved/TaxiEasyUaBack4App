package com.taxieasyua.back4app.ui.open_map.visicom.key;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("visicomKeyInfo")
    Call<ApiResponse> getVisicomKeyInfo();
}

