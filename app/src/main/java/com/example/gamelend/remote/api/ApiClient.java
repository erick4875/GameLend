package com.example.gamelend.remote.api;


import android.content.Context;

import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.remote.authenticator.TokenAuthenticator;
import com.example.gamelend.remote.interceptor.AuthInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.converter.gson.GsonConverterFactory;

// Clase para crear el "conector" a tu API
public class ApiClient {
    //Puerto de API antiguo
    //private static final String BASE_URL = "http://10.0.2.2:8080/"; // Localhost

    // direccion API
    public static final String BASE_URL = "http://10.0.2.2:8081/"; // Localhost
    private static Retrofit retrofit = null;
    private static TokenManager tokenManager = null;
    private static AuthInterceptor authInterceptor = null;
    private static TokenAuthenticator tokenAuthenticator = null;

    // metodo para inicializar TokenManager y AuthInterceptor
    private static void initializeAuthComponents(Context context) {
        Context appContext = context.getApplicationContext();
        if (tokenManager == null) {
            tokenManager = new TokenManager(context.getApplicationContext());
        }

        if (authInterceptor == null) {
            authInterceptor = new AuthInterceptor(tokenManager);
        }

        if (tokenAuthenticator == null) {
            tokenAuthenticator = new TokenAuthenticator(tokenManager, appContext);
        }
    }

    // metodo para obtener el conector (Retrofit)
    public static Retrofit getRetrofitInstance(Context context) {
        initializeAuthComponents(context);

        // si aún no lo ha creado
        if (retrofit == null) {
            // revisión de llamadas en logs
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            // crear cliente http personalizado
            OkHttpClient localOkHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(authInterceptor)
                    .authenticator(tokenAuthenticator)
                    .build();

            // crear el conector Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(localOkHttpClient)
                    .build();
        }
        return retrofit;
    }

    // metodo para acceder al tokenmanager
    public static TokenManager getTokenManager(Context context) {
        initializeAuthComponents(context);
        return tokenManager;
    }

}

