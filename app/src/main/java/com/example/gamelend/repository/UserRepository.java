package com.example.gamelend.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamelend.dto.LoginRequestDTO;
import com.example.gamelend.dto.TokenResponseDTO;
import com.example.gamelend.dto.UserResponseDTO;
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

    // Metodo para registrar un usuario
    public LiveData<TokenResponseDTO> login(String usuario, String contrasena) {
        MutableLiveData<TokenResponseDTO> liveData = new MutableLiveData<>();

        LoginRequestDTO request = new LoginRequestDTO(usuario, contrasena);

        apiService.login(request).enqueue(new Callback<TokenResponseDTO>() {
            @Override
            public void onResponse(Call<TokenResponseDTO> call, Response<TokenResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                } else {
                    liveData.postValue(null); // Manejar error
                }
            }

            @Override
            public void onFailure(Call<TokenResponseDTO> call, Throwable t) {
                liveData.postValue(null); // Manejar error de conexión
            }
        });

        return liveData;

    }
    // Metodo obternerUsuarios
        public LiveData<List<UserResponseDTO>> obtenerUsuarios(String token) {
            MutableLiveData<List<UserResponseDTO>> usuariosLiveData = new MutableLiveData<>();

            apiService.getAllUsers("Bearer " + token).enqueue(new Callback<List<UserResponseDTO>>() {
                @Override
                public void onResponse(Call<List<UserResponseDTO>> call, Response<List<UserResponseDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        usuariosLiveData.postValue(response.body());
                    } else {
                        usuariosLiveData.postValue(null); // Manejar error
                    }
                }

                @Override
                public void onFailure(Call<List<UserResponseDTO>> call, Throwable t) {
                    usuariosLiveData.postValue(null); // Manejar error de conexión
                }
            });

            return usuariosLiveData;
        }
}