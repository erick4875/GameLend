package com.example.gamelend.remote.authenticator;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.dto.TokenResponseDTO;
import com.example.gamelend.remote.api.ApiClient; // Para obtener ApiClient y su configuración base
import com.example.gamelend.remote.api.ApiService;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient; // Para una instancia separada si es necesario
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor; // Para el cliente de refresh
import retrofit2.Call; // Para la llamada síncrona
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory; // Para el cliente de refresh

public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";
    private final TokenManager tokenManager;
    private final Context applicationContext; // Usar directamente el applicationContext

    // Objeto de bloqueo para evitar múltiples llamadas de refresh concurrentes
    private static final Object lock = new Object();

    public TokenAuthenticator(TokenManager tokenManager, Context context) {
        this.tokenManager = tokenManager;
        this.applicationContext = context.getApplicationContext(); // Asegurar applicationContext
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        // Este metodo se llama si el servidor devuelve un 401 Unauthorized.

        final String oldAccessToken = tokenManager.getAccessToken();
        Request originalRequest = response.request();
        String requestAccessTokenHeader = originalRequest.header("Authorization");

        // Si la petición original no tenía token o el token que falló ya no es el que tenemos,
        // podría ser que ya se refrescó o que la petición no debía llevar token.
        // Si tenemos un nuevo token, lo intentamos.
        if (requestAccessTokenHeader == null || !requestAccessTokenHeader.startsWith("Bearer ") || oldAccessToken == null) {
            // Si hay un oldAccessToken significa que estábamos autenticados y falló. Si es diferente,
            // podría haber sido refrescado por otro hilo.
            if (oldAccessToken != null && (requestAccessTokenHeader == null || !requestAccessTokenHeader.equals("Bearer " + oldAccessToken))) {
                Log.d(TAG, "El token de la petición original no coincide con el guardado o el guardado ya cambió, reintentando con el token actual si existe.");
                return originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + oldAccessToken)
                        .build();
            }
            Log.d(TAG, "Petición original sin token de acceso válido o no hay token actual para reintentar.");
            return null; // No se puede autenticar
        }


        // Sincronizar para evitar que múltiples hilos intenten refrescar el token al mismo tiempo
        synchronized (lock) {
            // Comprobar de nuevo si el token ha sido refrescado por otro hilo mientras esperábamos
            final String currentAccessTokenAfterLock = tokenManager.getAccessToken();
            if (!oldAccessToken.equals(currentAccessTokenAfterLock)) {
                Log.d(TAG, "Token refrescado por otro hilo mientras se esperaba el bloqueo. Reintentando con el nuevo token.");
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + currentAccessTokenAfterLock)
                        .build();
            }

            // Proceder a refrescar el token
            Log.d(TAG, "Access token (" + oldAccessToken.substring(0, Math.min(oldAccessToken.length(),10)) + "...) parece haber expirado. Intentando refrescar...");
            String refreshTokenString = tokenManager.getRefreshToken();

            if (refreshTokenString == null || refreshTokenString.isEmpty()) {
                Log.e(TAG, "No hay refresh token disponible. Deslogueando.");
                tokenManager.clearTokens();
                return null; // No se puede autenticar
            }

            // Crear una nueva instancia de ApiService para la llamada de refresh.
            // Esta instancia NO debe usar este mismo Authenticator para evitar bucles.
            // Podría usar un OkHttpClient más simple.
            ApiService refreshService = createRefreshService();
            String authHeaderForRefresh = "Bearer " + refreshTokenString;

            try {
                // Realizar la llamada de refresh de forma SÍNCRONA
                retrofit2.Response<TokenResponseDTO> refreshResponse = refreshService.refreshToken(authHeaderForRefresh).execute();

                if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                    TokenResponseDTO newTokens = refreshResponse.body();
                    Log.d(TAG, "Tokens refrescados exitosamente. Nuevo Access Token: " + newTokens.getAccessToken().substring(0, Math.min(newTokens.getAccessToken().length(),10)) + "...");

                    // Guardar los nuevos tokens
                    tokenManager.saveAccessToken(newTokens.getAccessToken());

                    // Reintentar la petición original con el nuevo access token
                    return response.request().newBuilder()
                            .header("Authorization", "Bearer " + newTokens.getAccessToken())
                            .build();
                } else {
                    Log.e(TAG, "La llamada de refresh falló con código: " + refreshResponse.code() + " - " + refreshResponse.message());
                    // Si el refresh token es rechazado (ej. expirado, revocado), desloguear.
                    tokenManager.clearTokens();
                    // Notificar para navegar a Login);
                    return null;
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException durante la llamada de refresh: " + e.getMessage(), e);
                return null;
            }
        } // fin del bloque synchronized
    }

    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    // metodo para crear una instancia de ApiService específica para la llamada de refresh
    private ApiService createRefreshService() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // O Level.NONE si no quieres loguear la llamada de refresh

        OkHttpClient refreshOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL) // ip de apiclient
                .client(refreshOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ApiService.class);
    }
}
