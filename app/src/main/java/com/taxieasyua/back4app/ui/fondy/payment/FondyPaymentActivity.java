package com.taxieasyua.back4app.ui.fondy.payment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.taxieasyua.back4app.R;

public class FondyPaymentActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fondy_payment);

        webView = findViewById(R.id.webView);

        // Настройка WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // URL для открытия (checkoutUrl из ответа)
        String checkoutUrl = getIntent().getStringExtra("checkoutUrl");
        // Загрузка URL в WebView
        webView.loadUrl(checkoutUrl);
    }
}