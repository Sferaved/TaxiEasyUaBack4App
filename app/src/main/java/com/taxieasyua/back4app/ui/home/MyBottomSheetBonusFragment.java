package com.taxieasyua.back4app.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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

import org.json.JSONException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;


public class MyBottomSheetBonusFragment extends BottomSheetDialogFragment {

    String bonusMessage;
    ListView listView;
    String[] array, arrayCode;
    AppCompatButton btn_ok;
    int pos;

    public MyBottomSheetBonusFragment(String bonusMessage) {
        this.bonusMessage = bonusMessage;
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

        Cursor cursor = database.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            bonusPayment = cursor.getString(cursor.getColumnIndex("bonusPayment"));
        }
        if ("bonus_payment".equals(bonusPayment)) {
            listView.setItemChecked(1, true);
        } else {
            listView.setItemChecked(0, true);
        }


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
                dismiss();
            }
        });

        return view;
    }
   }

