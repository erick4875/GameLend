package com.example.gamelend.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;

public class TokenManager {

    private static final String PREFS_NAME = "GameLendPrefs";
    private static final String KEY_ACCESS_TOKEN = "jwt_access_token";
    private static final String KEY_REFRESH_TOKEN = "jwt_refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PUBLIC_NAME = "user_public_name";
    private static final String KEY_USER_ROLES = "user_roles";
    private static final String KEY_USER_EMAIL = "user_email"; // <-- NUEVA CLAVE PARA EMAIL

    private SharedPreferences prefs;
    private Gson gson;

    public TokenManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveAccessToken(String token) {
        if (token != null) {
            prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
            Log.d("TokenManager", "Access Token guardado.");
        }
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public void saveRefreshToken(String token) {
        if (token != null) {
            prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply();
            Log.d("TokenManager", "Refresh Token guardado.");
        }
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void saveUserId(Long userId) {
        if (userId != null) {
            prefs.edit().putLong(KEY_USER_ID, userId).apply();
            Log.d("TokenManager", "UserID guardado: " + userId);
        } else {
            prefs.edit().remove(KEY_USER_ID).apply();
        }
    }

    public Long getUserId() {
        if (prefs.contains(KEY_USER_ID)) {
            return prefs.getLong(KEY_USER_ID, -1L);
        }
        return null;
    }

    public void savePublicName(String publicName) {
        if (publicName != null) {
            prefs.edit().putString(KEY_PUBLIC_NAME, publicName).apply();
            Log.d("TokenManager", "PublicName guardado: " + publicName);
        } else {
            prefs.edit().remove(KEY_PUBLIC_NAME).apply();
        }
    }

    public String getPublicName() {
        return prefs.getString(KEY_PUBLIC_NAME, null);
    }

    public void saveRoles(List<String> roles) {
        if (roles != null) {
            String rolesJson = gson.toJson(roles);
            prefs.edit().putString(KEY_USER_ROLES, rolesJson).apply();
            Log.d("TokenManager", "Roles guardados: " + rolesJson);
        } else {
            prefs.edit().remove(KEY_USER_ROLES).apply();
        }
    }

    public List<String> getRoles() {
        String rolesJson = prefs.getString(KEY_USER_ROLES, null);
        if (rolesJson != null) {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            return gson.fromJson(rolesJson, type);
        }
        return new ArrayList<>();
    }

    // === NUEVOS MÉTODOS PARA EMAIL ===
    public void saveEmail(String email) {
        if (email != null && !email.isEmpty()) {
            prefs.edit().putString(KEY_USER_EMAIL, email).apply();
            Log.d("TokenManager", "Email guardado: " + email);
        } else {
            prefs.edit().remove(KEY_USER_EMAIL).apply();
        }
    }

    public String getEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }
    // =================================

    public void clearTokens() { // Ahora también limpia el email
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_PUBLIC_NAME);
        editor.remove(KEY_USER_ROLES);
        editor.remove(KEY_USER_EMAIL); // <-- LIMPIAR EMAIL
        editor.apply();
        Log.d("TokenManager", "Tokens y datos de usuario eliminados.");
    }

    public boolean hasAccessToken() {
        String token = getAccessToken();
        return token != null && !token.isEmpty();
    }
}
