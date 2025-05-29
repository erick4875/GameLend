package com.example.gamelend.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.dto.TokenResponseDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {

    private UserRepository userRepository;
    private TokenManager tokenManager;

    private final MutableLiveData<TokenResponseDTO> _loginResultLiveData = new MutableLiveData<>();
    public final LiveData<TokenResponseDTO> loginResultLiveData = _loginResultLiveData;

    private final MutableLiveData<String> _errorLiveData = new MutableLiveData<>();
    public final LiveData<String> errorLiveData = _errorLiveData;

    private final MutableLiveData<Boolean> _isLoadingLiveData = new MutableLiveData<>();
    public final LiveData<Boolean> isLoadingLiveData = _isLoadingLiveData;

    private Observer<TokenResponseDTO> loginObserver;
    private String emailUsedForLogin;

    public LoginViewModel(Application application) {
        super(application);
        this.userRepository = new UserRepository(application.getApplicationContext());
        this.tokenManager = ApiClient.getTokenManager(application.getApplicationContext());
    }

    public void performLogin(String email, String password) {
        _isLoadingLiveData.setValue(true);
        _errorLiveData.setValue(null);
        this.emailUsedForLogin = email;

        LiveData<TokenResponseDTO> apiResponseLiveData = userRepository.login(email, password);

        if (loginObserver != null && apiResponseLiveData.hasObservers()) {
            apiResponseLiveData.removeObserver(loginObserver);
        }

        loginObserver = new Observer<TokenResponseDTO>() {
            @Override
            public void onChanged(TokenResponseDTO tokenResponse) {
                apiResponseLiveData.removeObserver(this);
                _isLoadingLiveData.setValue(false);

                if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                    Log.d("LoginViewModel", "Login exitoso. AccessToken: " + tokenResponse.getAccessToken().substring(0, Math.min(10, tokenResponse.getAccessToken().length())) + "...");

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

                    String emailToPersist = null;
                    if (tokenResponse.getEmail() != null && !tokenResponse.getEmail().isEmpty()) {
                        emailToPersist = tokenResponse.getEmail();
                        Log.d("LoginViewModel", "Email obtenido de TokenResponseDTO: " + emailToPersist);
                    } else if (emailUsedForLogin != null && !emailUsedForLogin.isEmpty()) {
                        emailToPersist = emailUsedForLogin;
                        Log.w("LoginViewModel", "Email no presente en TokenResponseDTO, usando el email de entrada: " + emailToPersist);
                    }

                    if (emailToPersist != null) {
                        tokenManager.saveEmail(emailToPersist);
                        Log.d("LoginViewModel", "Email guardado en TokenManager: " + emailToPersist);
                    } else {
                        Log.w("LoginViewModel", "No se pudo determinar un email para guardar en TokenManager.");
                    }

                    _loginResultLiveData.postValue(tokenResponse);
                } else {
                    Log.e("LoginViewModel", "Login fallido o no se recibió token/respuesta esperada.");
                    if (_errorLiveData.getValue() == null) {
                        _errorLiveData.postValue("Credenciales incorrectas o error de conexión.");
                    }
                    _loginResultLiveData.postValue(null);
                }
            }
        };
        apiResponseLiveData.observeForever(loginObserver);
    }

    public void clearLoginError() {
        _errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}