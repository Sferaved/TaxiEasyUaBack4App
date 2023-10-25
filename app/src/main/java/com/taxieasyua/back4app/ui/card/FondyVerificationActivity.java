package com.taxieasyua.back4app.ui.card;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.fondy.callback.CallbackResponse;
import com.taxieasyua.back4app.ui.fondy.callback.CallbackService;
import com.taxieasyua.back4app.ui.fondy.revers.ApiResponseRev;
import com.taxieasyua.back4app.ui.fondy.revers.ReversApi;
import com.taxieasyua.back4app.ui.fondy.revers.ReversRequestData;
import com.taxieasyua.back4app.ui.fondy.revers.ReversRequestSent;
import com.taxieasyua.back4app.ui.fondy.revers.SuccessResponseDataRevers;
import com.taxieasyua.back4app.ui.fondy.status.ApiResponse;
import com.taxieasyua.back4app.ui.fondy.status.FondyApiService;
import com.taxieasyua.back4app.ui.fondy.status.StatusRequest;
import com.taxieasyua.back4app.ui.fondy.status.StatusRequestBody;
import com.taxieasyua.back4app.ui.fondy.status.SuccessfulResponseData;
import com.taxieasyua.back4app.ui.home.MyBottomSheetErrorFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetMessageFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FondyVerificationActivity extends AppCompatActivity {
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
        MyBottomSheetMessageFragment bottomSheetDialogFragment = new MyBottomSheetMessageFragment(getString(R.string.fondy_back_ver));
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
                            getCardToken();
                            getRevers(MainActivity.order_id,getString(R.string.return_pay), "100");
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
                        MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
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
                MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

    }

    private void getCardToken() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://m.easy-order-taxi.site") // Замените на фактический URL вашего сервера
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Создайте сервис
        CallbackService service = retrofit.create(CallbackService.class);
        String email = logCursor(MainActivity.TABLE_USER_INFO).get(3);
        // Выполните запрос
        Call<CallbackResponse> call = service.handleCallback(email);
        call.enqueue(new Callback<CallbackResponse>() {
            @Override
            public void onResponse(@NonNull Call<CallbackResponse> call, @NonNull Response<CallbackResponse> response) {
                if (response.isSuccessful()) {
                    CallbackResponse callbackResponse = response.body();
                    if (callbackResponse != null) {

                        String card_token = callbackResponse.getCard_token();//Токен карты

                        Log.d(TAG, "onResponse: card_token: " + card_token);

                        ContentValues cv = new ContentValues();
                        cv.put("rectoken", card_token);
                        SQLiteDatabase database = openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
                        database.update(MainActivity.TABLE_USER_INFO, cv, "id = ?",
                                new String[] { "1" });
                        database.close();

                        Log.d(TAG, "onResponse: " + logCursor(MainActivity.TABLE_USER_INFO));
                        Log.d(TAG, "onResponse: " + logCursor(MainActivity.TABLE_USER_INFO).get(6));
                    }
                } else {
                    // Обработка случаев, когда ответ не 200 OK
                }
            }

            @Override
            public void onFailure(Call<CallbackResponse> call, Throwable t) {
                // Обработка ошибки запроса
            }
        });
    }
    private void getRevers(String orderId, String comment, String amount) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pay.fondy.eu/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ReversApi apiService = retrofit.create(ReversApi.class);
        String merchantPassword = getString(R.string.fondy_key_storage);

        ReversRequestData reversRequestData = new ReversRequestData(
                orderId,
                comment,
                amount,
                MainActivity.MERCHANT_ID,
                merchantPassword
        );
        Log.d("TAG1", "getRevers: " + reversRequestData.toString());
        ReversRequestSent reversRequestSent = new ReversRequestSent(reversRequestData);


        Call<ApiResponseRev<SuccessResponseDataRevers>> call = apiService.makeRevers(reversRequestSent);

        call.enqueue(new Callback<ApiResponseRev<SuccessResponseDataRevers>>() {
            @Override
            public void onResponse(Call<ApiResponseRev<SuccessResponseDataRevers>> call, Response<ApiResponseRev<SuccessResponseDataRevers>> response) {

                if (response.isSuccessful()) {
                    ApiResponseRev<SuccessResponseDataRevers> apiResponse = response.body();
                    Log.d("TAG1", "JSON Response: " + new Gson().toJson(apiResponse));
                    if (apiResponse != null) {
                        SuccessResponseDataRevers responseData = apiResponse.getResponse();
                        Log.d("TAG1", "onResponse: " + responseData.toString());
                        if (responseData != null) {
                            // Обработка успешного ответа
                            Log.d("TAG1", "onResponse: " + responseData.toString());

                        }
                    }
                } else {
                    // Обработка ошибки запроса
                    Log.d("TAG", "onResponse: Ошибка запроса, код " + response.code());
                    try {
                        String errorBody = response.errorBody().string();
                        Log.d("TAG", "onResponse: Тело ошибки: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponseRev<SuccessResponseDataRevers>> call, Throwable t) {
                // Обработка ошибки сети или другие ошибки
                Log.d("TAG", "onFailure: Ошибка сети: " + t.getMessage());
            }
        });

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