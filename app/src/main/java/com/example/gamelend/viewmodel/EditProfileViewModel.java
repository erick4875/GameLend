package com.example.gamelend.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.dto.UserDTO; // Para la actualización
import com.example.gamelend.dto.UserResponseDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.repository.UserRepository;

public class EditProfileViewModel extends AndroidViewModel {

    private static final String TAG = "EditProfileViewModel";

    private UserRepository userRepository;
    private TokenManager tokenManager;

    // Para cargar los datos actuales del perfil
    private final MutableLiveData<UserResponseDTO> _userData = new MutableLiveData<>();
    public final LiveData<UserResponseDTO> userData = _userData;

    // Para el resultado de la actualización del perfil
    private final MutableLiveData<Boolean> _updateSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> updateSuccess = _updateSuccess;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    // Observadores para los LiveData del repositorio
    private Observer<UserResponseDTO> profileDataObserver;
    private LiveData<UserResponseDTO> currentProfileFetchLiveData;

    private Observer<UserResponseDTO> profileUpdateObserver;
    private LiveData<UserResponseDTO> currentProfileUpdateLiveData;


    public EditProfileViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application.getApplicationContext());
        tokenManager = ApiClient.getTokenManager(application.getApplicationContext());

        // Observar errores globales del perfil desde el repositorio (si los hubiera)
        userRepository.getUserProfileErrorLiveData().observeForever(errorMsg -> {
            if (errorMsg != null && _isLoading.getValue() == Boolean.TRUE) { // Solo si estamos cargando y hay error del repo
                _isLoading.setValue(false);
                _errorMessage.postValue(errorMsg);
            }
        });
        // Observar errores de actualización del perfil desde el repositorio

        userRepository.getUpdateUserErrorLiveData().observeForever(errorMsg -> {
            if (errorMsg != null && _isLoading.getValue() == Boolean.TRUE) {
                _isLoading.setValue(false);
                _errorMessage.postValue(errorMsg);
                _updateSuccess.postValue(false);
            }
        });
    }

    public void fetchCurrentUserData() {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);

        String userEmail = tokenManager.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            _isLoading.setValue(false);
            _errorMessage.postValue("No se pudo obtener el email del usuario. Inicie sesión de nuevo.");
            Log.e(TAG, "Email de usuario no encontrado en TokenManager para fetchCurrentUserData.");
            _userData.postValue(null);
            return;
        }

        Log.d(TAG, "Fetching profile for email: " + userEmail);

        if (currentProfileFetchLiveData != null && profileDataObserver != null && currentProfileFetchLiveData.hasObservers()) {
            currentProfileFetchLiveData.removeObserver(profileDataObserver);
        }

        currentProfileFetchLiveData = userRepository.getUserProfile(userEmail);

        profileDataObserver = new Observer<UserResponseDTO>() {
            @Override
            public void onChanged(UserResponseDTO userResponse) {
                if (currentProfileFetchLiveData != null) {
                    currentProfileFetchLiveData.removeObserver(this);
                }
                _isLoading.setValue(false);
                if (userResponse != null) {
                    _userData.postValue(userResponse);
                } else {
                    if (_errorMessage.getValue() == null) {
                        _errorMessage.postValue("Error al cargar los datos del perfil.");
                    }
                    _userData.postValue(null);
                }
            }
        };
        currentProfileFetchLiveData.observeForever(profileDataObserver);
    }

    public void updateUserProfile(Long userId, UserDTO userUpdateRequest) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _updateSuccess.setValue(null); // Resetear el estado de éxito

        LiveData<UserResponseDTO> updateResponseLiveData = userRepository.updateUser(userId, userUpdateRequest);

        if (currentProfileUpdateLiveData != null && profileUpdateObserver != null && currentProfileUpdateLiveData.hasObservers()) {
            currentProfileUpdateLiveData.removeObserver(profileUpdateObserver);
        }
        currentProfileUpdateLiveData = updateResponseLiveData;

        profileUpdateObserver = new Observer<UserResponseDTO>() {
            @Override
            public void onChanged(UserResponseDTO updatedUser) {
                if (currentProfileUpdateLiveData != null) {
                    currentProfileUpdateLiveData.removeObserver(this);
                }
                _isLoading.setValue(false);
                if (updatedUser != null) {
                    _userData.postValue(updatedUser);
                    _updateSuccess.postValue(true);
                    Log.d(TAG, "Perfil actualizado exitosamente para userId: " + userId);
                    if (updatedUser.getPublicName() != null) {
                        tokenManager.savePublicName(updatedUser.getPublicName());
                        Log.d(TAG, "PublicName actualizado en TokenManager a: " + updatedUser.getPublicName());
                    }
                } else {
                    if (_errorMessage.getValue() == null) {
                        _errorMessage.postValue("Error al actualizar el perfil.");
                    }
                    _updateSuccess.postValue(false);
                }
            }
        };
        currentProfileUpdateLiveData.observeForever(profileUpdateObserver);
    }

    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentProfileFetchLiveData != null && profileDataObserver != null && currentProfileFetchLiveData.hasObservers()) {
            currentProfileFetchLiveData.removeObserver(profileDataObserver);
        }
        if (currentProfileUpdateLiveData != null && profileUpdateObserver != null && currentProfileUpdateLiveData.hasObservers()) {
            currentProfileUpdateLiveData.removeObserver(profileUpdateObserver);
        }
    }
}

