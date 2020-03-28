package com.novatorem.twitchtovlc.service;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import com.squareup.moshi.Moshi;
import retrofit2.converter.moshi.MoshiConverterFactory;

class APIClient {

    private static Retrofit retrofit = null;

    static Retrofit getClient() {

        OkHttpClient client = new OkHttpClient.Builder().build();
        Moshi moshi = new Moshi.Builder().build();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://reqres.in")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(client)
                .build();

        return retrofit;
    }

}