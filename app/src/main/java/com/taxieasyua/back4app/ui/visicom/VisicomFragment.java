package com.taxieasyua.back4app.ui.visicom;


import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.databinding.FragmentVisicomBinding;
import com.taxieasyua.back4app.ui.finish.ApiClient;
import com.taxieasyua.back4app.ui.finish.ApiService;
import com.taxieasyua.back4app.ui.finish.City;
import com.taxieasyua.back4app.ui.finish.FinishActivity;
import com.taxieasyua.back4app.ui.home.MyBottomSheetBonusFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetCityFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetErrorFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetGPSFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetGeoFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetMessageFragment;
import com.taxieasyua.back4app.ui.home.MyPhoneDialogFragment;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;
import com.taxieasyua.back4app.ui.maps.FromJSONParser;
import com.taxieasyua.back4app.ui.maps.ToJSONParser;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapVisicomActivity;
import com.taxieasyua.back4app.ui.open_map.visicom.ActivityVisicomOnePage;
import com.taxieasyua.back4app.ui.open_map.visicom.MyBottomSheetVisicomOnePageFragment;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisicomFragment extends Fragment {

    public static ProgressBar progressBar;
    private FragmentVisicomBinding binding;
    private static final String TAG = "TAG_VISICOM";
    private MyPhoneDialogFragment bottomSheetDialogFragment;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    FloatingActionButton fab_call;

    public static AppCompatButton button, btn_change, btn_minus, btn_plus, btnOrder, buttonBonus, gpsbut;
    public static TextView geoText;
    static String api;
    private ArrayList<Map> adressArr = new ArrayList<>();
    public static long firstCost;

    @SuppressLint("StaticFieldLeak")
    public static TextView text_view_cost;
    @SuppressLint("StaticFieldLeak")
    public static TextView textViewTo;
    @SuppressLint("StaticFieldLeak")
    public static EditText to_number;
    public static String numberFlagTo;

    public static long cost;
    public static long addCost;
    public static String to;
    public static String geo_marker;
    String pay_method;
    public static String urlOrder;
    public static long MIN_COST_VALUE;
    public static long firstCostForMin;
    private static long discount;
    private String apiKey; // Впишіть апі ключ
    private final OkHttpClient client = new OkHttpClient();

    private static List<String> addresses;

    public static AppCompatButton btnAdd, btn_clear_from_text;

    public static ImageButton btn_clear_from, btn_clear_to;
    public static TextView textwhere, num2;


    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentVisicomBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        progressBar = binding.progressBar;

        fab_call = binding.fabCall;
        fab_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                List<String> stringList = logCursor(MainActivity.CITY_INFO, requireActivity());
                String phone = stringList.get(3);
                intent.setData(Uri.parse(phone));
                startActivity(intent);
            }
        });



        buttonBonus = binding.btnBonus;
        apiKey = requireActivity().getString(R.string.visicom_key_storage);

        List<String> stringList = logCursor(MainActivity.CITY_INFO, requireActivity());
        api =  stringList.get(2);

        if (!routMaps().isEmpty()) {
            adressArr = new ArrayList<>(routMaps().size());
        }

        addCost = 0;
        updateAddCost(String.valueOf(addCost));

        numberFlagTo = "2";
        progressBar = binding.progressBar;

        geoText = binding.textGeo;

        btn_clear_from_text = binding.btnClearFromText;
        btn_clear_from_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ActivityVisicomOnePage.class);
                intent.putExtra("start", "ok");
                intent.putExtra("end", "no");
                startActivity(intent);

            }
        });

        if(geoText.getText().toString().equals("")) {
            btn_clear_from_text.setVisibility(View.VISIBLE);
            String unuString = new String(Character.toChars(0x1F449));
            unuString += " " + getString(R.string.search_text);
            btn_clear_from_text.setText(unuString);
        }

        text_view_cost = binding.textViewCost;

        geo_marker = "visicom";

        Log.d(TAG, "onCreateView: geo_marker " + geo_marker);

        buttonBonus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyBottomSheetBonusFragment bottomSheetDialogFragment = new MyBottomSheetBonusFragment(Long.parseLong(text_view_cost.getText().toString()), geo_marker, api, text_view_cost);
                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

        textViewTo = binding.textTo;
        textViewTo.setText(getString(R.string.on_city_tv));

        addresses = new ArrayList<>();

        btn_minus = binding.btnMinus;
        btn_plus = binding.btnPlus;
        btnOrder = binding.btnOrder;

        btnAdd = binding.btnAdd;
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyBottomSheetGeoFragment bottomSheetDialogFragment = new MyBottomSheetGeoFragment(text_view_cost);
                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

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
        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    progressBar.setVisibility(View.VISIBLE);
                    List<String> stringList = logCursor(MainActivity.CITY_INFO, requireActivity());

                    pay_method =  logCursor(MainActivity.TABLE_SETTINGS_INFO, requireActivity()).get(4);

                    switch (stringList.get(1)) {
                        case "Kyiv City":
                        case "Dnipropetrovsk Oblast":
                        case "Odessa":
                        case "Zaporizhzhia":
                        case "Cherkasy Oblast":
                            break;
                        case "OdessaTest":
                            if(pay_method.equals("bonus_payment")) {
                                String bonus = logCursor(MainActivity.TABLE_USER_INFO, requireActivity()).get(5);
                                if(Long.parseLong(bonus) < cost * 100 ) {
                                    paymentType("nal_payment");
                                }
                            }
                            break;
                    }

                    Log.d(TAG, "onClick: pay_method " + pay_method );
                    List<String> stringListCity = logCursor(MainActivity.CITY_INFO, requireActivity());
                    String card_max_pay = stringListCity.get(4);
                    String bonus_max_pay = stringListCity.get(5);
                    switch (pay_method) {
                        case "bonus_payment":
                            if (Long.parseLong(bonus_max_pay) <= Long.parseLong(text_view_cost.getText().toString()) * 100) {
                                changePayMethodMax(text_view_cost.getText().toString(), pay_method);
                            } else {
                                orderRout();

                                try {
                                    if (verifyPhone(requireContext())) {
                                        orderFinished();
                                    }
                                } catch (MalformedURLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            break;
                        case "card_payment":
                        case "fondy_payment":
                        case "mono_payment":
                            if (Long.parseLong(card_max_pay) <= Long.parseLong(text_view_cost.getText().toString())) {
                                changePayMethodMax(text_view_cost.getText().toString(), pay_method);
                            } else {
                                orderRout();

                                try {
                                    if (verifyPhone(requireContext())) {
                                        orderFinished();
                                    }
                                } catch (MalformedURLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            break;
                        default:
                            orderRout();
                            if (verifyPhone(requireContext())) {
                                try {
                                    if (verifyPhone(requireContext())) {
                                        orderFinished();
                                    }
                                } catch (MalformedURLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            break;

                    }
                }
            }
        });
        btn_clear_from = binding.btnClearFrom;
        btn_clear_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geoText.setText("");
                Intent intent = new Intent(getContext(), ActivityVisicomOnePage.class);
                intent.putExtra("start", "ok");
                intent.putExtra("end", "no");
                startActivity(intent);
            }
        });
        btn_clear_to = binding.btnClearTo;
        btn_clear_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewTo.setText("");
                Intent intent = new Intent(getContext(), ActivityVisicomOnePage.class);
                intent.putExtra("start", "no");
                intent.putExtra("end", "ok");
                startActivity(intent);
            }
        });
        textwhere = binding.textwhere;
        num2 = binding.num2;


        gpsbut = binding.gpsbut;
        gpsbut.setOnClickListener(v -> {
            LocationManager lm = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;
            boolean network_enabled = false;

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ignored) {
            }

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ignored) {
            }

            if(!gps_enabled || !network_enabled) {
                MyBottomSheetGPSFragment bottomSheetDialogFragment = new MyBottomSheetGPSFragment();
                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            }  else  {
                // Разрешения уже предоставлены, выполнить ваш код
                if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                    checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                }  else {
                    String message = getString(R.string.gps_ok);
                    MyBottomSheetMessageFragment bottomSheetDialogFragment = new MyBottomSheetMessageFragment(message);
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            }

        });

        getLocalIpAddress();
        if(newRout()) {
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                firstLocationGPS();
            }
        } else {
            visicomCost();
        }

        return root;
    }

    public void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(requireActivity(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{permission}, requestCode);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
        database.close();
        return list;
    }

    private void getLocalIpAddress() {

        List<String> city = logCursor(MainActivity.CITY_INFO, requireActivity());
        Log.d(TAG, "getLocalIpAddress: city.get(1)" + city.get(1));
        if(city.size() != 0 && city.get(1).equals("")) {
//            VisicomFragment.progressBar.setVisibility(View.VISIBLE);
            ApiService apiService = ApiClient.getApiService();

            Call<City> call = apiService.cityOrder();

            call.enqueue(new Callback<City>() {
                @Override
                public void onResponse(@NonNull Call<City> call, @NonNull Response<City> response) {
                    if (response.isSuccessful()) {
                        City status = response.body();
                        if (status != null) {
                            String result = status.getResponse();
                            Log.d("TAG", "onResponse:result " + result);
                            MyBottomSheetCityFragment bottomSheetDialogFragment = new MyBottomSheetCityFragment(result);
                            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                        }
                    } else {
                        MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                    }
                }

                @Override
                public void onFailure(Call<City> call, Throwable t) {
                    // Обработка ошибок сети или других ошибок
                    String errorMessage = t.getMessage();
                    t.printStackTrace();
                    Log.d("TAG", "onFailure: " + errorMessage);

                }
            });
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

    private boolean newRout() {
        boolean result = false;


        String query = "SELECT * FROM " + MainActivity.ROUT_MARKER + " LIMIT 1";
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();

        // Получите значения полей из первой записи

        @SuppressLint("Range") double originLatitude = cursor.getDouble(cursor.getColumnIndex("startLat"));
        @SuppressLint("Range") String start = cursor.getString(cursor.getColumnIndex("start"));
        @SuppressLint("Range") String finish = cursor.getString(cursor.getColumnIndex("finish"));


        if (originLatitude == 0) {
            result = true;
        } else {
            geoText.setText(start);
            textViewTo.setText(finish);
        }
        cursor.close();
        database.close();

        Log.d(TAG, "newRout: result" + result);

        return result;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("Range")
    public String getTaxiUrlSearchMarkers(String urlAPI, Context context) {
        Log.d(TAG, "getTaxiUrlSearchMarkers: " + urlAPI);

        String query = "SELECT * FROM " + MainActivity.ROUT_MARKER + " LIMIT 1";
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();

        // Получите значения полей из первой записи

        double originLatitude = cursor.getDouble(cursor.getColumnIndex("startLat"));
        double originLongitude = cursor.getDouble(cursor.getColumnIndex("startLan"));
        double toLatitude = cursor.getDouble(cursor.getColumnIndex("to_lat"));
        double toLongitude = cursor.getDouble(cursor.getColumnIndex("to_lng"));
        String start = cursor.getString(cursor.getColumnIndex("start"));
        String finish = cursor.getString(cursor.getColumnIndex("finish"));
        Log.d(TAG, "getTaxiUrlSearchMarkers: start " + start);
        // Заменяем символ '/' в строках
        start = start.replace("/", "|");
        finish = finish.replace("/", "|");

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
        String payment_type = stringListInfo.get(4);
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
                    + displayName + "*" + userEmail  + "*" + payment_type;
        }
        if(urlAPI.equals("orderSearchMarkersVisicom")) {
            phoneNumber = logCursor(MainActivity.TABLE_USER_INFO, context).get(2);


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

        List<String> listCity = logCursor(MainActivity.CITY_INFO, requireActivity());
        String city = listCity.get(1);
        String api = listCity.get(2);

        String url = "https://m.easy-order-taxi.site/" + api + "/android/" + urlAPI + "/"
                + parameters + "/" + result + "/" + city  + "/" + context.getString(R.string.application);

        database.close();

        return url;
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
    ArrayList<Map> routMaps() {
        Map <String, String> routs;
        ArrayList<Map> routsArr = new ArrayList<>();
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
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

                    routs.put("from_lat", c.getString(c.getColumnIndexOrThrow ("from_lat")));
                    routs.put("from_lng", c.getString(c.getColumnIndexOrThrow ("from_lng")));

                    routs.put("to_lat", c.getString(c.getColumnIndexOrThrow ("to_lat")));
                    routs.put("to_lng", c.getString(c.getColumnIndexOrThrow ("to_lng")));
                    routsArr.add(i++, routs);
                } while (c.moveToNext());
            }
        }
        database.close();

        return routsArr;
    }
    @SuppressLint("Range")
    private double getFromTablePositionInfo(Context context, String columnName) {
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery("SELECT "+ columnName + " FROM " + MainActivity.TABLE_POSITION_INFO + " WHERE id = ?", new String[]{"1"});

        double result = 0.0; // Значение по умолчанию или обработка, если запись не найдена.

        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getDouble(cursor.getColumnIndex(columnName));
            cursor.close();
        }

        database.close();

        return result;
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void orderRout() {
        if(!verifyOrder(requireContext())) {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.black_list_message));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            return;
        }

        urlOrder = getTaxiUrlSearchMarkers( "orderSearchMarkersVisicom", requireActivity());
        Log.d(TAG, "order: urlOrder "  + urlOrder);
        if (!verifyPhone(requireContext())) {
            getPhoneNumber();
        }
        if (!verifyPhone(requireActivity())) {
            bottomSheetDialogFragment = new MyPhoneDialogFragment("visicom", text_view_cost.getText().toString());
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
    public void orderFinished() throws MalformedURLException {
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
                            sendUrlMap.get("from_lat"), sendUrlMap.get("from_lng"),
                            sendUrlMap.get("from_lat"), sendUrlMap.get("from_lng"),
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
                            sendUrlMap.get("from_lat"), sendUrlMap.get("from_lng"),
                            sendUrlMap.get("lat"), sendUrlMap.get("lng"),
                            requireActivity()
                    );
                }
            }
            String messageResult = getString(R.string.thanks_message) +
                    sendUrlMap.get("routefrom") + " " + getString(R.string.to_message) +
                    to_name + "." +
                    getString(R.string.call_of_order) + orderWeb + getString(R.string.UAH);
            String messageFondy = getString(R.string.fondy_message) + " " +
                    sendUrlMap.get("routefrom") + " " + getString(R.string.to_message) +
                    to_name + ".";

            Intent intent = new Intent(requireActivity(), FinishActivity.class);
            intent.putExtra("messageResult_key", messageResult);
            intent.putExtra("messageFondy_key", messageFondy);
            intent.putExtra("messageCost_key", orderWeb);
            intent.putExtra("sendUrlMap", new HashMap<>(sendUrlMap));
            intent.putExtra("UID_key", Objects.requireNonNull(sendUrlMap.get("dispatching_order_uid")));
            startActivity(intent);
        } else {

            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void updateRoutGeo(List<String> settings) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        cv.put("startLat",  Double.parseDouble(settings.get(0)));
        database.update(MainActivity.ROUT_GEO, cv, "id = ?",
                new String[] { "1" });
        cv.put("startLan", Double.parseDouble(settings.get(1)));
        database.update(MainActivity.ROUT_GEO, cv, "id = ?",
                new String[] { "1" });
        cv.put("toCost", settings.get(2));
        database.update(MainActivity.ROUT_GEO, cv, "id = ?",
                new String[] { "1" });
        cv.put("to_numberCost", settings.get(3));
        database.update(MainActivity.ROUT_GEO, cv, "id = ?",
                new String[] { "1" });

        // обновляем по id

        database.update(MainActivity.ROUT_GEO, cv, "id = ?",
                new String[] { "1" });
        database.close();

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
        Log.d(TAG, "verifyPhone: " + verify);
        return verify;
    }
    private void getPhoneNumber () {
        String mPhoneNumber;
        TelephonyManager tMgr = (TelephonyManager) requireActivity().getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Manifest.permission.READ_PHONE_NUMBERS: " + ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_PHONE_NUMBERS));
            Log.d("TAG", "Manifest.permission.READ_PHONE_STATE: " + ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_PHONE_STATE));
            return;
        }
        mPhoneNumber = tMgr.getLine1Number();
