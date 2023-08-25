package com.taxieasyua.back4app.ui.start;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class GoogleSignInActivity extends Activity {
    private static final int RC_SIGN_IN = 9001;
    private final String TAG = "TAG";
    static FloatingActionButton fab, btn_again;
    Button try_again_button;
    String api;
    Intent intent;
    SQLiteDatabase database;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);

        }

        List<String> stringListArr = logCursor(StartActivity.CITY_INFO);
        switch (stringListArr.get(1)){
            case "Kyiv City":
                api = StartActivity.apiKyiv;
                break;
            case "Dnipropetrovsk Oblast":
                api = StartActivity.apiDnipro;
                break;
            case "Odessa":
                api = StartActivity.apiOdessa;
                break;
            case "Zaporizhzhia":
                api = StartActivity.apiZaporizhzhia;
                break;
            case "Cherkasy Oblast":
                api = StartActivity.apiCherkasy;
                break;
            default:
                api = StartActivity.apiDnipro;
                break;
        }

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SuspiciousIndentation")
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
                    case "Cherkasy Oblast":
                        phone = "tel:0962294243";
                        break;
                    default:
                        phone = "tel:0674443804";
                        break;
                }
                intent.setData(Uri.parse(phone));
                startActivity(intent);
            }
        });

        try_again_button = findViewById(R.id.try_again_button);
        try_again_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), StartActivity.class));
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

// Создание клиента авторизации
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

// Запуск активности авторизации
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @SuppressLint("Range")
    public List<String> logCursor(String table) {
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
    public void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                handleSignInResult(task);
            } catch (MalformedURLException | JSONException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) throws MalformedURLException, JSONException, InterruptedException {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    //Change UI according to user data.
    public void updateUI(GoogleSignInAccount account) throws MalformedURLException {
        database = getApplicationContext().openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);
        ContentValues cv = new ContentValues();
        if(account != null){
            StartActivity.userEmail = account.getEmail();
            StartActivity.displayName = account.getDisplayName();

            addUser();

            String url = "https://m.easy-order-taxi.site/" + api + "/android/verifyBlackListUser/" +  StartActivity.userEmail;

            Map<String, String> sendUrlMap = CostJSONParser.sendURL(url);

            String message = sendUrlMap.get("message");

            assert message != null;
            if (message.equals("Не черном списке")) {

                cv.put("verifyOrder", "1");
                database.update(StartActivity.TABLE_USER_INFO, cv, "id = ?", new String[]{"1"});

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    intent = new Intent(GoogleSignInActivity.this, MainActivity.class);
                }

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    intent = new Intent(GoogleSignInActivity.this, OpenStreetMapActivity.class);
                }
                startActivity(intent);
            }
            if (message.equals("В черном списке")) {
                Toast.makeText(this, getString(R.string.firebase_error), Toast.LENGTH_SHORT).show();
                try_again_button.setVisibility(View.VISIBLE);
                cv.put("verifyOrder", "0");
                database.update(StartActivity.TABLE_USER_INFO, cv, "id = ?", new String[]{"1"});
            }
        }else {
            Toast.makeText(this, getString(R.string.firebase_error), Toast.LENGTH_SHORT).show();
            try_again_button.setVisibility(View.VISIBLE);
            cv.put("verifyOrder", "0");
            database.update(StartActivity.TABLE_USER_INFO, cv, "id = ?", new String[]{"1"});
        }
        database.close();

    }

    private void addUser() throws MalformedURLException {
        String urlString = "https://m.easy-order-taxi.site/" + api + "/android/addUser/" +  StartActivity.displayName + "/" + StartActivity.userEmail;

        URL url = new URL(urlString);
        Log.d("TAG", "sendURL: " + urlString);

        AsyncTask.execute(() -> {
            HttpsURLConnection urlConnection = null;
            try {
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.getResponseCode();
            } catch (IOException e) {
                Log.d(TAG, "addUser: " + new RuntimeException(e));
            }
            assert urlConnection != null;
            urlConnection.disconnect();
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        try_again_button.setVisibility(View.VISIBLE);
        try_again_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GoogleSignInActivity.this, StartActivity.class));
            }
        });
    }
}