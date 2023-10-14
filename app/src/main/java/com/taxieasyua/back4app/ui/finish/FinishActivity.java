package com.taxieasyua.back4app.ui.finish;

import static com.taxieasyua.back4app.ui.finish.ApiClient.BASE_URL;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.fondy.payment.ApiResponsePay;
import com.taxieasyua.back4app.ui.fondy.payment.FondyPaymentActivity;
import com.taxieasyua.back4app.ui.fondy.payment.PaymentApi;
import com.taxieasyua.back4app.ui.fondy.payment.RequestData;
import com.taxieasyua.back4app.ui.fondy.payment.StatusRequestPay;
import com.taxieasyua.back4app.ui.fondy.payment.SuccessResponseDataPay;
import com.taxieasyua.back4app.ui.fondy.payment.UniqueNumberGenerator;
import com.taxieasyua.back4app.ui.fondy.revers.ApiResponseRev;
import com.taxieasyua.back4app.ui.fondy.revers.ReversApi;
import com.taxieasyua.back4app.ui.fondy.revers.ReversRequestData;
import com.taxieasyua.back4app.ui.fondy.revers.ReversRequestSent;
import com.taxieasyua.back4app.ui.fondy.revers.SuccessResponseDataRevers;
import com.taxieasyua.back4app.ui.home.MyBottomSheetBlackListFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetErrorFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetFondyPayFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetMessageFragment;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FinishActivity extends AppCompatActivity {
    public static TextView text_status;

    String api;
    String baseUrl = "https://m.easy-order-taxi.site";
    Map<String, String> receivedMap;
    String UID_key;
    Thread thread;
    String pay_method;
    String order_id;
    String amount;
    TextView text_full_message;
    String messageResult, messageFondy;
    Button btn_pay;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        new VerifyUserTask().execute();
        List<String> stringListArr = logCursor(MainActivity.CITY_INFO);
        switch (stringListArr.get(1)){
            case "Kyiv City":
                api = MainActivity.apiKyiv;
                break;
            case "Dnipropetrovsk Oblast":
                api = MainActivity.apiDnipro;
                break;
            case "Odessa":
                api = MainActivity.apiOdessa;
                break;
            case "Zaporizhzhia":
                api = MainActivity.apiZaporizhzhia;
                break;
            case "Cherkasy Oblast":
                api = MainActivity.apiCherkasy;
                break;
            case "OdessaTest":
                api = MainActivity.apiTest;
                break;
            default:
                api = MainActivity.apiKyiv;
                break;
        }
        messageResult = getIntent().getStringExtra("messageResult_key");
        messageFondy = getIntent().getStringExtra("messageFondy_key");

        receivedMap = (HashMap<String, String>) getIntent().getSerializableExtra("sendUrlMap");
        amount = receivedMap.get("order_cost") + "00";



        Log.d("TAG", "onCreate: receivedMap" + receivedMap.toString());
        text_full_message = findViewById(R.id.text_full_message);
        text_full_message.setText(messageResult);

        UID_key = getIntent().getStringExtra("UID_key");

        text_status = findViewById(R.id.text_status);
        statusOrderWithDifferentValue(UID_key);

        pay_method = logCursor(MainActivity.TABLE_SETTINGS_INFO).get(4);
        btn_pay = findViewById(R.id.btn_pay);
        if (pay_method.equals("google_payment")) {
           btn_pay.setVisibility(View.VISIBLE);
        }
        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order_id = UniqueNumberGenerator.generateUniqueNumber(getApplication());
                callOrderIdMemory(order_id, UID_key);
                getUrlToPayment(order_id, messageFondy, amount);
                btn_pay.setVisibility(View.GONE);
            }
        });

        Button btn_reset_status = findViewById(R.id.btn_reset_status);
        btn_reset_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected()){
                    statusOrderWithDifferentValue(UID_key);
                } else {
                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                    bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            }
        });

        Button btn_cancel_order = findViewById(R.id.btn_cancel_order);
        long delayMillis = 5 * 60 * 1000;

        Handler handler = new Handler();

        if (pay_method.equals("bonus_payment")) {

             String url = baseUrl + "/bonusBalance/recordsBloke/" + UID_key;

             fetchBonus(url);
             handler.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     btn_cancel_order.setVisibility(View.GONE);
                 }
             }, delayMillis);
         }
        if (pay_method.equals("google_payment")) {

             handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btn_cancel_order.setVisibility(View.GONE);
                }
            }, delayMillis);
        }
        btn_cancel_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected()){
                    cancelOrder(UID_key);
                    if(!receivedMap.get("dispatching_order_uid_Double").equals(" ")) {
                        cancelOrderWithDifferentValue(receivedMap.get("dispatching_order_uid_Double"));
                    }
                    if (thread != null && thread.isAlive()) {
                        thread.interrupt();
                    }
                } else {
                    text_status.setText(R.string.verify_internet);
                }
            }
        });

        Button btn_again = findViewById(R.id.btn_again);
        btn_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!verifyOrder()) {
                    MyBottomSheetBlackListFragment bottomSheetDialogFragment = new MyBottomSheetBlackListFragment("orderCost");
                    bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                } else {
                    if(connected()){
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    } else {
                        MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                    }
                }
            }
        });

        Button btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
        FloatingActionButton fab_cal = findViewById(R.id.fab_call);
        fab_cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);

                List<String> stringList = logCursor(MainActivity.CITY_INFO);
                String phone = stringList.get(3);
                intent.setData(Uri.parse(phone));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (pay_method.equals("bonus_payment") || pay_method.equals("google_payment")) {
           thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // Здесь вызывайте вашу функцию fetchCarFound()
                    fetchCarFound();
                }
            });
           thread.start();
        }
    }

    private void fetchBonus(String url) {

        Call<BonusResponse> call = ApiClient.getApiService().getBonus(url);
        Log.d("TAG", "fetchBonus: " + url);
        call.enqueue(new Callback<BonusResponse>() {
            @Override
            public void onResponse(Call<BonusResponse> call, Response<BonusResponse> response) {
                BonusResponse bonusResponse = response.body();
                if (response.isSuccessful()) {

                    String bonus = String.valueOf(bonusResponse.getBonus());
                    String message = getString(R.string.block_mes) + " " + bonus + " " + getString(R.string.bon);

                    MyBottomSheetMessageFragment bottomSheetDialogFragment = new MyBottomSheetMessageFragment(message);
                    bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

                } else {
                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                    bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            }

            @Override
            public void onFailure(Call<BonusResponse> call, Throwable t) {
                // Обработка ошибок сети или других ошибок
                String errorMessage = t.getMessage();
                t.printStackTrace();
                // Дополнительная обработка ошибки
            }
        });
    }
    private void fetchCarFound() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl) // Замените BASE_URL на ваш базовый URL сервера
                .addConverterFactory(GsonConverterFactory.create())
                .build();

