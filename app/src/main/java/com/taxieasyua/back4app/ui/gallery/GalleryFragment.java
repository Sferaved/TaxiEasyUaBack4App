package com.taxieasyua.back4app.ui.gallery;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.databinding.FragmentGalleryBinding;
import com.taxieasyua.back4app.ui.finish.FinishActivity;
import com.taxieasyua.back4app.ui.fondy.payment.ApiResponsePay;
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
import com.taxieasyua.back4app.ui.home.MyBottomSheetBonusFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetErrorFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetGalleryFragment;
import com.taxieasyua.back4app.ui.maps.ToJSONParser;
import com.taxieasyua.back4app.ui.mono.MonoApi;
import com.taxieasyua.back4app.ui.mono.payment.RequestPayMono;
import com.taxieasyua.back4app.ui.mono.payment.ResponsePayMono;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;

import org.json.JSONException;

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

public class GalleryFragment extends Fragment {

    private static final String TAG = "TAG";
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar progressbar;
    private FragmentGalleryBinding binding;
    private ListView listView;
    private String[] array;
    @SuppressLint("StaticFieldLeak")
    public static TextView textView, text_view_cost;
    String from_mes, to_mes;
    public static AppCompatButton del_but, btnRouts, btn_minus, btn_plus, btnAdd, buttonBonus;
    int selectedItem;
    String FromAddressString, ToAddressString;
    private long firstCost;
    public static long  addCost, cost;
    public static Double from_lat;
    public static Double from_lng;
    public static Double to_lat;
    public static Double to_lng;
    long MIN_COST_VALUE;
    private String pay_method;
    private long costFirstForMin;
    private ArrayAdapter<String> listAdapter;
    private String messageFondy;
    private String urlOrder;
    private long discount;

