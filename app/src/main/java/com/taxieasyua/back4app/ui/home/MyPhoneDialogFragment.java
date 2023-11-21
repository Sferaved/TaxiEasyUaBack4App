package com.taxieasyua.back4app.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.finish.FinishActivity;
import com.taxieasyua.back4app.ui.fondy.payment.ApiResponsePay;
import com.taxieasyua.back4app.ui.fondy.payment.MyBottomSheetCardPayment;
import com.taxieasyua.back4app.ui.fondy.payment.PaymentApi;
import com.taxieasyua.back4app.ui.fondy.payment.RequestData;
import com.taxieasyua.back4app.ui.fondy.payment.StatusRequestPay;
import com.taxieasyua.back4app.ui.fondy.payment.SuccessResponseDataPay;
import com.taxieasyua.back4app.ui.fondy.payment.UniqueNumberGenerator;
import com.taxieasyua.back4app.ui.maps.ToJSONParser;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;

import java.io.UnsupportedEncodingException;
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


public class MyPhoneDialogFragment extends BottomSheetDialogFragment {
    EditText phoneNumber;
    AppCompatButton button;
    CheckBox checkBox;
    String page;
    private String TAG = "TAG";
    private SQLiteDatabase database;
    private String urlOrder;
    private String messageFondy;
    private String amount;

    public MyPhoneDialogFragment(String page, String amount) {
        this.page = page;
        this.amount = amount;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.phone_verify_layout, container, false);
        phoneNumber = view.findViewById(R.id.phoneNumber);
        button = view.findViewById(R.id.ok_button);
        checkBox = view.findViewById(R.id.checkbox);
        messageFondy = getString(R.string.fondy_message);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeFragment.progressBar.setVisibility(View.INVISIBLE);
                String PHONE_PATTERN = "((\\+?380)(\\d{9}))$";
                boolean val = Pattern.compile(PHONE_PATTERN).matcher(phoneNumber.getText().toString()).matches();

