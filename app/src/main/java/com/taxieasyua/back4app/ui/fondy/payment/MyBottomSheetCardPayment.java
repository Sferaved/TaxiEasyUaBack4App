package com.taxieasyua.back4app.ui.fondy.payment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.card.CardFragment;
import com.taxieasyua.back4app.ui.finish.ErrorPayActivity;
import com.taxieasyua.back4app.ui.finish.FinishActivity;
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
import com.taxieasyua.back4app.ui.gallery.GalleryFragment;
import com.taxieasyua.back4app.ui.home.HomeFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetErrorFragment;
import com.taxieasyua.back4app.ui.home.MyGeoDialogFragment;
import com.taxieasyua.back4app.ui.home.MyGeoMarkerDialogFragment;
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


public class MyBottomSheetCardPayment extends BottomSheetDialogFragment {
    private WebView webView;
    private String TAG = "TAG";
    private String checkoutUrl;
    private String amount;
    private AppCompatButton btnOk;
    String email, fragment_key, urlOrder;
    public MyBottomSheetCardPayment(
            String checkoutUrl,
            String amount,
            String fragment_key,
            String urlOrder) {
        this.checkoutUrl = checkoutUrl;
        this.amount = amount;
        this.fragment_key = fragment_key;
        this.urlOrder = urlOrder;
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fondy_payment, container, false);
        webView = view.findViewById(R.id.webView);
        email = logCursor(MainActivity.TABLE_USER_INFO, requireActivity()).get(3);
        btnOk = view.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.order_id != null) {
                    getStatus();
                }
            }

        });
        // Настройка WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(Objects.requireNonNull(checkoutUrl));
        return view;
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

                                    try {
                                        orderGeoMarker();
                                    } catch (MalformedURLException ignored) {

                                    }
                                    break;
                            }
                        };
                    }
                } else {
                    // Обработка ошибки запроса
                    Log.d("TAG", "onResponse: Ошибка запроса, код " + response.code());

                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<SuccessfulResponseData>> call, Throwable t) {
                // Обработка ошибки сети или другие ошибки
                Log.d("TAG", "onFailure: Ошибка сети: " + t.getMessage());

                MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        switch (Objects.requireNonNull(fragment_key)){
            case "home":
                HomeFragment.progressBar.setVisibility(View.INVISIBLE);
                break;
            case "gallery":
                GalleryFragment.progressbar.setVisibility(View.INVISIBLE);
                break;
            case "geo":
                MyGeoDialogFragment.progressBar.setVisibility(View.INVISIBLE);
                break;
            case "marker":
                MyGeoMarkerDialogFragment.progressBar.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public void orderGeoMarker() throws MalformedURLException {

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
                            requireContext()
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
                            requireContext()
                    );
                }
            }
            String messageResult = getString(R.string.thanks_message) +
                    OpenStreetMapActivity.FromAdressString + " " + getString(R.string.to_message) +
                    to_name + "." +
                    getString(R.string.call_of_order) + orderWeb + getString(R.string.UAH);
            String messageFondy = getString(R.string.fondy_message);

            Intent intent = new Intent(requireContext(), FinishActivity.class);
            intent.putExtra("messageResult_key", messageResult);
            intent.putExtra("messageFondy_key", messageFondy);
            intent.putExtra("messageCost_key", orderWeb);
            intent.putExtra("sendUrlMap", new HashMap<>(sendUrlMap));
            intent.putExtra("UID_key", Objects.requireNonNull(sendUrlMap.get("dispatching_order_uid")));
            startActivity(intent);
        } else {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        }
    }

    private void orderHome() throws MalformedURLException {

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
                            requireContext()
                    );
                } else {
                    insertRecordsOrders(
                            from_name, to_name,
                            from_number, to_number,
                            sendUrlMap.get("from_lat"), sendUrlMap.get("from_lng"),
                            sendUrlMap.get("lat"), sendUrlMap.get("lng"),
                            requireContext()
                    );
                }
            }
            String messageFondy = getString(R.string.fondy_message);

            Intent intent = new Intent(requireContext(), FinishActivity.class);
            intent.putExtra("messageResult_key", messageResult);
            intent.putExtra("messageFondy_key", messageFondy);
            intent.putExtra("messageCost_key", orderWeb);
            intent.putExtra("sendUrlMap", new HashMap<>(sendUrlMap));
            intent.putExtra("UID_key", String.valueOf(sendUrlMap.get("dispatching_order_uid")));
            startActivity(intent);

        } else {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
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


    private void getCardToken() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://m.easy-order-taxi.site") // Замените на фактический URL вашего сервера
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Создайте сервис
        CallbackService service = retrofit.create(CallbackService.class);

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
                        SQLiteDatabase database =  requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
                        database.update(MainActivity.TABLE_USER_INFO, cv, "id = ?",
                                new String[] { "1" });
                        database.close();

                        Log.d(TAG, "onResponse: " + logCursor(MainActivity.TABLE_USER_INFO, requireActivity()));
                        Log.d(TAG, "onResponse: " + logCursor(MainActivity.TABLE_USER_INFO, requireActivity()).get(6));
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

    @SuppressLint("Range")
    public static List<String> logCursor(String table, Context context) {
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

