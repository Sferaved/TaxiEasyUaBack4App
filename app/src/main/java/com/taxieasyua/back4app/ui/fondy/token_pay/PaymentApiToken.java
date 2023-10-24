package com.taxieasyua.back4app.ui.fondy.token_pay;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PaymentApiToken {
    @Headers("Content-Type: application/json")
    @POST("recurring")
    Call<ApiResponseToken<SuccessResponseDataToken>> makePayment(@Body StatusRequestToken paymentRequest);
}

