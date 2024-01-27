package com.taxieasyua.back4app.utils.messages;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MessageApiService {

    @GET("showMessage/{email}")
    Call<List<Message>> getMessages(@Path("email") String email);
}

