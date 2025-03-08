package com.example.gamelend.network;

import com.example.gamelend.model.dto.LoginRequest;
import com.example.gamelend.model.dto.LoginResponse;
import com.example.gamelend.model.dto.RespuestaGeneral;
import com.example.gamelend.model.dto.UsuarioResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("api/usuarios")
    Call<RespuestaGeneral<List<UsuarioResponseDTO>>> getUsuarios();
}
