package com.example.gamelend.repository;

import android.content.Context;
import android.net.Uri;
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
import com.example.gamelend.utils.FileUtils;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private ApiService apiService;
    private Context appContext;

    // LiveData para errores
    private MutableLiveData<String> loginErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> registrationErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> userProfileErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> updateUserErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> profileImageUploadErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> userListErrorLiveData = new MutableLiveData<>(); // <-- NUEVO para errores de getAllUsers

    public UserRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.apiService = ApiClient.getRetrofitInstance(appContext).create(ApiService.class);
    }

    // Getters para LiveData de error
    public LiveData<String> getLoginErrorLiveData() { return loginErrorLiveData; }
    public LiveData<String> getRegistrationErrorLiveData() { return registrationErrorLiveData; }
    public LiveData<String> getUserProfileErrorLiveData() { return userProfileErrorLiveData; }
    public LiveData<String> getUpdateUserErrorLiveData() { return updateUserErrorLiveData; }
    public LiveData<String> getProfileImageUploadErrorLiveData() { return profileImageUploadErrorLiveData; }
    public LiveData<String> getUserListErrorLiveData() { return userListErrorLiveData; } // <-- NUEVO GETTER

    public LiveData<TokenResponseDTO> login(String email, String password) {
        MutableLiveData<TokenResponseDTO> tokenLiveData = new MutableLiveData<>();
        loginErrorLiveData.postValue(null);
        apiService.login(new LoginRequestDTO(email, password)).enqueue(new Callback<TokenResponseDTO>() {
            @Override
            public void onResponse(Call<TokenResponseDTO> call, Response<TokenResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tokenLiveData.postValue(response.body());
                } else {
                    String errorMsg = "Error en login (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyStr = response.errorBody().string();
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyStr, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) errorMsg = errorResponse.getMessage();
                        } catch (Exception e) { Log.e(TAG, "Error parseando errorBody login", e); }
                    }
                    loginErrorLiveData.postValue(errorMsg);
                    tokenLiveData.postValue(null);
                }
            }
            @Override
            public void onFailure(Call<TokenResponseDTO> call, Throwable t) {
                loginErrorLiveData.postValue("Fallo de red en login: " + t.getMessage());
                tokenLiveData.postValue(null);
            }
        });
        return tokenLiveData;
    }

    public LiveData<TokenResponseDTO> registerUser(RegisterRequestDTO request) {
        MutableLiveData<TokenResponseDTO> result = new MutableLiveData<>();
        registrationErrorLiveData.postValue(null);
        apiService.register(request).enqueue(new Callback<TokenResponseDTO>() {
            @Override
            public void onResponse(Call<TokenResponseDTO> call, Response<TokenResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(response.body());
                } else {
                    String errorMessage = "Error en registro (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyStr = response.errorBody().string();
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyStr, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) errorMessage = errorResponse.getMessage();
                        } catch (Exception e) { Log.e(TAG, "Error parseando errorBody registro", e); }
                    }
                    registrationErrorLiveData.postValue(errorMessage);
                    result.postValue(null);
                }
            }
            @Override
            public void onFailure(Call<TokenResponseDTO> call, Throwable t) {
                registrationErrorLiveData.postValue("Fallo de red en registro: " + t.getMessage());
                result.postValue(null);
            }
        });
        return result;
    }

    public LiveData<UserResponseDTO> getUserProfile(String email) {
        MutableLiveData<UserResponseDTO> userProfileLiveData = new MutableLiveData<>();
        userProfileErrorLiveData.postValue(null);
        apiService.getUserProfile(email).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userProfileLiveData.postValue(response.body());
                } else {
                    String errorMsg = "Error al cargar perfil (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyStr = response.errorBody().string();
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyStr, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) errorMsg = errorResponse.getMessage();
                        } catch (Exception e) {Log.e(TAG, "Error parseando errorBody perfil", e);}
                    }
                    userProfileErrorLiveData.postValue(errorMsg);
                    userProfileLiveData.postValue(null);
                }
            }
            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                userProfileErrorLiveData.postValue("Fallo de red al cargar perfil: " + t.getMessage());
                userProfileLiveData.postValue(null);
            }
        });
        return userProfileLiveData;
    }

    public LiveData<UserResponseDTO> updateUser(Long userId, UserDTO userUpdateRequest) {
        MutableLiveData<UserResponseDTO> updateResult = new MutableLiveData<>();
        updateUserErrorLiveData.postValue(null);
        apiService.updateUser(userId, userUpdateRequest).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateResult.postValue(response.body());
                } else {
                    String errorMsg = "Error al actualizar perfil (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyStr = response.errorBody().string();
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyStr, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) errorMsg = errorResponse.getMessage();
                        } catch (Exception e) {Log.e(TAG, "Error parseando errorBody actualización", e);}
                    }
                    updateUserErrorLiveData.postValue(errorMsg);
                    updateResult.postValue(null);
                }
            }
            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                updateUserErrorLiveData.postValue("Fallo de red al actualizar perfil: " + t.getMessage());
                updateResult.postValue(null);
            }
        });
        return updateResult;
    }

    public LiveData<UserResponseDTO> uploadProfileImage(Long userId, Uri imageUri) {
        MutableLiveData<UserResponseDTO> uploadResultLiveData = new MutableLiveData<>();
        profileImageUploadErrorLiveData.postValue(null);

        File imageFile = FileUtils.getFileFromUri(appContext, imageUri);
        if (imageFile == null) {
            profileImageUploadErrorLiveData.postValue("Error al preparar la imagen para subir.");
            uploadResultLiveData.postValue(null);
            return uploadResultLiveData;
        }
        RequestBody requestFile = RequestBody.create(MediaType.parse(appContext.getContentResolver().getType(imageUri)), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        apiService.uploadProfileImage(userId, body).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    uploadResultLiveData.postValue(response.body());
                } else {
                    String errorMessage = "Error al subir imagen (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyStr = response.errorBody().string();
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyStr, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) errorMessage = errorResponse.getMessage();
                        } catch (IOException e) { Log.e(TAG, "Error parseando errorBody subida imagen", e); }
                    }
                    profileImageUploadErrorLiveData.postValue(errorMessage);
                    uploadResultLiveData.postValue(null);
                }
            }
            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                profileImageUploadErrorLiveData.postValue("Error de conexión al subir imagen: " + t.getMessage());
                uploadResultLiveData.postValue(null);
            }
        });
        return uploadResultLiveData;
    }

    /**
     * Obtiene la lista de todos los usuarios desde la API.
     * El AuthInterceptor se encarga del token de acceso.
     * @return LiveData que emitirá List<UserResponseDTO> si la llamada es exitosa,
     * o una lista vacía en caso de error (el error específico se posteará a userListErrorLiveData).
     */
    public LiveData<List<UserResponseDTO>> getAllUsers() {
        MutableLiveData<List<UserResponseDTO>> usersListResultLiveData = new MutableLiveData<>();
        userListErrorLiveData.postValue(null); // Limpiar error anterior

        apiService.getAllUsers().enqueue(new Callback<List<UserResponseDTO>>() {
            @Override
            public void onResponse(Call<List<UserResponseDTO>> call, Response<List<UserResponseDTO>> response) {
                if (response.isSuccessful()) {
                    if (response.code() == 204 || response.body() == null) {
                        usersListResultLiveData.postValue(new ArrayList<>());
                    } else {
                        usersListResultLiveData.postValue(response.body());
                    }
                } else {
                    String errorMessage = "Error al obtener usuarios (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            Log.e(TAG, "Cuerpo del error al obtener usuarios: " + errorBodyString);
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyString, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                errorMessage = errorResponse.getMessage();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear errorBody al obtener usuarios", e);
                        }
                    }
                    userListErrorLiveData.postValue(errorMessage);
                    usersListResultLiveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<UserResponseDTO>> call, Throwable t) {
                Log.e(TAG, "Fallo en conexión al obtener usuarios: " + t.getMessage(), t);
                userListErrorLiveData.postValue("Error de conexión al obtener usuarios: " + t.getMessage());
                usersListResultLiveData.postValue(new ArrayList<>());
            }
        });
        return usersListResultLiveData;
    }
}