package com.taxieasyua.back4app.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import com.taxieasyua.back4app.ui.gallery.GalleryFragment;
import com.taxieasyua.back4app.ui.maps.FromJSONParser;
import com.taxieasyua.back4app.ui.maps.ToJSONParser;
import com.taxieasyua.back4app.ui.open_map.MarkerOverlay;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;
import com.taxieasyua.back4app.ui.start.ResultSONParser;

import org.json.JSONException;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;


public class MyGeoDialogFragment extends BottomSheetDialogFragment {
    public TextView geoText;
    AppCompatButton button, old_address, btn_minus, btn_plus, btnOrder;
    public String[] arrayStreet;
    private static String api;
    ArrayList<Map> adressArr;
    long firstCost;

    public static TextView text_view_cost;
    static AutoCompleteTextView textViewTo;
    static EditText to_number;
    ArrayAdapter<String> adapter;
    String numberFlagTo;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    static MyGeoDialogFragment fragment;

    public static MyGeoDialogFragment newInstance(String fromGeo) {
        fragment = new MyGeoDialogFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.geo_first_layout, container, false);

        List<String> stringList = logCursor(MainActivity.CITY_INFO, getActivity());
        switch (stringList.get(1)){
            case "Dnipropetrovsk Oblast":
                arrayStreet = Dnipro.arrayStreet();
                api = MainActivity.apiDnipro;

                break;
            case "Odessa":
                arrayStreet = Odessa.arrayStreet();
                api = MainActivity.apiOdessa;

                break;
            case "Zaporizhzhia":
                arrayStreet = Zaporizhzhia.arrayStreet();
                api = MainActivity.apiZaporizhzhia;

                break;
            case "Cherkasy Oblast":
                arrayStreet = Cherkasy.arrayStreet();
                api = MainActivity.apiCherkasy;

                break;
            case "OdessaTest":
                arrayStreet = OdessaTest.arrayStreet();
                api = MainActivity.apiTest;

                break;
            default:
                arrayStreet = KyivCity.arrayStreet();
                api = MainActivity.apiKyiv;

                break;
        }


        if (!routMaps().isEmpty()) {
            adressArr = new ArrayList<>(routMaps().size());
        }

        numberFlagTo = "2";

        geoText = view.findViewById(R.id.textGeo);
        geoText.setText(OpenStreetMapActivity.FromAdressString);
        text_view_cost = view.findViewById(R.id.text_view_cost);

