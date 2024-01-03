package com.taxieasyua.back4app.utils.connect;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ConnectionSpeedTester {

    public interface SpeedTestListener {
        void onSpeedTestCompleted(double speed);

        void onSpeedTestFailed(String errorMessage);
    }

    public static void testConnectionSpeed(String absoluteUrl, String apiKey, SpeedTestListener listener) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(absoluteUrl)  // Важно использовать абсолютный URL
                .build();

        ApiServiceConnect apiService = retrofit.create(ApiServiceConnect.class);

        Call<Void> call = apiService.testConnection("adr_street", "Київ, заплавна", apiKey);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    long startTime = System.currentTimeMillis();
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;

                    double speed = calculateSpeed(duration);
                    listener.onSpeedTestCompleted(speed);
                } else {
                    listener.onSpeedTestFailed("Ошибка подключения. Код ответа: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                listener.onSpeedTestFailed("Ошибка подключения: " + t.getMessage());
            }
        });
    }

    private static double calculateSpeed(long duration) {
        return (double) duration / 1024;
    }
}
