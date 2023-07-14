package com.taxieasyua.back4app.ui.home;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.start.StartActivity;

import java.util.ArrayList;
import java.util.List;


public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private String tariff;
    ListView listView;
    public String[] arrayService;
    public static String[] arrayServiceCode;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_layout, container, false);
        listView = view.findViewById(R.id.list);

        arrayService = new String[]{
                getString(R.string.BAGGAGE),
                getString(R.string.ANIMAL),
                getString(R.string.CONDIT),
                getString(R.string.MEET),
                getString(R.string.COURIER),
                getString(R.string.TERMINAL),
                getString(R.string.CHECK),
                getString(R.string.BABY_SEAT),
                getString(R.string.DRIVER),
                getString(R.string.NO_SMOKE),
                getString(R.string.ENGLISH),
                getString(R.string.CABLE),
                getString(R.string.FUEL),
                getString(R.string.WIRES),
                getString(R.string.SMOKE),
        };
        arrayServiceCode = new String[]{
                "BAGGAGE",
                "ANIMAL",
                "CONDIT",
                "MEET",
                "COURIER",
                "TERMINAL",
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

        ArrayAdapter<String> adapterSet = new ArrayAdapter<>(view.getContext(), R.layout.services_adapter_layout, arrayService);
        listView.setAdapter(adapterSet);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        List<String> services = StartActivity.logCursor(StartActivity.TABLE_SERVICE_INFO);
        for (int i = 0; i < arrayServiceCode.length; i++) {
            if(services.get(i+1).equals("1")) {
                listView.setItemChecked(i,true);
            }
        }

        String[] tariffArr = new String[]{
                "Базовий онлайн",
                "Базовый",
                "Универсал",
                "Бизнес-класс",
                "Премиум-класс",
                "Эконом-класс",
                "Микроавтобус",
        };
        ArrayAdapter<String> adapterTariff = new ArrayAdapter<String>(view.getContext(), R.layout.my_simple_spinner_item, tariffArr);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Spinner spinner = view.findViewById(R.id.list_tariff);
        spinner.setAdapter(adapterTariff);
        spinner.setPrompt("Title");
        spinner.setBackgroundResource(R.drawable.spinner_border);
        StartActivity.cursorDb = StartActivity.database.query(StartActivity.TABLE_SETTINGS_INFO, null, null, null, null, null, null);
        String tariffOld =  StartActivity.logCursor(StartActivity.TABLE_SETTINGS_INFO).get(2);
        if (StartActivity.cursorDb != null && !StartActivity.cursorDb.isClosed())
            StartActivity.cursorDb.close();
        for (int i = 0; i < tariffArr.length; i++) {
            if(tariffArr[i].equals(tariffOld)) {
                spinner.setSelection(i);
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tariff = tariffArr[position];
                ContentValues cv = new ContentValues();
                cv.put("tarif", tariff);

                // обновляем по id
                StartActivity.database.update(StartActivity.TABLE_SETTINGS_INFO, cv, "id = ?",
                        new String[] { "1" });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        for (int i = 0; i < 15; i++) {
            Log.d("TAG", "onPause: " + arrayServiceCode[i]);
            ContentValues cv = new ContentValues();
            cv.put(arrayServiceCode[i], "0");
            StartActivity.database.update(StartActivity.TABLE_SERVICE_INFO, cv, "id = ?",
                    new String[] { "1" });
        }

        SparseBooleanArray booleanArray = listView.getCheckedItemPositions();
        booleanArray = listView.getCheckedItemPositions();
        for (int i = 0; i < booleanArray.size(); i++) {
            if(booleanArray.get(booleanArray.keyAt(i))) {
                ContentValues cv = new ContentValues();
                cv.put(arrayServiceCode[booleanArray.keyAt(i)], "1");
                StartActivity.database.update(StartActivity.TABLE_SERVICE_INFO, cv, "id = ?",
                        new String[] { "1" });

            }
        }

    }
}