    public static String[] arrayServiceCode() {
        return new String[]{
                "BAGGAGE",
                "ANIMAL",
                "CONDIT",
                "MEET",
                "COURIER",
                "CHECK_OUT",
                "BABY_SEAT",
                "DRIVER",
                "NO_SMOKE",
                "ENGLISH",
                "CABLE",
                "FUEL",
                "WIRES",
                "SMOKE",
        };
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        String discountText = logCursor(MainActivity.TABLE_SETTINGS_INFO, getContext()).get(3);
        addCost = 0;
        updateAddCost(String.valueOf(addCost));

        progressbar = binding.progressBar;

        textView = binding.textGallery;
        textView.setText(R.string.my_routs);

        listView = binding.listView;

        del_but = binding.delBut;
        del_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRouts();
            }
        });
        btnRouts = binding.btnRouts;
        text_view_cost = binding.textViewCost;
        btn_minus = binding.btnMinus;
        btn_plus = binding.btnPlus;

        btn_minus.setOnClickListener(v -> {
            List<String> stringListInfo = logCursor(MainActivity.TABLE_SETTINGS_INFO, requireActivity());
            addCost = Long.parseLong(stringListInfo.get(5));
            cost = Long.parseLong(text_view_cost.getText().toString());
            cost -= 5;
            addCost -= 5;
            if (cost <= MIN_COST_VALUE) {
                cost = MIN_COST_VALUE;
                addCost = MIN_COST_VALUE - costFirstForMin;
            }
            updateAddCost(String.valueOf(addCost));
            text_view_cost.setText(String.valueOf(cost));
        });

        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> stringListInfo = logCursor(MainActivity.TABLE_SETTINGS_INFO, requireActivity());
                addCost = Long.parseLong(stringListInfo.get(5));
                cost = Long.parseLong(text_view_cost.getText().toString());
                cost += 5;
                addCost += 5;
                updateAddCost(String.valueOf(addCost));
                text_view_cost.setText(String.valueOf(cost));
            }
        });
        btnAdd = binding.btnAdd;
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyBottomSheetGalleryFragment bottomSheetDialogFragment = new MyBottomSheetGalleryFragment();
                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

        btnRouts.setVisibility(View.INVISIBLE);

        array = arrayToRoutsAdapter ();
        if(array != null) {
            listAdapter = new ArrayAdapter<>(requireActivity(), R.layout.services_adapter_layout, array);
            listView.setAdapter(listAdapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            registerForContextMenu(listView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    del_but.setVisibility(View.VISIBLE);
                    btnRouts.setVisibility(View.VISIBLE);
                    text_view_cost.setVisibility(View.VISIBLE);
                    buttonBonus.setVisibility(View.VISIBLE);
                    btn_minus.setVisibility(View.VISIBLE);
                    btn_plus.setVisibility(View.VISIBLE);
                    btnAdd.setVisibility(View.VISIBLE);
                    selectedItem = position + 1;

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            dialogFromToOneRout(routChoice(selectedItem));
                        }
                    } catch (MalformedURLException | InterruptedException | JSONException e) {
                        Log.d("TAG", "onItemClick: " + e.toString());
                    }


                }
            });
        } else {
            textView.setText(R.string.no_routs);

        }
        btnRouts.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                pay_method = logCursor(MainActivity.TABLE_SETTINGS_INFO, requireActivity()).get(4);
                List<String> stringList = logCursor(MainActivity.CITY_INFO, requireActivity());

                switch (stringList.get(1)) {
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
                            if(Long.parseLong(bonus) < Long.parseLong(text_view_cost.getText().toString()) * 100 ) {
                                paymentType("nal_payment");
                            }
                        }
                        break;
                }
                orderRout();
                switch (pay_method) {
                    case "google_payment":
                        progressbar.setVisibility(View.VISIBLE);
                        MainActivity.order_id = UniqueNumberGenerator.generateUniqueNumber(getActivity());
                        messageFondy = getString(R.string.fondy_message);
                        String tokenCard = logCursor(MainActivity.TABLE_USER_INFO, requireActivity()).get(6);
                        Log.d(TAG, "onClick: tokenCard" + tokenCard);
                        if (tokenCard == null || tokenCard.equals("")) {
                            getUrlToPayment(MainActivity.order_id, messageFondy, text_view_cost.getText().toString() + "00");
                        } else {
                            paymentByToken(MainActivity.order_id, messageFondy, text_view_cost.getText().toString() + "00", tokenCard);
                        }
                        break;
                    case "mono_payment":
                        progressbar.setVisibility(View.VISIBLE);
                        MainActivity.order_id = UniqueNumberGenerator.generateUniqueNumber(getActivity());

                        int amount = Integer.parseInt(text_view_cost.getText().toString() + "00");
                        String reference = MainActivity.order_id;
                        String comment = getString(R.string.fondy_message);

                        getUrlToPaymentMono(amount, reference, comment);
                        break;
                    default:
                        progressbar.setVisibility(View.VISIBLE);
                        orderFinished();
                }



