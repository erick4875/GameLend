package com.example.gamelend.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.gamelend.dto.UserResponseDTO;
import com.example.gamelend.repository.UserRepository;
import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.remote.api.ApiClient;

public class UserProfileViewModel extends AndroidViewModel {

    private UserRepository userRepository;
    private TokenManager tokenManager;
    private final MutableLiveData<UserResponseDTO> _userData = new MutableLiveData<>();
    public final LiveData<UserResponseDTO> userData = _userData;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    private LiveData<UserResponseDTO> currentProfileLiveData;
    private Observer<UserResponseDTO> profileObserver;

    public UserProfileViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application.getApplicationContext());
        tokenManager = ApiClient.getTokenManager(application.getApplicationContext());
        userRepository.getUserProfileErrorLiveData().observeForever(errorMsg -> {
            if (errorMsg != null) {
                _errorMessage.postValue(errorMsg);
            }
        });
    }

    public void fetchUserProfileData() {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);

        String userEmail = tokenManager.getEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            _isLoading.setValue(false);
            _errorMessage.postValue("No se pudo obtener el email del usuario para cargar el perfil. Inicie sesión de nuevo.");
            Log.e("UserProfileViewModel", "Email de usuario no encontrado en TokenManager.");
            _userData.postValue(null);
            return;
        }

        Log.d("UserProfileViewModel", "Fetching profile for email: " + userEmail);

        if (currentProfileLiveData != null && profileObserver != null && currentProfileLiveData.hasObservers()) {
            currentProfileLiveData.removeObserver(profileObserver);
        }

        currentProfileLiveData = userRepository.getUserProfile(userEmail); // Obtener el nuevo LiveData

        profileObserver = new Observer<UserResponseDTO>() {
            @Override
            public void onChanged(UserResponseDTO userResponse) {
                if (currentProfileLiveData != null) {
                    currentProfileLiveData.removeObserver(this); // 'this' se refiere a esta instancia de Observer
                }

                _isLoading.setValue(false);
                if (userResponse != null) {
                    _userData.postValue(userResponse);
                } else {
                    if (_errorMessage.getValue() == null) { // Si el repo no posteó un error más específico
                        _errorMessage.postValue("Error al cargar el perfil del usuario.");
                    }
                    _userData.postValue(null); // Postear null para indicar que no hay datos de usuario válidos
                }
            }
        };
        currentProfileLiveData.observeForever(profileObserver);
    }

    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentProfileLiveData != null && profileObserver != null && currentProfileLiveData.hasObservers()) {
            currentProfileLiveData.removeObserver(profileObserver);
        }
    }
}