//        mPhoneNumber = null;
        if(mPhoneNumber != null) {
            String PHONE_PATTERN = "((\\+?380)(\\d{9}))$";
            boolean val = Pattern.compile(PHONE_PATTERN).matcher(mPhoneNumber).matches();
            Log.d("TAG", "onClick No validate: " + val);
            if (val == false) {
                Toast.makeText(requireActivity(), getString(R.string.format_phone) , Toast.LENGTH_SHORT).show();
                Log.d("TAG", "onClick:phoneNumber.getText().toString() " + mPhoneNumber);
//                requireActivity().finish();

            } else {
                updateRecordsUser(mPhoneNumber, getContext());
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
        Log.d(TAG, "insertRecordsOrders: from_lat" + from_lat);
        Log.d(TAG, "insertRecordsOrders: from_lng" + from_lng);

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

    private void changePayMethodMax(String textCost, String paymentType) {
        List<String> stringListCity = logCursor(MainActivity.CITY_INFO, requireActivity());

        String card_max_pay =  stringListCity.get(4);
        String bonus_max_pay =  stringListCity.get(5);
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

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        orderRout();
                    }
                    if (verifyPhone(requireContext())) {
                        orderFinished();
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                progressBar.setVisibility(View.GONE);
                alertDialog.dismiss();
            }
        });

        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.GONE);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();

    }


    private void firstLocationGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationCallback = new LocationCallback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                // Обработка полученных местоположений
                stopLocationUpdates();

                // Обработка полученных местоположений
                List<Location> locations = locationResult.getLocations();
                Log.d(TAG, "onLocationResult: locations 222222" + locations);

                if (!locations.isEmpty()) {
                    Location firstLocation = locations.get(0);

                    double latitude = firstLocation.getLatitude();
                    double longitude = firstLocation.getLongitude();


                    List<String> stringList = logCursor(MainActivity.CITY_INFO, requireActivity());
                    String api =  stringList.get(2);

                    String urlFrom = "https://m.easy-order-taxi.site/" + api + "/android/fromSearchGeo/" + latitude + "/" + longitude;
                    Map sendUrlFrom = null;
                    try {
                        sendUrlFrom = FromJSONParser.sendURL(urlFrom);

                    } catch (MalformedURLException | InterruptedException |
                             JSONException e) {
                        MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                        bottomSheetDialogFragment.show(getParentFragmentManager(), bottomSheetDialogFragment.getTag());
                    }
                    assert sendUrlFrom != null;
                    String FromAdressString = (String) sendUrlFrom.get("route_address_from");
                    if (FromAdressString != null) {
                        if (FromAdressString.equals("Точка на карте")) {
                            FromAdressString = getString(R.string.startPoint);
                        }
                    }
                    updateMyPosition(latitude, longitude, FromAdressString, requireActivity());
                    geoText.setText(FromAdressString);
                    btn_clear_from_text.setVisibility(View.INVISIBLE);
                    btn_clear_from.setVisibility(View.VISIBLE);

                    geoText.setVisibility(View.VISIBLE);

                    List<String> settings = new ArrayList<>();
                    String ToAdressString = textViewTo.getText().toString();
                    if(ToAdressString.equals(getString(R.string.on_city_tv)) ||
                            ToAdressString.equals("") ) {
                        settings.add(Double.toString(latitude));
                        settings.add(Double.toString(longitude));
                        settings.add(Double.toString(latitude));
                        settings.add(Double.toString(longitude));
                        settings.add(FromAdressString);
                        settings.add(getString(R.string.on_city_tv));
                    } else {

                        String query = "SELECT * FROM " + MainActivity.ROUT_MARKER + " LIMIT 1";
                        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
                        Cursor cursor = database.rawQuery(query, null);

                        cursor.moveToFirst();

                        // Получите значения полей из первой записи


                        @SuppressLint("Range") double toLatitude = cursor.getDouble(cursor.getColumnIndex("to_lat"));
                        @SuppressLint("Range") double toLongitude = cursor.getDouble(cursor.getColumnIndex("to_lng"));
                        cursor.close();
                        database.close();
//
//                        @SuppressLint("Range") String finish = cursor.getString(cursor.getColumnIndex("finish"));


                        settings.add(Double.toString(latitude));
                        settings.add(Double.toString(longitude));
                        settings.add(Double.toString(toLatitude));
                        settings.add(Double.toString(toLongitude));
                        settings.add(FromAdressString);
                        settings.add(ToAdressString);
                    }



                    updateRoutMarker(settings);
                    if(!logCursor(MainActivity.TABLE_USER_INFO, getContext()).get(3).equals("email")) {
                        visicomCost();
                    }

                    btn_change.setVisibility(View.VISIBLE);
                }
            }
        };

    }



    private void updateRoutMarker(List<String> settings) {
        Log.d(TAG, "updateRoutMarker: " + settings.toString());
        ContentValues cv = new ContentValues();
            cv.put("startLat", Double.parseDouble(settings.get(0)));
            cv.put("startLan", Double.parseDouble(settings.get(1)));
            cv.put("to_lat", Double.parseDouble(settings.get(0)));
            cv.put("to_lng", Double.parseDouble(settings.get(1)));
            cv.put("start", settings.get(4));
            cv.put("finish", settings.get(5));

        // обновляем по id
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        database.update(MainActivity.ROUT_MARKER, cv, "id = ?",
                new String[]{"1"});
        database.close();
    }
    private static void updateMyPosition(Double startLat, Double startLan, String position, Context context) {
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        ContentValues cv = new ContentValues();

        cv.put("startLat", startLat);
        database.update(MainActivity.TABLE_POSITION_INFO, cv, "id = ?",
                new String[] { "1" });
        cv.put("startLan", startLan);
        database.update(MainActivity.TABLE_POSITION_INFO, cv, "id = ?",
                new String[] { "1" });
        cv.put("position", position);
        database.update(MainActivity.TABLE_POSITION_INFO, cv, "id = ?",
                new String[] { "1" });
        database.close();

    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = createLocationRequest();
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }


    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000); // Интервал обновления местоположения в миллисекундах
        locationRequest.setFastestInterval(100); // Самый быстрый интервал обновления местоположения в миллисекундах
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Приоритет точного местоположения
        return locationRequest;
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
//            // Показываем объяснение пользователю, почему мы запрашиваем разрешение
//            // Можно использовать диалоговое окно или другой пользовательский интерфейс
//            MyBottomSheetGPSFragment bottomSheetDialogFragment = new MyBottomSheetGPSFragment();
//            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        } else {
//            ActivityCompat.requestPermissions(requireActivity(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void visicomCost() {
        String query = "SELECT * FROM " + MainActivity.ROUT_MARKER + " LIMIT 1";
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();

        // Получите значения полей из первой записи

        @SuppressLint("Range") String start = cursor.getString(cursor.getColumnIndex("start"));
        @SuppressLint("Range") String finish = cursor.getString(cursor.getColumnIndex("finish"));

        geoText.setText(start);
        textViewTo.setText(finish);

        String urlCost = getTaxiUrlSearchMarkers("costSearchMarkers", requireActivity());
        Log.d(TAG, "visicomCost: " + urlCost);
        Map<String, String> sendUrlMapCost = null;
        try {
            sendUrlMapCost = CostJSONParser.sendURL(urlCost);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        String message = sendUrlMapCost.get("message");
        String orderCost = sendUrlMapCost.get("order_cost");
        Log.d(TAG, "startCost: orderCost " + orderCost);

        assert orderCost != null;
        if (orderCost.equals("0")) {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        } else {
            String discountText = logCursor(MainActivity.TABLE_SETTINGS_INFO, requireContext()).get(3);
            long discountInt = Integer.parseInt(discountText);
            long discount;

                    VisicomFragment.firstCost = Long.parseLong(orderCost);
                    discount = VisicomFragment.firstCost * discountInt / 100;
                    VisicomFragment.firstCost = VisicomFragment.firstCost + discount;
                    updateAddCost(String.valueOf(discount));
                    VisicomFragment.text_view_cost.setText(String.valueOf(VisicomFragment.firstCost));
                    VisicomFragment.MIN_COST_VALUE = (long) (VisicomFragment.firstCost * 0.6);
                    VisicomFragment.firstCostForMin = VisicomFragment.firstCost;


                    VisicomFragment.geoText.setVisibility(View.VISIBLE);
                    VisicomFragment.btn_clear_from.setVisibility(View.VISIBLE);
                    VisicomFragment.textwhere.setVisibility(View.VISIBLE);
                    VisicomFragment.num2.setVisibility(View.VISIBLE);
                    VisicomFragment.textViewTo.setVisibility(View.VISIBLE);
                    VisicomFragment.btn_clear_to.setVisibility(View.VISIBLE);
                    VisicomFragment.btnAdd.setVisibility(View.VISIBLE);
                    VisicomFragment.buttonBonus.setVisibility(View.VISIBLE);
                    VisicomFragment.btn_minus.setVisibility(View.VISIBLE);
                    VisicomFragment.text_view_cost.setVisibility(View.VISIBLE);
                    VisicomFragment.btn_plus.setVisibility(View.VISIBLE);
                    VisicomFragment.btnOrder.setVisibility(View.VISIBLE);

                    VisicomFragment.btn_clear_from_text.setVisibility(View.GONE);
        }


    }
}