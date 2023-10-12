package com.taxieasyua.back4app.ui.fondy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cloudipsp.android.Card;
import com.cloudipsp.android.Cloudipsp;
import com.cloudipsp.android.CloudipspWebView;
import com.cloudipsp.android.Currency;
import com.cloudipsp.android.GooglePayCall;
import com.cloudipsp.android.Order;
import com.cloudipsp.android.Receipt;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vberegovoy on 6/20/17.
 */

abstract public class BaseExampleActivity extends Activity implements
        Cloudipsp.PayCallback,
        Cloudipsp.GooglePayCallback {
    private static final int RC_GOOGLE_PAY = 100500;
    private static final String K_GOOGLE_PAY_CALL = "google_pay_call";

    private EditText editAmount;
    private Spinner spinnerCcy;
    private EditText editEmail;
    private EditText editDescription;
    private CloudipspWebView webView;

    private Cloudipsp cloudipsp;
    private GooglePayCall googlePayCall;// <- this should be serialized on saving instance state

    protected abstract int getLayoutResId();

    protected abstract Card getCard();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        findViewById(R.id.btn_amount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillTest();
            }
        });
        String cost = getIntent().getStringExtra("cost");

        editAmount = findViewById(R.id.edit_amount);
        editAmount.setText(cost);

        spinnerCcy = findViewById(R.id.spinner_ccy);
        editEmail = findViewById(R.id.edit_email);

        String userEmail = logCursor(MainActivity.TABLE_USER_INFO, getApplicationContext()).get(3);
        editEmail.setText(userEmail);
        editEmail.setVisibility(View.INVISIBLE);
        editDescription = findViewById(R.id.edit_description);
//        editDescription.setText("Оплата за поездку по маршруту (Сплата прослуг таксі)");
        editDescription.setText("Сплата за допоміжну діяльність у сфері транспорту");
        findViewById(R.id.btn_pay_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Cloudipsp", "onClick: btn_pay_card ");
                processPayCard();
            }
        });
        findViewById(R.id.btn_pay_google).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processGooglePay();
            }
        });

        webView = findViewById(R.id.web_view);
//        cloudipsp = new Cloudipsp(1396424, webView);
        cloudipsp = new Cloudipsp(1534178, webView);

        spinnerCcy.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Currency.values()));

        if (savedInstanceState != null) {
            googlePayCall = savedInstanceState.getParcelable(K_GOOGLE_PAY_CALL);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(K_GOOGLE_PAY_CALL, googlePayCall);
    }

    @Override
    public void onBackPressed() {
        if (webView.waitingForConfirm()) {
            webView.skipConfirm();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_GOOGLE_PAY:
                if (!cloudipsp.googlePayComplete(resultCode, data, googlePayCall, this)) {
                    Toast.makeText(this, R.string.e_google_pay_canceled, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void fillTest() {
        editAmount.setText("1");
        editEmail.setText("test@example.com");
        editDescription.setText("test payment");
    }

    private void processPayCard() {
        final Order order = createOrder();
        Log.i("Cloudipsp", "processPayCard11111111111: ");
        if (order != null) {
            final Card card = getCard();
            Log.i("Cloudipsp", "card: " + card.toString());
            if (card != null) {
                Log.i("Cloudipsp", "processPayCard22222: ");
                cloudipsp.pay(card, order, this);
            }
        }
    }

    private void processGooglePay() {
        if (Cloudipsp.supportsGooglePay(this)) {
            final Order googlePayOrder = createOrder();
            if (googlePayOrder != null) {
                Log.i("Cloudipsp", "processGooglePay: ");
                cloudipsp.googlePayInitialize(googlePayOrder, this, RC_GOOGLE_PAY, this);
            }
        } else {
            Toast.makeText(this, R.string.e_google_pay_unsupported, Toast.LENGTH_LONG).show();

        }
    }

    private Order createOrder() {
        editAmount.setError(null);
        editEmail.setError(null);
        editDescription.setError(null);

        final int amount;
        try {
            amount = Integer.valueOf(editAmount.getText().toString())*100;
        } catch (Exception e) {
            editAmount.setError(getString(R.string.e_invalid_amount));
            return null;
        }

        final String email = editEmail.getText().toString();
        final String description = editDescription.getText().toString();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError(getString(R.string.e_invalid_email));
            return null;
        } else if (TextUtils.isEmpty(description)) {
            editDescription.setError(getString(R.string.e_invalid_description));
            return null;
        }
        final Currency currency = (Currency) spinnerCcy.getSelectedItem();
        final Order order = new Order(amount, currency, "vb_" + System.currentTimeMillis(), description, email);
        order.setLang(Order.Lang.ru);
        Log.d("Cloudipsp", "createOrder: order" + order.toString());
        return order;
    }

    @Override
    public void onPaidProcessed(Receipt receipt) {
        Toast.makeText(this, "Paid " + receipt.status.name() + "\nPaymentId:" + receipt.paymentId, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPaidFailure(Cloudipsp.Exception e) {
        if (e instanceof Cloudipsp.Exception.Failure) {
            Cloudipsp.Exception.Failure f = (Cloudipsp.Exception.Failure) e;

            Toast.makeText(this, "Failure\nErrorCode: " +
                    f.errorCode + "\nMessage: " + f.getMessage() + "\nRequestId: " + f.requestId, Toast.LENGTH_LONG).show();
        } else if (e instanceof Cloudipsp.Exception.NetworkSecurity) {
            Toast.makeText(this, "Network security error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else if (e instanceof Cloudipsp.Exception.ServerInternalError) {
            Toast.makeText(this, "Internal server error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else if (e instanceof Cloudipsp.Exception.NetworkAccess) {
            Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show();
        }
        e.printStackTrace();
    }

    @Override
    public void onGooglePayInitialized(GooglePayCall result) {
        this.googlePayCall = result;
    }
    @SuppressLint("Range")
    private List<String> logCursor(String table, Context context) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor c = database.query(table, null, null, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                String str;
                do {
                    str = "";
                    for (String cn : c.getColumnNames()) {
                        str = str.concat(cn + " = " + c.getString(c.getColumnIndex(cn)) + "; ");
                        list.add(c.getString(c.getColumnIndex(cn)));

                    }

                } while (c.moveToNext());
            }
        }
        database.close();
        return list;
    }

}
