package com.taxieasyua.back4app.ui.fondy.payment;

import com.google.gson.annotations.SerializedName;
import com.taxieasyua.back4app.ui.fondy.status.StatusRequestBody;

public class StatusRequestPay {
    @SerializedName("request")
    private RequestData request;

    public StatusRequestPay(RequestData request) {
        this.request = request;
    }

    public RequestData getRequest() {
        return request;
    }

    @Override
    public String toString() {
        return "{" +
                "request=" + request +
                '}';
    }
}