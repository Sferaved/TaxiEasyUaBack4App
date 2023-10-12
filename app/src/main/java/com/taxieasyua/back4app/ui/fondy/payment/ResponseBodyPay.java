package com.taxieasyua.back4app.ui.fondy.payment;

import com.google.gson.annotations.SerializedName;

public class ResponseBodyPay {
    @SerializedName("response")
    private SuccessResponseDataPay successResponse;

    @SerializedName("error_response")
    private ErrorDataPay errorResponse;

    public SuccessResponseDataPay getSuccessResponse() {
        return successResponse;
    }

    public ErrorDataPay getErrorResponse() {
        return errorResponse;
    }

    @Override
    public String toString() {
        return "ResponseBodyPay{" +
                "successResponse=" + successResponse +
                ", errorResponse=" + errorResponse +
                '}';
    }
}
