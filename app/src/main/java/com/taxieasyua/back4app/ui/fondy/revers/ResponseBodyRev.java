package com.taxieasyua.back4app.ui.fondy.revers;

import com.google.gson.annotations.SerializedName;

public class ResponseBodyRev {
    @SerializedName("response")
    private SuccessResponseDataRevers successResponse;
    @Override
    public String toString() {
        return "ResponseBodyRev{" +
                "successResponse=" + successResponse +
                '}';
    }
}
