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

    // === DECLARAR currentProfileLiveData y profileObserver COMO CAMPOS ===
    private LiveData<UserResponseDTO> currentProfileLiveData;
    private Observer<UserResponseDTO> profileObserver;
    // =================================================================

    public UserProfileViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application.getApplicationContext());
        tokenManager = ApiClient.getTokenManager(application.getApplicationContext());

        // Observar el LiveData de error del perfil del repositorio
        // Es importante manejar el ciclo de vida de esta observación si el ViewModel se destruye
        // o si el LiveData del repositorio es un singleton.
        // Por ahora, para simplificar, lo dejamos así, pero considera usar un MediatorLiveData
        // o transformar el LiveData del repositorio y observarlo desde la Activity/Fragment
        // con getViewLifecycleOwner().
        userRepository.getUserProfileErrorLiveData().observeForever(errorMsg -> {
            if (errorMsg != null) {
                _errorMessage.postValue(errorMsg);
            }
        });
    }

    public void fetchUserProfileData() {
        _isLoading.setValue(true);
        _errorMessage.setValue(null); // Limpiar error anterior

        String userEmail = tokenManager.getEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            _isLoading.setValue(false);
            _errorMessage.postValue("No se pudo obtener el email del usuario para cargar el perfil. Inicie sesión de nuevo.");
            Log.e("UserProfileViewModel", "Email de usuario no encontrado en TokenManager.");
            // Considera postear null a _userData si la UI espera eso para limpiar datos viejos
            _userData.postValue(null);
            return;
        }

        Log.d("UserProfileViewModel", "Fetching profile for email: " + userEmail);

        // Si hay una observación anterior en un LiveData previo, removerla
        // Esto es importante si fetchUserProfileData puede ser llamado múltiples veces
        // y userRepository.getUserProfile(email) devuelve una NUEVA instancia de LiveData cada vez.
        if (currentProfileLiveData != null && profileObserver != null && currentProfileLiveData.hasObservers()) {
            currentProfileLiveData.removeObserver(profileObserver);
        }

        currentProfileLiveData = userRepository.getUserProfile(userEmail); // Obtener el nuevo LiveData

        profileObserver = new Observer<UserResponseDTO>() {
            @Override
            public void onChanged(UserResponseDTO userResponse) {
                // Remover el observador aquí porque el LiveData del repositorio
                // (tal como está implementado en UserRepository.getUserProfile)
                // es un nuevo MutableLiveData para cada llamada, por lo que solo emitirá una vez.
                if (currentProfileLiveData != null) {
                    currentProfileLiveData.removeObserver(this); // 'this' se refiere a esta instancia de Observer
                }

                _isLoading.setValue(false);
                if (userResponse != null) {
                    _userData.postValue(userResponse);
                } else {
                    // El error específico ya debería haber sido posteado por la observación
                    // de userRepository.getUserProfileErrorLiveData().
                    // Si no, podemos poner un mensaje genérico o simplemente no actualizar _userData.
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
        // Remover el observador si todavía está activo cuando el ViewModel se destruye
        // y si currentProfileLiveData no es nulo.
        if (currentProfileLiveData != null && profileObserver != null && currentProfileLiveData.hasObservers()) {
            currentProfileLiveData.removeObserver(profileObserver);
        }
        // Si observaste userRepository.getUserProfileErrorLiveData() con observeForever directamente aquí
        // (en lugar del constructor), también lo removerías. Como está en el constructor,
        // su ciclo de vida está atado al del ViewModel de forma más implícita.
    }
}