        textViewTo = view.findViewById(R.id.text_to);
        to_number = view.findViewById(R.id.to_number);
        btn_minus = view.findViewById(R.id.btn_minus);
        btn_plus = view.findViewById(R.id.btn_plus);
        btnOrder = view.findViewById(R.id.btnOrder);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, arrayStreet);

        textViewTo.setAdapter(adapter);
        textViewTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(connected()) {

                    String to = String.valueOf(adapter.getItem(position));
                    if (to.indexOf("/") != -1) {
                        to = to.substring(0,  to.indexOf("/"));
                    };
                    String url = "https://m.easy-order-taxi.site/" + api + "/android/autocompleteSearchComboHid/" + to;


                    Log.d("TAG", "onClick urlCost: " + url);
                    Map sendUrlMapCost = null;
                    try {
                        sendUrlMapCost = ResultSONParser.sendURL(url);
                    } catch (MalformedURLException | InterruptedException | JSONException e) {

                    }

                    String orderCost = (String) sendUrlMapCost.get("message");
                    Log.d("TAG", "onClick Hid : " + orderCost);

                    if (orderCost.equals("1")) {
                        to_number.setVisibility(View.VISIBLE);
                        to_number.setText(" ");
                        to_number.requestFocus();
                        numberFlagTo = "1";
                    }  else if (orderCost.equals("0")) {
                        to_number.setText(" ");
                        to_number.setVisibility(View.INVISIBLE);
                        numberFlagTo = "0";
                    }

                     if (!verifyOrder(getActivity())) {
                         MyBottomSheetBlackListFragment bottomSheetDialogFragment = new MyBottomSheetBlackListFragment("orderCost");
                         bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                     } else {
                         String  to_numberCost;
                         Log.d("TAG", "onItemClick: numberFlagTo " + numberFlagTo);
                         if (numberFlagTo.equals("1") && to_number.getText().toString().equals(" ")) {

                             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                 to_number.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.selected_text_color)));
                                 to_number.setBackgroundTintBlendMode(BlendMode.SRC_IN); // Устанавливаем режим смешивания цветов
                             } else {
                                 ViewCompat.setBackgroundTintList(to_number, ColorStateList.valueOf(getResources().getColor(R.color.selected_text_color)));
                             }
                             to_numberCost = "1";
                         } else {
                             if (numberFlagTo.equals("0")) {
                                 to_numberCost = " ";
                             } else{
                                 to_numberCost = to_number.getText().toString();
                             }

                         }



                         String urlCost = null;
                         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                             urlCost = getTaxiUrlSearchGeo(OpenStreetMapActivity.startLat, OpenStreetMapActivity.startLan,
                                            to, to_numberCost, "costSearchGeo", getActivity());
                         }

                         Log.d("TAG", "onClick urlCost: " + urlCost);

                         try {
                             sendUrlMapCost = ToJSONParser.sendURL(urlCost);
                         } catch (MalformedURLException e) {
                             MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                             bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                         }

                         String message = (String) sendUrlMapCost.get("message");
                         orderCost = (String) sendUrlMapCost.get("order_cost");
                         Log.d("TAG", "onClick orderCost : " + orderCost);
                         if (orderCost.equals("0")) {
                              MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
                             bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                         } else {
                             OpenStreetMapActivity.cost = Long.parseLong(orderCost);
                             text_view_cost.setText(orderCost);
                         }
                    }
                }

            }
        });

        button = view.findViewById(R.id.change);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), R.string.check_position, Toast.LENGTH_SHORT).show();
                Configuration.getInstance().load(getActivity(), PreferenceManager.getDefaultSharedPreferences(getActivity()));


                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());


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
                                String urlFrom = "https://m.easy-order-taxi.site/" + api + "/android/fromSearchGeo/" + OpenStreetMapActivity.startLat + "/" + OpenStreetMapActivity.startLan;
                                Map sendUrlFrom = null;
                                try {
                                    sendUrlFrom = FromJSONParser.sendURL(urlFrom);

                                } catch (MalformedURLException | InterruptedException |
                                         JSONException e) {
                                    Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();

                                }
                                OpenStreetMapActivity.FromAdressString = (String) sendUrlFrom.get("route_address_from");
                                updateMyPosition(OpenStreetMapActivity.startLat, OpenStreetMapActivity.startLan, OpenStreetMapActivity.FromAdressString);

                                 startActivity(new Intent(getActivity(), OpenStreetMapActivity.class));
                            }
                        }



                        MyGeoDialogFragment bottomSheetDialogFragment = MyGeoDialogFragment.newInstance(OpenStreetMapActivity.FromAdressString);
                        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());



                    };
                };





                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    requestLocationPermission();
                }

            }
        });
        old_address = view.findViewById(R.id.old_address);
        old_address.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] array = arrayAdressAdapter();
                        if(array.length == 0) {
                            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.make_order_message));
                            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                        } else {
                            try {
                                dialogFromToGeoAdress(array);
                            } catch (MalformedURLException | InterruptedException |
                                     JSONException e) {
                                MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                            }
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

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    order();
                }
            }
        });
        startCost();

        OpenStreetMapActivity.progressBar.setVisibility(View.INVISIBLE);
        return view;
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
        if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);

        }
    }
    private void updateMyPosition(Double startLat, Double startLan, String position) {
        SQLiteDatabase database = getActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
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
    private void startCost () {



        String urlCost = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            urlCost = getTaxiUrlSearchMarkers(OpenStreetMapActivity.startLat, OpenStreetMapActivity.startLan,
                    OpenStreetMapActivity.startLat, OpenStreetMapActivity.startLan, "costSearchMarkers", getActivity());
        }

        Map<String, String> sendUrlMapCost = null;
        try {
            sendUrlMapCost = ToJSONParser.sendURL(urlCost);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        String message = (String) sendUrlMapCost.get("message");
        String orderCost = (String) sendUrlMapCost.get("order_cost");


        if (orderCost.equals("0")) {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        }
        if (!orderCost.equals("0")) {
            OpenStreetMapActivity.cost = Long.parseLong(orderCost);
            long MIN_COST_VALUE = (long) ((long) Double.parseDouble(orderCost) * 0.1);
            long MAX_COST_VALUE = Long.parseLong(orderCost) * 3;
            firstCost = Long.parseLong(orderCost);



            String discountText = logCursor(MainActivity.TABLE_SETTINGS_INFO, getActivity()).get(3);
            long discountInt = Integer.parseInt(discountText);
            long discount;
            discount =  firstCost * discountInt/100;
            firstCost = firstCost  + discount;

            OpenStreetMapActivity.addCost = discount;
            text_view_cost.setText(String.valueOf(firstCost));
            btn_minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firstCost -= 5;
                    OpenStreetMapActivity.addCost -= 5;
                    if (firstCost <= MIN_COST_VALUE) {
                        firstCost = MIN_COST_VALUE;
                        OpenStreetMapActivity.addCost = MIN_COST_VALUE - firstCost;
                    }
                    text_view_cost.setText(String.valueOf(firstCost));

                }
            });

            btn_plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firstCost += 5;
                    OpenStreetMapActivity.addCost += 5;
                    if (firstCost >= MAX_COST_VALUE) {
                        firstCost = MAX_COST_VALUE;
                        OpenStreetMapActivity.addCost = MAX_COST_VALUE - firstCost;
                    }
                    text_view_cost.setText(String.valueOf(firstCost));
                }
            });
        }
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
//                Log.d("TAG", "arrayAdressAdapter routMaps.get(j).get(\"from_lat\"): " + routMaps.get(j).get("from_street"));
//                Log.d("TAG", "arrayAdressAdapter routMaps.get(j).get(\"to_lat\"): " + routMaps.get(j).get("to_street"));

                if(!Objects.requireNonNull(routMaps.get(j).get("to_lat")).toString().equals(Objects.requireNonNull(routMaps.get(j).get("from_lat")).toString())) {
//                    if (!Objects.requireNonNull(routMaps.get(j).get("from_street")).toString().equals(Objects.requireNonNull(routMaps.get(j).get("from_lat")).toString()))
//                        if (!Objects.requireNonNull(routMaps.get(j).get("from_street")).toString().equals("Місце призначення") && !Objects.requireNonNull(routMaps.get(j).get("from_street")).toString().equals(Objects.requireNonNull(routMaps.get(j).get("from_number")).toString())) {
                    adressMap = new HashMap<>();
                    adressMap.put("street", routMaps.get(j).get("from_street").toString());
                    adressMap.put("number", routMaps.get(j).get("from_number").toString());
                    adressMap.put("to_lat", routMaps.get(j).get("from_lat").toString());
                    adressMap.put("to_lng", routMaps.get(j).get("from_lng").toString());
                    adressArrLoc.add(k++, adressMap);
                }
                if(!routMaps.get(j).get("to_street").toString().equals("Місце призначення")&&
                        !routMaps.get(j).get("to_street").toString().equals(routMaps.get(j).get("to_lat").toString()) &&
                        !routMaps.get(j).get("to_street").toString().equals(routMaps.get(j).get("to_number").toString()))
                {
                    adressMap = new HashMap<>();
                    adressMap.put("street", routMaps.get(j).get("to_street").toString());
                    adressMap.put("number", routMaps.get(j).get("to_number").toString());
                    adressMap.put("to_lat", routMaps.get(j).get("to_lat").toString());
                    adressMap.put("to_lng", routMaps.get(j).get("to_lng").toString());
                    adressArrLoc.add(k++, adressMap);
                }


