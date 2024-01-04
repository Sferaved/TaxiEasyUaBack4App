package com.taxieasyua.back4app.utils.ip;

import com.google.gson.annotations.SerializedName;

public class CountryResponse {
    @SerializedName("response")
    private String country;

    public String getCountry() {
        return country;
    }
}
