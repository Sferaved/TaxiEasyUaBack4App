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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.gallery.GalleryFragment;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MyBottomSheetBonusFragment extends BottomSheetDialogFragment {

    String bonusMessage;
    String rout;
    String api;
    TextView textView;
    String fragment;
    ListView listView;
    String[] array, arrayCode;
    AppCompatButton btn_ok;
    int pos;
    public MyBottomSheetBonusFragment(String bonusMessage) {
        this.bonusMessage = bonusMessage;
        this.rout = "";
    }
    public MyBottomSheetBonusFragment(String bonusMessage, String rout, String api) {
        this.bonusMessage = bonusMessage;
        this.rout = rout;
        this.api = api;

    }
    public MyBottomSheetBonusFragment(String bonusMessage, String rout, String api, TextView textView, String fragment) {
        this.bonusMessage = bonusMessage;
        this.rout = rout;
        this.api = api;
        this.textView = textView;
        this.fragment = fragment;
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
                getString(R.string.bonus_payment)
        };
        arrayCode = new  String[]{
                "nal_payment",
                "bonus_payment"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.services_adapter_layout, array);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        String bonusPayment = null;
        String query = "SELECT bonusPayment FROM " + MainActivity.TABLE_SETTINGS_INFO + " WHERE id = ?";
        String[] selectionArgs = new String[] { "1" };
        SQLiteDatabase database = view.getContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

        @SuppressLint("Recycle") Cursor cursor = database.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            bonusPayment = cursor.getString(cursor.getColumnIndex("bonusPayment"));
        }
        Log.d("TAG", "onCreateView: bonusPayment " + bonusPayment);
        if ("bonus_payment".equals(bonusPayment)) {
            listView.setItemChecked(1, true);
            MainActivity.bonusPayment = "bonus_payment";
            pos = 1;
        } else {
            listView.setItemChecked(0, true);
            MainActivity.bonusPayment = "nal_payment";
            pos = 0;
        }
        database.close();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("TAG", "onItemClick: position" + position);
                Log.d("TAG", "onItemClick: array  position" + arrayCode [position]);
                pos = position;
                MainActivity.bonusPayment =  arrayCode [pos];

                ContentValues cv = new ContentValues();
                SQLiteDatabase database = view.getContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

                cv.put("bonusPayment", arrayCode [pos]);
                database.update(MainActivity.TABLE_SETTINGS_INFO, cv, "id = ?",
                        new String[] { "1" });
                database.close();
            }

        });

        btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.bonusPayment =  arrayCode [pos];

                ContentValues cv = new ContentValues();
                SQLiteDatabase database = view.getContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

                cv.put("bonusPayment", arrayCode [pos]);
                database.update(MainActivity.TABLE_SETTINGS_INFO, cv, "id = ?",
                        new String[] { "1" });
                database.close();
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d("TAG", "onDismiss: rout " + rout);
        if(rout.equals("home")) {
            String urlCost = null;
            Map<String, String> sendUrlMapCost = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    urlCost = getTaxiUrlSearch("costSearch", getActivity());
                }

                sendUrlMapCost = CostJSONParser.sendURL(urlCost);
            } catch (MalformedURLException ignored) {

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

                HomeFragment.addCost = discount;
                firstCost = firstCost + HomeFragment.addCost;
                String costUpdate = String.valueOf(firstCost);
                HomeFragment.text_view_cost.setText(costUpdate);
            }
        }
        if(rout.equals("geo")) {
                String urlCost = null;
                Map<String, String> sendUrlMapCost = null;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        urlCost = getTaxiUrlSearchGeo("costSearchGeo", getActivity());
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
                MyGeoDialogFragment.addCost = discount;
                firstCost = firstCost + MyGeoDialogFragment.addCost;
                String costUpdate = String.valueOf(firstCost);
                textView.setText(costUpdate);
                }
            }
        if(rout.equals("marker")) {
                String urlCost = null;
                Map<String, String> sendUrlMapCost = null;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        urlCost = getTaxiUrlSearchMarkers("costSearchMarkers", getActivity());
                    }

                    sendUrlMapCost = CostJSONParser.sendURL(urlCost);
                } catch (MalformedURLException ignored) {

                }
            assert sendUrlMapCost != null;
            String orderCost = (String) sendUrlMapCost.get("order_cost");
            Log.d("TAG", "onDismiss: orderCost " + orderCost);
            assert orderCost != null;
            if (!orderCost.equals("0")) {
                String costUpdate;
                String discountText = logCursor(MainActivity.TABLE_SETTINGS_INFO, getContext()).get(3);
                long discountInt = Integer.parseInt(discountText);
                long discount;
                long firstCost = Long.parseLong(orderCost);
                discount = firstCost * discountInt / 100;


                if (fragment.equals("Gallery")) {
                    GalleryFragment.addCost = discount;
                    firstCost = firstCost + GalleryFragment.addCost;
                } else {
                    MyGeoMarkerDialogFragment.addCost = discount;
                    firstCost = firstCost + MyGeoMarkerDialogFragment.addCost;
                }
                costUpdate = String.valueOf(firstCost);
                textView.setText(costUpdate);
            }
            }
        }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTaxiUrlSearch(String urlAPI, Context context) {

        List<String> stringListRout = logCursor(MainActivity.ROUT_HOME, context);

        String from = stringListRout.get(1);
        String from_number = stringListRout.get(2);
        String to = stringListRout.get(3);
        String to_number = stringListRout.get(4);

        // Origin of route
        String str_origin = from + "/" + from_number;

        // Destination of route
        String str_dest = to + "/" + to_number;

        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

        String tarif =  logCursor(MainActivity.TABLE_SETTINGS_INFO, context).get(2);

        // Building the parameters to the web service

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
                    + displayName + "*" + userEmail  + "*" + MainActivity.bonusPayment;
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
            Log.d("TAG", "getTaxiUrlSearchGeo result:" + result + "/");
        } else {
            result = "no_extra_charge_codes";
        }

        String url = "https://m.easy-order-taxi.site/" + api + "/android/" + urlAPI + "/" + parameters + "/" + result;

        Log.d("TAG", "getTaxiUrlSearch: " + url);



        return url;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTaxiUrlSearchGeo(String urlAPI, Context context) {

        List<String> stringListRout = logCursor(MainActivity.ROUT_GEO, context);
        Log.d("TAG", "getTaxiUrlSearch: Bonus stringListRout" + stringListRout);

        double originLatitude = Double.parseDouble(stringListRout.get(1));
        double originLongitude = Double.parseDouble(stringListRout.get(2));
        String to = stringListRout.get(3);
        String to_number = stringListRout.get(4);

        if(to_number.equals("XXX")) {
            to_number = " ";
        }

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
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + MainActivity.bonusPayment;
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTaxiUrlSearchMarkers(String urlAPI, Context context) {

        List<String> stringListRout = logCursor(MainActivity.ROUT_MARKER, context);
        Log.d("TAG", "getTaxiUrlSearch: stringListRout" + stringListRout);

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
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + MainActivity.bonusPayment;
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
        SQLiteDatabase database = getActivity().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
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

