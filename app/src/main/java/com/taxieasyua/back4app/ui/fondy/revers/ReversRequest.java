package com.taxieasyua.back4app.ui.fondy.revers;

import com.google.gson.annotations.SerializedName;

public class ReversRequest {
    @SerializedName("request")
    private ReversRequestData request;

    public ReversRequest(String orderId, String comment, String amount, String merchantId, String merchantPassword) {
        this.request = new ReversRequestData(orderId, comment, amount, merchantId, merchantPassword);
    }

    @Override
    public String toString() {
        return "ReversRequest{" +
                "request=" + request +
                '}';
    }
}

