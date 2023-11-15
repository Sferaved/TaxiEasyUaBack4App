package com.taxieasyua.back4app.cities.api;

import com.google.gson.annotations.SerializedName;

public class CityResponse {
    @SerializedName("card_max_pay")
    private int cardMaxPay;

    @SerializedName("bonus_max_pay")
    private int bonusMaxPay;

    public int getCardMaxPay() {
        return cardMaxPay;
    }

    public int getBonusMaxPay() {
        return bonusMaxPay;
    }
}
