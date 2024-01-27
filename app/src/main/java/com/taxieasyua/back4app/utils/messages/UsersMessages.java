package com.taxieasyua.back4app.utils.messages;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayout;
import com.taxieasyua.back4app.NotificationHelper;
import com.taxieasyua.back4app.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersMessages {

    private static final String TAG = "TAG_Message";
    private String email;
    private Context context;

    public UsersMessages(String email, Context context) {
        this.email = email;
        this.context = context;
        Log.d(TAG, "email: " + email);
        // Вызываем метод для выполнения запроса
        getMessages();
    }

    private void getMessages() {
        MessageApiManager messageApiManager = new MessageApiManager();
        Call<List<Message>> call = messageApiManager.getMessages(email);

        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Message> messages = response.body();
                    String textMessage = "";
                    int i = 1;
                    // Обработка списка сообщений...
                    for (Message message : messages) {
                        textMessage += i++ + ". " + message.getTextMessage() +"\n";
                        Log.d(TAG, "Text: " + message.getTextMessage());
                        Log.d(TAG, "------------------------");
                    }
                    Log.d(TAG, "Text: " + textMessage);
                    notifyUser(textMessage);
                } else {
                    // Обработка неудачного ответа...
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Message>> call, @NonNull Throwable t) {
                // Обработка ошибки...
            }
        });
    }
    private void notifyUser (String message) {

        String title = context.getString(R.string.new_message) + " " + context.getString(R.string.app_name) ;

        NotificationHelper.showNotificationMessage(this.context, title, message);

    }
}