//                }
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
    public static String getTaxiUrlSearchMarkers(double originLatitude, double originLongitude,
                                                 double toLatitude, double toLongitude,
                                                 String urlAPI, Context context) {
        //  Проверка даты и времени
//        if(hasServer()) {

        List<String> stringList = logCursor(MainActivity.TABLE_ADD_SERVICE_INFO, context);
        String time = stringList.get(1);
        String comment = stringList.get(2);
        String date = stringList.get(3);

        // Origin of route
        String str_origin = originLatitude + "/" + originLongitude;

        // Destination of route
        String str_dest = toLatitude + "/" + toLongitude;

        //        Cursor cursorDb = MainActivity.database.query(MainActivity.TABLE_SETTINGS_INFO, null, null, null, null, null, null);
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        String tarif = logCursor(MainActivity.TABLE_SETTINGS_INFO, context).get(2);


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
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/" + displayName + "(" + userEmail + ")";
        }

        if(urlAPI.equals("orderSearchMarkers")) {
            phoneNumber = logCursor(MainActivity.TABLE_USER_INFO, context).get(2);


            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName  + "/" + OpenStreetMapActivity.addCost + "/" + time + "/" + comment + "/" + date;

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

        Boolean hasConnect = false;

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(
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
        SQLiteDatabase database = getActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
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

    private void dialogFromToGeoAdress(String[] array) throws MalformedURLException, InterruptedException, JSONException {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.from_to_geo_adress_layout, null);
        builder.setView(view);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.custom_list_item, array);
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
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Double to_lat = Double.valueOf((String) adressArr.get(listView.getCheckedItemPosition()).get("to_lat"));
                Double to_lng = Double.valueOf((String) adressArr.get(listView.getCheckedItemPosition()).get("to_lng"));
                Log.d("TAG", "onClick  to_lat, to_lng: " + to_lat + " " + to_lng);

                try {

                    String urlCost = OpenStreetMapActivity.getTaxiUrlSearchMarkers(OpenStreetMapActivity.startLat, OpenStreetMapActivity.startLan,
                            to_lat, to_lng, "costSearchMarkers", getActivity());

                    Map<String, String> sendUrlMapCost = ToJSONParser.sendURL(urlCost);

                    String message = sendUrlMapCost.get("message");
                    String orderCost = sendUrlMapCost.get("order_cost");

                    Log.d("TAG", "onClick urlCost: " + urlCost);

                    if (orderCost.equals("0")) {
                        MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(message);
                        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                    }
                    if (!orderCost.equals("0")) {
                        if (!verifyOrder(getActivity())) {
                            MyBottomSheetBlackListFragment bottomSheetDialogFragment = new MyBottomSheetBlackListFragment(orderCost);
                            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                        } else {
                            OpenStreetMapActivity.cost = Long.parseLong(orderCost);
                            text_view_cost.setText(orderCost);
                        }
                    }

                } catch (MalformedURLException e) {

                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.error_firebase_start));
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            }
        });
        builder.setNegativeButton(getString(R.string.change), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
        builder.show();
//        } else {
//            Toast.makeText(OpenStreetMapActivity.this, getString(R.string.server_error_connected), Toast.LENGTH_LONG).show();
//        }
    }
    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void order() {
        if(!verifyOrder(getContext())) {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.black_list_message));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            return;
        }
        boolean stop = false;

        if (numberFlagTo.equals("1") && to_number.getText().toString().equals(" ")) {
            to_number.setBackgroundTintList(ColorStateList.valueOf(R.color.selected_text_color));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                to_number.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.selected_text_color)));
                to_number.setBackgroundTintBlendMode(BlendMode.SRC_IN); // Устанавливаем режим смешивания цветов
            } else {
                ViewCompat.setBackgroundTintList(to_number, ColorStateList.valueOf(getResources().getColor(R.color.selected_text_color)));
            }
            stop = true;

        }
        if(stop) {return;}


        if (numberFlagTo.equals("1") && !to_number.getText().toString().equals(" ")) {
            to_number.setBackgroundTintList(ColorStateList.valueOf(R.color.selected_text_color));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                to_number.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.edit)));
                to_number.setBackgroundTintBlendMode(BlendMode.SRC_IN); // Устанавливаем режим смешивания цветов
            } else {
                ViewCompat.setBackgroundTintList(to_number, ColorStateList.valueOf(getResources().getColor(R.color.edit)));
            }


        }

        String toCost, to_numberCost;

        if (TextUtils.isEmpty(textViewTo.getText())) {
            toCost = String.valueOf(OpenStreetMapActivity.startLat);
            to_numberCost = " ";
        } else {
            toCost = String.valueOf(textViewTo.getText());
            to_numberCost = to_number.getText().toString();
        }

        if (!verifyPhone(getContext())) {
            getPhoneNumber();
        }
        if (!verifyPhone(getContext())) {
            MyPhoneDialogFragment bottomSheetDialogFragment = new MyPhoneDialogFragment();
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        }
        if(connected()) {
            if (verifyPhone(getContext())) {
                try {
                    String urlOrder = getTaxiUrlSearchGeo(OpenStreetMapActivity.startLat, OpenStreetMapActivity.startLan,
                            toCost, to_numberCost, "orderSearchGeo", getActivity());
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
                                        getActivity()
                                );
                            }
                        } else {
                            to_name = sendUrlMap.get("routeto") + " " + sendUrlMap.get("to_number");
                            if (!sendUrlMap.get("lat").equals("0")) {
                                insertRecordsOrders(
                                        sendUrlMap.get("routefrom"), sendUrlMap.get("routeto"),
                                        sendUrlMap.get("routefromnumber"), sendUrlMap.get("to_number"),
                                        Double.toString(OpenStreetMapActivity.startLat), Double.toString(OpenStreetMapActivity.startLan),
                                        sendUrlMap.get("lat"), sendUrlMap.get("lng"), getActivity()
                                );
                            }
                        }
                        String messageResult = getString(R.string.thanks_message) +
                                OpenStreetMapActivity.FromAdressString + " " + getString(R.string.to_message) +
                                to_name + "." +
                                getString(R.string.call_of_order) + orderWeb + getString(R.string.UAH);

