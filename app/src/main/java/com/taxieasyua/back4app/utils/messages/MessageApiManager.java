package com.taxieasyua.back4app.utils.messages;

import java.util.List;
import retrofit2.Call;

public class MessageApiManager {

    private MessageApiService messageApiService;

    public MessageApiManager() {
        messageApiService = ApiClientMessage.getClient().create(MessageApiService.class);
    }

    public Call<List<Message>> getMessages(String email) {
        return messageApiService.getMessages(email);
    }
}
