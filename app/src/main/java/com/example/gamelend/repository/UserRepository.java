package com.example.gamelend.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamelend.dto.ErrorResponseDTO;
import com.example.gamelend.dto.LoginRequestDTO;
import com.example.gamelend.dto.RegisterRequestDTO;
import com.example.gamelend.dto.TokenResponseDTO;
import com.example.gamelend.dto.UserDTO;
import com.example.gamelend.dto.UserResponseDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.remote.api.ApiService;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private ApiService apiService;
    private MutableLiveData<String> registrationErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> userProfileErrorLiveData = new MutableLiveData<>(); // Para errores de perfil
    private MutableLiveData<String> updateUserErrorLiveData = new MutableLiveData<>();

    public UserRepository(Context context) {
        this.apiService = ApiClient.getRetrofitInstance(context.getApplicationContext()).create(ApiService.class);
    }

    public LiveData<String> getRegistrationErrorLiveData() {
        return registrationErrorLiveData;
    }

    public LiveData<String> getUserProfileErrorLiveData() { // Getter para el error de perfil
        return userProfileErrorLiveData;
    }

    public LiveData<UserResponseDTO> updateUser(Long userId, UserDTO userUpdateRequest) {
        MutableLiveData<UserResponseDTO> updateResultLiveData = new MutableLiveData<>();
        updateUserErrorLiveData.postValue(null); // Limpiar error anterior

        apiService.updateUser(userId, userUpdateRequest).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateResultLiveData.postValue(response.body());
                } else {
                    String errorMessage = "Error al actualizar perfil (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            Log.e("UserRepository", "Cuerpo del error de actualización: " + errorBodyString);
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyString, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getDetails() != null && !errorResponse.getDetails().isEmpty()) {
                                errorMessage = String.join("\n", errorResponse.getDetails());
                            } else if (errorResponse != null && errorResponse.getMessage() != null) {
                                errorMessage = errorResponse.getMessage();
                            }
                        } catch (Exception e) {
                            Log.e("UserRepository", "Error al parsear errorBody de actualización", e);
                        }
                    }
                    updateUserErrorLiveData.postValue(errorMessage);
                    updateResultLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                Log.e("UserRepository", "Fallo en conexión al actualizar perfil: " + t.getMessage(), t);
                updateUserErrorLiveData.postValue("Error de conexión al actualizar perfil: " + t.getMessage());
                updateResultLiveData.postValue(null);
            }
        });
        return updateResultLiveData;
    }


    public LiveData<TokenResponseDTO> login(String email, String password) {
        MutableLiveData<TokenResponseDTO> loginResultLiveData = new MutableLiveData<>();
        LoginRequestDTO request = new LoginRequestDTO(email, password);

        apiService.login(request).enqueue(new Callback<TokenResponseDTO>() {
            @Override
            public void onResponse(Call<TokenResponseDTO> call, Response<TokenResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loginResultLiveData.postValue(response.body());
                } else {
                    loginResultLiveData.postValue(null);
                    Log.e("UserRepository", "Error en login: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TokenResponseDTO> call, Throwable t) {
                loginResultLiveData.postValue(null);
                Log.e("UserRepository", "Fallo en login: " + t.getMessage(), t);
            }
        });
        return loginResultLiveData;
    }

    public LiveData<List<UserResponseDTO>> getAllUsers() {
        MutableLiveData<List<UserResponseDTO>> usersLiveData = new MutableLiveData<>();
        apiService.getAllUsers().enqueue(new Callback<List<UserResponseDTO>>() {
            @Override
            public void onResponse(Call<List<UserResponseDTO>> call, Response<List<UserResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    usersLiveData.postValue(response.body());
                } else {
                    usersLiveData.postValue(null);
                    Log.e("UserRepository", "Error al obtener usuarios: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<UserResponseDTO>> call, Throwable t) {
                usersLiveData.postValue(null);
                Log.e("UserRepository", "Fallo al obtener usuarios: " + t.getMessage(), t);
            }
        });
        return usersLiveData;
    }

    public LiveData<TokenResponseDTO> registerUser(RegisterRequestDTO registerRequest) {
        MutableLiveData<TokenResponseDTO> registrationResultLiveData = new MutableLiveData<>();
        registrationErrorLiveData.postValue(null); // Limpiar error anterior

        apiService.register(registerRequest).enqueue(new Callback<TokenResponseDTO>() {
            @Override
            public void onResponse(Call<TokenResponseDTO> call, Response<TokenResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    registrationResultLiveData.postValue(response.body());
                } else {
                    String specificErrorMessage = "Error en registro (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            Log.e("UserRepository", "Cuerpo del error de registro: " + errorBodyString);
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyString, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getDetails() != null && !errorResponse.getDetails().isEmpty()) {
                                specificErrorMessage = String.join("\n", errorResponse.getDetails());
                            } else if (errorResponse != null && errorResponse.getMessage() != null) {
                                specificErrorMessage = errorResponse.getMessage();
                            }
                        } catch (Exception e) {
                            Log.e("UserRepository", "Error al parsear errorBody de registro", e);
                        }
                    }
                    registrationErrorLiveData.postValue(specificErrorMessage);
                    registrationResultLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<TokenResponseDTO> call, Throwable t) {
                Log.e("UserRepository", "Fallo en conexión de registro: " + t.getMessage(), t);
                registrationErrorLiveData.postValue("Error de conexión: " + t.getMessage());
                registrationResultLiveData.postValue(null);
            }
        });
        return registrationResultLiveData;
    }

    // === NUEVO MÉTODO PARA OBTENER EL PERFIL DEL USUARIO ===

    /**
     * Obtiene el perfil del usuario actualmente autenticado.
     * El AuthInterceptor se encarga de añadir el token de acceso.
     *
     * @return LiveData que emitirá el UserResponseDTO con los datos del perfil,
     * o null en caso de error.
     */
    public LiveData<UserResponseDTO> getUserProfile(String email) {
        MutableLiveData<UserResponseDTO> userProfileResultLiveData = new MutableLiveData<>();
        userProfileErrorLiveData.postValue(null); // Limpiar error anterior

        apiService.getUserProfile(email).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userProfileResultLiveData.postValue(response.body());
                } else {
                    String errorMessage = "Error al cargar perfil (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            Log.e("UserRepository", "Cuerpo del error de perfil: " + errorBodyString);
                            // Podrías parsear un ErrorResponseDTO si tu endpoint de perfil devuelve errores estructurados
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyString, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                errorMessage = errorResponse.getMessage();
                            } else if (errorResponse != null && errorResponse.getDetails() != null && !errorResponse.getDetails().isEmpty()) {
                                errorMessage = String.join("\n", errorResponse.getDetails());
                            }
                        } catch (Exception e) {
                            Log.e("UserRepository", "Error al parsear errorBody de perfil", e);
                        }
                    }
                    userProfileErrorLiveData.postValue(errorMessage);
                    userProfileResultLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                Log.e("UserRepository", "Fallo en conexión al cargar perfil: " + t.getMessage(), t);
                userProfileErrorLiveData.postValue("Error de conexión al cargar perfil: " + t.getMessage());
                userProfileResultLiveData.postValue(null);
            }
        });
        return userProfileResultLiveData;
    }
    // =======================================================
}
