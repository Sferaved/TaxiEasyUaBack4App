package com.cloudipsp.android;

import androidx.annotation.NonNull;

/**
 * Created by vberegovoy on 09.11.15.
 */
public enum Currency {
    UAH,
    RUB,
    USD,
    EUR,
    GBP,
    KZT;

    @NonNull
    @Override
    public String toString() {
        return name();
    }


}