//                        finishLat = Double.parseDouble((String) sendUrlMap.get("lat"));
//                        finishLan = Double.parseDouble((String) sendUrlMap.get("lng"));
//                        if(finishLan != 0) {
//
//                            setMarker(finishLat, finishLan, to_name);
//                            GeoPoint endPoint = new GeoPoint(finishLat, finishLan);
//                            showRout(startPoint, endPoint);
//                        }
                        //                                                                            Toast.makeText(OpenStreetMapActivity.this, messageResult, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), FinishActivity.class);
                        intent.putExtra("messageResult_key", messageResult);
                        intent.putExtra("UID_key", Objects.requireNonNull(sendUrlMap.get("dispatching_order_uid")));
                        startActivity(intent);
                    } else {

                        MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(sendUrlMap.get("message"));
                        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                    }


                } catch (MalformedURLException ignored) {

                }

//                try {
//                    String urlOrder = getTaxiUrlSearch(from, from_number.getText().toString(), toCost, to_numberCost, "orderSearch", getActivity());
//                    Map<String, String> sendUrlMap = ToJSONParser.sendURL(urlOrder);
//
//                    String orderWeb = (String) sendUrlMap.get("order_cost");
//                    if (!orderWeb.equals("0")) {
//
//                        String from_name = (String) sendUrlMap.get("routefrom");
//                        String to_name = (String) sendUrlMap.get("routeto");
//                        if (from_name.equals(to_name)) {
//                            messageResult = getString(R.string.thanks_message) +
//                                    from_name + " " + from_number.getText() + " " +  getString(R.string.on_city) +
//                                    getString(R.string.cost_of_order) + orderWeb + getString(R.string.UAH);
//
//
//                        } else {
//                            messageResult =  getString(R.string.thanks_message) +
//                                    from_name + " " + from_number.getText() + " " + getString(R.string.to_message) +
//                                    to_name + " " + to_number.getText() + "." +
//                                    getString(R.string.cost_of_order) + orderWeb + getString(R.string.UAH);
//                        }
//
//                        if(!sendUrlMap.get("from_lat").equals("0") && !sendUrlMap.get("lat").equals("0")) {
//                            if(from_name.equals(to_name)) {
//                                insertRecordsOrders(
//                                        from_name, from_name,
//                                        from_number.getText().toString(), from_number.getText().toString(),
//                                        (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
//                                        (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
//                                        getContext()
//                                );
//                            } else {
//                                insertRecordsOrders(
//                                        from_name, to_name,
//                                        from_number.getText().toString(), to_number.getText().toString(),
//                                        (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
//                                        (String) sendUrlMap.get("lat"), (String) sendUrlMap.get("lng"), getContext()
//                                );
//
//                            }
//                        }
//
//                        Intent intent = new Intent(getActivity(), FinishActivity.class);
//                        intent.putExtra("messageResult_key", messageResult);
//                        intent.putExtra("UID_key", String.valueOf(sendUrlMap.get("dispatching_order_uid")));
//                        startActivity(intent);
//
//                    } else {
//                        MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
//                        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
//                    }
//
//
//                } catch (MalformedURLException e) {
//                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
//                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
//                }
            }
        } else {
            MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        }






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

        return verify;
    }
    private void getPhoneNumber () {
        String mPhoneNumber;
        TelephonyManager tMgr = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Manifest.permission.READ_PHONE_NUMBERS: " + ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_NUMBERS));
            Log.d("TAG", "Manifest.permission.READ_PHONE_STATE: " + ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE));
            return;
        }
        mPhoneNumber = tMgr.getLine1Number();
