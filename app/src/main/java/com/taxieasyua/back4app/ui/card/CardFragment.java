package com.taxieasyua.back4app.ui.card;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.NetworkChangeReceiver;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.databinding.FragmentCardBinding;
import com.taxieasyua.back4app.ui.fondy.payment.ApiResponsePay;
import com.taxieasyua.back4app.ui.fondy.payment.PaymentApi;
import com.taxieasyua.back4app.ui.fondy.payment.RequestData;
import com.taxieasyua.back4app.ui.fondy.payment.StatusRequestPay;
import com.taxieasyua.back4app.ui.fondy.payment.SuccessResponseDataPay;
import com.taxieasyua.back4app.ui.fondy.payment.UniqueNumberGenerator;
import com.taxieasyua.back4app.ui.home.MyBottomSheetErrorFragment;
import com.taxieasyua.back4app.ui.mono.MonoApi;
import com.taxieasyua.back4app.ui.mono.cancel.RequestCancelMono;
import com.taxieasyua.back4app.ui.mono.cancel.ResponseCancelMono;
import com.taxieasyua.back4app.ui.payment_system.PayApi;
import com.taxieasyua.back4app.ui.payment_system.ResponsePaySystem;
import com.taxieasyua.back4app.utils.connect.NetworkUtils;

import java.io.IOException;
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

public class CardFragment extends Fragment {

    private @NonNull FragmentCardBinding binding;
    public static AppCompatButton btnCardLink;

    private NetworkChangeReceiver networkChangeReceiver;
    private String baseUrl = "https://m.easy-order-taxi.site";
    private String messageFondy;
    public static ProgressBar progressBar;
    private String TAG = "TAG_CARD";
    String email;
    String amount = "100";
    public static TextView textCard;

