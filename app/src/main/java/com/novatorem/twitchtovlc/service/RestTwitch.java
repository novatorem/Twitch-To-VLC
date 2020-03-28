package com.novatorem.twitchtovlc.service;

import com.squareup.moshi.Json;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class RestTwitch {
    private String BASE_URL = "https://eventnotifierjson2.herokuapp.com/";
    public static void main(String[] args) throws IOException {
        Procedure data = getProcedure();

        Moshi moshi = new Moshi.Builder().build();

        // if you want to see the json that will be generated ...
        //String json = moshi.adapter(Procedure.class).indent("  ").toJson(data);
        //System.out.println(json);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:8675/")
                .addConverterFactory(MoshiConverterFactory.create(moshi)) // com.squareup.retrofit2:converter-moshi:latest.version
                .build();

        MyApi api = retrofit.create(MyApi.class);

        Response<Void> resp = api.postProcedure(data).execute();
        if (resp.isSuccessful())
            System.out.println("Success!");
    }

    /**
     * helper method to create a 'Procedure' object
     */
    private static Procedure getProcedure() {
        Procedure proc = new Procedure();
        proc.name = "BR_SP_BRN_GET_STORE_ACTIVE_SHIPMENTS";

        proc.parameters = new ArrayList<>();
        proc.parameters.add(new Parameter("StoreCode", "B201"));
        return proc;
    }

    // ------------------------------------------------------------------------------------------
    // DTO classes for json serialization.  Do better validation / immutability when for realsies
    // ------------------------------------------------------------------------------------------
    static class Procedure {
        @Json(name = "ProcName") String name;
        @Json(name = "Parameters") List<Parameter> parameters;
    }

    static class Parameter {
        @Json(name = "Name") String name;
        @Json(name = "Value") String value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    // ------------------------------------------------------------------------------------------
    // Retrofit Api
    // ------------------------------------------------------------------------------------------
    interface MyApi {
        @POST("my-endpoint")
        Call<Void> postProcedure(@Body Procedure procedure);
    }
}