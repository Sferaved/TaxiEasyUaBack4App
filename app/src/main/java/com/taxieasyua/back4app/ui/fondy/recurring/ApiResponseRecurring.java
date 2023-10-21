package com.taxieasyua.back4app.ui.fondy.recurring;

import com.google.gson.annotations.SerializedName;

public class ApiResponseRecurring<T> {
    @SerializedName("response")
    private T response;

    public T getResponse() {
        return response;
    }
}



