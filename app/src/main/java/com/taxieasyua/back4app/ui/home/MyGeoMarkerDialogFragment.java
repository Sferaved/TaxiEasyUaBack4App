package com.taxieasyua.back4app.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.cities.Cherkasy.Cherkasy;
import com.taxieasyua.back4app.cities.Dnipro.Dnipro;
import com.taxieasyua.back4app.cities.Kyiv.KyivCity;
import com.taxieasyua.back4app.cities.Odessa.Odessa;
import com.taxieasyua.back4app.cities.Odessa.OdessaTest;
import com.taxieasyua.back4app.cities.Zaporizhzhia.Zaporizhzhia;
import com.taxieasyua.back4app.ui.finish.FinishActivity;
import com.taxieasyua.back4app.ui.fondy.payment.ApiResponsePay;
import com.taxieasyua.back4app.ui.fondy.payment.FondyPaymentActivity;
import com.taxieasyua.back4app.ui.fondy.payment.MyBottomSheetCardPayment;
import com.taxieasyua.back4app.ui.fondy.payment.PaymentApi;
import com.taxieasyua.back4app.ui.fondy.payment.RequestData;
import com.taxieasyua.back4app.ui.fondy.payment.StatusRequestPay;
import com.taxieasyua.back4app.ui.fondy.payment.SuccessResponseDataPay;
import com.taxieasyua.back4app.ui.fondy.payment.UniqueNumberGenerator;
import com.taxieasyua.back4app.ui.fondy.token_pay.ApiResponseToken;
import com.taxieasyua.back4app.ui.fondy.token_pay.PaymentApiToken;
import com.taxieasyua.back4app.ui.fondy.token_pay.RequestDataToken;
import com.taxieasyua.back4app.ui.fondy.token_pay.StatusRequestToken;
import com.taxieasyua.back4app.ui.fondy.token_pay.SuccessResponseDataToken;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;
import com.taxieasyua.back4app.ui.maps.ToJSONParser;
import com.taxieasyua.back4app.ui.mono.MonoApi;
import com.taxieasyua.back4app.ui.mono.payment.RequestPayMono;
import com.taxieasyua.back4app.ui.mono.payment.ResponsePayMono;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;
import com.taxieasyua.back4app.ui.payment_system.PayApi;
import com.taxieasyua.back4app.ui.payment_system.ResponsePaySystem;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MyGeoMarkerDialogFragment extends BottomSheetDialogFragment {
    public TextView geoText;
    public static AppCompatButton button, btn_minus, btn_plus, btnOrder, buttonBonus;
    public String[] arrayStreet;
    private static String api;
    long firstCost;

    @SuppressLint("StaticFieldLeak")
    public static TextView text_view_cost, textViewTo;
    @SuppressLint("StaticFieldLeak")
    public static EditText to_number;

    public static String numberFlagTo;

    public static long cost;
    public static long addCost;
    public static String to;
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar progressBar;
    private String TAG = "TAG";
    private String pay_method;
    private String messageFondy;
    public static String urlOrder;
    public static long firstCostForMin;
    private long discount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.geo_marker_layout, container, false);
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        final int initialMarginBottom = 0;

        final View decorView = requireActivity().getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            decorView.getWindowVisibleDisplayFrame(rect);
            int screenHeight = decorView.getHeight();
            int keypadHeight = screenHeight - rect.bottom;

            ConstraintLayout myLinearLayout = view.findViewById(R.id.constraint); // Замените на ваш ID представления
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) myLinearLayout.getLayoutParams();

            if (keypadHeight > screenHeight * 0.15) {
                // Клавиатура отображается, установите отступ в зависимости от размера клавиатуры
                layoutParams.bottomMargin = keypadHeight + initialMarginBottom;
            } else {
                // Клавиатура скрыта, установите изначальный отступ
                layoutParams.bottomMargin = initialMarginBottom;
            }

            myLinearLayout.setLayoutParams(layoutParams);
        });

        List<String> stringList = logCursor(MainActivity.CITY_INFO, requireActivity());
        api =  stringList.get(2);
        switch (stringList.get(1)){
            case "Dnipropetrovsk Oblast":
                arrayStreet = Dnipro.arrayStreet();
                break;
            case "Odessa":
                arrayStreet = Odessa.arrayStreet();
                break;
            case "Zaporizhzhia":
                arrayStreet = Zaporizhzhia.arrayStreet();
                break;
            case "Cherkasy Oblast":
                arrayStreet = Cherkasy.arrayStreet();break;
            case "OdessaTest":
                arrayStreet = OdessaTest.arrayStreet();
                break;
            default:
                arrayStreet = KyivCity.arrayStreet();
                break;
        }

        addCost = 0;
        updateAddCost(String.valueOf(addCost));

        numberFlagTo = "2";
        progressBar = view.findViewById(R.id.progress_bar);
        geoText = view.findViewById(R.id.textGeo);
        geoText.setText(OpenStreetMapActivity.FromAdressString);

        text_view_cost = view.findViewById(R.id.text_view_cost);
        text_view_cost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Вызывается перед изменением текста
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Вызывается во время изменения текста
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Вызывается после изменения текста
                String newText = editable.toString();
                // Здесь вы можете обработать новый текст
                firstCost = Long.parseLong(newText);
            }
        });

        textViewTo = view.findViewById(R.id.text_to);
        textViewTo.setText(OpenStreetMapActivity.ToAdressString);
        int inputTypeTo = textViewTo.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        textViewTo.setInputType(inputTypeTo);

        btn_minus = view.findViewById(R.id.btn_minus);
        btn_plus = view.findViewById(R.id.btn_plus);
        btnOrder = view.findViewById(R.id.btnOrder);


        button = view.findViewById(R.id.change);
        button.setOnClickListener(v -> {
            OpenStreetMapActivity.progressBar.setVisibility(View.VISIBLE);
            geoText.setText("");
            startActivity(new Intent(requireActivity(), OpenStreetMapActivity.class));
        });


        AppCompatButton buttonAddServicesView =  view.findViewById(R.id.btnAdd);
        buttonAddServicesView.setOnClickListener(v -> {
            MyBottomSheetGeoMarkerFragment bottomSheetDialogFragment = new MyBottomSheetGeoMarkerFragment();
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        });

        btnOrder.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                OpenStreetMapActivity.progressBar.setVisibility(View.VISIBLE);
                List<String> stringList1 = logCursor(MainActivity.CITY_INFO, requireActivity());
                pay_method =  logCursor(MainActivity.TABLE_SETTINGS_INFO, requireActivity()).get(4);
                if(pay_method.equals("card_payment")){
                    pay_method = pay_system();
                }

                switch (stringList1.get(1)) {
                    case "Kyiv City":
                    case "Dnipropetrovsk Oblast":
                    case "Odessa":
                    case "Zaporizhzhia":
                    case "Cherkasy Oblast":
                        break;
                    case "OdessaTest":
                        List<String> stringListInfo = logCursor(MainActivity.TABLE_SETTINGS_INFO, requireActivity());
                        String bonusPayment =  stringListInfo.get(4);
                        if(bonusPayment.equals("bonus_payment")) {
                            String bonus = logCursor(MainActivity.TABLE_USER_INFO, requireActivity()).get(5);
                            if(Long.parseLong(bonus) < cost * 100 ) {
                                paymentType("nal_payment");
                            }
                        }
                        break;
                }
                orderRout();
                if (verifyPhone(requireActivity())) {
                    orderFinished();


                }
            }
        });
        buttonBonus = view.findViewById(R.id.btnBonus);
        startCost();
        OpenStreetMapActivity.progressBar.setVisibility(View.INVISIBLE);

        buttonBonus.setOnClickListener(v -> {
            MyBottomSheetBonusFragment bottomSheetDialogFragment = new MyBottomSheetBonusFragment(Long.parseLong(text_view_cost.getText().toString()), "marker", api, text_view_cost, "GeoMarker");
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        });
        return view;
    }

    private String baseUrl = "https://m.easy-order-taxi.site";
    private String pay_system() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PayApi apiService = retrofit.create(PayApi.class);
        Call<ResponsePaySystem> call = apiService.getPaySystem();
        call.enqueue(new Callback<ResponsePaySystem>() {
            @Override
            public void onResponse(@NonNull Call<ResponsePaySystem> call, @NonNull Response<ResponsePaySystem> response) {
                if (response.isSuccessful()) {
                    // Обработка успешного ответа
                    ResponsePaySystem responsePaySystem = response.body();
                    assert responsePaySystem != null;
                    String paymentCode = responsePaySystem.getPay_system();
                    String paymentCodeNew = "fondy";

                    switch (paymentCode) {
                        case "fondy":
                            paymentCodeNew = "fondy_payment";
                            break;
                        case "mono":
                            paymentCodeNew = "mono_payment";
                            break;
                    }

                    ContentValues cv = new ContentValues();
                    cv.put("bonusPayment", paymentCodeNew);
                    // обновляем по id
                    SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
                    database.update(MainActivity.TABLE_SETTINGS_INFO, cv, "id = ?",
                            new String[] { "1" });
                    database.close();

                } else {
                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());

                }
            }

            @Override
            public void onFailure(Call<ResponsePaySystem> call, Throwable t) {
                if (isAdded()) {
                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            }
        });
        return logCursor(MainActivity.TABLE_SETTINGS_INFO, requireActivity()).get(4);
    }

    private void getUrlToPaymentMono(int amount, String reference, String comment) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.monobank.ua/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MonoApi monoApi = retrofit.create(MonoApi.class);

        RequestPayMono paymentRequest = new RequestPayMono(
                amount,
                reference,
                comment
        );

        Log.d("TAG1", "getUrlToPayment: " + paymentRequest.toString());

        String token = getResources().getString(R.string.mono_key_storage); // Получение токена из ресурсов
        Call<ResponsePayMono> call = monoApi.invoiceCreate(token, paymentRequest);

        call.enqueue(new Callback<ResponsePayMono>() {

            @Override
            public void onResponse(@NonNull Call<ResponsePayMono> call, Response<ResponsePayMono> response) {
                Log.d("TAG1", "onResponse: 1111" + response.code());
                if (response.isSuccessful()) {
                    ResponsePayMono apiResponse = response.body();

                    Log.d("TAG1", "onResponse: " +  new Gson().toJson(apiResponse));
                    try {
                        String pageUrl = response.body().getPageUrl();;
                        MainActivity.invoiceId = response.body().getInvoiceId();;

                        // Теперь у вас есть объект ResponseBodyRev для обработки
                        if (pageUrl != null) {

                            // Обработка успешного ответа

                            MyBottomSheetCardPayment bottomSheetDialogFragment = new MyBottomSheetCardPayment(
                                    pageUrl,
                                    text_view_cost.getText().toString(),
                                    "marker",
                                    urlOrder
                            );
                            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());

                        } else {
                            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.pay_failure));
                            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());

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
            public void onFailure(Call<ResponsePayMono> call, Throwable t) {
                Log.d("TAG1", "onFailure1111: " + t.toString());
            }


        });
    }


    private void paymentByToken(
            String order_id,
            String orderDescription,
            String amount,
            String rectoken
    ) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pay.fondy.eu/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PaymentApiToken paymentApi = retrofit.create(PaymentApiToken.class);
        String merchantPassword = getString(R.string.fondy_key_storage);
        List<String> stringList = logCursor(MainActivity.TABLE_USER_INFO, Objects.requireNonNull(requireActivity()));
        String email = stringList.get(3);

        RequestDataToken paymentRequest = new RequestDataToken(
                order_id,
                orderDescription,
                amount,
                MainActivity.MERCHANT_ID,
                merchantPassword,
                rectoken,
                email
        );


        StatusRequestToken statusRequest = new StatusRequestToken(paymentRequest);
        Log.d("TAG1", "getUrlToPayment: " + statusRequest.toString());

        Call<ApiResponseToken<SuccessResponseDataToken>> call = paymentApi.makePayment(statusRequest);

        call.enqueue(new Callback<ApiResponseToken<SuccessResponseDataToken>>() {

            @Override
            public void onResponse(@NonNull Call<ApiResponseToken<SuccessResponseDataToken>> call, Response<ApiResponseToken<SuccessResponseDataToken>> response) {
                Log.d("TAG1", "onResponse: 1111" + response.code());
                if (response.isSuccessful()) {
                    ApiResponseToken<SuccessResponseDataToken> apiResponse = response.body();

                    Log.d("TAG1", "onResponse: " +  new Gson().toJson(apiResponse));
                    try {
                        SuccessResponseDataToken responseBody = response.body().getResponse();;

                        // Теперь у вас есть объект ResponseBodyRev для обработки
                        if (responseBody != null) {
                            Log.d(TAG, "JSON Response: " + new Gson().toJson(apiResponse));
                            String orderStatus = responseBody.getOrderStatus();
                            if ("approved".equals(orderStatus)) {
                                // Обработка успешного ответа
                                orderFinished();
                            } else {
                                // Обработка ответа об ошибке
                                String errorResponseMessage = responseBody.getErrorMessage();
                                String errorResponseCode = responseBody.getErrorCode();
                                Log.d("TAG1", "onResponse: errorResponseMessage " + errorResponseMessage);
                                Log.d("TAG1", "onResponse: errorResponseCode" + errorResponseCode);
                                MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.pay_failure));
                                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                                // Отобразить сообщение об ошибке пользователю
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
            public void onFailure(Call<ApiResponseToken<SuccessResponseDataToken>> call, Throwable t) {
                Log.d("TAG1", "onFailure1111: " + t.toString());
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
        String email = logCursor(MainActivity.TABLE_USER_INFO, Objects.requireNonNull(requireActivity())).get(3);

        RequestData paymentRequest = new RequestData(
                order_id,
                orderDescription,
                amount,
                MainActivity.MERCHANT_ID,
                merchantPassword,
                email
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
                        assert response.body() != null;
                        SuccessResponseDataPay responseBody = response.body().getResponse();;

                        // Теперь у вас есть объект ResponseBodyRev для обработки
                        if (responseBody != null) {
                            String responseStatus = responseBody.getResponseStatus();
                            String checkoutUrl = responseBody.getCheckoutUrl();
                            if ("success".equals(responseStatus)) {
                                // Обработка успешного ответа

                                MyBottomSheetCardPayment bottomSheetDialogFragment = new MyBottomSheetCardPayment(
                                        checkoutUrl,
                                        text_view_cost.getText().toString(),
                                        "marker",
                                        urlOrder
                                );
                                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());

//
//                                Intent paymentIntent = new Intent(requireActivity(), FondyPaymentActivity.class);
//                                paymentIntent.putExtra("checkoutUrl", checkoutUrl);
//                                paymentIntent.putExtra("urlOrder", urlOrder);
//                                paymentIntent.putExtra("orderCost", text_view_cost.getText().toString());
//                                paymentIntent.putExtra("fragment_key", "geo");
//                                startActivity(paymentIntent);
                            } else if ("failure".equals(responseStatus)) {
                                // Обработка ответа об ошибке
                                String errorResponseMessage = responseBody.getErrorMessage();
                                String errorResponseCode = responseBody.getErrorCode();
                                Log.d("TAG1", "onResponse: errorResponseMessage " + errorResponseMessage);
                                Log.d("TAG1", "onResponse: errorResponseCode" + errorResponseCode);
                                MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.pay_failure));
                                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
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

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void orderRout() {
        if(!verifyOrder(requireContext())) {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.black_list_message));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            return;
        }
        urlOrder = getTaxiUrlSearchMarkers("orderSearchMarkers", requireActivity());
        if (!verifyPhone(requireActivity())) {
            getPhoneNumber();
        }
        if (!verifyPhone(requireActivity())) {
            bottomSheetDialogFragment = new MyPhoneDialogFragment("marker", text_view_cost.getText().toString());
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            OpenStreetMapActivity.progressBar.setVisibility(View.INVISIBLE);
        }

    }
    private void orderFinished() {
        try {
            Map<String, String> sendUrlMap = ToJSONParser.sendURL(urlOrder);
            Log.d("TAG", "Map sendUrlMap = ToJSONParser.sendURL(urlOrder); " + sendUrlMap);

            String orderWeb = sendUrlMap.get("order_cost");

            assert orderWeb != null;
            if (!orderWeb.equals("0")) {
                String to_name;
                if (Objects.equals(sendUrlMap.get("routefrom"), sendUrlMap.get("routeto"))) {
                    to_name = getString(R.string.on_city_tv);
                    if (!Objects.requireNonNull(sendUrlMap.get("lat")).equals("0")) {
                        insertRecordsOrders(
                                sendUrlMap.get("routefrom"), sendUrlMap.get("routefrom"),
                                sendUrlMap.get("routefromnumber"), sendUrlMap.get("routefromnumber"),
                                Double.toString(OpenStreetMapActivity.startLat), Double.toString(OpenStreetMapActivity.startLan),
                                Double.toString(OpenStreetMapActivity.startLat), Double.toString(OpenStreetMapActivity.startLan),
                                requireActivity()
                        );
                    }
                } else {
                    if(Objects.equals(sendUrlMap.get("routeto"), "Точка на карте")) {
                        to_name = requireActivity().getString(R.string.end_point_marker);
                    } else {
                        to_name = sendUrlMap.get("routeto") + " " + sendUrlMap.get("to_number");
                    }

                    if (!Objects.requireNonNull(sendUrlMap.get("lat")).equals("0")) {
                        insertRecordsOrders(
                                sendUrlMap.get("routefrom"), to_name,
                                sendUrlMap.get("routefromnumber"), sendUrlMap.get("to_number"),
                                Double.toString(OpenStreetMapActivity.startLat), Double.toString(OpenStreetMapActivity.startLan),
                                sendUrlMap.get("lat"), sendUrlMap.get("lng"), requireActivity()
                        );
                    }
                }
                String messageResult = getString(R.string.thanks_message) +
                        OpenStreetMapActivity.FromAdressString + " " + getString(R.string.to_message) +
                        to_name + "." +
                        getString(R.string.call_of_order) + orderWeb + getString(R.string.UAH);
                String messageFondy = getString(R.string.fondy_message) + " " +
                        OpenStreetMapActivity.FromAdressString + " " + getString(R.string.to_message) +
                        to_name + ".";

                Intent intent = new Intent(requireActivity(), FinishActivity.class);
                intent.putExtra("messageResult_key", messageResult);
                intent.putExtra("messageFondy_key", messageFondy);
                intent.putExtra("messageCost_key", orderWeb);
                intent.putExtra("sendUrlMap", new HashMap<>(sendUrlMap));
                intent.putExtra("UID_key", Objects.requireNonNull(sendUrlMap.get("dispatching_order_uid")));
                startActivity(intent);
            } else {

                MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(sendUrlMap.get("message"));
                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                OpenStreetMapActivity.progressBar.setVisibility(View.INVISIBLE);
            }


        } catch (MalformedURLException ignored) {

        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        OpenStreetMapActivity.progressBar.setVisibility(View.INVISIBLE);
    }

    private void updateAddCost(String addCost) {
        ContentValues cv = new ContentValues();
        Log.d(TAG, "updateAddCost: addCost" + addCost);
        cv.put("addCost", addCost);

        // обновляем по id
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        database.update(MainActivity.TABLE_SETTINGS_INFO, cv, "id = ?",
                new String[] { "1" });
        database.close();
    }

    private void startCost() {
        String urlCost = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            List<String> settings = new ArrayList<>();
            settings.add(String.valueOf(OpenStreetMapActivity.startLat));
            settings.add(String.valueOf(OpenStreetMapActivity.startLan));
            settings.add(String.valueOf(OpenStreetMapActivity.finishLat));
            settings.add(String.valueOf(OpenStreetMapActivity.finishLan));

            updateRoutMarker(settings);
            urlCost = getTaxiUrlSearchMarkers( "costSearchMarkers", requireActivity());
        }

        Map<String, String> sendUrlMapCost;
        try {
            sendUrlMapCost = CostJSONParser.sendURL(urlCost);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        OpenStreetMapActivity.progressBar.setVisibility(View.INVISIBLE);
        String message = sendUrlMapCost.get("message");
        String orderCost = sendUrlMapCost.get("order_cost");
        Log.d("TAG", "startCost: orderCost " + orderCost);

        assert orderCost != null;
        if (orderCost.equals("0")) {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        }
        if (!orderCost.equals("0")) {

            String discountTextM = logCursor(MainActivity.TABLE_SETTINGS_INFO, getContext()).get(3);
            Log.d(TAG, "startCost: discountText" + logCursor(MainActivity.TABLE_SETTINGS_INFO, getContext()).toString());
            int discountInt = Integer.parseInt(discountTextM);
            firstCost = Long.parseLong(orderCost);
            discount = firstCost * discountInt / 100;

            firstCost = firstCost + discount;

            text_view_cost.setText(String.valueOf(firstCost));

            firstCostForMin = firstCost;
            long MIN_COST_VALUE = (long) (firstCost*0.6);

            btn_minus.setOnClickListener(v -> {
                List<String> stringListInfo = logCursor(MainActivity.TABLE_SETTINGS_INFO, requireActivity());
                addCost = Long.parseLong(stringListInfo.get(5));
                firstCost -= 5;
                addCost -= 5;
                if (firstCost <= MIN_COST_VALUE) {
                    firstCost = MIN_COST_VALUE;
                    addCost = MIN_COST_VALUE - firstCostForMin;
                }
                updateAddCost(String.valueOf(addCost));
                Log.d("TAG", "startCost: addCost " + addCost);
                text_view_cost.setText(String.valueOf(firstCost));
            });

            btn_plus.setOnClickListener(v -> {
                List<String> stringListInfo = logCursor(MainActivity.TABLE_SETTINGS_INFO, requireActivity());
                addCost = Long.parseLong(stringListInfo.get(5));
                firstCost += 5;
                addCost += 5;
                updateAddCost(String.valueOf(addCost));
                Log.d("TAG", "startCost: addCost " + addCost);
                text_view_cost.setText(String.valueOf(firstCost));
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private String getTaxiUrlSearchMarkers(String urlAPI, Context context) {

        String query = "SELECT * FROM " + MainActivity.ROUT_MARKER + " LIMIT 1";
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();

        // Получите значения полей из первой записи

        double originLatitude = cursor.getDouble(cursor.getColumnIndex("startLat"));
        double originLongitude = cursor.getDouble(cursor.getColumnIndex("startLan"));
        double toLatitude = cursor.getDouble(cursor.getColumnIndex("to_lat"));
        double toLongitude = cursor.getDouble(cursor.getColumnIndex("to_lng"));
        // Origin of route
        String str_origin = originLatitude + "/" + originLongitude;

        // Destination of route
        String str_dest = toLatitude + "/" + toLongitude;

        cursor.close();

        List<String> stringList = logCursor(MainActivity.TABLE_ADD_SERVICE_INFO, context);
        String time = stringList.get(1);
        String comment = stringList.get(2);
        String date = stringList.get(3);


        List<String> stringListInfo = logCursor(MainActivity.TABLE_SETTINGS_INFO, context);
        String tarif =  stringListInfo.get(2);
        String bonusPayment =  stringListInfo.get(4);
        String addCost = stringListInfo.get(5);

        // Building the parameters to the web service

        String parameters = null;
        String phoneNumber = "no phone";
        String userEmail = logCursor(MainActivity.TABLE_USER_INFO, context).get(3);
        String displayName = logCursor(MainActivity.TABLE_USER_INFO, context).get(4);

        if(urlAPI.equals("costSearchMarkers")) {
            Cursor c = database.query(MainActivity.TABLE_USER_INFO, null, null, null, null, null, null);

            if (c.getCount() == 1) {
                phoneNumber = logCursor(MainActivity.TABLE_USER_INFO, context).get(2);
                c.close();
            }
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + bonusPayment;
        }
        if(urlAPI.equals("orderSearchMarkers")) {
            phoneNumber = logCursor(MainActivity.TABLE_USER_INFO, context).get(2);


            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + bonusPayment + "/" + addCost + "/" + time + "/" + comment + "/" + date;

            ContentValues cv = new ContentValues();

            cv.put("time", "no_time");
            cv.put("comment", "no_comment");
            cv.put("date", "no_date");

            // обновляем по id
            database.update(MainActivity.TABLE_ADD_SERVICE_INFO, cv, "id = ?",
                    new String[] { "1" });

        }

        // Building the url to the web service
        List<String> services = logCursor(MainActivity.TABLE_SERVICE_INFO, context);
        List<String> servicesChecked = new ArrayList<>();
        String result;
        boolean servicesVer = false;
        for (int i = 1; i < services.size()-1 ; i++) {
            if(services.get(i).equals("1")) {
                servicesVer = true;
                break;
            }
        }
        if(servicesVer) {
            for (int i = 0; i < OpenStreetMapActivity.arrayServiceCode().length; i++) {
                if(services.get(i+1).equals("1")) {
                    servicesChecked.add(OpenStreetMapActivity.arrayServiceCode()[i]);
                }
            }
            for (int i = 0; i < servicesChecked.size(); i++) {
                if(servicesChecked.get(i).equals("CHECK_OUT")) {
                    servicesChecked.set(i, "CHECK");
                }
            }
            result = String.join("*", servicesChecked);
            Log.d("TAG", "getTaxiUrlSearchGeo result:" + result + "/");
        } else {
            result = "no_extra_charge_codes";
        }

        String url = "https://m.easy-order-taxi.site/" + api + "/android/" + urlAPI + "/" + parameters + "/" + result;


        database.close();


        return url;

    }



    private boolean connected() {

        boolean hasConnect = false;

        ConnectivityManager cm = (ConnectivityManager) requireActivity().getSystemService(
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

    @Override
    public void onPause() {
        super.onPause();
        OpenStreetMapActivity.fab_open_marker.setVisibility(View.VISIBLE);
//        startActivity(new Intent(requireActivity(), OpenStreetMapActivity.class));
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
        assert c != null;
        c.close();
        database.close();
        return list;
    }


    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void order() {
        if(!verifyOrder(requireContext())) {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.black_list_message));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            return;
        }

        if (!verifyPhone(requireActivity())) {
            getPhoneNumber();
        }
        if (!verifyPhone(requireActivity())) {
            bottomSheetDialogFragment = new MyPhoneDialogFragment("marker", text_view_cost.getText().toString());
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            OpenStreetMapActivity.progressBar.setVisibility(View.INVISIBLE);
        }
        if(connected()) {
            if (verifyPhone(requireActivity())) {
                try {


                    String urlOrder = getTaxiUrlSearchMarkers("orderSearchMarkers", requireActivity());
                    Map<String, String> sendUrlMap = ToJSONParser.sendURL(urlOrder);
                    Log.d("TAG", "Map sendUrlMap = ToJSONParser.sendURL(urlOrder); " + sendUrlMap);

                    String orderWeb = sendUrlMap.get("order_cost");

                    assert orderWeb != null;
                    if (!orderWeb.equals("0")) {
                        String to_name;
                        if (Objects.equals(sendUrlMap.get("routefrom"), sendUrlMap.get("routeto"))) {
                            to_name = getString(R.string.on_city_tv);
                            if (!Objects.requireNonNull(sendUrlMap.get("lat")).equals("0")) {
                                insertRecordsOrders(
                                        sendUrlMap.get("routefrom"), sendUrlMap.get("routefrom"),
                                        sendUrlMap.get("routefromnumber"), sendUrlMap.get("routefromnumber"),
                                        Double.toString(OpenStreetMapActivity.startLat), Double.toString(OpenStreetMapActivity.startLan),
                                        Double.toString(OpenStreetMapActivity.startLat), Double.toString(OpenStreetMapActivity.startLan),
                                        requireActivity()
                                );
                            }
                        } else {
                            if(Objects.equals(sendUrlMap.get("routeto"), "Точка на карте")) {
                                to_name = requireActivity().getString(R.string.end_point_marker);
                            } else {
                                to_name = sendUrlMap.get("routeto") + " " + sendUrlMap.get("to_number");
                            }

                            if (!Objects.requireNonNull(sendUrlMap.get("lat")).equals("0")) {
                                insertRecordsOrders(
                                        sendUrlMap.get("routefrom"), to_name,
                                        sendUrlMap.get("routefromnumber"), sendUrlMap.get("to_number"),
                                        Double.toString(OpenStreetMapActivity.startLat), Double.toString(OpenStreetMapActivity.startLan),
                                        sendUrlMap.get("lat"), sendUrlMap.get("lng"), requireActivity()
                                );
                            }
                        }
                        String messageResult = getString(R.string.thanks_message) +
                                OpenStreetMapActivity.FromAdressString + " " + getString(R.string.to_message) +
                                to_name + "." +
                                getString(R.string.call_of_order) + orderWeb + getString(R.string.UAH);
                        String messageFondy = getString(R.string.fondy_message) + " " +
                                OpenStreetMapActivity.FromAdressString + " " + getString(R.string.to_message) +
                                to_name + ".";

                        Intent intent = new Intent(requireActivity(), FinishActivity.class);
                        intent.putExtra("messageResult_key", messageResult);
                        intent.putExtra("messageFondy_key", messageFondy);
                        intent.putExtra("messageCost_key", orderWeb);
                        intent.putExtra("sendUrlMap", new HashMap<>(sendUrlMap));
                        intent.putExtra("UID_key", Objects.requireNonNull(sendUrlMap.get("dispatching_order_uid")));
                        startActivity(intent);
                    } else {

                        MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(sendUrlMap.get("message"));
                        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                        OpenStreetMapActivity.progressBar.setVisibility(View.INVISIBLE);
                    }


                } catch (MalformedURLException ignored) {

                }


            }
        } else {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            OpenStreetMapActivity.progressBar.setVisibility(View.INVISIBLE);
        }
    }


    private boolean verifyOrder(Context context) {
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.query(MainActivity.TABLE_USER_INFO, null, null, null, null, null, null);

        boolean verify = true;
        if (cursor.getCount() == 1) {

            if (logCursor(MainActivity.TABLE_USER_INFO, context).get(1).equals("0")) {
                verify = false;
            }
            cursor.close();
        }
        database.close();
        return verify;
    }

    private boolean verifyPhone(Context context) {
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.query(MainActivity.TABLE_USER_INFO, null, null, null, null, null, null);
        boolean verify = true;
        if (cursor.getCount() == 1) {

            if (logCursor(MainActivity.TABLE_USER_INFO, context).get(2).equals("+380")) {
                verify = false;
            }
            cursor.close();
        }

        return verify;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getPhoneNumber () {
        String mPhoneNumber;
        TelephonyManager tMgr = (TelephonyManager) requireActivity().getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Manifest.permission.READ_PHONE_NUMBERS: " + ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_PHONE_NUMBERS));
            Log.d("TAG", "Manifest.permission.READ_PHONE_STATE: " + ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_PHONE_STATE));
            return;
        }
        mPhoneNumber = tMgr.getLine1Number();
        if(mPhoneNumber != null) {
            String PHONE_PATTERN = "((\\+?380)(\\d{9}))$";
            boolean val = Pattern.compile(PHONE_PATTERN).matcher(mPhoneNumber).matches();
            Log.d("TAG", "onClick No validate: " + val);
            if (!val) {
                Toast.makeText(requireActivity(), getString(R.string.format_phone) , Toast.LENGTH_SHORT).show();
                Log.d("TAG", "onClick:phoneNumber.getText().toString() " + mPhoneNumber);
            } else {
                updateRecordsUser(mPhoneNumber, requireActivity());
            }
        }

    }

    private void updateRecordsUser(String result, Context context) {
        ContentValues cv = new ContentValues();

        cv.put("phone_number", result);

        // обновляем по id
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        int updCount = database.update(MainActivity.TABLE_USER_INFO, cv, "id = ?",
                new String[] { "1" });
        Log.d("TAG", "updated rows count = " + updCount);
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
    private void updateRoutMarker(List<String> settings) {
        ContentValues cv = new ContentValues();

        cv.put("startLat",  Double.parseDouble(settings.get(0)));
        cv.put("startLan", Double.parseDouble(settings.get(1)));
        cv.put("to_lat", Double.parseDouble(settings.get(2)));
        cv.put("to_lng", Double.parseDouble(settings.get(3)));

        // обновляем по id
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        database.update(MainActivity.ROUT_MARKER, cv, "id = ?",
                new String[] { "1" });
        database.close();
    }

    private void paymentType(String paymentCode) {
        ContentValues cv = new ContentValues();
        cv.put("bonusPayment", paymentCode);
        // обновляем по id
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        database.update(MainActivity.TABLE_SETTINGS_INFO, cv, "id = ?",
                new String[] { "1" });
        database.close();
    }
    private MyPhoneDialogFragment bottomSheetDialogFragment;
    @Override
    public void onResume() {
        super.onResume();
        if(bottomSheetDialogFragment != null) {
            bottomSheetDialogFragment.dismiss();
        }

    }
}