    public static ListView listView;
    public static String table;
    String pay_method;
    NavController navController;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            navController.navigate(R.id.nav_visicom);
        }
        binding = FragmentCardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        btnCardLink  = binding.btnCardLink;
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();


        List<String>  arrayList = logCursor(MainActivity.CITY_INFO, requireActivity());
        String MERCHANT_ID = arrayList.get(6);
        textCard = binding.textCard;
        listView = binding.listView;
        progressBar = binding.progressBar;
        Log.d(TAG, "onResume: " + MERCHANT_ID);
        if (MERCHANT_ID != null) {
            textCard.setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            networkChangeReceiver = new NetworkChangeReceiver();
            email = logCursor(MainActivity.TABLE_USER_INFO, Objects.requireNonNull(requireActivity())).get(3);

            paySystem(new PaySystemCallback() {
                @Override
                public void onPaySystemResult(String paymentCode) {
                    // Здесь вы можете использовать полученное значение paymentCode
                    pay_method = paymentCode;
                    btnCardLink.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            Log.d(TAG, "onClick: " + pay_method);
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                            if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                                navController.navigate(R.id.nav_visicom);
                            } else {
                                MainActivity.order_id = UniqueNumberGenerator.generateUniqueNumber(getActivity());
                                messageFondy = getString(R.string.fondy_message);

                                switch (pay_method) {
                                    case "fondy_payment":
                                        getUrlToPaymentFondy(MainActivity.order_id, messageFondy);
                                        break;
                                    case "mono_payment":
                                        getUrlToPaymentMono(MainActivity.order_id, messageFondy);
                                        break;
                                }
                                progressBar.setVisibility(View.GONE);

                            }
                        }
                    });

                    ArrayList<Map<String, String>> cardMaps = new ArrayList<>();

                    switch (pay_method) {
                        case "fondy_payment":
                            cardMaps = getCardMapsFromDatabase(MainActivity.TABLE_FONDY_CARDS);
                            table = MainActivity.TABLE_FONDY_CARDS;
                            break;
                        case "mono_payment":
                            cardMaps = getCardMapsFromDatabase(MainActivity.TABLE_MONO_CARDS);
                            table = MainActivity.TABLE_MONO_CARDS;
                            break;
                    }

                    if (cardMaps != null && !cardMaps.isEmpty()) {
                        CustomCardAdapter listAdapter = new CustomCardAdapter(requireActivity(), cardMaps, table);
                        listView.setAdapter(listAdapter);
                        progressBar.setVisibility(View.GONE);
                    } else {
                        textCard.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        textCard.setText(R.string.no_cards);
                        progressBar.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onPaySystemFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(errorMessage);
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            });

        } else {
            textCard.setVisibility(View.GONE);
            btnCardLink.setVisibility(View.GONE);
            MyBottomSheetErrorCardFragment bottomSheetDialogFragment = new MyBottomSheetErrorCardFragment(getString(R.string.city_no_cards));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());

        }

    }

    @SuppressLint("Range")
    private ArrayList<Map<String, String>> getCardMapsFromDatabase(String table) {
        ArrayList<Map<String, String>> cardMaps = new ArrayList<>();
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        // Выполните запрос к таблице TABLE_FONDY_CARDS и получите данные
        Cursor cursor = database.query(table, null, null, null, null, null, null);
        Log.d(TAG, "getCardMapsFromDatabase: card count: " + cursor.getCount());

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Map<String, String> cardMap = new HashMap<>();
                    cardMap.put("card_type", cursor.getString(cursor.getColumnIndex("card_type")));
                    cardMap.put("bank_name", cursor.getString(cursor.getColumnIndex("bank_name")));
                    cardMap.put("masked_card", cursor.getString(cursor.getColumnIndex("masked_card")));
                    cardMap.put("rectoken", cursor.getString(cursor.getColumnIndex("rectoken")));
                    cardMap.put("rectoken_check", cursor.getString(cursor.getColumnIndex("rectoken_check")));

                    cardMaps.add(cardMap);
                } while (cursor.moveToNext());
            }
            cursor.close();

        }
        database.close();

        return cardMaps;
    }


    private void getUrlToPaymentMono(String orderId, String messageFondy) {
    }
    private void getReversMono(
            String invoiceId,
            String extRef,
            int amount
    ) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.monobank.ua/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MonoApi monoApi = retrofit.create(MonoApi.class);

        RequestCancelMono paymentRequest = new RequestCancelMono(
                invoiceId,
                extRef,
                amount
        );
        Log.d("TAG1", "getRevers: " + paymentRequest.toString());

        String token = getResources().getString(R.string.mono_key_storage); // Получение токена из ресурсов
        Call<ResponseCancelMono> call = monoApi.invoiceCancel(token, paymentRequest);

        call.enqueue(new Callback<ResponseCancelMono>() {
            @Override
            public void onResponse(@NonNull Call<ResponseCancelMono> call, @NonNull Response<ResponseCancelMono> response) {

                if (response.isSuccessful()) {
                    ResponseCancelMono apiResponse = response.body();
                    Log.d("TAG2", "JSON Response: " + new Gson().toJson(apiResponse));
                    if (apiResponse != null) {
                        String responseData = apiResponse.getStatus();
                        Log.d("TAG2", "onResponse: " + responseData.toString());
                        // Обработка успешного ответа

                        switch (responseData) {
                            case "processing":
                                Log.d("TAG2", "onResponse: " + "заява на скасування знаходиться в обробці");
                                break;
                            case "success":
                                Log.d("TAG2", "onResponse: " + "заяву на скасування виконано успішно");
                                break;
                            case "failure":
                                Log.d("TAG2", "onResponse: " + "неуспішне скасування");
                                Log.d("TAG2", "onResponse: ErrCode: " + apiResponse.getErrCode());
                                Log.d("TAG2", "onResponse: ErrText: " + apiResponse.getErrText());
                                break;
                        }

                    }
                } else {
                    // Обработка ошибки запроса
                    Log.d("TAG2", "onResponse: Ошибка запроса, код " + response.code());
                    try {
                        assert response.errorBody() != null;
                        String errorBody = response.errorBody().string();
                        Log.d("TAG2", "onResponse: Тело ошибки: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseCancelMono> call, Throwable t) {
                // Обработка ошибки сети или другие ошибки
                Log.d("TAG2", "onFailure: Ошибка сети: " + t.getMessage());
            }
        });

    }
    private void paySystem(final PaySystemCallback callback) {
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

                    String paymentCodeNew = "fondy"; // Изначально устанавливаем значение

                    switch (paymentCode) {
                        case "fondy":
                            paymentCodeNew = "fondy_payment";
                            break;
                        case "mono":
                            paymentCodeNew = "mono_payment";
                            break;
                    }

                    // Вызываем обработчик, передавая полученное значение
                    if (getActivity() != null) {
                        // Fragment is attached to an activity, it's safe to call onPaySystemResult
                        callback.onPaySystemResult(paymentCodeNew);
                    }
                } else {
                    // Обработка ошибки
                    callback.onPaySystemFailure(getString(R.string.verify_internet));
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<ResponsePaySystem> call, @NonNull Throwable t) {
                // Проверяем, что фрагмент присоединен к контексту
                if (isAdded()) {
                    // Обработка ошибки
                    callback.onPaySystemFailure(getString(R.string.verify_internet));
                }
                progressBar.setVisibility(View.GONE);
            }

        });
    }

    // Интерфейс для обработки результата и ошибки
    public interface PaySystemCallback {
        void onPaySystemResult(String paymentCode);
        void onPaySystemFailure(String errorMessage);
    }



    private void getUrlToPaymentFondy(String order_id, String orderDescription) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pay.fondy.eu/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        List<String>  arrayList = logCursor(MainActivity.CITY_INFO, requireActivity());
        String MERCHANT_ID = arrayList.get(6);
        String merchantPassword = arrayList.get(7);

        RequestData paymentRequest = new RequestData(
                order_id,
                orderDescription,
                amount,
                MERCHANT_ID,
                merchantPassword,
                email
        );


        StatusRequestPay statusRequest = new StatusRequestPay(paymentRequest);
        Log.d(TAG, "getUrlToPaymentFondy: " + statusRequest.toString());

        Call<ApiResponsePay<SuccessResponseDataPay>> call = paymentApi.makePayment(statusRequest);

        call.enqueue(new Callback<ApiResponsePay<SuccessResponseDataPay>>() {

            @Override
            public void onResponse(@NonNull Call<ApiResponsePay<SuccessResponseDataPay>> call, Response<ApiResponsePay<SuccessResponseDataPay>> response) {
                Log.d(TAG, "onResponse: 1111" + response.code());
                if (response.isSuccessful()) {
                    ApiResponsePay<SuccessResponseDataPay> apiResponse = response.body();

                    Log.d(TAG, "onResponse: " +  new Gson().toJson(apiResponse));
                    try {
                        SuccessResponseDataPay responseBody = response.body().getResponse();;

                        // Теперь у вас есть объект ResponseBodyRev для обработки
                        if (responseBody != null) {
                            String responseStatus = responseBody.getResponseStatus();
                            String checkoutUrl = responseBody.getCheckoutUrl();
                            if ("success".equals(responseStatus)) {
                                // Обработка успешного ответа

                                MyBottomSheetCardVerification bottomSheetDialogFragment = new MyBottomSheetCardVerification(checkoutUrl, amount);
                                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());


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

                    }
                } else {
                    // Обработка ошибки
                    Log.d("TAG1", "onFailure: " + response.code());
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponsePay<SuccessResponseDataPay>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.d("TAG1", "onFailure1111: " + t.toString());
            }


        });
    }

    @SuppressLint("Range")
    public List<String> logCursor(String table, Context context) {
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}