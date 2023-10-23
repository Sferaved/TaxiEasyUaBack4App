package com.taxieasyua.back4app.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.finish.ApiClient;
import com.taxieasyua.back4app.ui.finish.FinishActivity;
import com.taxieasyua.back4app.ui.finish.Status;
import com.taxieasyua.back4app.ui.fondy.payment.ApiResponsePay;
import com.taxieasyua.back4app.ui.fondy.payment.FondyPaymentActivity;
import com.taxieasyua.back4app.ui.fondy.payment.PaymentApi;
import com.taxieasyua.back4app.ui.fondy.payment.RequestData;
import com.taxieasyua.back4app.ui.fondy.payment.StatusRequestPay;
import com.taxieasyua.back4app.ui.fondy.payment.SuccessResponseDataPay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MyBottomSheetFondyPayFragment extends BottomSheetDialogFragment {

    AppCompatButton btn_ok, btn_no;
    TextView messageTV, costTV;

    String order_id, orderDescription, amount, UID_key, api;

    public MyBottomSheetFondyPayFragment(String order_id, String orderDescription, String amount, String UID_key, String api) {
        this.order_id = order_id;
        this.orderDescription = orderDescription;
        this.amount = amount;
        this.UID_key = UID_key;
        this.api = api;
    }

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fondy_pay_layout, container, false);


        messageTV = view.findViewById(R.id.text_message);
        messageTV.setText(orderDescription);

        costTV = view.findViewById(R.id.text_view_cost);
        String originalAmount = amount; // Ваша исходная строка
        String trimmedAmount = originalAmount.substring(0, originalAmount.length() - 2);

        costTV.setText(getString(R.string.cost_of_road_fondy)+trimmedAmount + getString(R.string.UAH) );


        btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUrlToPayment(order_id, orderDescription, amount);
                dismiss();
            }
        });

        btn_no = view.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelOrder(UID_key);
                dismiss();
            }
        });
        return view;
    }
    private void getUrlToPayment(String order_id, String orderDescription, String amount) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pay.fondy.eu/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        String merchantPassword = getString(R.string.fondy_key_storage);
        String email = logCursor(MainActivity.TABLE_USER_INFO).get(3);
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
                        SuccessResponseDataPay responseBody = response.body().getResponse();;

                        // Теперь у вас есть объект ResponseBodyRev для обработки
                        if (responseBody != null) {
                            String responseStatus = responseBody.getResponseStatus();
                            String checkoutUrl = responseBody.getCheckoutUrl();
                            if ("success".equals(responseStatus)) {

                                // Обработка успешного ответа
                                handleSuccessResponse(checkoutUrl);
//                                FinishActivity.checkoutUrl = checkoutUrl;
//                                Intent paymentIntent = new Intent(getActivity(), FondyPaymentActivity.class);
//                                paymentIntent.putExtra("checkoutUrl", checkoutUrl);
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

    @SuppressLint("Range")
    private List<String> logCursor(String table) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
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

    private void handleSuccessResponse(String checkoutUrl) {

        // В этом методе вы можете использовать checkoutUrl
        Log.d("TAG1", "handleSuccessResponse: checkoutUrl) " + checkoutUrl);
        if (checkoutUrl != null) {
            Intent paymentIntent = new Intent(getActivity(), FondyPaymentActivity.class);
            paymentIntent.putExtra("checkoutUrl", checkoutUrl);
            startActivity(paymentIntent);
        }
    }
    String baseUrl = "https://m.easy-order-taxi.site";
    private void cancelOrder(String value) {

        String url = baseUrl + "/" + api + "/android/webordersCancel/" + value;
        Call<Status> call = ApiClient.getApiService().cancelOrder(url);
        Log.d("TAG", "cancelOrderWithDifferentValue cancelOrderUrl: " + url);
        call.enqueue(new Callback<Status>() {
            @Override
            public void onResponse(Call<Status> call, Response<Status> response) {
            }

            @Override
            public void onFailure(Call<Status> call, Throwable t) {
                // Обработка ошибок сети или других ошибок
                String errorMessage = t.getMessage();
                t.printStackTrace();
                Log.d("TAG", "onFailure: " + errorMessage);
                FinishActivity.text_status.setText(R.string.verify_internet);
            }
        });
    }
   }
