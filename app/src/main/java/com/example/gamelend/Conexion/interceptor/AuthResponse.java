package com.example.gamelend.Conexion.interceptor;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
