package com.example.connecto;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface Api {

    String BASE_URL = "https://randomuser.me";
    @Headers("Content-Type: text/html")
    @GET("/api")
    Call<ResponseBody> getsuperHeroes();
}
