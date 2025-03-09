package com.example.gamelend.Conexion.interceptor;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;

    public AuthInterceptor(Context context) {

        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // No agregar token a peticiones de login/registro
        if (original.url().toString().contains("/auth/")) {
            return chain.proceed(original);
        }

        // Obtener token almacenado
        SharedPreferences prefs = context.getSharedPreferences("GameLend", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");

        // Agregar token a la petición
        Request.Builder builder = original.newBuilder()
                .header("Authorization", "Bearer " + token);

        return chain.proceed(builder.build());
    }
}