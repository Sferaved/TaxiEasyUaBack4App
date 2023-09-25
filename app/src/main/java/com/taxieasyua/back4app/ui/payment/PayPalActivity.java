package com.taxieasyua.back4app.ui.payment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.taxieasyua.back4app.R;

import org.json.JSONException;

import java.math.BigDecimal;

public class PayPalActivity extends AppCompatActivity {
    private AppCompatButton btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_pal);

        PayPalConfiguration config = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) // Используйте ENVIRONMENT_PRODUCTION в боевом режиме
                .clientId("YOUR_PAYPAL_CLIENT_ID");

        Intent intent = new Intent(getApplicationContext(), PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);


        btn = findViewById(R.id.pay_pal_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PayPalPayment payment = new PayPalPayment(new BigDecimal("10.00"), "USD", "Description", PayPalPayment.PAYMENT_INTENT_SALE);
                Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
                startActivityForResult(intent, 1); // 1 - это код запроса для обработки результата


            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // Платеж успешен. Вы можете получить информацию о транзакции, если это необходимо.
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    String paymentDetails = null;
                    try {
                        paymentDetails = confirmation.toJSONObject().toString(4);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Log.i("PayPal", "PaymentConfirmation info: " + paymentDetails);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Платеж был отменен пользователем.
                Log.i("PayPal", "Payment canceled");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                // Неверные дополнительные данные.
                Log.i("PayPal", "Invalid PayPal configuration");
            }
        }
    }

}