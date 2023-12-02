package com.taxieasyua.back4app.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.cities.api.CityApiClient;
import com.taxieasyua.back4app.cities.api.CityResponse;
import com.taxieasyua.back4app.cities.api.CityResponseMerchantFondy;
import com.taxieasyua.back4app.cities.api.CityService;
import com.taxieasyua.back4app.ui.visicom.VisicomFragment;


import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class MyBottomSheetCityFragment extends BottomSheetDialogFragment {

    private static final String TAG = "TAG_CITY";
    ListView listView;
    String city;
    AppCompatButton btn_ok;
    private String cityMenu;
    private String message;

    public MyBottomSheetCityFragment() {
        // Пустой конструктор без аргументов
    }

    public MyBottomSheetCityFragment(String city) {
        this.city = city;
    }

    private final String[] cityList = new String[]{
            "Київ",
            "Дніпро",
            "Одеса",
            "Запоріжжя",
            "Черкаси",
            "Тест"
    };
    private final String[] cityCode = new String[]{
            "Kyiv City",
            "Dnipropetrovsk Oblast",
            "Odessa",
            "Zaporizhzhia",
            "Cherkasy Oblast",
            "OdessaTest",
    };

    int positionFirst;

    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cities_list_layout, container, false);
        listView = view.findViewById(R.id.listViewBonus);
        VisicomFragment.progressBar.setVisibility(View.INVISIBLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.services_adapter_layout, cityList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (city == null) {
            List<String> listCity = logCursor(MainActivity.CITY_INFO);
            city = listCity.get(1);
        }
        switch (city) {
            case "Dnipropetrovsk Oblast":
                positionFirst = 1;
                break;
            case "Odessa":
                positionFirst = 2;
                break;
            case "Zaporizhzhia":
                positionFirst = 3;
                break;
            case "Cherkasy Oblast":
                positionFirst = 4;
                break;
            case "OdessaTest":
                positionFirst = 5;
                break;
            default:
                positionFirst = 0;
                break;
        }
        ContentValues cv = new ContentValues();
        SQLiteDatabase database = view.getContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        cv.put("city", cityCode[positionFirst]);
        database.update(MainActivity.CITY_INFO, cv, "id = ?",
                new String[]{"1"});
        database.close();

        cityMaxPay(cityCode[positionFirst]);
        merchantFondy(cityCode[positionFirst]);
        listView.setItemChecked(positionFirst, true);
        int positionFirstOld = positionFirst;

        switch (cityCode[positionFirst]){
            case "Dnipropetrovsk Oblast":
                // Днепр
                cityMenu = getString(R.string.city_dnipro);
                break;
            case "Odessa":
                // Одесса
                cityMenu = getString(R.string.city_odessa);
                break;
            case "Zaporizhzhia":
                // Запорожье
                cityMenu = getString(R.string.city_zaporizhzhia);
                break;
            case "Cherkasy Oblast":
                // Черкассы
                cityMenu = getString(R.string.city_cherkasy);
                break;
            case "OdessaTest":
                cityMenu = "Test";
                break;
            default:
                cityMenu = getString(R.string.city_kyiv);
                break;
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                positionFirst = position;
                switch (cityCode[positionFirst]){
                    case "Dnipropetrovsk Oblast":
                        // Днепр
                        cityMenu = getString(R.string.city_dnipro);
                        break;
                    case "Odessa":
                        // Одесса
                        cityMenu = getString(R.string.city_odessa);
                        break;
                    case "Zaporizhzhia":
                        // Запорожье
                        cityMenu = getString(R.string.city_zaporizhzhia);
                        break;
                    case "Cherkasy Oblast":
                        // Черкассы
                        cityMenu = getString(R.string.city_cherkasy);
                        break;
                    case "OdessaTest":
                        cityMenu = "Test";
                        break;
                    default:
                        cityMenu = getString(R.string.city_kyiv);
                        break;
                }
                if (positionFirstOld != positionFirst) {
                    ContentValues cv = new ContentValues();
                    SQLiteDatabase database = view.getContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

                    cv.put("city", cityCode[positionFirst]);
                    database.update(MainActivity.CITY_INFO, cv, "id = ?",
                            new String[]{"1"});
                    database.close();

                    cityMaxPay(cityCode[positionFirst]);
                    merchantFondy(cityCode[positionFirst]);

                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                    resetRoutHome();
                    navController.navigate(R.id.nav_visicom);

                    message = getString(R.string.change_message) + getString(R.string.hi_mes) + " "+ getString(R.string.menu_city) + " " + cityMenu + ".";

                    if (MainActivity.navVisicomMenuItem != null) {
                        // Новый текст элемента меню
                        String newTitle =  getString(R.string.menu_city) + " " + cityMenu;
                        // Изменяем текст элемента меню
                        MainActivity.navVisicomMenuItem.setTitle(newTitle);
                    }

//                    MyBottomSheetMessageFragment bottomSheetDialogFragment = new MyBottomSheetMessageFragment(message);
//                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                }
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    private void cityMaxPay(String $city) {
        CityService cityService = CityApiClient.getClient().create(CityService.class);

        // Замените "your_city" на фактическое название города
        Call<CityResponse> call = cityService.getMaxPayValues($city);

        call.enqueue(new Callback<CityResponse>() {
            @Override
            public void onResponse(Call<CityResponse> call, Response<CityResponse> response) {
                if (response.isSuccessful()) {
                    CityResponse cityResponse = response.body();
                    if (cityResponse != null) {
                        int cardMaxPay = cityResponse.getCardMaxPay();
                        int bonusMaxPay = cityResponse.getBonusMaxPay();

                        ContentValues cv = new ContentValues();
                        cv.put("card_max_pay", cardMaxPay);
                        cv.put("bonus_max_pay", bonusMaxPay);
                        if (isAdded()) {
                            SQLiteDatabase database = requireActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
                            database.update(MainActivity.CITY_INFO, cv, "id = ?",
                                    new String[]{"1"});

                            database.close();
                        }


                        // Добавьте здесь код для обработки полученных значений
                    }
                } else {
                    Log.e("Request", "Failed. Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CityResponse> call, Throwable t) {
                Log.e("Request", "Failed. Error message: " + t.getMessage());
            }
        });
    }

    private void merchantFondy(String $city) {
        CityService cityService = CityApiClient.getClient().create(CityService.class);

        // Замените "your_city" на фактическое название города
        Call<CityResponseMerchantFondy> call = cityService.getMerchantFondy($city);

        call.enqueue(new Callback<CityResponseMerchantFondy>() {
            @Override
            public void onResponse(@NonNull Call<CityResponseMerchantFondy> call, @NonNull Response<CityResponseMerchantFondy> response) {
                if (response.isSuccessful()) {
                    CityResponseMerchantFondy cityResponse = response.body();
                    Log.d(TAG, "onResponse: cityResponse" + cityResponse);
                    if (cityResponse != null) {
                        String merchant_fondy = cityResponse.getMerchantFondy();
                        String fondy_key_storage = cityResponse.getFondyKeyStorage();

                        ContentValues cv = new ContentValues();
                        cv.put("merchant_fondy", merchant_fondy);
                        cv.put("fondy_key_storage", fondy_key_storage);

                        if (isAdded()) {
                            SQLiteDatabase database = getContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
                            database.update(MainActivity.CITY_INFO, cv, "id = ?",
                                    new String[]{"1"});

                            database.close();
                        }


                        Log.d(TAG, "onResponse: merchant_fondy" + merchant_fondy);
                        Log.d(TAG, "onResponse: fondy_key_storage" + fondy_key_storage);


                        // Добавьте здесь код для обработки полученных значений
                    }
                } else {
                    Log.e("Request", "Failed. Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CityResponseMerchantFondy> call, Throwable t) {
                Log.e("Request", "Failed. Error message: " + t.getMessage());
            }
        });
    }

    public void resetRoutHome() {
        ContentValues cv = new ContentValues();

        cv.put("from_street", " ");
        cv.put("from_number", " ");
        cv.put("to_street", " ");
        cv.put("to_number", " ");

        // обновляем по id
        SQLiteDatabase database = requireContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        database.update(MainActivity.ROUT_HOME, cv, "id = ?",
                new String[]{"1"});
        database.close();
    }

    @SuppressLint("Range")
    public List<String> logCursor(String table) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor c = db.query(table, null, null, null, null, null, null);
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
        db.close();
        return list;
    }
}

