package com.taxieasyua.back4app.ui.start;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.NotificationHelper;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class FirebaseSignIn extends AppCompatActivity {

    static FloatingActionButton fab, btn_again;
    String api;
    private static final int REQUEST_ENABLE_GPS = 1001;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);

        }

        List<String> stringListArr = logCursor(MainActivity.CITY_INFO);
        switch (stringListArr.get(1)){
           case "Dnipropetrovsk Oblast":
                api = MainActivity.apiDnipro;
                break;
            case "Odessa":
                api = MainActivity.apiOdessa;
                break;
            case "Zaporizhzhia":
                api = MainActivity.apiZaporizhzhia;
                break;
            case "Cherkasy Oblast":
                api = MainActivity.apiCherkasy;
                break;
            case "OdessaTest":
                api = MainActivity.apiTest;
                break;
            default:
                api = MainActivity.apiKyiv;
                break;
        }

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                String phone;
                List<String> stringList = logCursor(MainActivity.CITY_INFO);
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

//        startSignInInBackground();
    }

//    private void startSignInInBackground() {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // Инициализация FirebaseApp
//                FirebaseApp.initializeApp(FirebaseSignIn.this);
//
//                // Choose authentication providers
//                List<AuthUI.IdpConfig> providers = Arrays.asList(
//                        new AuthUI.IdpConfig.GoogleBuilder().build());
//
//                // Create and launch sign-in intent
//                Intent signInIntent = AuthUI.getInstance()
//                        .createSignInIntentBuilder()
//                        .setAvailableProviders(providers)
//                        .build();
//                try {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            signInLauncher.launch(signInIntent);
//                        }
//                    });
//                } catch (NullPointerException e) {
//                    finish();
//                    startActivity(new Intent(FirebaseSignIn.this, StopActivity.class));
//                }
//            }
//        });
//        thread.start();
//    }


    @SuppressLint("Range")
    public List<String> logCursor(String table) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase database = this.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
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
    @Override
    protected void onResume() {
        super.onResume();
        btn_again = findViewById(R.id.btn_again);
        btn_again.setVisibility(View.VISIBLE);
        btn_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FirebaseSignIn.this, MainActivity.class));
            }
        });

    }

//    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
//            new FirebaseAuthUIActivityResultContract(),
//            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
//                @Override
//                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
//                    try {
//                        onSignInResult(result);
//                    } catch (MalformedURLException | JSONException | InterruptedException e) {
//                        Log.d("TAG", "onCreate:" + new RuntimeException(e));
//                    }
//                }
//            }
//    );


//    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) throws MalformedURLException, JSONException, InterruptedException {
//        ContentValues cv = new ContentValues();
//        try {
//            if (result.getResultCode() == RESULT_OK) {
//                // Successfully signed in
//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                MainActivity.userEmail = user.getEmail();
//                MainActivity.displayName = user.getDisplayName();
//                updateRecordsUserInfo("email", user.getEmail());
//                updateRecordsUserInfo("username", user.getDisplayName());
//                addUser();
//
//                // Проверяем состояние GPS
//                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                boolean gpsEnabled = false;
//                try {
//                    gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
//                } catch (Exception ignored) {
//                }
//
//                // Если GPS выключен, выводим диалог с предложением его включить
//                if (!gpsEnabled) {
//                    openGPSSettings();
//                } else {
//                    // Проверяем состояние Location Service с помощью колбэка
//                    checkLocationServiceEnabled(new LocationServiceCallback() {
//                        @Override
//                        public void onLocationServiceResult(boolean isEnabled) throws MalformedURLException {
//                            if (isEnabled) {
//                               // Проверяем разрешения на местоположение
//                                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                                        && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                                    checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
//                                    checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
//                                }
//                            }
//
//                        }
//                    });
//                }
//
//                // Здесь также происходит обновление значения verifyOrder в базе данных
//
//                cv.put("verifyOrder", "1");
//                SQLiteDatabase database = getApplicationContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
//                database.update(MainActivity.TABLE_USER_INFO, cv, "id = ?", new String[]{"1"});
//                database.close();
//                Intent intent = new Intent(FirebaseSignIn.this, MainActivity.class);
//                startActivity(intent);
//            } else {
//                // Sign in failed
//                Toast.makeText(this, getString(R.string.firebase_error), Toast.LENGTH_SHORT).show();
//                btn_again.setVisibility(View.VISIBLE);
//
//                cv.put("verifyOrder", "0");
//                SQLiteDatabase database = getApplicationContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
//                database.update(MainActivity.TABLE_USER_INFO, cv, "id = ?", new String[]{"1"});
//                database.close();
//            }
//        } catch (NullPointerException e) {
//            // Error handling
//            Toast.makeText(this, getString(R.string.firebase_error), Toast.LENGTH_SHORT).show();
//
//            cv.put("verifyOrder", "0");
//            SQLiteDatabase database = getApplicationContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
//            database.update(MainActivity.TABLE_USER_INFO, cv, "id = ?", new String[]{"1"});
//            database.close();
//        }
//}
    private void updateRecordsUserInfo(String userInfo, String result) {
        SQLiteDatabase database = getApplicationContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        ContentValues cv = new ContentValues();

        cv.put(userInfo, result);

        // обновляем по id
        database.update(MainActivity.TABLE_USER_INFO, cv, "id = ?",
                new String[] { "1" });
        database.close();
    }


