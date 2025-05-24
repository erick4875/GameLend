package com.example.gamelend.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.dto.LoginRequestDTO; // Tu DTO para la petición de login
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
    // Guardar el email usado para el login para poder almacenarlo si el login es exitoso
    private String emailUsedForLogin;

    public LoginViewModel(Application application) {
        super(application);
        this.userRepository = new UserRepository(application.getApplicationContext());
        this.tokenManager = ApiClient.getTokenManager(application.getApplicationContext());
    }

    public void performLogin(String email, String password) {
        _isLoadingLiveData.setValue(true);
        _errorLiveData.setValue(null); // Limpiar error anterior
        this.emailUsedForLogin = email; // Guardar el email que se intenta loguear

        LiveData<TokenResponseDTO> apiResponseLiveData = userRepository.login(email, password);

        if (loginObserver != null && apiResponseLiveData.hasObservers()) {
            apiResponseLiveData.removeObserver(loginObserver);
        }

        loginObserver = new Observer<TokenResponseDTO>() {
            @Override
            public void onChanged(TokenResponseDTO tokenResponse) {
                apiResponseLiveData.removeObserver(this); // Remover después de la primera emisión
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
                    // === GUARDAR EL EMAIL USADO PARA EL LOGIN ===
                    if (emailUsedForLogin != null && !emailUsedForLogin.isEmpty()) {
                        tokenManager.saveEmail(emailUsedForLogin);
                        Log.d("LoginViewModel", "Email guardado en TokenManager: " + emailUsedForLogin);
                    }
                    // =========================================
                    _loginResultLiveData.postValue(tokenResponse);
                } else {
                    Log.e("LoginViewModel", "Login fallido o no se recibió token/respuesta esperada.");
                    // El error específico del repositorio (si lo hay) debería ser observado por separado
                    // o propagado de otra manera. Si no, mostramos uno genérico.
                    if (_errorLiveData.getValue() == null) { // Si el repo no posteó un error más específico
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
        // Si apiResponseLiveData fuera un campo de instancia y persistente,
        // y loginObserver todavía lo estuviera observando, lo removerías aquí.
        // Como lo removemos en onChanged, este onCleared no necesita hacer más para ESE observador.
    }
}
