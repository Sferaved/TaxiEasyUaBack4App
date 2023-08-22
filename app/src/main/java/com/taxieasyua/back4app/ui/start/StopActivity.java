package com.taxieasyua.back4app.ui.start;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.taxieasyua.back4app.R;

import java.util.ArrayList;
import java.util.List;

public class StopActivity extends Activity {
    static FloatingActionButton fab, btn_again;

    Button try_again_button;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);

        fab = findViewById(R.id.fab);
        try_again_button = findViewById(R.id.try_again_button);
        try_again_button.setVisibility(View.VISIBLE);
        try_again_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StopActivity.this, StartActivity.class));
            }
        });


        btn_again = findViewById(R.id.btn_again);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                String phone;
                List<String> stringList = logCursor(StartActivity.CITY_INFO);
                switch (stringList.get(1)){
                    case "Kyiv City":
                        phone = "tel:0674443804";
                        break;
                    case "Dnipropetrovsk Oblast":
                        phone = "tel:0667257070";
                        break;
                    case "Odessa":
                        phone = "tel:0737257070";
                        break;
                    case "Zaporizhzhia":
                        phone = "tel:0687257070";
                        break;
                    default:
                        phone = "tel:0674443804";
                        break;
                }
                intent.setData(Uri.parse(phone));
                startActivity(intent);
            }
        });


        btn_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StopActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("SuspiciousIndentation")
    @Override
    protected void onResume() {
        super.onResume();
    }
    @SuppressLint("Range")
    private List<String> logCursor(String table) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase database = this.openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);
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
