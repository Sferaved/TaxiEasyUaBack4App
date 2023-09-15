package com.taxieasyua.back4app.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;

import java.util.ArrayList;
import java.util.List;


public class MyBottomSheetBonusFragment extends BottomSheetDialogFragment {
    TextView textViewBonus;
    AppCompatButton btn_sent;
    String bonusMessage;
    AppCompatButton btn_min, btn_plus;
    int bonus;

    public MyBottomSheetBonusFragment(String bonusMessage) {
        this.bonusMessage = bonusMessage;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bonus_list_layout, container, false);

        btn_sent = view.findViewById(R.id.btn_sent);
        btn_sent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        textViewBonus = view.findViewById(R.id.textViewBonus);
        textViewBonus.setText(bonusMessage);


        int MIN_VALUE = 0;
        int MAX_VALUE = Integer.valueOf(bonusMessage);
        bonus = Integer.valueOf(bonusMessage);

        btn_min = view.findViewById(R.id.btn_minus);
        btn_min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bonus = Integer.valueOf(textViewBonus.getText().toString());
                bonus -= 1;
                if (bonus <= MIN_VALUE) {
                    bonus = MIN_VALUE;
                }
                textViewBonus.setText(String.valueOf(bonus));
            }
        });
        btn_plus = view.findViewById(R.id.btn_plus);
        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bonus = Integer.valueOf(textViewBonus.getText().toString());
                bonus += 1;
                if (bonus >= MAX_VALUE) {
                    bonus = MAX_VALUE;
                }
                textViewBonus.setText(String.valueOf(bonus));
            }
        });



        return view;
    }


   }

