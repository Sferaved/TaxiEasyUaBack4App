package com.taxieasyua.back4app.ui.fondy.payment;

import com.google.gson.annotations.SerializedName;

public class SuccessResponseDataPay {
    @SerializedName("response_status")
    private String responseStatus;

    @SerializedName("checkout_url")
    private String checkoutUrl;
    @SerializedName("error_message")
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @SerializedName("error_code")
    private String errorCode;

    public String getResponseStatus() {
        return responseStatus;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }
}