// Создайте экземпляр ApiService
        ApiService apiService = retrofit.create(ApiService.class);

// Вызов метода startNewProcessExecutionStatus с передачей параметров
        Call<Void> call = apiService.startNewProcessExecutionStatus(
                receivedMap.get("doubleOrder")
        );
        String url = call.request().url().toString();
        Log.d("TAG", "URL запроса: " + url);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Обработайте ошибку при выполнении запроса
            }
        });

    }

    public void callOrderIdMemory(String fondyOrderId, String uid) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Void> call = apiService.orderIdMemory(fondyOrderId, uid);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Обработка успешного ответа
                } else {
                    // Обработка неуспешного ответа
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Обработка ошибки
            }
        });
    }
    private boolean verifyOrder() {
        SQLiteDatabase database = this.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.query(MainActivity.TABLE_USER_INFO, null, null, null, null, null, null);

        boolean verify = true;
        if (cursor.getCount() == 1) {

            if (logCursor(MainActivity.TABLE_USER_INFO).get(1).equals("0")) {
                verify = false;
            }
            cursor.close();
        }
        database.close();
        return verify;
    }
    private boolean connected() {

        boolean hasConnect = false;

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            hasConnect = true;
        }
        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            hasConnect = true;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            hasConnect = true;
        }

        return hasConnect;
    }
    @SuppressLint("Range")
    private List<String> logCursor(String table) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase database = this.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
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
    private void cancelOrder(String value) {

        String url = baseUrl + "/" + api + "/android/webordersCancel/" + value;
        Call<Status> call = ApiClient.getApiService().cancelOrder(url);
        Log.d("TAG", "cancelOrderWithDifferentValue cancelOrderUrl: " + url);
        call.enqueue(new Callback<Status>() {
            @Override
            public void onResponse(Call<Status> call, Response<Status> response) {
                if (response.isSuccessful()) {
                    Status status = response.body();
                    if (status != null) {
                        String result =  String.valueOf(status.getResponse());
                        text_status.setText(result);

                        if (pay_method.equals("google_payment")) {
                            String orderId = order_id;
                            String comment = getString(R.string.fondy_revers_message) + messageFondy;

                            Log.d("TAG1", "onResponse: orderId" + orderId);
                            Log.d("TAG1", "onResponse: comment " + comment);
                            Log.d("TAG1", "onResponse: amount " + amount);

                            getRevers(orderId, comment, amount);
                        }

                    } else {
                        text_status.setText(R.string.verify_internet);
                    }
                } else {
                    // Обработка неуспешного ответа
                    text_status.setText(R.string.verify_internet);
                }
            }

            @Override
            public void onFailure(Call<Status> call, Throwable t) {
                // Обработка ошибок сети или других ошибок
                String errorMessage = t.getMessage();
                t.printStackTrace();
                Log.d("TAG", "onFailure: " + errorMessage);
                text_status.setText(R.string.verify_internet);
            }
        });
    }
    private void cancelOrderWithDifferentValue(String value) {

        String url = baseUrl + "/" + api + "/android/webordersCancel/" + value;
        Call<Status> call = ApiClient.getApiService().cancelOrder(url);
        Log.d("TAG", "cancelOrderWithDifferentValue cancelOrderUrl: " + url);
        call.enqueue(new Callback<Status>() {
            @Override
            public void onResponse(Call<Status> call, Response<Status> response) {
                if (response.isSuccessful()) {
                    Status status = response.body();
                    if (status != null) {
                        String result =  String.valueOf(status.getResponse());
                        text_status.setText(result);
                    } else {
                        text_status.setText(R.string.verify_internet);
                    }
                } else {
                    // Обработка неуспешного ответа
                    text_status.setText(R.string.verify_internet);
                }
            }

            @Override
            public void onFailure(Call<Status> call, Throwable t) {
                // Обработка ошибок сети или других ошибок
                String errorMessage = t.getMessage();
                t.printStackTrace();
                Log.d("TAG", "onFailure: " + errorMessage);
                text_status.setText(R.string.verify_internet);
            }
        });
    }
    private void getUrlToPayment(String order_id, String orderDescription, String amount) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pay.fondy.eu/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        String merchantPassword = getString(R.string.fondy_key_storage);

        RequestData paymentRequest = new RequestData(
                order_id,
                orderDescription,
                amount,
                MainActivity.MERCHANT_ID,
                merchantPassword
        );


        StatusRequestPay statusRequest = new StatusRequestPay(paymentRequest);
        Log.d("TAG1", "getUrlToPayment: " + statusRequest.toString());

        Call<ApiResponsePay<SuccessResponseDataPay>> call = paymentApi.makePayment(statusRequest);

        call.enqueue(new Callback<ApiResponsePay<SuccessResponseDataPay>>() {

            @Override
            public void onResponse(@NonNull Call<ApiResponsePay<SuccessResponseDataPay>> call, Response<ApiResponsePay<SuccessResponseDataPay>> response) {
                Log.d("TAG1", "onResponse: 1111" + response.code());
                if (response.isSuccessful()) {
                    ApiResponsePay<SuccessResponseDataPay> apiResponse = response.body();

                    Log.d("TAG1", "onResponse: " +  new Gson().toJson(apiResponse));
                    try {
                        SuccessResponseDataPay responseBody = response.body().getResponse();;

                        // Теперь у вас есть объект ResponseBodyRev для обработки
                        if (responseBody != null) {
                            String responseStatus = responseBody.getResponseStatus();
                            String checkoutUrl = responseBody.getCheckoutUrl();
                            if ("success".equals(responseStatus)) {
                                // Обработка успешного ответа
                                Intent paymentIntent = new Intent(FinishActivity.this, FondyPaymentActivity.class);
                                paymentIntent.putExtra("checkoutUrl", checkoutUrl);
                                startActivity(paymentIntent);
                            } else if ("failure".equals(responseStatus)) {
                                // Обработка ответа об ошибке
                                String errorResponseMessage = responseBody.getErrorMessage();
                                String errorResponseCode = responseBody.getErrorCode();
                                Log.d("TAG1", "onResponse: errorResponseMessage " + errorResponseMessage);
                                Log.d("TAG1", "onResponse: errorResponseCode" + errorResponseCode);
                                // Отобразить сообщение об ошибке пользователю
                            } else {
                                // Обработка других возможных статусов ответа
                            }
                        } else {
                            // Обработка пустого тела ответа
                        }
                    } catch (JsonSyntaxException e) {
                        // Возникла ошибка при разборе JSON, возможно, сервер вернул неправильный формат ответа
                        Log.e("TAG1", "Error parsing JSON response: " + e.getMessage());
                    }
                } else {
                    // Обработка ошибки
                    Log.d("TAG1", "onFailure: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponsePay<SuccessResponseDataPay>> call, Throwable t) {
                Log.d("TAG1", "onFailure1111: " + t.toString());
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
    private void statusOrderWithDifferentValue(String value) {
        String url = baseUrl + "/" + api + "/android/historyUIDStatus/" + value;

        Call<OrderResponse> call = ApiClient.getApiService().statusOrder(url);
        Log.d("TAG", "cancelOrderWithDifferentValue cancelOrderUrl: " + url);

        // Выполняем запрос асинхронно
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful()) {
                    // Получаем объект OrderResponse из успешного ответа
                    OrderResponse orderResponse = response.body();

                    // Далее вы можете использовать полученные данные из orderResponse
                    // например:
                    assert orderResponse != null;
                    String executionStatus = orderResponse.getExecutionStatus();
                    String orderCarInfo = orderResponse.getOrderCarInfo();
                    String driverPhone = orderResponse.getDriverPhone();
                    String requiredTime = orderResponse.getRequiredTime();
                    if (requiredTime != null && !requiredTime.isEmpty()) {
                        requiredTime = formatDate (orderResponse.getRequiredTime());
                    }


                    String message;
                    // Обработка различных вариантов executionStatus
                    switch (executionStatus) {
                        case "WaitingCarSearch":
                            message = getString(R.string.ex_st_1);
                            break;
                        case "SearchesForCar":
                            message = getString(R.string.ex_st_0);
                            break;
                        case "CarFound":
                            // Формируем сообщение с учетом возможных пустых значений переменных
                            StringBuilder messageBuilder = new StringBuilder(getString(R.string.ex_st_2));

                            if (orderCarInfo != null && !orderCarInfo.isEmpty()) {
                                messageBuilder.append(getString(R.string.ex_st_3)).append(orderCarInfo);
                            }

                            if (driverPhone != null && !driverPhone.isEmpty()) {
                                messageBuilder.append(getString(R.string.ex_st_4)).append(driverPhone);
                            }

                            if (requiredTime != null && !requiredTime.isEmpty()) {
                                messageBuilder.append(getString(R.string.ex_st_5)).append(requiredTime);
                            }

                            message = messageBuilder.toString();
                            break;
                        default:
                            message = getString(R.string.def_status);
                            break;
                    }

                    text_status.setText(message);

                } else {
                    text_status.setText(getString(R.string.def_status));
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                text_status.setText(getString(R.string.def_status));
            }
        });
    }

    private String formatDate (String requiredTime) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        // Формат для вывода в украинской локализации
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", new Locale("uk", "UA"));
        // Преобразуем строку в объект Date
        Date date = null;
        try {
            date = inputFormat.parse(requiredTime);
        } catch (ParseException e) {
            Log.d("TAG", "onCreate:" + new RuntimeException(e));
        }

        // Форматируем дату и время в украинском формате
        return outputFormat.format(date);

    }

    public class VerifyUserTask extends AsyncTask<Void, Void, Map<String, String>> {
        private Exception exception;
        @Override
        protected Map<String, String> doInBackground(Void... voids) {
            String userEmail = logCursor(MainActivity.TABLE_USER_INFO).get(3);

            String url = "https://m.easy-order-taxi.site/android/verifyBlackListUser/" + userEmail + "/" + "com.taxieasyua.back4app";
            try {
                return CostJSONParser.sendURL(url);
            } catch (Exception e) {
                exception = e;
                return null;
            }

        }

        @Override
        protected void onPostExecute(Map<String, String> sendUrlMap) {
            String message = sendUrlMap.get("message");
            ContentValues cv = new ContentValues();
            SQLiteDatabase database = openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
            if (message != null) {

                if (message.equals("В черном списке")) {

                    cv.put("verifyOrder", "0");
                    database.update(MainActivity.TABLE_USER_INFO, cv, "id = ?", new String[]{"1"});
                }
            }
            database.close();
        }
    }

}
