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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MyBottomSheetBonusFragment extends BottomSheetDialogFragment {

    String bonusMessage;
    String rout;
    String api;
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
            String orderCost = (String) sendUrlMapCost.get("order_cost");

            if (!orderCost.equals("0")) {
                String costUpdate = String.valueOf(HomeFragment.addCost +  Long.parseLong(orderCost));
                HomeFragment.text_view_cost.setText(costUpdate);
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


        List<String> stringList = logCursor(MainActivity.TABLE_ADD_SERVICE_INFO, context);
        String time = stringList.get(1);
        String comment = stringList.get(2);
        String date = stringList.get(3);



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

        if(urlAPI.equals("orderSearch")) {
            phoneNumber = logCursor(MainActivity.TABLE_USER_INFO, context).get(2);

            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName + "*" + userEmail  + "*" + MainActivity.bonusPayment + "/" + HomeFragment.addCost + "/" + time + "/" + comment + "/" + date;

            ContentValues cv = new ContentValues();

            cv.put("time", "no_time");
            cv.put("comment", "no_comment");
            cv.put("date", "no_date");

            // обновляем по id
            database.update(MainActivity.TABLE_ADD_SERVICE_INFO, cv, "id = ?",
                    new String[] { "1" });

        }

        // Building the url to the web service
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
    public List<String> logCursor(String table, Context context) {
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

