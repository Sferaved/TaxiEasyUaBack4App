package com.taxieasyua.back4app.ui.start;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.NotificationHelper;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ServerConnection;
import com.taxieasyua.back4app.cities.Cherkasy.Cherkasy;
import com.taxieasyua.back4app.cities.Dnipro.Dnipro;
import com.taxieasyua.back4app.cities.Kyiv.KyivCity;
import com.taxieasyua.back4app.cities.Odessa.Odessa;
import com.taxieasyua.back4app.cities.Odessa.OdessaTest;
import com.taxieasyua.back4app.cities.Zaporizhzhia.Zaporizhzhia;
import com.taxieasyua.back4app.ui.finish.ApiClient;
import com.taxieasyua.back4app.ui.finish.ApiService;
import com.taxieasyua.back4app.ui.finish.City;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StartActivity extends Activity {
    public static final String DB_NAME = "data_26082023_35";
    public static final String TABLE_USER_INFO = "userInfo";
    public static final String TABLE_SETTINGS_INFO = "settingsInfo";
    public static final String TABLE_ORDERS_INFO = "ordersInfo";
    public static final String TABLE_SERVICE_INFO = "serviceInfo";
    public static final String TABLE_ADD_SERVICE_INFO = "serviceAddInfo";
    public static final String CITY_INFO = "cityInfo";
    public static final String TABLE_POSITION_INFO = "myPosition";

    public static SQLiteDatabase database;
    public static Cursor cursorDb;
    static FloatingActionButton fab, btn_again;

    Intent intent;
    public static String userEmail, displayName;

    public static final String  apiTest = "apiTest";
    public static final String  apiKyiv = "apiPas2";
    public static final String  apiDnipro = "apiPas2_Dnipro";
    public static final String  apiOdessa = "apiPas2_Odessa";
    public static final String  apiZaporizhzhia = "apiPas2_Zaporizhzhia";
    public static final String  apiCherkasy = "apiPas2_Cherkasy";
    private static String  api = "apiPas2";

    public static long addCost, cost;
    public static boolean verifyPhone;
    Button try_again_button;
    private BroadcastReceiver connectivityReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);



    }
    private void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    // Создаем метод для установки повторяющегося будильника
    private void setRepeatingAlarm() {
        // Получаем системный сервис AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Создаем намерение для запуска StartActivity
        Intent intent = new Intent(this, StartActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Устанавливаем повторяющийся будильник с интервалом 60 секунд
        long intervalMillis = 60 * 1000; // 60 секунд
        long triggerTimeMillis = System.currentTimeMillis() + intervalMillis;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTimeMillis, intervalMillis, pendingIntent);

        // Проверяем наличие интернет-соединения
        connectivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                if (hasConnectionAlarm()) {
                    // Если есть подключение к интернету, отменяем повторяющийся будильник
                    alarmManager.cancel(pendingIntent);
                    try_again_button.setVisibility(View.INVISIBLE);
                    startActivity(new Intent(StartActivity.this, StartActivity.class));
                    if (connectivityReceiver != null) {
                        unregisterReceiver(connectivityReceiver);
                    }
                }
            }
        };

        // Регистрируем BroadcastReceiver для изменений состояния сети
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, intentFilter);

    }
    private boolean hasConnectionAlarm() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }
    @SuppressLint("SuspiciousIndentation")
    @Override
    protected void onResume() {
        super.onResume();

        try {
            initDB();
        } catch (MalformedURLException | JSONException | InterruptedException ignored) {

        }

        List<String> stringList = logCursor(StartActivity.CITY_INFO);
        Log.d("TAG", "onCreate: " + stringList );
        if(stringList.size()!=0) {
            switch (stringList.get(1)) {
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
                case "OdessaTest":

                    api = StartActivity.apiTest;

                    break;
                default:

                    api = StartActivity.apiKyiv;

                    break;
            }
        }
        try_again_button = findViewById(R.id.try_again_button);
        try_again_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, StartActivity.class));
            }
        });
        Log.d("TAG", "onCreate: " + api);
        if(hasConnection() && hasServer()) {

            try {
                startIp();
                blackList();
                version();
            } catch (MalformedURLException ignored) {
            }
            isConnectedToGoogle(new ConnectionCallback() {
                @Override
                public void onConnectionResult(long responseTime) {
                    if (responseTime < 0 || responseTime >= 2000) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(StartActivity.this, R.string.slow_internet, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(StartActivity.this, StopActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                }
            });
        }
        else  {
            Toast.makeText(this, R.string.verify_internet, Toast.LENGTH_SHORT).show();
            try_again_button.setVisibility(View.VISIBLE);
            setRepeatingAlarm();
        }

        fab = findViewById(R.id.fab);
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


       btn_again.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               intent = new Intent(StartActivity.this, StartActivity.class);
               startActivity(intent);
           }
       });

    }

    public CompletableFuture<Boolean> checkConnectionAsync() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        ServerConnection.checkConnection("https://m.easy-order-taxi.site/", new ServerConnection.ConnectionCallback() {
            @Override
            public void onConnectionResult(boolean isConnected) {
                future.complete(isConnected);
            }
        });

        return future;
    }

    private boolean hasServer() {
        CompletableFuture<Boolean> connectionFuture = checkConnectionAsync();
        boolean isConnected = false;
        try {
            isConnected = connectionFuture.get();
        } catch (Exception ignored) {

        }
        return  isConnected;
    };

    public boolean hasConnection() {
         ConnectivityManager cm = (ConnectivityManager) StartActivity.this.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        }
        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {

            return true;
        }

        return false;
    }
    public interface ConnectionCallback {
        void onConnectionResult(long responseTime);
    }

    public void isConnectedToGoogle(ConnectionCallback callback) {


        final long[] responseTime = {0}; // Объявляем как final массив

        AsyncTask.execute(() -> {
            try {
                String googleEndpoint = "https://www.google.com/";
                long startTime = System.currentTimeMillis();

                URL url = new URL(googleEndpoint);
                HttpsURLConnection connection = null;
                connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(2000); // Установите тайм-аут подключения в миллисекундах
                connection.connect();

                long endTime = System.currentTimeMillis();
                responseTime[0] = endTime - startTime; // Используем индекс 0 для записи времени

                connection.disconnect();

                // Вызываем метод обратного вызова
                callback.onConnectionResult(responseTime[0]);
            } catch (IOException e) {
                // В случае ошибки вызываем метод обратного вызова с временем -1
                callback.onConnectionResult(-1);
            }
        });
    }


    public void startIp() throws MalformedURLException {

        String urlString = "https://m.easy-order-taxi.site/" +  api + "/android/startIP";
        Log.d("TAG", "startIp: " + urlString);
        URL url = new URL(urlString);

        AsyncTask.execute(() -> {
            HttpsURLConnection urlConnection = null;
            try {
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.getResponseCode();
            } catch (IOException e) {

            }
            urlConnection.disconnect();
        });

    }

    private void initDB() throws MalformedURLException, JSONException, InterruptedException {
//        this.deleteDatabase(DB_NAME);
        database = this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        Log.d("TAG", "initDB: " + database);

        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USER_INFO + "(id integer primary key autoincrement," +
                " verifyOrder text," +
                " phone_number text," +
                " email text," +
                " username text);");

        cursorDb = database.query(TABLE_USER_INFO, null, null, null, null, null, null);
        if (cursorDb.getCount() == 0) {
            insertUserInfo();
        }


        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SETTINGS_INFO + "(id integer primary key autoincrement," +
                " type_auto text," +
                " tarif text);");

        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_POSITION_INFO + "(id integer primary key autoincrement," +
                " startLat double," +
                " startLan double," +
                " position text);");
        cursorDb = database.query(TABLE_POSITION_INFO, null, null, null, null, null, null);
        if (cursorDb.getCount() == 0) {
            insertMyPosition();
        }

        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS_INFO + "(id integer primary key autoincrement," +
                " from_street text," +
                " from_number text," +
                " from_lat text," +
                " from_lng text," +
                " to_street text," +
                " to_number text," +
                " to_lat text," +
                " to_lng text);");
