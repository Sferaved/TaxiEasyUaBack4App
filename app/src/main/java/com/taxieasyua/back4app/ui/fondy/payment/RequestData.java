package com.taxieasyua.back4app.ui.fondy.payment;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.taxieasyua.back4app.ui.fondy.SignatureGenerator;

import java.util.TreeMap;
import java.util.Map;

public class RequestData {
    @SerializedName("order_id")
    private String order_id; // Имя поля должно соответствовать JSON-запросу
    @SerializedName("order_desc")
    private String order_desc; // Имя поля должно соответствовать JSON-запросу
    @SerializedName("currency")
    private String currency; // Это поле не нужно аннотировать, так как имя совпадает
    @SerializedName("amount")
    private String amount; // Это поле не нужно аннотировать, так как имя совпадает
    @SerializedName("signature")
    private String signature; // Это поле не нужно аннотировать, так как имя совпадает
    @SerializedName("merchant_id")
    private String merchant_id; // Имя поля должно соответствовать JSON-запросу

    public RequestData(String orderId, String orderDescription, String amount, String merchantId, String merchantPassword) {
        this.order_id = orderId; // Используйте поле order_id, а не orderId
        this.order_desc = orderDescription; // Используйте поле order_desc, а не orderDescription
        this.currency = "UAH"; // Установите значение валюты
        this.amount = amount;
        this.merchant_id = merchantId; // Используйте поле merchant_id, а не merchantId
        this.signature = generateSignature(merchantPassword, createParameterMap());
    }

    private Map<String, String> createParameterMap() {
        Map<String, String> params = new TreeMap<>();
        params.put("order_id", order_id);
        params.put("order_desc", order_desc);
        params.put("currency", currency);
        params.put("amount", amount);
        params.put("merchant_id", merchant_id);
        // Добавьте другие параметры, если необходимо

        return params;
    }

    private String generateSignature(String merchantPassword, Map<String, String> params) {
        return SignatureGenerator.generateSignature(merchantPassword, params);
    }

    @Override
    public String toString() {
        return "ReversRequestData{" +
                "order_id='" + order_id + '\'' +
                ", order_desc='" + order_desc + '\'' +
                ", currency='" + currency + '\'' +
                ", amount='" + amount + '\'' +
                ", signature='" + signature + '\'' +
                ", merchant_id='" + merchant_id + '\'' +
                '}';
    }
}