//                if (pay_method.equals("google_payment")) {
//                    progressbar.setVisibility(View.VISIBLE);
//                    MainActivity.order_id = UniqueNumberGenerator.generateUniqueNumber(getActivity());
//                    messageFondy = getString(R.string.fondy_message);
//
//                    String tokenCard = logCursor(MainActivity.TABLE_USER_INFO, requireActivity()).get(6);
//                    Log.d(TAG, "onClick: tokenCard" + tokenCard);
//                    if (tokenCard == null || tokenCard.equals("")) {
//                        getUrlToPayment(MainActivity.order_id, messageFondy, text_view_cost.getText().toString()+ "00");
//                    } else {
//                        paymentByToken(MainActivity.order_id, messageFondy, text_view_cost.getText().toString() + "00", tokenCard);
//                    }
//                } else {
//                   orderFinished();
//                }

            }
        });

        buttonBonus = binding.btnBonus;

        buttonBonus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> stringList = logCursor(MainActivity.CITY_INFO, requireActivity());
                String api =  stringList.get(2);
                MyBottomSheetBonusFragment bottomSheetDialogFragment = new MyBottomSheetBonusFragment(Long.parseLong(text_view_cost.getText().toString()), "marker", api, text_view_cost, "Gallery") ;
                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });
        del_but.setVisibility(View.INVISIBLE);
        text_view_cost.setVisibility(View.INVISIBLE);
        btnRouts.setVisibility(View.INVISIBLE);
        btn_minus.setVisibility(View.INVISIBLE);
        btn_plus.setVisibility(View.INVISIBLE);
        btnAdd.setVisibility(View.INVISIBLE);
        buttonBonus.setVisibility(View.INVISIBLE);

        return root;
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
                                    "gallery",
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

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void orderRout() {

        if(connected()) {
            urlOrder = getTaxiUrlSearchMarkers("orderSearchMarkers", requireActivity());
        } else {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
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
                } else {
                    if(Objects.equals(sendUrlMap.get("routeto"), "Точка на карте")) {
                        to_name = requireActivity().getString(R.string.end_point_marker);
                    } else {
                        to_name = sendUrlMap.get("routeto") + " " + sendUrlMap.get("to_number");
                    }
                }
                String messageResult = getString(R.string.thanks_message) +
                        sendUrlMap.get("routefrom") + sendUrlMap.get("routefromnumber") + " " + getString(R.string.to_message) +
                        to_name + "." +
                        getString(R.string.call_of_order) + orderWeb + getString(R.string.UAH);
                String messageFondy = getString(R.string.fondy_message) + " " +
                        sendUrlMap.get("routefrom") + sendUrlMap.get("routefromnumber") + " " + getString(R.string.to_message) +
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

    private void paymentType(String paymentCode) {
        ContentValues cv = new ContentValues();
        cv.put("bonusPayment", paymentCode);
        // обновляем по id
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        database.update(MainActivity.TABLE_SETTINGS_INFO, cv, "id = ?",
                new String[] { "1" });
        database.close();
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
                                        "gallery",
                                        urlOrder
                                        );
                                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());

