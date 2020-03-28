package com.novatorem.twitchtovlc.service;

import com.novatorem.twitchtovlc.calls.APICalls;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

interface APIInterface {
    @POST("/api/users")
    Call<APICalls> createUser(@Body APICalls user);

    @GET("/api/users?")
    Call<APICalls> doGetUserList(@Query("page") String page);

    @FormUrlEncoded
    @POST("/api/users?")
    Call<APICalls> doCreateUserWithField(@Field("name") String name, @Field("job") String job);
}