package com.taxieasyua.back4app.ui.fondy.payment;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PaymentApi {
    @Headers("Content-Type: application/json")
    @POST("checkout/url")
    Call<ApiResponsePay<SuccessResponseDataPay>> makePayment(@Body StatusRequestPay paymentRequest);
}

