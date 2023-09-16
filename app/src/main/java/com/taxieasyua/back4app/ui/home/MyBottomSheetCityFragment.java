package com.taxieasyua.back4app.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;


public class MyBottomSheetCityFragment extends BottomSheetDialogFragment {

    ListView listView;
    String city;

    public MyBottomSheetCityFragment(String city) {
        this.city = city;
    }
    private final String[] cityList = new String[]{
            "Київ",
            "Дніпро",
            "Одеса",
            "Запоріжжя",
            "Черкаси",

    };
    private final String[] cityCode = new String[]{
            "Kyiv City",
            "Dnipropetrovsk Oblast",
            "Odessa",
            "Zaporizhzhia",
            "Cherkasy Oblast",

    };



    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bonus_list_layout, container, false);
        listView = view.findViewById(R.id.listViewBonus);
        HomeFragment.progressBar.setVisibility(View.INVISIBLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.services_adapter_layout, cityList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        int position = 0;


        switch (this.city){
            case "Kyiv City":
                position = 0;
                break;
            case "Dnipropetrovsk Oblast":
                position = 1;
                break;
            case "Odessa":
                position = 2;
                break;
            case "Zaporizhzhia":
                position = 3;
                break;
            case "Cherkasy Oblast":
                position = 4;
                break;
            default:
                position = 0;
                break;
        }
        listView.setItemChecked(position,true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("TAG", "onItemClick: position" + position);
                Log.d("TAG", "onItemClick: array  position" + cityList [position]);


                ContentValues cv = new ContentValues();
                SQLiteDatabase database = view.getContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

                cv.put("city", cityCode [position]);
                database.update(MainActivity.CITY_INFO, cv, "id = ?",
                        new String[] { "1" });
                database.close();
                getActivity().finishAffinity();
                Toast.makeText(getActivity(), getString(R.string.change_message) + cityList [position]   , Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), MainActivity.class));

            }
        });

        return view;
    }


   }

