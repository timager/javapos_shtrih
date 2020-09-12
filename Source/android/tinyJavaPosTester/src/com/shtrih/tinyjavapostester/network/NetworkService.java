package com.shtrih.tinyjavapostester.network;

import android.content.Context;

import com.chuckerteam.chucker.api.ChuckerInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkService {
    private static NetworkService instance;
    private static final String BASE_URL = "https://office.cmd-online.ru";
    private Retrofit retrofit;

    private NetworkService(Context context) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new ChuckerInterceptor(context))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static NetworkService getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkService(context);
        }
        return instance;
    }

    public Api getApi() {
        return retrofit.create(Api.class);
    }
}
