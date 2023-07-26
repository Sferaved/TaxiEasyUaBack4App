package com.taxieasyua.back4app.ui.finish;

import com.taxieasyua.back4app.ui.start.StartActivity;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET(StartActivity.api + "/android/webordersCancel/{value}")
    Call<Status> cancelOrder(@Path("value") String value);

    @GET(StartActivity.api + "/android/historyUIDStatus/{value}")
    Call<OrderResponse> statusOrder(@Path("value") String value);
}

