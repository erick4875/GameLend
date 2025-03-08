package com.example.gamelend.Conexion;


import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getConexion(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)  // Ajusta los tiempos de espera si es necesario
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:8080/")  // URL base del servidor Spring Boot
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)  // Usa el cliente personalizado
                    .build();
        }
        return retrofit;
    }
}

