package com.taxieasyua.back4app.ui.fondy.status;

import com.google.gson.annotations.SerializedName;

import com.google.gson.annotations.SerializedName;

public class StatusResponseData {
    @SerializedName("response")
    private SuccessfulResponseData response; // Изменено на SuccessfulResponseData

    public SuccessfulResponseData getResponse() {
        return response;
    }
}

