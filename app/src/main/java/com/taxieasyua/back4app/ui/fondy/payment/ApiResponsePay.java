package com.taxieasyua.back4app.ui.fondy.payment;

import com.google.gson.annotations.SerializedName;

public class ApiResponsePay<T> {
    @SerializedName("response")
    private T response;

    public T getResponse() {
        return response;
    }
}



