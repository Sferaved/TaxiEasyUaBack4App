package com.taxieasyua.back4app.ui.fondy.callback;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CallbackService {
    @GET("/get-card-token/{email}")
    Call<CallbackResponse> handleCallback(@Path("email") String email);
}
