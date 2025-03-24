package com.example.gamelend.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamelend.dto.LoginRequest;
import com.example.gamelend.dto.LoginResponse;
import com.example.gamelend.dto.RespuestaGeneral;
import com.example.gamelend.dto.UsuarioResponseDTO;
import com.example.gamelend.remote.api.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private ApiService apiService;

    // Constructor
    public UserRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    // Método LOGIN
    public LiveData<LoginResponse> login(String usuario, String contrasena) {
        MutableLiveData<LoginResponse> liveData = new MutableLiveData<>();

        LoginRequest request = new LoginRequest(usuario, contrasena);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                } else {
                    liveData.postValue(null); // Manejar error
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                liveData.postValue(null); // Manejar error de conexión
            }
        });

        return liveData;
    }

    // Método OBTENER USUARIOS
    public LiveData<List<UsuarioResponseDTO>> obtenerUsuarios() {
        MutableLiveData<List<UsuarioResponseDTO>> usuariosLiveData = new MutableLiveData<>();

        apiService.getUsuarios().enqueue(new Callback<RespuestaGeneral<List<UsuarioResponseDTO>>>() {
            @Override
            public void onResponse(Call<RespuestaGeneral<List<UsuarioResponseDTO>>> call, Response<RespuestaGeneral<List<UsuarioResponseDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isExito()) {
                    usuariosLiveData.postValue(response.body().getCuerpo());
                } else {
                    usuariosLiveData.postValue(null); // Manejar error
                }
            }

            @Override
            public void onFailure(Call<RespuestaGeneral<List<UsuarioResponseDTO>>> call, Throwable t) {
                usuariosLiveData.postValue(null); // Manejar error de conexión
            }
        });

        return usuariosLiveData;
    }
}