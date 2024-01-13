package com.taxieasyua.back4app.utils.user;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("user_name")
    private String userName;

    public String getUserName() {
        return userName;
    }
}