//                                Intent paymentIntent = new Intent(requireActivity(), FondyPaymentActivity.class);
//                                paymentIntent.putExtra("checkoutUrl", checkoutUrl);
//                                paymentIntent.putExtra("urlOrder", urlOrder);
//                                paymentIntent.putExtra("orderCost", text_view_cost.getText().toString());
//                                paymentIntent.putExtra("fragment_key", "gallery");
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
     private void updateRoutMarker(List<String> settings) {

        Log.d("TAG", "updateRoutMarker: settings - " + settings);

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void dialogFromToOneRout(Map <String, String> rout) throws MalformedURLException, InterruptedException, JSONException {
        if(connected()) {
            Log.d("TAG", "dialogFromToOneRout: " + rout.toString());
            from_lat =  Double.valueOf(rout.get("from_lat"));
            from_lng = Double.valueOf(rout.get("from_lng"));
            to_lat = Double.valueOf(rout.get("to_lat"));
            to_lng = Double.valueOf(rout.get("to_lng"));

            Log.d("TAG", "dialogFromToOneRout: from_lat - " + from_lat);
            Log.d("TAG", "dialogFromToOneRout: from_lng - " + from_lng);
            Log.d("TAG", "dialogFromToOneRout: to_lat - " + to_lat);
            Log.d("TAG", "dialogFromToOneRout: to_lng - " + to_lng);

            FromAddressString = rout.get("from_street") + rout.get("from_number") ;
            Log.d("TAG1", "dialogFromToOneRout: FromAddressString" + FromAddressString);
            ToAddressString = rout.get("to_street") + rout.get("to_number");
            if(rout.get("from_street").equals(rout.get("to_street"))) {
                ToAddressString =  getString(R.string.on_city_tv);;
            }
            Log.d("TAG", "dialogFromToOneRout: ToAddressString" + ToAddressString);
            List<String> settings = new ArrayList<>();

            settings.add(rout.get("from_lat"));
            settings.add(rout.get("from_lng"));
            settings.add(rout.get("to_lat"));
            settings.add(rout.get("to_lng"));

            updateRoutMarker(settings);
            String urlCost = getTaxiUrlSearchMarkers("costSearchMarkers", getContext());

            Map<String, String> sendUrlMapCost = ToJSONParser.sendURL(urlCost);

            String message = sendUrlMapCost.get("message");
            String orderCost = sendUrlMapCost.get("order_cost");
            Log.d(TAG, "dialogFromToOneRout:orderCost " + orderCost);


            if (!orderCost.equals("0")) {
                String discountText = logCursor(MainActivity.TABLE_SETTINGS_INFO, getContext()).get(3);
                long discountInt = Integer.parseInt(discountText);
                cost = Long.parseLong(orderCost);
                discount = cost * discountInt / 100;

                cost += discount;
                updateAddCost(String.valueOf(discount));
                text_view_cost.setText(String.valueOf(cost));

                costFirstForMin = cost;
                MIN_COST_VALUE = (long) (cost*0.6);
            } else {
                MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());

                text_view_cost.setVisibility(View.INVISIBLE);
                btnRouts.setVisibility(View.INVISIBLE);
                btn_minus.setVisibility(View.INVISIBLE);
                btn_plus.setVisibility(View.INVISIBLE);
                btnAdd.setVisibility(View.INVISIBLE);
                buttonBonus.setVisibility(View.INVISIBLE);
            }
        } else {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        }
    }
    private Map <String, String> routChoice(int i) {
        Map <String, String> rout = new HashMap<>();
        SQLiteDatabase database = getContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor c = database.query(MainActivity.TABLE_ORDERS_INFO, null, null, null, null, null, null);
        c.move(i);
        rout.put("id", c.getString(c.getColumnIndexOrThrow ("id")));
        rout.put("from_lat", c.getString(c.getColumnIndexOrThrow ("from_lat")));
        rout.put("from_lng", c.getString(c.getColumnIndexOrThrow ("from_lng")));
        rout.put("to_lat", c.getString(c.getColumnIndexOrThrow ("to_lat")));
        rout.put("to_lng", c.getString(c.getColumnIndexOrThrow ("to_lng")));
        rout.put("from_street", c.getString(c.getColumnIndexOrThrow ("from_street")));
        rout.put("from_number", c.getString(c.getColumnIndexOrThrow ("from_number")));
        rout.put("to_street", c.getString(c.getColumnIndexOrThrow ("to_street")));
        rout.put("to_number", c.getString(c.getColumnIndexOrThrow ("to_number")));

        Log.d("TAG", "routMaps: " + rout);
        return rout;
    }
    private boolean connected() {

        Boolean hasConnect = false;

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
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTaxiUrlSearchMarkers(String urlAPI, Context context) {

        List<String> stringListRout = logCursor(MainActivity.ROUT_MARKER, context);
        Log.d("TAG", "getTaxiUrlSearch: stringListRout" + stringListRout);

        double originLatitude = Double.parseDouble(stringListRout.get(1));
        double originLongitude = Double.parseDouble(stringListRout.get(2));
        double toLatitude = Double.parseDouble(stringListRout.get(3));
        double toLongitude = Double.parseDouble(stringListRout.get(4));



        List<String> stringList = logCursor(MainActivity.TABLE_ADD_SERVICE_INFO, context);
        String time = stringList.get(1);
        String comment = stringList.get(2);
        String date = stringList.get(3);

        // Origin of route
        String str_origin = originLatitude + "/" + originLongitude;

        // Destination of route
        String str_dest = toLatitude + "/" + toLongitude;

        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

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
        List<String> stringListCity = logCursor(MainActivity.CITY_INFO, requireActivity());
        String api =  stringListCity.get(2);
        String url = "https://m.easy-order-taxi.site/" + api + "/android/" + urlAPI + "/" + parameters + "/" + result;


        database.close();


        return url;

    }
    private boolean verifyOrder(Context context) {
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.query(MainActivity.TABLE_USER_INFO, null, null, null, null, null, null);

        boolean verify = true;
        if (cursor.getCount() == 1) {

            if (logCursor(MainActivity.TABLE_USER_INFO, context).get(1).equals("0")) {
                verify = false;Log.d("TAG", "verifyOrder:verify " +verify);
            }
            cursor.close();
        }
        database.close();
        return verify;
    }
    @SuppressLint("Range")
    public List<String> logCursor(String table, Context context) {
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

        return list;
    }
    private void reIndexOrders() {
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE  temp_table" + "(id integer primary key autoincrement," +
                " from_street text," +
                " from_number text," +
                " from_lat text," +
                " from_lng text," +
                " to_street text," +
                " to_number text," +
                " to_lat text," +
                " to_lng text);");
        // Копирование данных из старой таблицы во временную
        database.execSQL("INSERT INTO temp_table SELECT * FROM " + MainActivity.TABLE_ORDERS_INFO);

        // Удаление старой таблицы
        database.execSQL("DROP TABLE " + MainActivity.TABLE_ORDERS_INFO);

        // Создание новой таблицы
        database.execSQL("CREATE TABLE " + MainActivity.TABLE_ORDERS_INFO + "(id integer primary key autoincrement," +
                " from_street text," +
                " from_number text," +
                " from_lat text," +
                " from_lng text," +
                " to_street text," +
                " to_number text," +
                " to_lat text," +
                " to_lng text);");

        String query = "INSERT INTO " + MainActivity.TABLE_ORDERS_INFO + " (from_street, from_number, from_lat, from_lng, to_street, to_number, to_lat, to_lng) " +
                "SELECT from_street, from_number, from_lat, from_lng, to_street, to_number, to_lat, to_lng FROM temp_table";

        // Копирование данных из временной таблицы в новую
        database.execSQL(query);

        // Удаление временной таблицы
        database.execSQL("DROP TABLE temp_table");
        database.close();
    }
    private void deleteRouts () {
        SparseBooleanArray checkespositions = listView.getCheckedItemPositions();
        ArrayList<Integer> selectespositions = new ArrayList<>();

        for (int i = 0; i < checkespositions.size(); i++) {
            int pos = checkespositions.keyAt(i);
            if (checkespositions.get(pos)) {
                selectespositions.add(pos);
            }
        }

        for (int position : selectespositions) {
            int i = position + 1;

            String deleteQuery = "DELETE FROM " + MainActivity.TABLE_ORDERS_INFO + " WHERE id = " + i  + ";";
            SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

            database.execSQL(deleteQuery);
            database.close();
        }
        reIndexOrders();
        array = arrayToRoutsAdapter();
        if (array != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.services_adapter_layout, array);
            listView.setAdapter(adapter);
        } else {
            // Если массив пустой, отобразите текст "no_routs" вместо списка
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.services_adapter_layout, new String[]{});
            listView.setAdapter(adapter);
            textView.setText(R.string.no_routs);

            del_but.setVisibility(View.INVISIBLE);
            text_view_cost.setVisibility(View.INVISIBLE);
            btnRouts.setVisibility(View.INVISIBLE);
            btn_minus.setVisibility(View.INVISIBLE);
            btn_plus.setVisibility(View.INVISIBLE);
            btnAdd.setVisibility(View.INVISIBLE);
            buttonBonus.setVisibility(View.INVISIBLE);

        }
        del_but.setVisibility(View.INVISIBLE);
        text_view_cost.setVisibility(View.INVISIBLE);
        btnRouts.setVisibility(View.INVISIBLE);
        btn_minus.setVisibility(View.INVISIBLE);
        btn_plus.setVisibility(View.INVISIBLE);
        btnAdd.setVisibility(View.INVISIBLE);
        buttonBonus.setVisibility(View.INVISIBLE);


    }
    private String[] arrayToRoutsAdapter() {
        ArrayList<Map> routMaps = routMaps(getContext());
        String[] arrayRouts;
        if(routMaps.size() != 0) {
            arrayRouts = new String[routMaps.size()];
            for (int i = 0; i < routMaps.size(); i++) {
                if(routMaps.get(i).get("from_street").toString().equals("Місце відправлення")) {
                    from_mes = getString(R.string.start_point_text);
                }
                else {
                    from_mes = routMaps.get(i).get("from_street").toString();
                }

                if(routMaps.get(i).get("to_street").toString().equals("Місце призначення")) {
                    to_mes = getString(R.string.end_point_marker);
                }
                else {
                    to_mes = routMaps.get(i).get("to_street").toString();
                }


                if(!routMaps.get(i).get("from_street").toString().equals(routMaps.get(i).get("to_street").toString())) {
                    if (!routMaps.get(i).get("from_street").toString().equals(routMaps.get(i).get("from_number").toString())) {


                        Log.d("TAG", "arrayToRoutsAdapter:   routMaps.get(i).get(\"from_street\").toString()" +  routMaps.get(i).get("from_street").toString());

                        arrayRouts[i] = from_mes + " " +
                                routMaps.get(i).get("from_number").toString() + " -> " +
                                to_mes + " " +
                                routMaps.get(i).get("to_number").toString();
                    } else if(!routMaps.get(i).get("to_street").toString().equals(routMaps.get(i).get("to_number").toString())) {
                        Log.d("TAG", "arrayToRoutsAdapter:   routMaps.get(i).get(\"from_street\").toString()" +  routMaps.get(i).get("from_street").toString());

                        arrayRouts[i] = routMaps.get(i).get("from_street").toString() +
                                getString(R.string.to_message) +
                                routMaps.get(i).get("to_street").toString() + " " +
                                routMaps.get(i).get("to_number").toString();
                    } else {

                        Log.d("TAG", "arrayToRoutsAdapter:   routMaps.get(i).get(\"from_street\").toString()" +  routMaps.get(i).get("from_street").toString());

                        arrayRouts[i] = from_mes + " " +
                                getString(R.string.to_message) +
                                to_mes;

                    }

                } else {

                    Log.d("TAG", "arrayToRoutsAdapter:   routMaps.get(i).get(\"from_street\").toString()" +  routMaps.get(i).get("from_street").toString());

                    arrayRouts[i] = from_mes + " " +
                            routMaps.get(i).get("from_number").toString() + " -> " +
                            getString(R.string.on_city_tv);
                }

            }
        } else {
            arrayRouts = null;
        }
        return arrayRouts;
    }
    private ArrayList<Map> routMaps(Context context) {
        Map <String, String> routs;
        ArrayList<Map> routsArr = new ArrayList<>();
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor c = database.query(MainActivity.TABLE_ORDERS_INFO, null, null, null, null, null, null);
        int i = 0;
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    routs = new HashMap<>();
                    routs.put("id", c.getString(c.getColumnIndexOrThrow ("id")));
                    routs.put("from_street", c.getString(c.getColumnIndexOrThrow ("from_street")));
                    routs.put("from_number", c.getString(c.getColumnIndexOrThrow ("from_number")));
                    routs.put("to_street", c.getString(c.getColumnIndexOrThrow ("to_street")));
                    routs.put("to_number", c.getString(c.getColumnIndexOrThrow ("to_number")));
                    routsArr.add(i++, routs);
                } while (c.moveToNext());
            }
        }
        database.close();
        Log.d("TAG", "routMaps: " + routsArr);
        return routsArr;
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: selectedItem " + selectedItem);
        listView.clearChoices();
        listView.requestLayout(); // Обновляем визуальное состояние списка
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged(); // Обновляем адаптер
        }
        del_but.setVisibility(View.INVISIBLE);
        text_view_cost.setVisibility(View.INVISIBLE);
        btnRouts.setVisibility(View.INVISIBLE);
        btn_minus.setVisibility(View.INVISIBLE);
        btn_plus.setVisibility(View.INVISIBLE);
        btnAdd.setVisibility(View.INVISIBLE);
        buttonBonus.setVisibility(View.INVISIBLE);
    }
}