//        Log.d("TAG", "initDB TABLE_ORDERS_INFO:" + logCursor(TABLE_ORDERS_INFO));
        cursorDb = database.query(TABLE_SETTINGS_INFO, null, null, null, null, null, null);
        if (cursorDb.getCount() == 0) {
            List<String> settings = new ArrayList<>();
            settings.add("usually");
            settings.add(" ");
            insertFirstSettings(settings);
            if (cursorDb != null && !cursorDb.isClosed())
                cursorDb.close();
        }
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SERVICE_INFO + "(id integer primary key autoincrement," +
                " BAGGAGE text," +
                " ANIMAL text," +
                " CONDIT text," +
                " MEET text," +
                " COURIER text," +
                " CHECK_OUT text," +
                " BABY_SEAT text," +
                " DRIVER text," +
                " NO_SMOKE text," +
                " ENGLISH text," +
                " CABLE text," +
                " FUEL text," +
                " WIRES text," +
                " SMOKE text);");
        cursorDb = database.query(TABLE_SERVICE_INFO, null, null, null, null, null, null);
        if (cursorDb.getCount() == 0) {
            insertServices();
        }

        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ADD_SERVICE_INFO + "(id integer primary key autoincrement," +
                " time text," +
                " comment text," +
                " date text);");
        cursorDb = database.query(TABLE_ADD_SERVICE_INFO, null, null, null, null, null, null);
        if (cursorDb.getCount() == 0) {
            insertAddServices();
        } else {
            resetRecordsAddServices();
        }


        database.execSQL("CREATE TABLE IF NOT EXISTS " + CITY_INFO + "(id integer primary key autoincrement," +
                " city text);");
        getLocalIpAddress();

        Cursor cursor = StartActivity.database.query(StartActivity.TABLE_USER_INFO, null, null, null, null, null, null);
        verifyPhone = cursor.getCount() == 1;
    }

    private void insertFirstSettings(List<String> settings) {
        String sql = "INSERT INTO " + TABLE_SETTINGS_INFO + " VALUES(?,?,?);";
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();
        try {
            statement.clearBindings();
            statement.bindString(2, settings.get(0));
            statement.bindString(3, settings.get(1));

            statement.execute();
            database.setTransactionSuccessful();

        } finally {
            database.endTransaction();
        }
    }
    private void insertServices() {
        String sql = "INSERT INTO " + TABLE_SERVICE_INFO + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();
        try {
            statement.clearBindings();
            statement.bindString(2, "0");
            statement.bindString(3, "0");
            statement.bindString(4, "0");
            statement.bindString(5, "0");
            statement.bindString(6, "0");
            statement.bindString(7, "0");
            statement.bindString(8, "0");
            statement.bindString(9, "0");
            statement.bindString(10,"0");
            statement.bindString(11,"0");
            statement.bindString(12,"0");
            statement.bindString(13,"0");
            statement.bindString(14,"0");
            statement.bindString(15,"0");

            statement.execute();
            database.setTransactionSuccessful();

        } finally {
            database.endTransaction();
        }
    }
    private void insertAddServices() {
        String sql = "INSERT INTO " + TABLE_ADD_SERVICE_INFO + " VALUES(?,?,?,?);";
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();
        try {
            statement.clearBindings();
            statement.bindString(2, "no_time");
            statement.bindString(3, "no_comment");
            statement.bindString(4, "no_date");

            statement.execute();
            database.setTransactionSuccessful();

        } finally {
            database.endTransaction();
        }
    }
    private void insertUserInfo() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
        }



        String sql = "INSERT INTO " + TABLE_USER_INFO + " VALUES(?,?,?,?,?);";
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();
        try {
            statement.clearBindings();
            statement.bindString(2, "0");
            statement.bindString(3, "+380");
            statement.bindString(4, "email");
            statement.bindString(5, "username");

            statement.execute();
            database.setTransactionSuccessful();

        } finally {
            database.endTransaction();
        }
    }
    public static void resetRecordsAddServices() {
        ContentValues cv = new ContentValues();

        cv.put("time", "no_time");
        cv.put("comment", "no_comment");
        cv.put("date", "no_date");

        // обновляем по id
        database.update(TABLE_ADD_SERVICE_INFO, cv, "id = ?",
                new String[] { "1" });
    }

    public static void insertRecordsUser(String phoneNumber) {
        String sql = "INSERT INTO " + TABLE_USER_INFO + " VALUES(?,?,?);";
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();
        try {
            statement.clearBindings();
            statement.bindString(3, phoneNumber);

            statement.execute();
            database.setTransactionSuccessful();

        } finally {
            database.endTransaction();
        }
        fab.setVisibility(View.VISIBLE);
    }

    public static void updateRecordsUser(String result) {
        ContentValues cv = new ContentValues();

        cv.put("phone_number", result);

        // обновляем по id
        database.update(TABLE_USER_INFO, cv, "id = ?",
                new String[] { "1" });
    }
    public static void updateRecordsUserInfo(String userInfo, String result) {
        ContentValues cv = new ContentValues();

        cv.put(userInfo, result);

        // обновляем по id
        database.update(TABLE_USER_INFO, cv, "id = ?",
                new String[] { "1" });
    }

    @SuppressLint("Range")
    public static List<String> logCursor(String table) {
        List<String> list = new ArrayList<>();
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

    // This function is called when user accept or decline the permission.
// Request Code is used to check which permission called this function.
// This request code is provided when user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        if (connectivityReceiver != null) {
            unregisterReceiver(connectivityReceiver);
        }
    }

    private void getLocalIpAddress() {
        ApiService apiService = ApiClient.getApiService();

        Call<City> call = apiService.cityOrder();

        call.enqueue(new Callback<City>() {
            @Override
            public void onResponse(Call<City> call, Response<City> response) {
                if (response.isSuccessful()) {
                    City status = response.body();
                    if (status != null) {
                        String result = status.getResponse();
                        String message = getString(R.string.your_city);
                        SQLiteDatabase database = openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);
                        ContentValues cv = new ContentValues();
                        cv.put("tarif", " ");
                        database.update(StartActivity.TABLE_SETTINGS_INFO, cv, "id = ?",
                                new String[] { "1" });
                        database.close();
                        insertCity(result);
                        Log.d("TAG", "onResponse: " + result);
                        switch (result){
                            case "Dnipropetrovsk Oblast":
                                message += getString(R.string.Dnipro_city);
                                api = StartActivity.apiDnipro;
                                break;
                            case "Odessa":
                                message += getString(R.string.Odessa);
                                api = StartActivity.apiOdessa;
                                break;
                            case "Zaporizhzhia":
                                message += getString(R.string.Zaporizhzhia);
                                api = StartActivity.apiZaporizhzhia;
                                break;
                            case "Cherkasy Oblast":
                                message += getString(R.string.Cherkasy);
                                api = StartActivity.apiCherkasy;
                                break;
                            default:
                                message += getString(R.string.Kyiv_city);
                                api = StartActivity.apiKyiv;
                                break;
                        }
                        Log.d("TAG", "onResponse: StartActivity.TABLE_SETTINGS_INFO " + logCursor(StartActivity.TABLE_SETTINGS_INFO));
                        Toast.makeText(StartActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(StartActivity.this, R.string.verify_internet, Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(Call<City> call, Throwable t) {
                // Обработка ошибок сети или других ошибок
                String errorMessage = t.getMessage();
                t.printStackTrace();
                Log.d("TAG", "onFailure: " + errorMessage);

            }
        });
    }
    private void insertCity(String city) {
        String sql = "INSERT INTO " + CITY_INFO + " VALUES(?,?);";
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();
        try {
            statement.clearBindings();
            statement.bindString(2, city);

            statement.execute();
            database.setTransactionSuccessful();

        } finally {
            database.endTransaction();
        }
    }
    private void insertMyPosition() {
        String sql = "INSERT INTO " + StartActivity.TABLE_POSITION_INFO + " VALUES(?,?,?,?);";

        SQLiteDatabase database = openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);

        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();
        try {
            statement.clearBindings();
            statement.bindDouble(2, 0);
            statement.bindDouble(3, 0);
            statement.bindString(4, " ");

            statement.execute();
            database.setTransactionSuccessful();

        } finally {
            database.endTransaction();
        }
    }
    private void blackList() throws MalformedURLException {

        String userEmail = logCursor(TABLE_USER_INFO).get(3);
        if(userEmail.equals("email")) {
            Log.d("TAG", "blackList:userEmail " + userEmail);
//            startActivity(new Intent(StartActivity.this, GoogleSignInActivity.class));
            startActivity(new Intent(StartActivity.this, FirebaseSignIn.class));

        } else {

            String url = "https://m.easy-order-taxi.site/" + api + "/android/verifyBlackListUser/" +  userEmail;

            Map<String, String> sendUrlMap = CostJSONParser.sendURL(url);

            String message = sendUrlMap.get("message");

            ContentValues cv = new ContentValues();
            assert message != null;
            if (message.equals("Не черном списке")) {
                cv.put("verifyOrder", "1");
                database.update(TABLE_USER_INFO, cv, "id = ?", new String[]{"1"});

                    startActivity(new Intent(this, MainActivity.class));
            }
            if (message.equals("В черном списке")) {
//                Toast.makeText(this, getString(R.string.firebase_error), Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StartActivity.this, getString(R.string.firebase_error), Toast.LENGTH_SHORT).show();
                        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                        try_again_button.setVisibility(View.VISIBLE);
                        cv.put("verifyOrder", "0");
                        database.update(StartActivity.TABLE_USER_INFO, cv, "id = ?", new String[]{"1"});
                    }
                });


            }
        }


    }

    private void version() throws MalformedURLException {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            checkPermission(Manifest.permission.POST_NOTIFICATIONS, PackageManager.PERMISSION_GRANTED);
            return;
        }

        String url = "https://m.easy-order-taxi.site/" + api + "/android/" +"versionAPI";


        Log.d("TAG", "onClick urlCost: " + url);
        Map sendUrlMapCost = null;
        try {
            sendUrlMapCost = ResultSONParser.sendURL(url);
        } catch (MalformedURLException | InterruptedException | JSONException e) {
            Log.d("TAG", "onCreate:" + new RuntimeException(e));
        }

        String message = (String) sendUrlMapCost.get("message");
        if(!message.equals(getString(R.string.version_code))) {
            NotificationHelper notificationHelper = new NotificationHelper();

            String title = getString(R.string.new_version);
            String messageNotif = getString(R.string.news_of_version);
            String urlStr = "https://play.google.com/store/apps/details?id= com.taxieasyua.back4app";

            notificationHelper.showNotification(this, title, messageNotif, urlStr);
        }



    }
}