// Другой код вашего Fragment или Activity...
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void openGPSSettings() {
        // Проверяем, включен ли GPS
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {
            // Если GPS не включен, открываем окно настроек для GPS
            MaterialAlertDialogBuilder builder =  new MaterialAlertDialogBuilder(FirebaseSignIn.this, R.style.AlertDialogTheme);
            LayoutInflater inflater = FirebaseSignIn.this.getLayoutInflater();

            View view_cost = inflater.inflate(R.layout.message_layout, null);
            builder.setView(view_cost);
            TextView message = view_cost.findViewById(R.id.textMessage);
            message.setText(R.string.gps_info);
            builder.setPositiveButton(R.string.gps_on, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, REQUEST_ENABLE_GPS);
                        }
                    })
                    .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(FirebaseSignIn.this, MainActivity.class);
                            startActivity(intent);
                        }
                    })
                    .show();

        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, OpenStreetMapActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_GPS) {
            // Проверяем, был ли GPS успешно включен
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isGPSEnabled) {
                // Если GPS включен после возвращения из окна настроек, запускаем OpenStreetMapActivity
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(this, OpenStreetMapActivity.class));
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                }
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
        }
    }

    private void checkLocationServiceEnabled(LocationServiceCallback callback) throws MalformedURLException {
        Context context = getApplicationContext(); // Получите контекст вашего приложения

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        // Проверяем, доступны ли данные о местоположении
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationServiceResult(false);
//            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
//            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
            return;
        }

        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                try {
                    callback.onLocationServiceResult(true);
                } catch (MalformedURLException e) {
                    Log.d("TAG", "onCreate:" + new RuntimeException(e));
                }
            } else {
                try {
                    callback.onLocationServiceResult(false);
                } catch (MalformedURLException e) {
                    Log.d("TAG", "onCreate:" + new RuntimeException(e));
                }
            }
        });
    }
    // Интерфейс колбэка
    public interface LocationServiceCallback {
        void onLocationServiceResult(boolean isEnabled) throws MalformedURLException;
    }
    public void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

//    private void addUser() throws MalformedURLException, JSONException, InterruptedException {
//
//        String urlString = "https://m.easy-order-taxi.site/" + api + "/android/addUser/" +  MainActivity.displayName + "/" + MainActivity.userEmail;
//
//        URL url = new URL(urlString);
//        Log.d("TAG", "sendURL: " + urlString);
//
//        AsyncTask.execute(() -> {
//            HttpsURLConnection urlConnection = null;
//            try {
//                urlConnection = (HttpsURLConnection) url.openConnection();
//                urlConnection.setDoInput(true);
//                urlConnection.getResponseCode();
//            } catch (IOException e) {
//                Log.d("TAG", "onCreate:" + new RuntimeException(e));
//            }
//            urlConnection.disconnect();
//        });
//    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Button try_again_button = findViewById(R.id.try_again_button);
        try_again_button.setVisibility(View.VISIBLE);
        try_again_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}