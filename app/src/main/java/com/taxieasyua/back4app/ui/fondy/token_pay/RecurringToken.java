package com.taxieasyua.back4app.ui.fondy.token_pay;

import com.google.gson.annotations.SerializedName;

public class RecurringToken {
    @SerializedName("request")
    private RequestDataToken request;

    public RecurringToken(String orderId, String orderDescription, String amount, String merchantId, String merchantPassword, String rectoken, String email) {
        this.request = new RequestDataToken(orderId, orderDescription, amount, merchantId, merchantPassword, rectoken, email);
    }

    @Override
    public String toString() {
        return "ReversRequest{" +
                "request=" + request +
                '}';
    }
}

