package com.taxieasyua.back4app.ui.open_map.visicom;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.BlendMode;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.cities.Cherkasy.Cherkasy;
import com.taxieasyua.back4app.cities.Dnipro.Dnipro;
import com.taxieasyua.back4app.cities.Kyiv.KyivCity;
import com.taxieasyua.back4app.cities.Odessa.Odessa;
import com.taxieasyua.back4app.cities.Odessa.OdessaTest;
import com.taxieasyua.back4app.cities.Zaporizhzhia.Zaporizhzhia;
import com.taxieasyua.back4app.ui.finish.FinishActivity;
import com.taxieasyua.back4app.ui.home.MyBottomSheetBlackListFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetBonusFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetErrorFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetGeoFragment;
import com.taxieasyua.back4app.ui.home.MyPhoneDialogFragment;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;
import com.taxieasyua.back4app.ui.maps.FromJSONParser;
import com.taxieasyua.back4app.ui.maps.ToJSONParser;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;
import com.taxieasyua.back4app.ui.open_map.visicom.resp.GeocodeGeoCentroid;
import com.taxieasyua.back4app.ui.open_map.visicom.resp.GeocodeResponse;
import com.taxieasyua.back4app.ui.start.ResultSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class GeoDialogVisicomFragment extends BottomSheetDialogFragment {
    private static final String TAG = "TAG_GEO";
    public TextView geoText;
    public static AppCompatButton button, old_address, btn_minus, btn_plus, btnOrder, buttonBonus;
    static String api;
    private ArrayList<Map> adressArr = new ArrayList<>();
    long firstCost;

    @SuppressLint("StaticFieldLeak")
    public static TextView text_view_cost;
    @SuppressLint("StaticFieldLeak")
    public static EditText textViewTo;
    @SuppressLint("StaticFieldLeak")
    public static EditText to_number;
    public static String numberFlagTo;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    @SuppressLint("StaticFieldLeak")
    static GeoDialogVisicomFragment fragment;
    public static long cost;
    public static long addCost;
    public static String to;
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar progressBar;
    static String urlAddress;
    public static String geo_marker;
    String pay_method;
    public static String urlOrder;
    private long MIN_COST_VALUE;
    public static long firstCostForMin;
    private static long discount;

    private ListView addressListView;
    private ArrayAdapter<String> addressAdapter;

    private final String apiUrl = "https://api.visicom.ua/data-api/5.0/uk/geocode.json";
    private final String apiKey = "77bb21fd8ee6cbfde9bc5733e01eaf59"; // Впишіть апі ключ
    private final OkHttpClient client = new OkHttpClient();

    private static List<double[]> coordinatesList;
    private static List<String> addresses;
    private String citySearch;
    BottomSheetBehavior<View> bottomSheetBehavior;

    public static GeoDialogVisicomFragment newInstance() {
        fragment = new GeoDialogVisicomFragment();
        return fragment;
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.geo_visicom_layout, container, false);
        buttonBonus = view.findViewById(R.id.btnBonus);



        List<String> stringList = logCursor(MainActivity.CITY_INFO, requireActivity());
        api =  stringList.get(2);
        switch (stringList.get(1)){
            case "Dnipropetrovsk Oblast":
//                arrayStreet = Dnipro.arrayStreet();
                break;
            case "Odessa":
                citySearch = "Одеса";
//                arrayStreet = Odessa.arrayStreet();
                break;
            case "Zaporizhzhia":
//                arrayStreet = Zaporizhzhia.arrayStreet();
                break;
            case "Cherkasy Oblast":
//                arrayStreet = Cherkasy.arrayStreet();
                break;
            case "OdessaTest":
                citySearch = "Одеса";
//                arrayStreet = OdessaTest.arrayStreet();
                break;
            default:
//                arrayStreet = KyivCity.arrayStreet();
                break;
        }


        if (!routMaps().isEmpty()) {
            adressArr = new ArrayList<>(routMaps().size());
        }

        addCost = 0;
        updateAddCost(String.valueOf(addCost));

        numberFlagTo = "2";
        progressBar = view.findViewById(R.id.progress_bar);
        geoText = view.findViewById(R.id.textGeo);
        Log.d("TAG", "onCreateView: OpenStreetMapActivity.FromAdressString" + OpenStreetMapActivity.FromAdressString);
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
        geo_marker = "geo";

        Log.d(TAG, "onCreateView: geo_marker " + geo_marker);
        buttonBonus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyBottomSheetBonusFragment bottomSheetDialogFragment = new MyBottomSheetBonusFragment(Long.parseLong(text_view_cost.getText().toString()), geo_marker, api, text_view_cost);
                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

        textViewTo = view.findViewById(R.id.text_to);
        addresses = new ArrayList<>();

        btn_minus = view.findViewById(R.id.btn_minus);
        btn_plus = view.findViewById(R.id.btn_plus);
        btnOrder = view.findViewById(R.id.btnOrder);


        addressListView = view.findViewById(R.id.addressListView);

        List<String> addresses = new ArrayList<>();
        addressAdapter = new ArrayAdapter<>(requireActivity(), R.layout.drop_down_layout, addresses);
        addressListView.setAdapter(addressAdapter);

        addressListView.setOnItemClickListener((parent, viewC, position, id) -> {

            // Получить координаты по позиции элемента в списке
            if (position < coordinatesList.size()) {
                double[] coordinates = coordinatesList.get(position);
                Log.d(TAG, "Clicked item at position " + position + ": [" + coordinates[0] + ", " + coordinates[1] + "]");
                textViewTo.setText(addresses.get(position));

                List<String> settings = new ArrayList<>();
                settings.add(Double.toString(OpenStreetMapActivity.startLat));
                settings.add(Double.toString(OpenStreetMapActivity.startLan));
                settings.add(Double.toString(coordinates[1]));
                settings.add(Double.toString(coordinates[0]));

                settings.add(geoText.getText().toString());
                settings.add(textViewTo.getText().toString());

                Log.d(TAG, "startCost: marker " + settings);

                updateRoutMarker(settings);

                textViewTo.setSelection(textViewTo.getText().toString().length());

                // Здесь вы можете выполнить дополнительные действия с полученными координатами

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    visicomCost();
                }
            }
        });
        textViewTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Вызывается перед изменением текста
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Вызывается при изменении текста
                performAddressSearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Вызывается после изменения текста
            }
        });

        button = view.findViewById(R.id.change);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                geoText.setText("");
                Toast.makeText(requireActivity(), R.string.check_position, Toast.LENGTH_SHORT).show();
                Configuration.getInstance().load(requireActivity(), PreferenceManager.getDefaultSharedPreferences(requireActivity()));


                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());


                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // Обработка полученных местоположений
                        stopLocationUpdates();

                        // Обработка полученных местоположений
                        List<Location> locations = locationResult.getLocations();
                        if (!locations.isEmpty()) {
                            Location firstLocation = locations.get(0);
                            if (OpenStreetMapActivity.startLat != firstLocation.getLatitude() && OpenStreetMapActivity.startLan != firstLocation.getLongitude()){

                                double latitude = firstLocation.getLatitude();
                                double longitude = firstLocation.getLongitude();
                                OpenStreetMapActivity.startLat = latitude;
                                OpenStreetMapActivity.startLan = longitude;
                                String urlFrom = "https://m.easy-order-taxi.site/" + api + "/android/fromSearchGeo/" +
                                        String.valueOf(OpenStreetMapActivity.startLat) + "/" + String.valueOf(OpenStreetMapActivity.startLan);
                                Map sendUrlFrom = null;
                                try {
                                    sendUrlFrom = FromJSONParser.sendURL(urlFrom);

                                } catch (MalformedURLException | InterruptedException |
                                         JSONException e) {
                                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());

                                }
                                OpenStreetMapActivity.FromAdressString = (String) sendUrlFrom.get("route_address_from");

                                updateMyPosition(OpenStreetMapActivity.startLat, OpenStreetMapActivity.startLan, OpenStreetMapActivity.FromAdressString);
                                    requireActivity().finish();
                                 startActivity(new Intent(requireActivity(), OpenStreetMapActivity.class));
                            }
                        }

                    };
                };
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    requestLocationPermission();
                }

            }
        });
        old_address = view.findViewById(R.id.old_address);
        String[] array = arrayAdressAdapter();
        if(array.length == 0) {
            old_address.setVisibility(View.INVISIBLE);
        }
        old_address.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            dialogFromToGeoAdress(array);
                        } catch (MalformedURLException | InterruptedException |
                                 JSONException e) {
                            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                        }
                    }
                });

        AppCompatButton buttonAddServicesView =  view.findViewById(R.id.btnAdd);
        buttonAddServicesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyBottomSheetGeoFragment bottomSheetDialogFragment = new MyBottomSheetGeoFragment();
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
                    switch (pay_method) {
                        case "bonus_payment":
                        case "card_payment":
                        case "fondy_payment":
                        case "mono_payment":
                            changePayMethodMax(text_view_cost.getText().toString(), pay_method);
                            break;
                        default:
                            orderRout();
                            if (verifyPhone(requireContext())) {
                                try {
                                    orderFinished();
                                } catch (MalformedURLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            break;

                    }
                }
            }
        });


        startCost();
        OpenStreetMapActivity.progressBar.setVisibility(View.INVISIBLE);

        return view;
    }

    private void performAddressSearch(String inputText) {
        try {
            int charCount = inputText.length();
            String url = apiUrl;

            if (charCount > 3) {
                // Если символов больше 4 и отсутствует ",N", то выполняем первую строку
                if (!inputText.substring(4).contains(", ")) {
                    url = apiUrl + "?categories=adr_street&text=" + inputText + "&key=" + apiKey;
                } else {
                    // Если ", " присутствует после первых четырех символов, то выполняем вторую строку
                    url = apiUrl + "?text=" + inputText + "&key=" + apiKey;
                }
            }

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "performAddressSearch: " + url);
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                    try {
                        String responseData = response.body().string();
                        Log.d(TAG, "onResponse: " + responseData);
                        processAddressData(responseData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            // Log the exception or display an error message
        }
    }

    private void processAddressData(String responseData) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);
            JSONArray features = jsonResponse.getJSONArray("features");
            Log.d(TAG, "processAddressData: features" + features);
            addresses = new ArrayList<>();
            coordinatesList = new ArrayList<>(); // Список для хранения координат


            for (int i = 0; i < Math.min(features.length(), 5); i++) {
                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                Log.d(TAG, "processAddressData: properties" + properties);
                JSONObject geoCentroid = features.getJSONObject(i).getJSONObject("geo_centroid");

                if (!properties.getString("country_code").equals("ua")) {
                    Log.d(TAG, "processAddressData: Skipped address - Country is not Україна");
                    continue;
                }

                if (properties.getString("categories").equals("adr_street")) {

                    String settlement = properties.optString("settlement", "").toLowerCase();
                    String city = citySearch.toLowerCase();

                    if (settlement.contains(city)) {
                        String address = String.format("%s %s, ",
                                properties.getString("name"),
                                properties.getString("type"));
                        addresses.add(address);
                    }
                }
                 else if (properties.getString("categories").equals("adr_address")) {

                    String settlement = properties.optString("settlement", "").toLowerCase();
                    String city = citySearch.toLowerCase();

                    if (settlement.contains(city)) {
                        String address = String.format("%s %s, %s, %s %s",

                                properties.getString("street"),
                                properties.getString("street_type"),
                                properties.getString("name"),
                                properties.getString("settlement_type"),
                                properties.getString("settlement"));

                        addresses.add(address);
                    }
                }


                double longitude = geoCentroid.getJSONArray("coordinates").getDouble(0);
                double latitude = geoCentroid.getJSONArray("coordinates").getDouble(1);
                coordinatesList.add(new double[]{longitude, latitude});

            }
            Log.d(TAG, "processAddressData: " + addresses);
            if (addresses.size() != 0) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Log.d(TAG, "processAddressData: " + addresses);
                    addressListView.setVisibility(View.VISIBLE);
                    button.setVisibility(View.GONE);
                    old_address.setVisibility(View.GONE);
                    addressAdapter.clear();
                    addressAdapter.addAll(addresses);
                    addressAdapter.notifyDataSetChanged();

                    // Проверка, не пуст ли адаптер
                    if (addressAdapter.getCount() > 0) {
                        Log.d(TAG, "processAddressData: ArrayAdapter contents");
                        for (int i = 0; i < addressAdapter.getCount(); i++) {
                            Log.d(TAG, "Item " + i + ": " + addressAdapter.getItem(i));
                        }
                    }
                });
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private void startLocationUpdates() {
        LocationRequest locationRequest = createLocationRequest();
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateMyPosition(OpenStreetMapActivity.startLat, OpenStreetMapActivity.startLan, OpenStreetMapActivity.FromAdressString);
    }
    public void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(requireActivity(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{permission}, requestCode);

        }
    }
    private void updateMyPosition(Double startLat, Double startLan, String position) {
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        ContentValues cv = new ContentValues();
        Log.d("TAG", "updateMyPosition: startLat" + startLat);
        Log.d("TAG", "updateMyPosition: startLan" + startLan);
        cv.put("startLat", startLat); // Сохраняем как число, а не строку
        database.update(MainActivity.TABLE_POSITION_INFO, cv, "id = ?",
                new String[] { "1" });
        cv.put("startLan", startLan); // Сохраняем как число, а не строку
        database.update(MainActivity.TABLE_POSITION_INFO, cv, "id = ?",
                new String[] { "1" });
        cv.put("position", position);
        database.update(MainActivity.TABLE_POSITION_INFO, cv, "id = ?",
                new String[] { "1" });
        database.close();


        Log.d("TAG", "updateMyPosition: logCursor(MainActivity.TABLE_POSITION_INFO " + logCursor(MainActivity.TABLE_POSITION_INFO, requireActivity()));
        Log.d("TAG", "updateMyPosition: getFromTablePositionInfo(requireActivity(), \"startLat\" ) " + getFromTablePositionInfo(requireActivity(), "startLat" ));
    }

    private void startCost() {
        String urlCost = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            List<String> settings = new ArrayList<>();
            settings.add(Double.toString(OpenStreetMapActivity.startLat));
            settings.add(Double.toString(OpenStreetMapActivity.startLan));
            settings.add(Double.toString(OpenStreetMapActivity.startLat));
            settings.add(Double.toString(OpenStreetMapActivity.startLan));
            settings.add(geoText.getText().toString());
            settings.add(geoText.getText().toString());
            Log.d(TAG, "startCost: marker " + settings);
            updateRoutMarker(settings);

            settings = new ArrayList<>();
            settings.add(Double.toString(OpenStreetMapActivity.startLat));
            settings.add(Double.toString(OpenStreetMapActivity.startLan));
            settings.add(Double.toString(OpenStreetMapActivity.startLat));
            settings.add(" ");

            Log.d(TAG, "startCost: Geo" + settings);
            updateRoutGeo(settings);
            urlCost = getTaxiUrlSearchMarkers("costSearchMarkers", requireActivity());
        }

        Map<String, String> sendUrlMapCost = null;
        try {
            sendUrlMapCost = CostJSONParser.sendURL(urlCost);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        String message = sendUrlMapCost.get("message");
        String orderCost = sendUrlMapCost.get("order_cost");
        Log.d("TAG", "startCost: orderCost " + orderCost);

        if (orderCost.equals("0")) {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        }
        if (!orderCost.equals("0")) {

            String discountText = logCursor(MainActivity.TABLE_SETTINGS_INFO, getContext()).get(3);
            long discountInt = Integer.parseInt(discountText);

            firstCost = Long.parseLong(orderCost);
            discount = firstCost * discountInt / 100;
            firstCost = firstCost + discount;
            updateAddCost(String.valueOf(discount));
            text_view_cost.setText(String.valueOf(firstCost));
            MIN_COST_VALUE = (long) (firstCost*0.6);
            firstCostForMin = firstCost;
        }
        if(!text_view_cost.getText().toString().equals("")) {
            firstCost = Long.parseLong(text_view_cost.getText().toString());
            Log.d("TAG", "startCost: firstCost " + firstCost);
            Log.d("TAG", "startCost: addCost " + addCost);

            }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void visicomCost() {
        String urlCost = null;

        urlCost = getTaxiUrlSearchMarkers("costSearchMarkers", requireActivity());
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

        if (orderCost.equals("0")) {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        }
        if (!orderCost.equals("0")) {

            String discountText = logCursor(MainActivity.TABLE_SETTINGS_INFO, getContext()).get(3);
            long discountInt = Integer.parseInt(discountText);

            firstCost = Long.parseLong(orderCost);
            discount = firstCost * discountInt / 100;
            firstCost = firstCost + discount;
            updateAddCost(String.valueOf(discount));
            text_view_cost.setText(String.valueOf(firstCost));
            MIN_COST_VALUE = (long) (firstCost*0.6);
            firstCostForMin = firstCost;
        }
        if(!text_view_cost.getText().toString().equals("")) {
            firstCost = Long.parseLong(text_view_cost.getText().toString());
            Log.d("TAG", "startCost: firstCost " + firstCost);
            Log.d("TAG", "startCost: addCost " + addCost);

        }
//        addressAdapter.clear();
//        addressAdapter.notifyDataSetChanged();
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
    private String[] arrayAdressAdapter() {
        ArrayList<Map>  routMaps = routMaps();

        HashMap<String, String> adressMap;
        String[] arrayRouts;
        ArrayList<Map> adressArrLoc = new ArrayList<>(routMaps().size());

        int i = 0, k = 0;
        boolean flag;
        if(routMaps.size() != 0) {

            for (int j = 0; j < routMaps.size(); j++) {
                Object toLatObject = routMaps.get(j).get("to_lat");
                Object fromLatObject = routMaps.get(j).get("from_lat");

                if (toLatObject != null && fromLatObject != null) {
                    String toLat = toLatObject.toString();
                    String fromLat = fromLatObject.toString();

                    if (!toLat.equals(fromLat)) {
                        if (!Objects.requireNonNull(routMaps.get(j).get("to_lat")).toString().equals(Objects.requireNonNull(routMaps.get(j).get("from_lat")).toString())) {
                            adressMap = new HashMap<>();
                            adressMap.put("street", routMaps.get(j).get("from_street").toString());
                            adressMap.put("number", routMaps.get(j).get("from_number").toString());
                            adressMap.put("to_lat", routMaps.get(j).get("from_lat").toString());
                            adressMap.put("to_lng", routMaps.get(j).get("from_lng").toString());
                            adressArrLoc.add(k++, adressMap);
                        }
                        if (!routMaps.get(j).get("to_street").toString().equals("Місце призначення") &&
                                !routMaps.get(j).get("to_street").toString().equals(routMaps.get(j).get("to_lat").toString()) &&
                                !routMaps.get(j).get("to_street").toString().equals(routMaps.get(j).get("to_number").toString())) {
                            adressMap = new HashMap<>();
                            adressMap.put("street", routMaps.get(j).get("to_street").toString());
                            adressMap.put("number", routMaps.get(j).get("to_number").toString());
                            adressMap.put("to_lat", routMaps.get(j).get("to_lat").toString());
                            adressMap.put("to_lng", routMaps.get(j).get("to_lng").toString());
                            adressArrLoc.add(k++, adressMap);
                        }
                    }
                }

            };
            Log.d("TAG", "arrayAdressAdapter: adressArrLoc " + adressArrLoc.toString());
        } else {
            arrayRouts = null;
        }
        i=0;
        ArrayList<String> arrayList = new ArrayList<>();
        for (int j = 0; j <  adressArrLoc.size(); j++) {

            flag = true;
            for (int l = 0; l <  adressArr.size(); l++) {

                if ( adressArrLoc.get(j).get("street").equals(adressArr.get(l).get("street"))) {
                    flag = false;
                    break;
                }
            }

            if(adressArrLoc.get(j) != null && flag) {
                arrayList.add(adressArrLoc.get(j).get("street") + " " +
                        adressArrLoc.get(j).get("number"));
                adressMap = new HashMap<>();
                adressMap.put("street", (String) adressArrLoc.get(j).get("street"));
                adressMap.put("number", (String) adressArrLoc.get(j).get("number"));

                adressMap.put("to_lat", (String) adressArrLoc.get(j).get("to_lat"));
                adressMap.put("to_lng", (String) adressArrLoc.get(j).get("to_lng"));
                adressArr.add(i++, adressMap);
            };


        }
        arrayRouts = new String[arrayList.size()];
        for (int l = 0; l < arrayList.size(); l++) {
            arrayRouts[l] = arrayList.get(l);
        }

        return arrayRouts;
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

        // Заменяем символ '/' в строках
        start = start.replace("/", "%2F");
        finish = finish.replace("/", "%2F");

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

        String url = "https://m.easy-order-taxi.site/" + api + "/android/" + urlAPI + "/" + parameters + "/" + result;

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
    @Override
    public void onPause() {
        super.onPause();
//        Toast.makeText(requireActivity(), getString(R.string.to_marker_mes), Toast.LENGTH_LONG).show();
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

    static Double to_lat;
    static Double to_lng;
    private void dialogFromToGeoAdress(String[] array) throws MalformedURLException, InterruptedException, JSONException {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.AlertDialogTheme);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.from_to_geo_adress_layout, null);
        builder.setView(view);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.custom_list_item, array);
        ListView listView = view.findViewById(R.id.listAddress);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemChecked(0, true);


        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Обработка выбора элемента
//                selectedItem = position;
                // Дополнительный код по обработке выбора элемента
//                 to_lat = Double.valueOf((String) adressArr.get(listView.getCheckedItemPosition()).get("to_lat"));
//                 to_lng = Double.valueOf((String) adressArr.get(listView.getCheckedItemPosition()).get("to_lng"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Код, если ни один элемент не выбран
            }
        });
        builder.setPositiveButton(R.string.order, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {

                to_lat = Double.valueOf((String) adressArr.get(listView.getCheckedItemPosition()).get("to_lat"));
                to_lng = Double.valueOf((String) adressArr.get(listView.getCheckedItemPosition()).get("to_lng"));

                OpenStreetMapActivity.finishLat = to_lat;
                OpenStreetMapActivity.finishLan = to_lng;

                Log.d("TAG", "onClick: OpenStreetMapActivity.finishLat " + OpenStreetMapActivity.finishLat);
                Log.d("TAG", "onClick: OpenStreetMapActivity.finishLan " + OpenStreetMapActivity.finishLan);

                List<String> settings = new ArrayList<>();
                settings.add(String.valueOf(OpenStreetMapActivity.startLat));
                settings.add(String.valueOf(OpenStreetMapActivity.startLan));
                settings.add(String.valueOf(OpenStreetMapActivity.finishLat));
                settings.add(String.valueOf(OpenStreetMapActivity.finishLan));

                settings.add(geoText.getText().toString());
                settings.add(textViewTo.getText().toString());

                updateRoutMarker(settings);
                geo_marker = "marker";

                try {

                    urlAddress = getTaxiUrlSearchMarkers("costSearchMarkers", requireActivity());

                    Map<String, String> sendUrlMapCost = CostJSONParser.sendURL(urlAddress);

                    String message = sendUrlMapCost.get("message");
                    String orderCost = sendUrlMapCost.get("order_cost");
                    geo_marker = "marker";
                    Log.d("TAG", "onClick urlAddress: " + urlAddress);

                    if (orderCost.equals("0")) {
                        MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
                        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                    }
                    if (!orderCost.equals("0")) {
                        if (!verifyOrder(requireActivity())) {
                            MyBottomSheetBlackListFragment bottomSheetDialogFragment = new MyBottomSheetBlackListFragment(orderCost);
                            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                        } else {
                            String discountText = logCursor(MainActivity.TABLE_SETTINGS_INFO, requireContext()).get(3);


                            long discountInt = Integer.parseInt(discountText);
                            firstCost = Long.parseLong(orderCost);
                            discount= firstCost * discountInt / 100;
                            updateAddCost(String.valueOf(discount));
                            firstCost = firstCost + discount;
                            text_view_cost.setText(String.valueOf(firstCost));
                            MIN_COST_VALUE = (long) (firstCost*0.1);
                            firstCostForMin = firstCost;
                            to = adressArr.get(listView.getCheckedItemPosition()).get("street").toString();
                            textViewTo.setText(to);
                            if(connected()) {
                                if (to.indexOf("/") != -1) {
                                    to = to.substring(0,  to.indexOf("/"));
                                };
                                String urlCost = "https://m.easy-order-taxi.site/" + api + "/android/autocompleteSearchComboHid/" + to;

                                Log.d("TAG", "onClick urlCost: " + urlCost);

                                try {
                                    sendUrlMapCost = ResultSONParser.sendURL(urlCost);
                                } catch (MalformedURLException | InterruptedException | JSONException ignored) {

                                }

                                orderCost = (String) sendUrlMapCost.get("message");
                                Log.d("TAG", "onClick Hid : " + orderCost);

                                if (orderCost.equals("1")) {
                                    to_number.setVisibility(View.VISIBLE);
                                    to_number.setText(" ");
                                    to_number.requestFocus();
                                    numberFlagTo = "1";
                                }  else if (orderCost.equals("0")) {
                                    to_number.setText("XXX");
                                    to_number.setVisibility(View.INVISIBLE);
                                    numberFlagTo = "0";
                                }
                            }

                            }


                    }else {

                        MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(sendUrlMapCost.get("message"));
                        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                } catch (MalformedURLException e) {

                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.error_firebase_start));
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_button), null);
        builder.show();

    }
    public static String toCost;
    public static String to_numberCost;
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
            bottomSheetDialogFragment = new MyPhoneDialogFragment("geo", text_view_cost.getText().toString());
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

    private void updateRoutMarker(List<String> settings) {
        ContentValues cv = new ContentValues();

        cv.put("startLat",  Double.parseDouble(settings.get(0)));
        cv.put("startLan", Double.parseDouble(settings.get(1)));
        cv.put("to_lat", Double.parseDouble(settings.get(2)));
        cv.put("to_lng", Double.parseDouble(settings.get(3)));
        cv.put("start", settings.get(4));
        cv.put("finish", settings.get(5));

        // обновляем по id
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        database.update(MainActivity.ROUT_MARKER, cv, "id = ?",
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
                    orderFinished();
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
            // Показываем объяснение пользователю, почему мы запрашиваем разрешение
            // Можно использовать диалоговое окно или другой пользовательский интерфейс
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
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
    private MyPhoneDialogFragment bottomSheetDialogFragment;
    @Override
    public void onResume() {
        super.onResume();
        if(bottomSheetDialogFragment != null) {
            bottomSheetDialogFragment.dismiss();
        }

    }

}


