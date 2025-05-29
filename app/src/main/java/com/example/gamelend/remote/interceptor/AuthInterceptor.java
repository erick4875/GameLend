package com.example.gamelend.remote.interceptor;

import com.example.gamelend.auth.TokenManager;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

// Interceptor encargado de añadir el token de autenticación a las cabeceras de peticiones HTTP salientes
public class AuthInterceptor implements Interceptor {
    private TokenManager tokenManager; // instancia de Token Manager

    // Constructor: recibe el contexto de la aplicación
    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    // Este es el metodo principal que se ejecuta para cada petición HTTP
    @Override
    public Response intercept(Chain chain) throws IOException {
        // Obtener la petición que se va a enviar
        Request originalRequest = chain.request();

        // no añadimos token si la URL es para login/registro (rutas de autenticacion 'auth')
        if (originalRequest.url().toString().contains("/auth/")) {
            // continua la petición
            return chain.proceed(originalRequest);
        }

        // Obtener el token de acceso desde TokenManager
        String accessToken = tokenManager.getAccessToken();

        // Si tenemos un token, lo añadimos a la cabecera de la petición
        Request newRequest;
        // Solo añadimos la cabecera si el token existe y no está vacío
        if (accessToken != null && !accessToken.isEmpty()) {
            // Creamos nueva petición basada en la original, añadiendo la cabecera "Authorization"
            Request.Builder builder = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + accessToken);
            newRequest = builder.build();
        } else {
            // Si no hay token, procedemos con la petición original sin modificarla.
            // El backend se encargará de denegar el acceso si la ruta es protegida.
            newRequest = originalRequest;
        }

        // Dejamos que la petición continúe
        return chain.proceed(newRequest);
    }
}