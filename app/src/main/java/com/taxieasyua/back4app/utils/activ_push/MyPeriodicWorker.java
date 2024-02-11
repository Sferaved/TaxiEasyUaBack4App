package com.taxieasyua.back4app.utils.activ_push;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.NotificationHelper;
import com.taxieasyua.back4app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MyPeriodicWorker extends Worker {

    private String TAG = "TAG_Per";
    private static final String PREFS_NAME = "UserActivityPrefs";
    private static final String LAST_ACTIVITY_KEY = "lastActivityTimestamp";

    public MyPeriodicWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Выполнить необходимую работу здесь
        // Например, отправить уведомление или выполнить другое задание
        Context context = getApplicationContext();
        boolean isUserActive = checkUserActivity(context);
        Log.d(TAG, "onReceive: isUserActive " + isUserActive);

        if (!isUserActive) {
            updateLastActivityTimestamp(context);
            // Если пользователь не активен, отправьте уведомление
            sendNotification(context);
        }
        return Result.success(); // Возвращаем Result.success(), если работа выполнена успешно
    }

    public static void schedulePeriodicWork() {
        // Создаем периодическую работу с интервалом 24 часа
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                MyPeriodicWorker.class,
                24, // интервал
                TimeUnit.HOURS
        ).build();

        // Запускаем периодическую работу
        WorkManager.getInstance().enqueue(periodicWorkRequest);
    }

    private boolean checkUserActivity(Context context) {
        // Получение состояния приложения (в переднем плане или фоне)
        boolean isAppInForeground = ((MyApplication) context.getApplicationContext()).isAppInForeground();
        Log.d(TAG, "checkUserActivity " + isAppInForeground);

        // Если приложение в переднем плане, считаем его активным
        if (isAppInForeground) {
            return true;
        }

        // Приложение в фоновом режиме, выполняем логику проверки времени активности пользователя
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        long lastActivityTimestamp = prefs.getLong(LAST_ACTIVITY_KEY, 0);
        long currentTime = System.currentTimeMillis();

        Log.d(TAG, "lastActivit: CHECK " + timeFormatter(lastActivityTimestamp));
        Log.d(TAG, "currentTime: CHECK " + timeFormatter(currentTime));
        // Проверка, прошло ли более 25 дней с последней активности
//        return (currentTime - lastActivityTimestamp) <= (60 * 1000);
        return (currentTime - lastActivityTimestamp) < (25 * 24 * 60 * 60 * 1000);
    }
    private String timeFormatter(long timeMsec) {
        Date formattedTime = new Date(timeMsec);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(formattedTime);
    }
    private void sendNotification(Context context) {
        // Ваш текст и заголовок уведомления
        String title = context.getString(R.string.new_message) + " " + context.getString(R.string.app_name);
        String message = context.getString(R.string.new_order_notify);

        // Создайте интент для открытия MainActivity при нажатии на уведомление
        Intent openMainActivityIntent = new Intent(context, MainActivity.class);
        openMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // PendingIntent для открытия MainActivity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openMainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Используйте ваш класс NotificationHelper для отправки уведомления
        NotificationHelper.showNotificationMessageOpen(context, title, message, pendingIntent);

    }
    private void updateLastActivityTimestamp(Context context) {

        // Обновление времени последней активности в SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Log.d(TAG, "updateLastActivityTimestamp: " + timeFormatter(System.currentTimeMillis()));
        editor.putLong(LAST_ACTIVITY_KEY, System.currentTimeMillis());
        editor.apply();
    }
}
