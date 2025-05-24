package com.example.gamelend.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.dto.RegisterRequestDTO;
import com.example.gamelend.dto.TokenResponseDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.repository.UserRepository;

public class RegisterViewModel extends AndroidViewModel {

    private static final String TAG = "RegisterViewModel";

    private UserRepository userRepository;
    private TokenManager tokenManager;

    private final MutableLiveData<TokenResponseDTO> _registrationResultLiveData = new MutableLiveData<>();
    public final LiveData<TokenResponseDTO> registrationResultLiveData = _registrationResultLiveData;

    private final MutableLiveData<String> _errorLiveData = new MutableLiveData<>();
    public final LiveData<String> errorLiveData = _errorLiveData;

    private final MutableLiveData<Boolean> _isLoadingLiveData = new MutableLiveData<>();
    public final LiveData<Boolean> isLoadingLiveData = _isLoadingLiveData;

    private Observer<TokenResponseDTO> registrationApiObserver;
    private LiveData<TokenResponseDTO> currentApiResponseLiveData;

    public RegisterViewModel(Application application) {
        super(application);
        this.userRepository = new UserRepository(application.getApplicationContext());
        this.tokenManager = ApiClient.getTokenManager(application.getApplicationContext());

        userRepository.getRegistrationErrorLiveData().observeForever(errorMessage -> {
            if (errorMessage != null) {
                Log.d(TAG, "Error recibido del repositorio: " + errorMessage);
                _isLoadingLiveData.postValue(false);
                _errorLiveData.postValue(errorMessage);
                _registrationResultLiveData.postValue(null);
            }
        });
    }

    public void performRegistration(RegisterRequestDTO request) {
        _isLoadingLiveData.setValue(true);
        _errorLiveData.setValue(null);
        _registrationResultLiveData.setValue(null);

        if (currentApiResponseLiveData != null && registrationApiObserver != null && currentApiResponseLiveData.hasObservers()) {
            currentApiResponseLiveData.removeObserver(registrationApiObserver);
        }

        currentApiResponseLiveData = userRepository.registerUser(request);

        registrationApiObserver = new Observer<TokenResponseDTO>() {
            @Override
            public void onChanged(TokenResponseDTO tokenResponse) {
                if (currentApiResponseLiveData != null) {
                    currentApiResponseLiveData.removeObserver(this);
                }

                if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                    _isLoadingLiveData.setValue(false); // Detener carga en caso de éxito
                    Log.d(TAG, "Registro exitoso. AccessToken: " + tokenResponse.getAccessToken().substring(0, Math.min(10, tokenResponse.getAccessToken().length())) + "...");
                    tokenManager.saveAccessToken(tokenResponse.getAccessToken());
                    if (tokenResponse.getRefreshToken() != null) {
                        tokenManager.saveRefreshToken(tokenResponse.getRefreshToken());
                    }
                    if (tokenResponse.getUserId() != null) {
                        tokenManager.saveUserId(tokenResponse.getUserId());
                    }
                    if (tokenResponse.getPublicName() != null) {
                        tokenManager.savePublicName(tokenResponse.getPublicName());
                    }
                    if (tokenResponse.getRoles() != null) {
                        tokenManager.saveRoles(tokenResponse.getRoles());
                    }
                    // === CORRECCIÓN AQUÍ: Usar getEmail() para el POJO de Android ===
                    String emailToSave = request.getEmail(); // Usar el getter
                    if (emailToSave != null && !emailToSave.isEmpty()) {
                        tokenManager.saveEmail(emailToSave);
                        Log.d(TAG, "Email de registro guardado en TokenManager: " + emailToSave);
                    }
                    // ==========================================================
                    _registrationResultLiveData.postValue(tokenResponse);
                }
                // Si tokenResponse es null, el error ya fue manejado por el observador
                // de userRepository.getRegistrationErrorLiveData().
                // isLoadingLiveData ya se habrá puesto a false en ese observador.
            }
        };
        currentApiResponseLiveData.observeForever(registrationApiObserver);
    }

    public void clearRegistrationError() {
        _errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentApiResponseLiveData != null && registrationApiObserver != null && currentApiResponseLiveData.hasObservers()) {
            currentApiResponseLiveData.removeObserver(registrationApiObserver);
        }
    }
}

