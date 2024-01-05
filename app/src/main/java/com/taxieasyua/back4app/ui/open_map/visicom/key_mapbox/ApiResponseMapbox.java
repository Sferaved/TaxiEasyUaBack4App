package com.taxieasyua.back4app.ui.open_map.visicom.key_mapbox;

import com.google.gson.annotations.SerializedName;

public class ApiResponseMapbox {

    @SerializedName("keyMapbox")
    private String keyMapbox;

    public String getKeyMapbox() {
        return keyMapbox;
    }
}