                if (!val) {
                    String message = getString(R.string.format_phone);
                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                }
                if (val) {
                    MainActivity.verifyPhone = true;
                    updateRecordsUser(phoneNumber.getText().toString(), requireActivity());
                    String pay_method = logCursor(MainActivity.TABLE_SETTINGS_INFO).get(4);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        List<String> stringListCity = logCursor(MainActivity.CITY_INFO);
                        String card_max_pay = stringListCity.get(4);
                        String bonus_max_pay = stringListCity.get(5);
                        switch (pay_method) {
                            case "bonus_payment":
                                if (Long.parseLong(bonus_max_pay) <= Long.parseLong(amount) * 100) {
                                    changePayMethodMax(amount, pay_method);
                                } else {
                                    switch (page) {
                                        case "home" :
                                            orderHome();
                                            dismiss();
                                            break;
                                        case "geo" :
                                            orderGeo();
                                            dismiss();
                                            break;
                                        case "marker" :
                                            orderMarker();
                                            dismiss();
                                            break;
                                    }
                                }
                                break;
                            case "card_payment":
                            case "fondy_payment":
                            case "mono_payment":
                                if (Long.parseLong(card_max_pay) <= Long.parseLong(amount)) {
                                    changePayMethodMax(amount, pay_method);
                                } else {
                                    switch (page) {
                                        case "home" :
                                            orderHome();
                                            dismiss();
                                            break;
                                        case "geo" :
                                            orderGeo();
                                            dismiss();
                                            break;
                                        case "marker" :
                                            orderMarker();
                                            dismiss();
                                            break;
                                    }
                                }
                                break;
                            default:
                                switch (page) {
                                    case "home" :
                                        orderHome();
                                        dismiss();
                                        break;
                                    case "geo" :
                                        orderGeo();
                                        dismiss();
                                        break;
                                    case "marker" :
                                        orderMarker();
                                        dismiss();
                                        break;
                                }
                        }

                    }







                }
            }
        });
        return view;
    }
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Получаем контекст активности
        database = context.openOrCreateDatabase(MainActivity.DB_NAME, Context.MODE_PRIVATE, null);
    }
    private void orderHome() {
        if (connected()) {
            try {
                String urlOrder = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    urlOrder = getTaxiUrlSearch("orderSearch");
                }
                Map<String, String> sendUrlMap = ToJSONParser.sendURL(urlOrder);

                String orderWeb = sendUrlMap.get("order_cost");
                String message = sendUrlMap.get("message");
                String messageResult;
                if (!orderWeb.equals("0")) {

                    String from_name = (String) sendUrlMap.get("routefrom");
                    String to_name = (String) sendUrlMap.get("routeto");
                    if (from_name.equals(to_name)) {
                        messageResult = getString(R.string.thanks_message) +
                                from_name + " " + HomeFragment.from_number.getText() + " " + getString(R.string.on_city) +
                                getString(R.string.cost_of_order) + orderWeb + getString(R.string.UAH);


                    } else {
                        messageResult = getString(R.string.thanks_message) +
                                from_name + " " + HomeFragment.from_number.getText() + " " + getString(R.string.to_message) +
                                to_name + " " + HomeFragment.to_number.getText() + "." +
                                getString(R.string.cost_of_order) + orderWeb + getString(R.string.UAH);
                    }

                    if (!sendUrlMap.get("from_lat").equals("0") && !sendUrlMap.get("lat").equals("0")) {
                        if (from_name.equals(to_name)) {
                            insertRecordsOrders(
                                    from_name, from_name,
                                    HomeFragment.from_number.getText().toString(), HomeFragment.from_number.getText().toString(),
                                    (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
                                    (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
                                    requireContext()
                            );
                        } else {
                            insertRecordsOrders(
                                    from_name, to_name,
                                    HomeFragment.from_number.getText().toString(), HomeFragment.to_number.getText().toString(),
                                    (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
                                    (String) sendUrlMap.get("lat"), (String) sendUrlMap.get("lng"),
                                    requireContext()
                            );

                        }
                    }

                    Intent intent = new Intent(requireActivity(), FinishActivity.class);
                    intent.putExtra("messageResult_key", messageResult);
                    intent.putExtra("messageCost_key", orderWeb);
                    intent.putExtra("sendUrlMap", new HashMap<>(sendUrlMap));
                    intent.putExtra("UID_key", String.valueOf(sendUrlMap.get("dispatching_order_uid")));
                    startActivity(intent);
                    HomeFragment.progressBar.setVisibility(View.INVISIBLE);

                } else {
                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                }


            } catch (MalformedURLException e) {
                MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        } else {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            HomeFragment.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void orderGeo() {
        if(connected()) {
            try {

                String urlOrder = null;

                if(MyGeoDialogFragment.urlAddress == null) {
                    List<String> settings = new ArrayList<>();
                    settings.add(String.valueOf(OpenStreetMapActivity.startLat));
                    settings.add(String.valueOf(OpenStreetMapActivity.startLan));
                    settings.add(MyGeoDialogFragment.toCost);
                    settings.add(MyGeoDialogFragment.to_numberCost);
                    settings.add(OpenStreetMapActivity.FromAdressString);
//                    settings.add(MyGeoDialogFragment.textViewTo.getText().toString());
                    updateRoutGeo(settings);

                    urlOrder = getTaxiUrlSearchMarkers( "orderSearchMarkersVisicom");
                } else {
//                    List<String> settings = new ArrayList<>();
//                    settings.add(String.valueOf(OpenStreetMapActivity.startLat));
//                    settings.add(String.valueOf(OpenStreetMapActivity.startLan));
//                    settings.add(String.valueOf(MyGeoDialogFragment.to_lat));
//                    settings.add(String.valueOf(MyGeoDialogFragment.to_lng));
//                    settings.add(OpenStreetMapActivity.FromAdressString);
//                    settings.add(MyGeoDialogFragment.textViewTo.getText().toString());
//
//                    updateRoutMarker(settings);
                    urlOrder = MyGeoDialogFragment.urlAddress;

                }

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
                                    requireActivity()
                            );
                        }
                    } else {

                        if(sendUrlMap.get("routeto").equals("Точка на карте")) {
                            to_name = requireActivity().getString(R.string.end_point_marker);
                        } else {
                            to_name = sendUrlMap.get("routeto") + " " + sendUrlMap.get("to_number");
                        }

                        if (!sendUrlMap.get("lat").equals("0")) {
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


                    Intent intent = new Intent(requireActivity(), FinishActivity.class);
                    intent.putExtra("messageResult_key", messageResult);
                    intent.putExtra("messageCost_key", orderWeb);
                    intent.putExtra("sendUrlMap", new HashMap<>(sendUrlMap));
                    intent.putExtra("UID_key", Objects.requireNonNull(sendUrlMap.get("dispatching_order_uid")));
                    startActivity(intent);
                } else {

                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                    if(MyGeoDialogFragment.progressBar != null){
                        MyGeoDialogFragment.progressBar.setVisibility(View.GONE);
                    }

                }


            } catch (MalformedURLException ignored) {
            }

        } else {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            MyGeoDialogFragment.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void orderMarker() {
        if(connected()) {
            try {
                String urlOrder = getTaxiUrlSearchMarkers("orderSearchMarkers");
                Map<String, String> sendUrlMap = ToJSONParser.sendURL(urlOrder);
                Log.d("TAG", "Map sendUrlMap = ToJSONParser.sendURL(urlOrder); " + sendUrlMap);

                String orderWeb = sendUrlMap.get("order_cost");

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
                                    requireActivity()
                            );
                        }
                    } else {
                        if(sendUrlMap.get("routeto").equals("Точка на карте")) {
                            to_name = requireActivity().getString(R.string.end_point_marker);
                        } else {
                            to_name = sendUrlMap.get("routeto") + " " + sendUrlMap.get("to_number");
                        }

                        if (!sendUrlMap.get("lat").equals("0")) {
                            insertRecordsOrders(
                                    sendUrlMap.get("routefrom"), to_name,
                                    sendUrlMap.get("routefromnumber"), sendUrlMap.get("to_number"),
                                    Double.toString(OpenStreetMapActivity.startLat), Double.toString(OpenStreetMapActivity.startLan),
                                    sendUrlMap.get("lat"), sendUrlMap.get("lng"),
                                    requireActivity()
                            );
                        }
                    }
                    String messageResult = getString(R.string.thanks_message) +
                            OpenStreetMapActivity.FromAdressString + " " + getString(R.string.to_message) +
                            to_name + "." +
                            getString(R.string.call_of_order) + orderWeb + getString(R.string.UAH);


                    Intent intent = new Intent(requireActivity(), FinishActivity.class);
                    intent.putExtra("messageResult_key", messageResult);
                    intent.putExtra("messageCost_key", orderWeb);
                    intent.putExtra("sendUrlMap", new HashMap<>(sendUrlMap));
                    intent.putExtra("UID_key", Objects.requireNonNull(sendUrlMap.get("dispatching_order_uid")));
                    startActivity(intent);
                } else {

                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(sendUrlMap.get("message"));
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                    OpenStreetMapActivity.progressBar.setVisibility(View.INVISIBLE);
                    MyGeoMarkerDialogFragment.progressBar.setVisibility(View.INVISIBLE);
                }


            } catch (MalformedURLException ignored) {

            }
        } else {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            OpenStreetMapActivity.progressBar.setVisibility(View.INVISIBLE);
            MyGeoMarkerDialogFragment.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private void updateRoutGeo(List<String> settings) {
        ContentValues cv = new ContentValues();

        cv.put("startLat",  Double.parseDouble(settings.get(0)));
        cv.put("startLan", Double.parseDouble(settings.get(1)));
        cv.put("toCost", settings.get(2));
        cv.put("to_numberCost", settings.get(3));

        // обновляем по id
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        database.update(MainActivity.ROUT_GEO, cv, "id = ?",
                new String[] { "1" });
        database.close();

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
    private String getTaxiUrlSearch(String urlAPI) {

        List<String> stringListRout = logCursor(MainActivity.ROUT_HOME);

        String originalString = stringListRout.get(1);
        int indexOfSlash = originalString.indexOf("/");
        String from = (indexOfSlash != -1) ? originalString.substring(0, indexOfSlash) : originalString;

        String from_number = stringListRout.get(2);

        originalString = stringListRout.get(3);
        indexOfSlash = originalString.indexOf("/");
        String to = (indexOfSlash != -1) ? originalString.substring(0, indexOfSlash) : originalString;

        String to_number = stringListRout.get(4);


        List<String> stringList = logCursor(MainActivity.TABLE_ADD_SERVICE_INFO);
        String time = stringList.get(1);
        String comment = stringList.get(2);
        String date = stringList.get(3);

        // Origin of route
        String str_origin = from + "/" + from_number;

        // Destination of route
        String str_dest = to + "/" + to_number;

        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);


        List<String> stringListInfo = logCursor(MainActivity.TABLE_SETTINGS_INFO);
        String tarif =  stringListInfo.get(2);
        String payment_type =  stringListInfo.get(4);

        // Building the parameters to the web service

        String parameters = null;
        String phoneNumber = "no phone";
        String userEmail = logCursor(MainActivity.TABLE_USER_INFO).get(3);
        String displayName = logCursor(MainActivity.TABLE_USER_INFO).get(4);

        if(urlAPI.equals("costSearch")) {
            Cursor c = database.query(MainActivity.TABLE_USER_INFO, null, null, null, null, null, null);
            if (c.getCount() == 1) {
                phoneNumber = logCursor(MainActivity.TABLE_USER_INFO).get(2);
                c.close();
            }

            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + payment_type;
        }

        if(urlAPI.equals("orderSearch")) {
            phoneNumber = logCursor(MainActivity.TABLE_USER_INFO).get(2);

            String addCost = stringListInfo.get(5);

            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + payment_type + "/" + addCost + "/" + time + "/" + comment + "/" + date;

            ContentValues cv = new ContentValues();

            cv.put("time", "no_time");
            cv.put("comment", "no_comment");
            cv.put("date", "no_date");

            // обновляем по id
            database.update(MainActivity.TABLE_ADD_SERVICE_INFO, cv, "id = ?",
                    new String[] { "1" });

        }

        // Building the url to the web service
        List<String> services = logCursor(MainActivity.TABLE_SERVICE_INFO);
        List<String> servicesChecked = new ArrayList<>();
        String result;
        boolean servicesVer = false;
        for (int i = 1; i <= 14 ; i++) {
            if(services.get(i).equals("1")) {
                servicesVer = true;
                break;
            }
        }
        if(servicesVer) {
            for (int i = 0; i < HomeFragment.arrayServiceCode().length; i++) {
                if(services.get(i+1).equals("1")) {
                    servicesChecked.add(HomeFragment.arrayServiceCode()[i]);
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
        List<String> stringListCity = logCursor(MainActivity.CITY_INFO);
        String api =  stringListCity.get(2);
        String url = "https://m.easy-order-taxi.site/" + api + "/android/" + urlAPI + "/" + parameters + "/" + result;

        Log.d("TAG", "getTaxiUrlSearch: " + url);
        return url;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private String getTaxiUrlSearchGeo(String urlAPI) {

        String query = "SELECT * FROM " + MainActivity.ROUT_GEO + " LIMIT 1";
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();

        // Получите значения полей из первой записи

        double originLatitude = cursor.getDouble(cursor.getColumnIndex("startLat"));
        double originLongitude = cursor.getDouble(cursor.getColumnIndex("startLan"));
        String to = cursor.getString(cursor.getColumnIndex("toCost"));
        String to_number = cursor.getString(cursor.getColumnIndex("to_numberCost"));

        cursor.close();

        if(to_number.equals("XXX")) {
            to_number = " ";
        }
        List<String> stringList = logCursor(MainActivity.TABLE_ADD_SERVICE_INFO);
        String time = stringList.get(1);
        String comment = stringList.get(2);
        String date = stringList.get(3);

        // Origin of route
        String str_origin = originLatitude + "/" + originLongitude;

        // Destination of route
        String str_dest = to + "/" + to_number;

        List<String> stringListInfo = logCursor(MainActivity.TABLE_SETTINGS_INFO);
        String tarif =  stringListInfo.get(2);
        String payment_type =  stringListInfo.get(4);

        // Building the parameters to the web service

        String parameters = null;
        String phoneNumber = "no phone";
        String userEmail = logCursor(MainActivity.TABLE_USER_INFO).get(3);
        String displayName = logCursor(MainActivity.TABLE_USER_INFO).get(4);

        if(urlAPI.equals("costSearchGeo")) {
            Cursor c = database.query(MainActivity.TABLE_USER_INFO, null, null, null, null, null, null);

            if (c.getCount() == 1) {
                phoneNumber = logCursor(MainActivity.TABLE_USER_INFO).get(2);
                c.close();
            }
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + payment_type;
        }

        if(urlAPI.equals("orderSearchGeo")) {
            phoneNumber = logCursor(MainActivity.TABLE_USER_INFO).get(2);
            String addCost = stringListInfo.get(5);
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + payment_type + "/" + addCost + "/" + time + "/" + comment + "/" + date;

            ContentValues cv = new ContentValues();

            cv.put("time", "no_time");
            cv.put("comment", "no_comment");
            cv.put("date", "no_date");

            // обновляем по id
            database.update(MainActivity.TABLE_ADD_SERVICE_INFO, cv, "id = ?",
                    new String[] { "1" });
        }

        // Building the url to the web service
        List<String> services = logCursor(MainActivity.TABLE_SERVICE_INFO);
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

        String url = "https://m.easy-order-taxi.site/" + MyGeoDialogFragment.api + "/android/" + urlAPI + "/" + parameters + "/" + result;
        Log.d("TAG", "getTaxiUrlSearch services: " + url);

        return url;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTaxiUrlSearchMarkers(String urlAPI) {

        List<String> stringListRout = logCursor(MainActivity.ROUT_MARKER);
        Log.d("TAG", "getTaxiUrlSearch: stringListRout" + stringListRout);

        double originLatitude = Double.parseDouble(stringListRout.get(1));
        double originLongitude = Double.parseDouble(stringListRout.get(2));
        double toLatitude = Double.parseDouble(stringListRout.get(3));
        double toLongitude = Double.parseDouble(stringListRout.get(4));
        String start = stringListRout.get(5);
        String finish = stringListRout.get(6);
        // Заменяем символ '/' в строках
        start = start.replace("/", "%2F");
        finish = finish.replace("/", "%2F");

        List<String> stringList = logCursor(MainActivity.TABLE_ADD_SERVICE_INFO);
        String time = stringList.get(1);
        String comment = stringList.get(2);
        String date = stringList.get(3);

        // Origin of route
        String str_origin = originLatitude + "/" + originLongitude;

        // Destination of route
        String str_dest = toLatitude + "/" + toLongitude;

        //        Cursor cursorDb = MainActivity.database.query(MainActivity.TABLE_SETTINGS_INFO, null, null, null, null, null, null);
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

        List<String> stringListInfo = logCursor(MainActivity.TABLE_SETTINGS_INFO);
        String tarif =  stringListInfo.get(2);
        String payment_type =  stringListInfo.get(4);

        String api = logCursor(MainActivity.CITY_INFO).get(2);

        // Building the parameters to the web service

        String parameters = null;
        String phoneNumber = "no phone";
        String userEmail = logCursor(MainActivity.TABLE_USER_INFO).get(3);
        String displayName = logCursor(MainActivity.TABLE_USER_INFO).get(4);

        if(urlAPI.equals("costSearchMarkers")) {
            Cursor c = database.query(MainActivity.TABLE_USER_INFO, null, null, null, null, null, null);

            if (c.getCount() == 1) {
                phoneNumber = logCursor(MainActivity.TABLE_USER_INFO).get(2);
                c.close();
            }
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + payment_type;
        }
        if(urlAPI.equals("orderSearchMarkersVisicom")) {
            phoneNumber = logCursor(MainActivity.TABLE_USER_INFO).get(2);
            String addCost = stringListInfo.get(5);

            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + payment_type + "/" + addCost + "/"
                    + time + "/" + comment + "/" + date+ "/" + start + "/" + finish;

            ContentValues cv = new ContentValues();

            cv.put("time", "no_time");
            cv.put("comment", "no_comment");
            cv.put("date", "no_date");

            // обновляем по id
            database.update(MainActivity.TABLE_ADD_SERVICE_INFO, cv, "id = ?",
                    new String[] { "1" });

        }

        // Building the url to the web service
        List<String> services = logCursor(MainActivity.TABLE_SERVICE_INFO);
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


    private void changePayMethodMax(String textCost, String paymentType) {
        List<String> stringListCity = logCursor(MainActivity.CITY_INFO);
        String card_max_pay = stringListCity.get(4);
        String bonus_max_pay = stringListCity.get(5);

        // Инфлейтим макет для кастомного диалога
        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);

        AlertDialog alertDialog = new AlertDialog.Builder(requireActivity()).create();
        alertDialog.setView(dialogView);
        alertDialog.setCancelable(false);
        // Настраиваем элементы макета


        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        messageTextView.setText(R.string.max_limit_message);

        Button okButton = dialogView.findViewById(R.id.dialog_ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (paymentType) {
                    case "bonus_payment":
                        if (Long.parseLong(bonus_max_pay) <= Long.parseLong(textCost) * 100) {
                            paymentType("nal_payment");
                        }
                        break;
                    case "card_payment":
                    case "fondy_payment":
                    case "mono_payment":
                        if (Long.parseLong(card_max_pay) <= Long.parseLong(textCost)) {
                            paymentType("nal_payment");
                        }
                        break;
                }
                switch (page) {
                    case "home" :
                        orderHome();
                        dismiss();
                        break;
                    case "geo" :
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            orderGeo();
                        }
                        dismiss();
                        break;
                    case "marker" :
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            orderMarker();
                        }
                        dismiss();
                        break;
                }
                alertDialog.dismiss();
            }
        });

        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeFragment.progressBar.setVisibility(View.INVISIBLE);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }
    private void paymentType(String paymentCode) {
        ContentValues cv = new ContentValues();
        cv.put("payment_type", paymentCode);
        // обновляем по id
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        database.update(MainActivity.TABLE_SETTINGS_INFO, cv, "id = ?",
                new String[] { "1" });
        database.close();
    }

    @SuppressLint("Range")
    public List<String> logCursor(String table) {
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
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && requireActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkBox.isChecked()) {
                    phoneNumber.setVisibility(View.VISIBLE);
                    button.setVisibility(View.VISIBLE);

                } else {
                    phoneNumber.setVisibility(View.INVISIBLE);
                    button.setVisibility(View.INVISIBLE);
                }
            }
        });

    }



    public static void updateRecordsUser(String result, Context context) {
        ContentValues cv = new ContentValues();

        cv.put("phone_number", result);

        // обновляем по id
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        int updCount = database.update(MainActivity.TABLE_USER_INFO, cv, "id = ?",
                new String[] { "1" });
        Log.d("TAG", "updated rows count = " + updCount);


    }

}

