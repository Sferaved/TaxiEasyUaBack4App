package com.taxieasyua.back4app.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.visicom.VisicomFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MyBottomSheetGPSFragment extends BottomSheetDialogFragment {

    AppCompatButton btn_ok, btn_no;


    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gps_layout, container, false);


        btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(HomeFragment.progressBar != null) {
                    HomeFragment.progressBar.setVisibility(View.INVISIBLE);
                }
                if(VisicomFragment.progressBar != null) {
                    VisicomFragment.progressBar.setVisibility(View.INVISIBLE);
                }
                requireActivity().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        btn_no = view.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(HomeFragment.progressBar != null) {
                    HomeFragment.progressBar.setVisibility(View.INVISIBLE);
                }
                if(VisicomFragment.progressBar != null) {
                    VisicomFragment.progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
        if(HomeFragment.progressBar != null) {
            HomeFragment.progressBar.setVisibility(View.INVISIBLE);
        }
        if(VisicomFragment.progressBar != null) {
            VisicomFragment.progressBar.setVisibility(View.INVISIBLE);
        }
        return view;
    }

}