//        mPhoneNumber = null;
        if(mPhoneNumber != null) {
            String PHONE_PATTERN = "((\\+?380)(\\d{9}))$";
            boolean val = Pattern.compile(PHONE_PATTERN).matcher(mPhoneNumber).matches();
            Log.d("TAG", "onClick No validate: " + val);
            if (val == false) {
                Toast.makeText(getActivity(), getString(R.string.format_phone) , Toast.LENGTH_SHORT).show();
                Log.d("TAG", "onClick:phoneNumber.getText().toString() " + mPhoneNumber);
//                getActivity().finish();

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String getTaxiUrlSearchGeo(double originLatitude, double originLongitude, String to, String to_number, String urlAPI, Context context) {
//    if(hasServer()) {
        //  Проверка даты и времени

        List<String> stringList = logCursor(MainActivity.TABLE_ADD_SERVICE_INFO, context);
        String time = stringList.get(1);
        String comment = stringList.get(2);
        String date = stringList.get(3);

        // Origin of route
        String str_origin = originLatitude + "/" + originLongitude;

        // Destination of route
        String str_dest = to + "/" + to_number;

        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

        String tarif = logCursor(MainActivity.TABLE_SETTINGS_INFO, context).get(2);


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
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/" + displayName + "(" + userEmail + ")";
        }

        if(urlAPI.equals("orderSearchGeo")) {
            phoneNumber = logCursor(MainActivity.TABLE_USER_INFO, context).get(2);

            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName  + "/" + OpenStreetMapActivity.addCost + "/" + time + "/" + comment + "/" + date;

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
        Log.d("TAG", "getTaxiUrlSearch services: " + url);

        return url;


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
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Показываем объяснение пользователю, почему мы запрашиваем разрешение
            // Можно использовать диалоговое окно или другой пользовательский интерфейс
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }
}


