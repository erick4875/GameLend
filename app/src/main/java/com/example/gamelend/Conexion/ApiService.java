package com.example.gamelend.Conexion;

import com.example.gamelend.Clases.Usuario;
import com.example.gamelend.dto.LoginRequest;
import com.example.gamelend.dto.LoginResponse;
import com.example.gamelend.dto.RespuestaGeneral;
import com.example.gamelend.dto.UsuarioResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("usuarios")  // Endpoint de la API
    Call<List<Usuario>> obtenerUsuarios();
}