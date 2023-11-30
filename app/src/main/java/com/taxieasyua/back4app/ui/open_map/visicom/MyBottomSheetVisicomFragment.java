package com.taxieasyua.back4app.ui.open_map.visicom;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.cities.Kyiv.KyivRegion;
import com.taxieasyua.back4app.ui.home.HomeFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetErrorFragment;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;
import com.taxieasyua.back4app.ui.visicom.VisicomFragment;
import com.taxieasyua.back4app.utils.KeyboardUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class MyBottomSheetVisicomFragment extends BottomSheetDialogFragment {

    private static final String TAG = "TAG_VIS_ADDR";
    AppCompatButton btn_ok, btn_no;
    EditText fromEditAddress, toEditAddress;
    private ImageButton btn_clear_from, btn_clear_to;

    private final String apiUrl = "https://api.visicom.ua/data-api/5.0/uk/geocode.json";
    private String apiKey;
    private static List<double[]> coordinatesList;
    private static List<String[]> addresses;
    private final OkHttpClient client = new OkHttpClient();
    private String startPoint, finishPoint;
    ListView addressListView;

    private boolean verifyBuildingStart;
    private boolean verifyBuildingFinish;
    private TextView textGeoError, text_toError;
    private String fragmentInput;
    private String citySearch;
    private String[] resultArray;
    private String[] kyivRegionArr;
    private int positionChecked;
    private String zone;

    public MyBottomSheetVisicomFragment(String fragmentInput) {
        this.fragmentInput = fragmentInput;
    }

    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.visicom_address_layout, container, false);
        setCancelable(false);
        List<String> stringList = logCursor(MainActivity.CITY_INFO, requireActivity());
        switch (stringList.get(1)){
            case "Dnipropetrovsk Oblast":
                citySearch = "Дніпр";
                break;
            case "Odessa":
            case "OdessaTest":
                citySearch = "Одеса";
                break;
            case "Zaporizhzhia":
                citySearch = "Запорі";
                break;
            case "Cherkasy Oblast":
                citySearch = "Черкас";
                break;
            default:
                citySearch = "Київ";
                kyivRegionArr = KyivRegion.city();
                break;
        }
        textGeoError = view.findViewById(R.id.textGeoError);
        text_toError = view.findViewById(R.id.text_toError);

        apiKey = requireActivity().getString(R.string.visicom_key_storage);
        addressListView = view.findViewById(R.id.listAddress);

        btn_ok = view.findViewById(R.id.btn_ok);


        btn_no = view.findViewById(R.id.btn_no);
        btn_ok.setVisibility(View.INVISIBLE);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        fromEditAddress = view.findViewById(R.id.textGeo);
        switch (fragmentInput) {
            case "map":
                fromEditAddress.setText(GeoDialogVisicomFragment.geoText.getText().toString());
                break;
            case "home":
                fromEditAddress.setText(VisicomFragment.geoText.getText().toString());
                break;
        }

        fromEditAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String inputString = charSequence.toString();
                int charCount = inputString.length();
                Log.d(TAG, "onTextChanged: inputString" + inputString);
                Log.d(TAG, "onTextChanged: finishPoint" + startPoint);
                if (charCount > 2) {
                    if (startPoint == null) {
                        performAddressSearch(inputString, "start");
                    } else if (!startPoint.equals(inputString)) {
                        performAddressSearch(inputString, "start");
                    }
                    textGeoError.setVisibility(View.GONE);
                }
                btn_clear_from.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        toEditAddress = view.findViewById(R.id.text_to);
        switch (fragmentInput) {
            case "map":
                toEditAddress.setText(GeoDialogVisicomFragment.textViewTo.getText().toString());
                break;
            case "home":
                toEditAddress.setText(VisicomFragment.textViewTo.getText().toString());
                break;
        }


        toEditAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Вызывается перед изменением текста
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Вызывается при изменении текста
                String inputString = charSequence.toString();
                int charCount = inputString.length();
                Log.d(TAG, "onTextChanged: inputString" + inputString);
                Log.d(TAG, "onTextChanged: finishPoint" + finishPoint);
                if (charCount > 2) {
                    if (finishPoint == null) {
                        performAddressSearch(inputString, "finish");
                    } else if (!finishPoint.equals(inputString)) {
                        performAddressSearch(inputString,"finish");
                    }
                }
                btn_clear_to.setVisibility(View.VISIBLE);
                text_toError.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Вызывается после изменения текста
            }
        });
        btn_clear_from = view.findViewById(R.id.btn_clear_from);
        btn_clear_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromEditAddress.setText("");
                btn_clear_from.setVisibility(View.INVISIBLE);
                textGeoError.setVisibility(View.GONE);
            }
        });
        btn_clear_to = view.findViewById(R.id.btn_clear_to);
        btn_clear_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toEditAddress.setText("");
                btn_clear_to.setVisibility(View.INVISIBLE);
                text_toError.setVisibility(View.GONE);
            }
        });



        return view;
    }



    private void performAddressSearch(String inputText, String point) {
        try {
            String url = apiUrl;
            if(point.equals("start")) {
                verifyBuildingStart = false;
            } else  {
                verifyBuildingFinish = false;
            }

            if (!inputText.substring(3).contains(", ")) {

                url = url + "?categories=adr_street&text=" + inputText + "&key=" + apiKey;
                if(point.equals("start")) {
                    verifyBuildingStart = true;
                } else  {
                    verifyBuildingFinish = true;
                }
            } else {
                // Если ", " присутствует после первых четырех символов, то выполняем вторую строку
                Log.d(TAG, "performAddressSearch: inputText1111 " + inputText);
                String number = numbers(inputText);


                Log.d(TAG, "performAddressSearch: inputText" + inputTextBuild());
                inputText = inputTextBuild() + ", " + number;

                url = url + "?categories=adr_address&text=" + inputText + "&key=" + apiKey;

                Log.d(TAG, "performAddressSearch: 6666666666 " + url);
                if(point.equals("start")) {
                    verifyBuildingStart = false;
                } else  {
                    verifyBuildingFinish = false;
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
                        processAddressData(responseData, point);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            // Log the exception or display an error message
        }
    }

    private String inputTextBuild() {
//        Log.d(TAG, "inputTextBuild: positionChecked" + positionChecked);
//        for (String[] addressArray : addresses) {
//            Log.d(TAG, "Address: " + Arrays.toString(addressArray));
//        }
//        List<String> nameList = new ArrayList<>();
//
//        List<String> settlementList = new ArrayList<>();
//
//        for (int i = 0; i < addresses.size(); i++) {
//            if (i == positionChecked) {
//                String[] addressArray = addresses.get(i);
//
//                // Выбираем значения из массива и добавляем их в соответствующие списки
//
//                nameList.add(addressArray[1]);
//
//                settlementList.add(addressArray[3]);
//            }
//        }
//
//        Log.d(TAG, "inputTextBuild: nameList" + nameList.toString());
//        Log.d(TAG, "inputTextBuild: settlementList" + settlementList.toString());


        String[] selectedAddress = addresses.get(positionChecked);
        Log.d(TAG, "inputTextBuild: " + Arrays.toString(selectedAddress));
        // Получение элементов отдельно
        String name = selectedAddress[1];
        zone = selectedAddress[2];
        String settlement = selectedAddress[3];
        String result = settlement + ", " +  name;

        return result;

    }

    @SuppressLint("ResourceType")
    private void processAddressData(String responseData, String point) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);
            JSONArray features = jsonResponse.getJSONArray("features");
            Log.d(TAG, "processAddressData: features" + features);
            addresses = new ArrayList<>();
            coordinatesList = new ArrayList<>(); // Список для хранения координат


            for (int i = 0; i < features.length(); i++) {
                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");

                JSONObject geoCentroid = features.getJSONObject(i).getJSONObject("geo_centroid");

                if (!properties.getString("country_code").equals("ua")) {
                    Log.d(TAG, "processAddressData: Skipped address - Country is not Україна");
                    continue;
                }

                if (properties.getString("categories").equals("adr_street")) {

                    String settlement = properties.optString("settlement", "").toLowerCase();
                    String city = citySearch.toLowerCase();
                    String address;

                    if (settlement.contains(city)) {
                        if(properties.has("zone")) {
                            address = String.format("%s %s (%s), ",
                                    properties.getString("type"),
                                    properties.getString("name"),
                                    properties.getString("zone"));
                            addresses.add(new String[] {
                                    address,
                                    properties.getString("name"),
                                    properties.getString("zone"),
                                    properties.getString("settlement"),
                            });
                        } else {
                            address = String.format("%s %s, ",
                                    properties.getString("type"),
                                    properties.getString("name"));
                            addresses.add(new String[] {
                                    address,
                                    properties.getString("name"),
                                    "",
                                    properties.getString("settlement"),
                            });
                        }

                        double longitude = geoCentroid.getJSONArray("coordinates").getDouble(0);
                        double latitude = geoCentroid.getJSONArray("coordinates").getDouble(1);

                        coordinatesList.add(new double[]{longitude, latitude});
                    }

                    // Проверка по Киевской области

                    if (citySearch.equals("Київ")) {
                        if (checkWordInArray(properties.getString("settlement"), kyivRegionArr)) {
                            address = String.format("%s %s (%s), ",
                                    properties.getString("type"),
                                    properties.getString("name"),
                                    properties.getString("settlement"));


                            addresses.add(new String[] {
                                    address,
                                    properties.getString("name"),
                                    "",
                                    properties.getString("settlement"),
                            });
                            double longitude = geoCentroid.getJSONArray("coordinates").getDouble(0);
                            double latitude = geoCentroid.getJSONArray("coordinates").getDouble(1);

                            coordinatesList.add(new double[]{longitude, latitude});
                        }
                    }

                }
                if (properties.getString("categories").equals("adr_address")) {
                    String settlement = properties.optString("settlement", "").toLowerCase();
                    String city = citySearch.toLowerCase();
                    String address;

                    if (settlement.contains(city)) {
                        Log.d(TAG, "processAddressData: properties ййй" + properties);
                        if(properties.has("zone")) {
                            // Получение элементов отдельно

                            Log.d(TAG, "processAddressData: zone" + zone);
                                if(properties.getString("zone").equals(zone)) {
                                    address = String.format("%s %s %s, %s, %s %s",

                                            properties.getString("street_type"),
                                            properties.getString("street"),
                                            properties.getString("name"),
                                            properties.getString("zone"),
                                            properties.getString("settlement_type"),
                                            properties.getString("settlement"));
                                    addresses.add(new String[] {
                                            address,
                                            "",
                                            "",
                                            "",
                                    });

                                }

                            } else {
                                address = String.format("%s %s, %s, %s %s",

                                        properties.getString("street_type"),
                                        properties.getString("street"),
                                        properties.getString("name"),
                                        properties.getString("settlement_type"),
                                        properties.getString("settlement"));
                                addresses.add(new String[] {
                                        address,
                                        "",
                                        "",
                                        "",
                                });
                            }


                        double longitude = geoCentroid.getJSONArray("coordinates").getDouble(0);
                        double latitude = geoCentroid.getJSONArray("coordinates").getDouble(1);
                        Log.d(TAG, "processAddressData: latitude longitude" + latitude + " " + longitude);

                        coordinatesList.add(new double[]{longitude, latitude});
                        }
                    // Проверка по Киевской области

                    if (citySearch.equals("Київ")) {
                        Log.d(TAG, "processAddressData:citySearch " + citySearch);
                        if (checkWordInArray(properties.getString("settlement"), kyivRegionArr)) {
                            address = String.format("%s %s %s, %s %s ",
                                    properties.getString("street_type"),
                                    properties.getString("street"),
                                    properties.getString("name"),
                                    properties.getString("settlement_type"),
                                    properties.getString("settlement"));

                            Log.d(TAG, "processAddressData: address" + address);
                            addresses.add(new String[] {
                                    address,
                                    "",
                                    "",
                                    "",
                            });
                            double longitude = geoCentroid.getJSONArray("coordinates").getDouble(0);
                            double latitude = geoCentroid.getJSONArray("coordinates").getDouble(1);

                            coordinatesList.add(new double[]{longitude, latitude});
                        }
                    }
                }
            }

            addresses.add(new String[] {
                    getString(R.string.address_on_map),
                    "",
                    "",
                    "",
            });

            if (addresses.size() != 0) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    List<String> addressesList = new ArrayList<>();
                    List<String> nameList = new ArrayList<>();
                    List<String> zoneList = new ArrayList<>();
                    List<String> settlementList = new ArrayList<>();

                    for (String[] addressArray : addresses) {
                        // Выбираем значение 'address' из массива и добавляем его в addressesList
                        addressesList.add(addressArray[0]);
                        nameList.add(addressArray[1]);
                        zoneList.add(addressArray[2]);
                        settlementList.add(addressArray[3]);
                    }


                    ArrayAdapter<String> addressAdapter = new ArrayAdapter<>(requireActivity(), R.layout.custom_list_item, addressesList);

                    addressListView.setVisibility(View.VISIBLE);
                    addressListView.setAdapter(addressAdapter);
                    addressListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    addressListView.setItemChecked(0, true);

                    addressListView.setOnItemClickListener((parent, viewC, position, id) -> {
                        positionChecked = position;
                        Log.d(TAG, "processAddressData: waawdad" + positionChecked);
                        if (position == addressesList.size()-1) {
                            switch (fragmentInput) {
                                case "map":
                                    GeoDialogVisicomFragment.fragment.dismiss();
                                    dismiss();
                                    break;
                                case "home" :
                                    startActivity(new Intent(requireActivity(), OpenStreetMapActivity.class));
                                    dismiss();
                                    break;
                            }
                        } else {
                            double[] coordinates = coordinatesList.get(position);

                            if (point.equals("start")) {
                                startPoint = addressesList.get(position);
                                fromEditAddress.setText(startPoint);
                                fromEditAddress.setSelection(startPoint.length());

                                if(!verifyBuildingStart) {

                                    List<String> settings = new ArrayList<>();

                                    settings.add(Double.toString(coordinates[1]));
                                    settings.add(Double.toString(coordinates[0]));
                                    if (toEditAddress.getText().toString().equals(R.string.on_city_tv)) {
                                        settings.add(Double.toString(coordinates[1]));
                                        settings.add(Double.toString(coordinates[0]));
                                        settings.add(addressesList.get(position));
                                        settings.add(addressesList.get(position));
                                    } else {
                                        if(OpenStreetMapActivity.finishLan == 0){
                                            settings.add("");
                                            settings.add("");
                                        } else {
                                            settings.add(String.valueOf(OpenStreetMapActivity.finishLat));
                                            settings.add(String.valueOf(OpenStreetMapActivity.finishLan));
                                        }
                                        settings.add(addressesList.get(position));
                                        settings.add(toEditAddress.getText().toString());
                                    }
                                    updateRoutMarker(settings);
                                    switch (fragmentInput) {
                                        case "map" :
                                            GeoDialogVisicomFragment.geoText.setText(startPoint);
                                            OpenStreetMapActivity.startLat = coordinates[1];
                                            OpenStreetMapActivity.startLan = coordinates[0];
                                            OpenStreetMapActivity.FromAdressString = addressesList.get(position);


                                            Log.d(TAG, "settings: " + settings);
                                            fromEditAddress.setSelection(addressesList.get(position).length());

                                            double startLat = coordinates[1];
                                            double startLan = coordinates[0];
                                            if(OpenStreetMapActivity.m != null) {
                                                OpenStreetMapActivity.map.getOverlays().remove(OpenStreetMapActivity.m);
                                                OpenStreetMapActivity.map.invalidate();
                                                OpenStreetMapActivity.m = null;
                                            }

                                            GeoPoint initialGeoPoint = new GeoPoint(startLat-0.0009, startLan);
                                            OpenStreetMapActivity.map.getController().setCenter(initialGeoPoint);

                                            OpenStreetMapActivity.setMarker(startLat, startLan, startPoint, requireContext());
                                            OpenStreetMapActivity.map.invalidate();
                                            break;
                                        case "home" :
                                            VisicomFragment.geoText.setText(startPoint);
                                            break;
                                    }

                                }
                            } else if(point.equals("finish")) {
                                finishPoint = addressesList.get(position);
                                toEditAddress.setText(finishPoint);
                                toEditAddress.setSelection(finishPoint.length());
                                btn_clear_to.setVisibility(View.VISIBLE);
                                if(!verifyBuildingFinish) {
                                    List<String> settings = new ArrayList<>();
                                    switch (fragmentInput) {
                                        case "map":
                                            GeoDialogVisicomFragment.textViewTo.setText(addressesList.get(position));
                                            GeoDialogVisicomFragment.btn_clear_to.setVisibility(View.VISIBLE);
                                            if (!fromEditAddress.getText().toString().equals("")) {
                                                settings.add(Double.toString(OpenStreetMapActivity.startLat));
                                                settings.add(Double.toString(OpenStreetMapActivity.startLan));
                                                settings.add(Double.toString(coordinates[1]));
                                                settings.add(Double.toString(coordinates[0]));

                                                settings.add(fromEditAddress.getText().toString());
                                                settings.add(addressesList.get(position));

                                            }
                                            GeoPoint endPoint = new GeoPoint(coordinates[1], coordinates[0]);
                                            OpenStreetMapActivity.endPoint = endPoint;
                                            OpenStreetMapActivity.ToAdressString = finishPoint;
                                            showRoutMap(endPoint);
                                            break;
                                        case "home":
                                            VisicomFragment.textViewTo.setText(addressesList.get(position));
                                            VisicomFragment.btn_clear_to.setVisibility(View.VISIBLE);
                                            if (!fromEditAddress.getText().toString().equals("")) {
                                                settings.add(Double.toString(0));
                                                settings.add(Double.toString(0));
                                                settings.add(Double.toString(coordinates[1]));
                                                settings.add(Double.toString(coordinates[0]));

                                                settings.add(fromEditAddress.getText().toString());
                                                settings.add(addressesList.get(position));
                                            }
                                            break;
                                    }

                                    updateRoutMarker(settings);
                                    Log.d(TAG, "settings: " + settings);
                                    toEditAddress.setSelection(addressesList.get(position).length());

                                }
                            }
                        }

//                        addresses = null;
//                        coordinatesList = null;
                        addressListView.setVisibility(View.INVISIBLE);
                    });
                    btn_ok.setVisibility(View.VISIBLE);
                    btn_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(verifyBuildingStart) {
                                textGeoError.setVisibility(View.VISIBLE);
                                textGeoError.setText(R.string.house_vis_mes);

                                fromEditAddress.requestFocus();
                                fromEditAddress.setSelection(fromEditAddress.getText().toString().length());
                                KeyboardUtils.showKeyboard(getContext(), fromEditAddress);
                            }
                            if(verifyBuildingFinish) {
                                text_toError.setVisibility(View.VISIBLE);
                                text_toError.setText(R.string.house_vis_mes);

                                toEditAddress.requestFocus();
                                toEditAddress.setSelection(toEditAddress.getText().toString().length());
                                KeyboardUtils.showKeyboard(getContext(), toEditAddress);
                            }
                            Log.d(TAG, "onClick: verifyBuildingStart" + verifyBuildingStart);
                            Log.d(TAG, "onClick: verifyBuildingFinish" + verifyBuildingFinish);
                            if(verifyBuildingStart == false && verifyBuildingFinish == false) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    visicomCost();
                                    dismiss();
                                }
                            }

                        }
                    });
                });
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean checkWordInArray(String wordToCheck, String[]  searchArr) {

        boolean result = false;
        for (String word : searchArr) {
            if (word.equals(wordToCheck)) {
                // Слово найдено в массиве
               result = true;
               break;
            }
        }
        Log.d(TAG, "checkWordInArray: result" + result);
        return result;
    }

    private static String[] removeTextInParentheses(String inputText) {
        // Поиск индекса открывающей и закрывающей скобок
        int startIndex = inputText.indexOf('(');
        int endIndex = inputText.indexOf(')');

        // Если обе скобки найдены и закрывающая скобка идет после открывающей
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            // Получение текста в скобках
            String removedValueInParentheses = inputText.substring(startIndex + 1, endIndex);

            // Удаление текста в круглых скобках из исходной строки
            String result = inputText.substring(0, startIndex) + inputText.substring(endIndex + 1);

            // Возвращение результатов в виде массива
            return new String[]{result.trim(), removedValueInParentheses.trim()};
        } else {
            // Если скобки не найдены, вернуть исходную строку
            return new String[]{inputText.trim(), ""};
        }
    }

    private String numbers(String inputString) {


        // Регулярное выражение для поиска чисел после запятой и пробела
        String regex = ".*,\\s*([0-9]+).*";

        // Создание Pattern и Matcher
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputString);
        String numbersAfterComma = null;

        // Поиск соответствия
        if (matcher.matches()) {
            // Получение чисел после запятой и пробела
            numbersAfterComma = matcher.group(1);

            // Вывод чисел
            Log.d(TAG, "numbers: " + numbersAfterComma);

        }
        return numbersAfterComma;
    }




    private void showRoutMap(GeoPoint geoPoint) {
        if(OpenStreetMapActivity.marker != null) {
            OpenStreetMapActivity.map.getOverlays().remove(OpenStreetMapActivity.marker);
            OpenStreetMapActivity.map.invalidate();
            OpenStreetMapActivity.marker = null;
        }


        OpenStreetMapActivity.marker = new Marker(OpenStreetMapActivity.map);
        OpenStreetMapActivity.marker.setPosition(geoPoint);
        OpenStreetMapActivity.marker.setTextLabelBackgroundColor(
                Color.TRANSPARENT
        );
        OpenStreetMapActivity.marker.setTextLabelForegroundColor(
                Color.RED
        );
        OpenStreetMapActivity.marker.setTextLabelFontSize(40);
        OpenStreetMapActivity.marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        String unuString = new String(Character.toChars(0x1F449));

        OpenStreetMapActivity.marker.setTitle("2."+ unuString + OpenStreetMapActivity.ToAdressString);

        @SuppressLint("UseCompatLoadingForDrawables") Drawable originalDrawable = requireActivity().getResources().getDrawable(R.drawable.marker_green);
        int width = 48;
        int height = 48;
        Bitmap bitmap = Bitmap.createScaledBitmap(((BitmapDrawable) originalDrawable).getBitmap(), width, height, false);

        // Создайте новый Drawable из уменьшенного изображения
        Drawable scaledDrawable = new BitmapDrawable(requireActivity().getResources(), bitmap);
        OpenStreetMapActivity.marker.setIcon(scaledDrawable);

        OpenStreetMapActivity.marker.showInfoWindow();

        OpenStreetMapActivity.map.getOverlays().add(OpenStreetMapActivity.marker);

        GeoPoint initialGeoPoint = new GeoPoint(geoPoint.getLatitude()-0.01, geoPoint.getLongitude());
        OpenStreetMapActivity.map.getController().setCenter(initialGeoPoint);
        OpenStreetMapActivity.mapController.setZoom(16);

        OpenStreetMapActivity.map.invalidate();

        GeoPoint startPoint = new GeoPoint(OpenStreetMapActivity.startLat, OpenStreetMapActivity.startLan);

        OpenStreetMapActivity.showRout(startPoint, OpenStreetMapActivity.endPoint);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void visicomCost() {
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
            switch (fragmentInput) {
                case "map" :
                    GeoDialogVisicomFragment.firstCost = Long.parseLong(orderCost);
                    discount = GeoDialogVisicomFragment.firstCost * discountInt / 100;
                    GeoDialogVisicomFragment.firstCost = GeoDialogVisicomFragment.firstCost + discount;
                    updateAddCost(String.valueOf(discount));
                    GeoDialogVisicomFragment.text_view_cost.setText(String.valueOf(GeoDialogVisicomFragment.firstCost));
                    GeoDialogVisicomFragment.MIN_COST_VALUE = (long) (GeoDialogVisicomFragment.firstCost*0.6);
                    GeoDialogVisicomFragment.firstCostForMin = GeoDialogVisicomFragment.firstCost;
                    break;
                case "home" :
                    VisicomFragment.firstCost = Long.parseLong(orderCost);
                    discount = VisicomFragment.firstCost * discountInt / 100;
                    VisicomFragment.firstCost = VisicomFragment.firstCost + discount;
                    updateAddCost(String.valueOf(discount));
                    VisicomFragment.text_view_cost.setText(String.valueOf(VisicomFragment.firstCost));
                    VisicomFragment.MIN_COST_VALUE = (long) (VisicomFragment.firstCost*0.6);
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
                    break;
            }
        }


    }

    private void updateAddCost(String addCost) {
        ContentValues cv = new ContentValues();
        Log.d("TAG", "updateAddCost: addCost" + addCost);
        cv.put("addCost", addCost);

        // обновляем по id
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        database.update(MainActivity.TABLE_SETTINGS_INFO, cv, "id = ?",
                new String[] { "1" });
        database.close();
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

    private void updateRoutMarker(List<String> settings) {
        Log.d(TAG, "updateRoutMarker: " + settings.toString());
        ContentValues cv = new ContentValues();
        if(Double.parseDouble(settings.get(0)) != 0) {
            cv.put("startLat",  Double.parseDouble(settings.get(0)));
            cv.put("startLan", Double.parseDouble(settings.get(1)));
        }

        if(!settings.get(2).equals("")){
            cv.put("to_lat", Double.parseDouble(settings.get(2)));
            cv.put("to_lng", Double.parseDouble(settings.get(3)));
        } else {
            cv.put("to_lat",  Double.parseDouble(settings.get(0)));
            cv.put("to_lng", Double.parseDouble(settings.get(1)));
        }
        cv.put("start", settings.get(4));
        cv.put("finish", settings.get(5));

        // обновляем по id
        SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        database.update(MainActivity.ROUT_MARKER, cv, "id = ?",
                new String[] { "1" });
        database.close();
    }

    @SuppressLint("Range")
    private List<String> logCursor(String table, Context context) {
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

