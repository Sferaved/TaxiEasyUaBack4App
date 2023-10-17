package com.taxieasyua.back4app.ui.fondy.payment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.BlendMode;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.finish.ErrorPayActivity;
import com.taxieasyua.back4app.ui.finish.FinishActivity;
import com.taxieasyua.back4app.ui.fondy.status.ApiResponse;
import com.taxieasyua.back4app.ui.fondy.status.FondyApiService;
import com.taxieasyua.back4app.ui.fondy.status.StatusRequest;
import com.taxieasyua.back4app.ui.fondy.status.StatusRequestBody;
import com.taxieasyua.back4app.ui.fondy.status.SuccessfulResponseData;
import com.taxieasyua.back4app.ui.gallery.GalleryFragment;
import com.taxieasyua.back4app.ui.home.HomeFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetErrorFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetMessageFragment;
import com.taxieasyua.back4app.ui.home.MyGeoDialogFragment;
import com.taxieasyua.back4app.ui.home.MyGeoMarkerDialogFragment;
import com.taxieasyua.back4app.ui.home.MyPhoneDialogFragment;
import com.taxieasyua.back4app.ui.maps.ToJSONParser;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FondyPaymentActivity extends AppCompatActivity {
    private WebView webView;
    private String TAG = "TAG";


    @SuppressLint({"SetJavaScriptEnabled", "MissingInflatedId"})
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
        webView.loadUrl(Objects.requireNonNull(checkoutUrl));
        MyBottomSheetMessageFragment bottomSheetDialogFragment = new MyBottomSheetMessageFragment(getString(R.string.fondy_back));
        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Ваш код для выполнения после закрытия активности
        if(MainActivity.order_id != null) {
            getStatus();
        }
    }

    private void getStatus() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pay.fondy.eu/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FondyApiService apiService = retrofit.create(FondyApiService.class);
        String merchantPassword = getString(R.string.fondy_key_storage);

        StatusRequestBody requestBody = new StatusRequestBody(
                MainActivity.order_id,
                MainActivity.MERCHANT_ID,
                merchantPassword
        );
        StatusRequest statusRequest = new StatusRequest(requestBody);
        Log.d("TAG1", "getUrlToPayment: " + statusRequest.toString());

        Call<ApiResponse<SuccessfulResponseData>> call = apiService.checkOrderStatus(statusRequest);

        call.enqueue(new Callback<ApiResponse<SuccessfulResponseData>>() {
            @SuppressLint("NewApi")
            @Override
            public void onResponse(Call<ApiResponse<SuccessfulResponseData>> call, Response<ApiResponse<SuccessfulResponseData>> response) {
                if (response.isSuccessful()) {
                    ApiResponse<SuccessfulResponseData> apiResponse = response.body();
                    Log.d(TAG, "JSON Response: " + new Gson().toJson(apiResponse));
                    if (apiResponse != null) {
                        SuccessfulResponseData responseData = apiResponse.getResponse();
                        Log.d(TAG, "onResponse: " + responseData.toString());
                        // Обработка успешного ответа
                        Log.d("TAG", "getMerchantId: " + responseData.getMerchantId());
                        Log.d("TAG", "getOrderStatus: " + responseData.getOrderStatus());
                        Log.d("TAG", "getResponse_description: " + responseData.getResponseDescription());
                        String orderStatus = responseData.getOrderStatus();
                        if(orderStatus.equals("approved")){
                            String fragment_key = getIntent().getStringExtra("fragment_key");

                            switch (Objects.requireNonNull(fragment_key)){
                                case "home":
                                    HomeFragment.progressBar.setVisibility(View.VISIBLE);
                                    try {
                                        orderHome();
                                    } catch (MalformedURLException ignored) {

                                    }
                                    break;
                                case "gallery":
                                case "geo":
                                case "marker":
                                    HomeFragment.progressBar.setVisibility(View.VISIBLE);
                                    try {
                                        orderGeoMarker();
                                    } catch (MalformedURLException ignored) {

                                    }
                                    break;
                            }
                        } else {
                           finish();
                        }
                    }
                } else {
                    // Обработка ошибки запроса
                    Log.d("TAG", "onResponse: Ошибка запроса, код " + response.code());
                    try {
                        assert response.errorBody() != null;
                        String errorBody = response.errorBody().string();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.payment_error));
                        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                        Log.d("TAG", "onResponse: Тело ошибки: " + errorBody);
                    } catch (IOException ignored) {

                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<SuccessfulResponseData>> call, Throwable t) {
                // Обработка ошибки сети или другие ошибки
                Log.d("TAG", "onFailure: Ошибка сети: " + t.getMessage());

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.payment_error));
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

    }

    public void orderGeoMarker() throws MalformedURLException {
        String urlOrder = getIntent().getStringExtra("urlOrder");
        Map<String, String> sendUrlMap = ToJSONParser.sendURL(urlOrder);
        Log.d("TAG", "Map sendUrlMap = ToJSONParser.sendURL(urlOrder); " + sendUrlMap);

        String orderWeb = sendUrlMap.get("order_cost");
        String message = sendUrlMap.get("message");

        if (!orderWeb.equals("0")) {
            String to_name;
            if (Objects.equals(sendUrlMap.get("routefrom"), sendUrlMap.get("routeto"))) {
                to_name = getString(R.string.on_city_tv);
                if (!sendUrlMap.get("lat").equals("0")) {
                    insertRecordsOrders(
                            sendUrlMap.get("routefrom"), sendUrlMap.get("routefrom"),
                            sendUrlMap.get("routefromnumber"), sendUrlMap.get("routefromnumber"),
                            Double.toString(OpenStreetMapActivity.startLat), Double.toString(OpenStreetMapActivity.startLan),
                            Double.toString(OpenStreetMapActivity.startLat), Double.toString(OpenStreetMapActivity.startLan),
                            getApplicationContext()
                    );
                }
            } else {

                if(sendUrlMap.get("routeto").equals("Точка на карте")) {
                    to_name =  getString(R.string.end_point_marker);
                } else {
                    to_name = sendUrlMap.get("routeto") + " " + sendUrlMap.get("to_number");
                }

                if (!sendUrlMap.get("lat").equals("0")) {
                    insertRecordsOrders(
                            sendUrlMap.get("routefrom"), to_name,
                            sendUrlMap.get("routefromnumber"), sendUrlMap.get("to_number"),
                            Double.toString(OpenStreetMapActivity.startLat), Double.toString(OpenStreetMapActivity.startLan),
                            sendUrlMap.get("lat"), sendUrlMap.get("lng"),
                            getApplicationContext()
                    );
                }
            }
            String messageResult = getString(R.string.thanks_message) +
                    OpenStreetMapActivity.FromAdressString + " " + getString(R.string.to_message) +
                    to_name + "." +
                    getString(R.string.call_of_order) + orderWeb + getString(R.string.UAH);
            String messageFondy = getString(R.string.fondy_message);

            Intent intent = new Intent(getApplicationContext(), FinishActivity.class);
            intent.putExtra("messageResult_key", messageResult);
            intent.putExtra("messageFondy_key", messageFondy);
            intent.putExtra("messageCost_key", orderWeb);
            intent.putExtra("sendUrlMap", new HashMap<>(sendUrlMap));
            intent.putExtra("UID_key", Objects.requireNonNull(sendUrlMap.get("dispatching_order_uid")));
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), ErrorPayActivity.class);
            intent.putExtra("messageError", message);
            intent.putExtra("urlOrder", urlOrder);
            String orderCost = getIntent().getStringExtra("orderCost");
            intent.putExtra("orderCost", orderCost);
            startActivity(intent);

        }
    }

    private void orderHome() throws MalformedURLException {

        String urlOrder = getIntent().getStringExtra("urlOrder");
        Log.d(TAG, "orderHome: " + urlOrder);
        Map<String, String> sendUrlMap = ToJSONParser.sendURL(urlOrder);

            String orderWeb = sendUrlMap.get("order_cost");
            String message = sendUrlMap.get("message");


            if (!orderWeb.equals("0")) {

                String from_name = sendUrlMap.get("routefrom");
                String from_number = sendUrlMap.get("routefromnumber");
                String to_name = sendUrlMap.get("routeto");
                String to_number = sendUrlMap.get("to_number");
                String messageResult;
                if (from_name.equals(to_name)) {
                    messageResult = getString(R.string.thanks_message) +
                            from_name + " " + from_number + getString(R.string.on_city) +
                            getString(R.string.cost_of_order) + orderWeb + getString(R.string.UAH);
                } else {
                    messageResult =  getString(R.string.thanks_message) +
                            from_name + " " + from_number + " " + getString(R.string.to_message) +
                            to_name + " " + to_number + "." +
                            getString(R.string.cost_of_order) + orderWeb + getString(R.string.UAH);
                }
                Log.d(TAG, "order: sendUrlMap.get(\"from_lat\")" + sendUrlMap.get("from_lat"));
                Log.d(TAG, "order: sendUrlMap.get(\"lat\")" + sendUrlMap.get("lat"));
                if(!sendUrlMap.get("from_lat").equals("0") && !sendUrlMap.get("lat").equals("0")) {
                    if(from_name.equals(to_name)) {
                        insertRecordsOrders(
                                from_name, from_name,
                                from_number, from_number,
                                sendUrlMap.get("from_lat"), sendUrlMap.get("from_lng"),
                                sendUrlMap.get("from_lat"), sendUrlMap.get("from_lng"),
                                getApplicationContext()
                        );
                    } else {
                        insertRecordsOrders(
                                from_name, to_name,
                                from_number, to_number,
                                sendUrlMap.get("from_lat"), sendUrlMap.get("from_lng"),
                                sendUrlMap.get("lat"), sendUrlMap.get("lng"),
                                getApplicationContext()
                        );
                    }
                }
                String messageFondy = getString(R.string.fondy_message);

                Intent intent = new Intent(getApplicationContext(), FinishActivity.class);
                intent.putExtra("messageResult_key", messageResult);
                intent.putExtra("messageFondy_key", messageFondy);
                intent.putExtra("messageCost_key", orderWeb);
                intent.putExtra("sendUrlMap", new HashMap<>(sendUrlMap));
                intent.putExtra("UID_key", String.valueOf(sendUrlMap.get("dispatching_order_uid")));
                startActivity(intent);

            } else {
                Intent intent = new Intent(getApplicationContext(), ErrorPayActivity.class);
                intent.putExtra("messageError", message);
                intent.putExtra("urlOrder", urlOrder);
                String orderCost = getIntent().getStringExtra("orderCost");
                intent.putExtra("orderCost", orderCost);
                startActivity(intent);
            }
    }
    private static void insertRecordsOrders( String from, String to,
                                             String from_number, String to_number,
                                             String from_lat, String from_lng,
                                             String to_lat, String to_lng, Context context) {

        String selection = "from_street = ?";
        String[] selectionArgs = new String[] {from};
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor_from = database.query(MainActivity.TABLE_ORDERS_INFO,
                null, selection, selectionArgs, null, null, null);

        selection = "to_street = ?";
        selectionArgs = new String[] {to};

        Cursor cursor_to = database.query(MainActivity.TABLE_ORDERS_INFO,
                null, selection, selectionArgs, null, null, null);



        if (cursor_from.getCount() == 0 || cursor_to.getCount() == 0) {

            String sql = "INSERT INTO " + MainActivity.TABLE_ORDERS_INFO + " VALUES(?,?,?,?,?,?,?,?,?);";
            SQLiteStatement statement = database.compileStatement(sql);
            database.beginTransaction();
            try {
                statement.clearBindings();
                statement.bindString(2, from);
                statement.bindString(3, from_number);
                statement.bindString(4, from_lat);
                statement.bindString(5, from_lng);
                statement.bindString(6, to);
                statement.bindString(7, to_number);
                statement.bindString(8, to_lat);
                statement.bindString(9, to_lng);

                statement.execute();
                database.setTransactionSuccessful();

            } finally {
                database.endTransaction();
            }

        }

        cursor_from.close();
        cursor_to.close();

    }
    @SuppressLint("Range")
    public List<String> logCursor(String table) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase database = openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
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