package com.taxieasyua.back4app.ui.fondy.payment;

import com.google.gson.annotations.SerializedName;

public class PaymentRequest {
    @SerializedName("request")
    private RequestData request;

    public PaymentRequest(String orderId, String orderDescription, String amount, String merchantId, String merchantPassword) {
        this.request = new RequestData(orderId, orderDescription, amount, merchantId, merchantPassword);
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "request=" + request +
                '}';
    }
}

