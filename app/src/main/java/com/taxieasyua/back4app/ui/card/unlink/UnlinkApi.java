package com.taxieasyua.back4app.ui.card.unlink;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UnlinkApi {
    @GET("/delete-card-token/{email}")
    Call<Void> deleteCardToken(
            @Path("email") String email
    );

}

