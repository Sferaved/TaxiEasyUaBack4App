package com.taxieasyua.back4app.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.card.CardFragment;
import com.taxieasyua.back4app.ui.card.CustomCardAdapter;
import com.taxieasyua.back4app.ui.fondy.payment.UniqueNumberGenerator;
import com.taxieasyua.back4app.ui.gallery.GalleryFragment;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;
import com.taxieasyua.back4app.ui.payment_system.PayApi;
import com.taxieasyua.back4app.ui.payment_system.ResponsePaySystem;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MyBottomSheetBonusFragment extends BottomSheetDialogFragment {

    private static final String TAG = "TAG_BON";
    long cost;
    String rout;
    String api;
    TextView textView;
    String fragment;
    ListView listView;
    String[] array, arrayCode;
    AppCompatButton btn_ok;
    int pos;
    String pay_method;
    private String baseUrl = "https://m.easy-order-taxi.site";

    public MyBottomSheetBonusFragment(long cost, String rout, String api, TextView textView) {
        this.cost = cost;
        this.rout = rout;
        this.api = api;
        this.textView = textView;
    }

    @SuppressLint({"MissingInflatedId", "Range"})
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bonus_list_layout, container, false);


        listView = view.findViewById(R.id.listViewBonus);
        array = new  String[]{
                getString(R.string.nal_payment),
                getString(R.string.bonus_payment),
                getString(R.string.card_payment),
        };
        arrayCode = new  String[]{
                "nal_payment",
                "bonus_payment",
                "card_payment",
        };

        CustomArrayAdapter adapter = new CustomArrayAdapter(requireActivity(), R.layout.services_adapter_layout, Arrays.asList(array));
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        fistItem();

        String bonus = logCursor(MainActivity.TABLE_USER_INFO, requireActivity()).get(5);

        if(Long.parseLong(bonus) >= cost * 100 ) {
            List<String> stringList = logCursor(MainActivity.CITY_INFO, requireActivity());

            switch (stringList.get(1)) {
                case "Kyiv City":
                case "Dnipropetrovsk Oblast":
                case "Odessa":
                case "Zaporizhzhia":
                case "Cherkasy Oblast":
                    adapter.setItemEnabled(1, false);
                    listView.setItemChecked(0, true);
                    paymentType(arrayCode [0]);
                    break;
            }
        } else {
            adapter.setItemEnabled(1, false);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos = position;
                Log.d(TAG, "onItemClick: pos " + pos);
                if (pos == 2) {
                    paySystem(new CardFragment.PaySystemCallback() {
                        @Override
                        public void onPaySystemResult(String paymentCode) {
                            Log.d(TAG, "onPaySystemResult: paymentCode" + paymentCode);
                            // Здесь вы можете использовать полученное значение paymentCode
                             paymentType(paymentCode);
                        }

                        @Override
                        public void onPaySystemFailure(String errorMessage) {
                        }
                    });
                } else {
                    paymentType(arrayCode [pos]);
                }

            }

        });

        btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    private void paymentType(String paymentCode) {

        ContentValues cv = new ContentValues();
        Log.d(TAG, "paymentType: paymentCode 1111" + paymentCode);

        cv.put("payment_type", paymentCode);
        // обновляем по id
        if(isAdded()){
            SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
            database.update(MainActivity.TABLE_SETTINGS_INFO, cv, "id = ?",
                    new String[] { "1" });
            database.close();
        }

    }

    @SuppressLint("Range")
    private void fistItem() {

        String payment_type = logCursor(MainActivity.TABLE_SETTINGS_INFO, requireActivity()).get(4);

        Log.d(TAG, "fistItem: " + payment_type);
        switch (payment_type) {
            case "nal_payment":
                listView.setItemChecked(0, true);
                pos = 0;
                paymentType(arrayCode [pos]);
                break;
            case "bonus_payment":
                listView.setItemChecked(1, true);
                pos = 1;
                paymentType(arrayCode [pos]);
                break;
            case "card_payment":
            case "fondy_payment":
            case "mono_payment":
                listView.setItemChecked(2, true);
                pos = 2;
                paySystem(new CardFragment.PaySystemCallback() {
                    @Override
                    public void onPaySystemResult(String paymentCode) {
                        Log.d(TAG, "onPaySystemResult: paymentCode" + paymentCode);
                        // Здесь вы можете использовать полученное значение paymentCode
                        paymentType(paymentCode);

                    }

                    @Override
                    public void onPaySystemFailure(String errorMessage) {
                    }
                });

                break;
        }
   }
    private void paySystem(final CardFragment.PaySystemCallback callback) {
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
                    callback.onPaySystemResult(paymentCodeNew);
                } else {
                    // Обработка ошибки
                    callback.onPaySystemFailure(getString(R.string.verify_internet));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponsePaySystem> call, @NonNull Throwable t) {
                // Обработка ошибки
                callback.onPaySystemFailure(getString(R.string.verify_internet));
            }
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        Log.d(TAG, "onDismiss: rout " + rout);
        if(rout.equals("home")) {
            String urlCost = null;
            Map<String, String> sendUrlMapCost = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    urlCost = getTaxiUrlSearch("costSearch", requireActivity());
                }

                sendUrlMapCost = CostJSONParser.sendURL(urlCost);
            } catch (MalformedURLException | UnsupportedEncodingException ignored) {

            }
            assert sendUrlMapCost != null;
            String orderCost = (String) sendUrlMapCost.get("order_cost");

            assert orderCost != null;
            if (!orderCost.equals("0")) {
                String discountText = logCursor(MainActivity.TABLE_SETTINGS_INFO, getContext()).get(3);
                long discountInt = Integer.parseInt(discountText);
                long discount;
                long firstCost = Long.parseLong(orderCost);
                discount = firstCost * discountInt / 100;


                firstCost = firstCost + discount;
                updateAddCost(String.valueOf(discount));

                HomeFragment.costFirstForMin = firstCost;
                String costUpdate = String.valueOf(firstCost);
                textView.setText(costUpdate);

            }
        }
        if(rout.equals("geo")) {
                String urlCost = null;
                Map<String, String> sendUrlMapCost = null;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        urlCost = getTaxiUrlSearchGeo("costSearchGeo", requireActivity());
                    }

                    sendUrlMapCost = CostJSONParser.sendURL(urlCost);
                } catch (MalformedURLException ignored) {

                }
                String orderCost = (String) sendUrlMapCost.get("order_cost");

            assert orderCost != null;
            if (!orderCost.equals("0")) {
                String discountText = logCursor(MainActivity.TABLE_SETTINGS_INFO, getContext()).get(3);
                long discountInt = Integer.parseInt(discountText);
                long discount;
                long firstCost = Long.parseLong(orderCost);
                discount = firstCost * discountInt / 100;

                firstCost = firstCost + discount;
                updateAddCost(String.valueOf(discount));

                String costUpdate = String.valueOf(firstCost);
                MyGeoDialogFragment.firstCostForMin = firstCost;
                textView.setText(costUpdate);
                }
            }
        if(rout.equals("marker")) {
                String urlCost = null;
                Map<String, String> sendUrlMapCost = null;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        urlCost = getTaxiUrlSearchMarkers("costSearchMarkers", requireActivity());
                    }

                    sendUrlMapCost = CostJSONParser.sendURL(urlCost);
                } catch (MalformedURLException ignored) {

                }
            assert sendUrlMapCost != null;
            String orderCost = (String) sendUrlMapCost.get("order_cost");
            Log.d(TAG, "onDismiss: orderCost " + orderCost);
            assert orderCost != null;
            if (!orderCost.equals("0")) {
                String costUpdate;
                String discountText = logCursor(MainActivity.TABLE_SETTINGS_INFO, getContext()).get(3);
                long discountInt = Integer.parseInt(discountText);
                long discount;
                long firstCost = Long.parseLong(orderCost);
                discount = firstCost * discountInt / 100;

                firstCost = firstCost + discount;
                updateAddCost(String.valueOf(discount));

                MyGeoMarkerDialogFragment.firstCostForMin = firstCost;
                costUpdate = String.valueOf(firstCost);
                textView.setText(costUpdate);
            }
            }
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTaxiUrlSearch(String urlAPI, Context context) throws UnsupportedEncodingException {

        List<String> stringListRout = logCursor(MainActivity.ROUT_HOME, context);

        String originalString = stringListRout.get(1);
        int indexOfSlash = originalString.indexOf("/");
        String from = (indexOfSlash != -1) ? originalString.substring(0, indexOfSlash) : originalString;

        String from_number = stringListRout.get(2);

        originalString = stringListRout.get(3);
        indexOfSlash = originalString.indexOf("/");
        String to = (indexOfSlash != -1) ? originalString.substring(0, indexOfSlash) : originalString;

        String to_number = stringListRout.get(4);

        // Origin of route
        String str_origin = from + "/" + from_number;

        // Destination of route
        String str_dest = to + "/" + to_number;

        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

        List<String> stringList = logCursor(MainActivity.TABLE_SETTINGS_INFO, context);
        String tarif =  stringList.get(2);
        String payment_type = stringList.get(4);

        String parameters = null;
        String phoneNumber = "no phone";
        String userEmail = logCursor(MainActivity.TABLE_USER_INFO, context).get(3);
        String displayName = logCursor(MainActivity.TABLE_USER_INFO, context).get(4);

        if(urlAPI.equals("costSearch")) {
            Cursor c = database.query(MainActivity.TABLE_USER_INFO, null, null, null, null, null, null);
            if (c.getCount() == 1) {
                phoneNumber = logCursor(MainActivity.TABLE_USER_INFO, context).get(2);
                c.close();
            }
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + payment_type;
        }

        // Building the url to the web service
        List<String> services = logCursor(MainActivity.TABLE_SERVICE_INFO, context);
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
            for (int i = 0; i < arrayServiceCode().length; i++) {
                if(services.get(i+1).equals("1")) {
                    servicesChecked.add(arrayServiceCode()[i]);
                }
            }
            for (int i = 0; i < servicesChecked.size(); i++) {
                if(servicesChecked.get(i).equals("CHECK_OUT")) {
                    servicesChecked.set(i, "CHECK");
                }
            }
            result = String.join("*", servicesChecked);
            Log.d(TAG, "getTaxiUrlSearchGeo result:" + result + "/");
        } else {
            result = "no_extra_charge_codes";
        }

        String url = "https://m.easy-order-taxi.site/" + api + "/android/" + urlAPI + "/" + parameters + "/" + result;

        Log.d(TAG, "getTaxiUrlSearch: " + url);

        return url;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private String getTaxiUrlSearchGeo(String urlAPI, Context context) {

        String query = "SELECT * FROM " + MainActivity.ROUT_GEO + " LIMIT 1";
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
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

        // Origin of route
        String str_origin = originLatitude + "/" + originLongitude;

        // Destination of route
        String str_dest = to + "/" + to_number;

        List<String> stringList = logCursor(MainActivity.TABLE_SETTINGS_INFO, context);
        String tarif =  stringList.get(2);
        String payment_type = stringList.get(4);

        // Building the parameters to the web service

        String parameters = null;
        String phoneNumber = "no phone";
        String userEmail = logCursor(MainActivity.TABLE_USER_INFO, context).get(3);
        String displayName = logCursor(MainActivity.TABLE_USER_INFO, context).get(4);

        if(urlAPI.equals("costSearchGeo")) {
            Cursor c = database.query(MainActivity.TABLE_USER_INFO, null, null, null, null, null, null);

            if (c.getCount() == 1) {
                phoneNumber = logCursor(MainActivity.TABLE_USER_INFO, context).get(2);
                c.close();
            }
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + payment_type;
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
            Log.d(TAG, "getTaxiUrlSearchGeo result:" + result + "/");
        } else {
            result = "no_extra_charge_codes";
        }

        String url = "https://m.easy-order-taxi.site/" + api + "/android/" + urlAPI + "/" + parameters + "/" + result;
        Log.d(TAG, "getTaxiUrlSearch services: " + url);

        return url;


    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTaxiUrlSearchMarkers(String urlAPI, Context context) {

        List<String> stringListRout = logCursor(MainActivity.ROUT_MARKER, context);
        Log.d(TAG, "getTaxiUrlSearch: stringListRout" + stringListRout);

        double originLatitude = Double.parseDouble(stringListRout.get(1));
        double originLongitude = Double.parseDouble(stringListRout.get(2));
        double toLatitude = Double.parseDouble(stringListRout.get(3));
        double toLongitude = Double.parseDouble(stringListRout.get(4));

        // Origin of route
        String str_origin = originLatitude + "/" + originLongitude;

        // Destination of route
        String str_dest = toLatitude + "/" + toLongitude;

        //        Cursor cursorDb = MainActivity.database.query(MainActivity.TABLE_SETTINGS_INFO, null, null, null, null, null, null);
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

        List<String> stringList = logCursor(MainActivity.TABLE_SETTINGS_INFO, context);
        String tarif =  stringList.get(2);
        String payment_type = stringList.get(4);

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
                    + displayName + "*" + userEmail  + "*" + payment_type;
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
            Log.d(TAG, "getTaxiUrlSearchGeo result:" + result + "/");
        } else {
            result = "no_extra_charge_codes";
        }

        String url = "https://m.easy-order-taxi.site/" + api + "/android/" + urlAPI + "/" + parameters + "/" + result;


        database.close();


        return url;

    }


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
    @SuppressLint("Range")
    private List<String> logCursor(String table, Context context) {
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
   }

