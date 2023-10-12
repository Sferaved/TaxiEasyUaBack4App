package com.taxieasyua.back4app.ui.fondy.payment;

import com.google.gson.annotations.SerializedName;
import com.taxieasyua.back4app.ui.fondy.status.ErrorData;

public class ErrorResponse {
    @SerializedName("response")
    private ErrorData response;

    public ErrorData getResponse() {
        return response;
    }
}

