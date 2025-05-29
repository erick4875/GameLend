package com.example.gamelend.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.dto.UserDTO;
import com.example.gamelend.dto.UserResponseDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.repository.UserRepository;

public class EditProfileViewModel extends AndroidViewModel {

    private static final String TAG = "EditProfileViewModel";

    private final UserRepository userRepository;
    private final TokenManager tokenManager;

    // Para cargar los datos actuales del perfil
    private final MutableLiveData<UserResponseDTO> _userData = new MutableLiveData<>();
    public final LiveData<UserResponseDTO> userData = _userData;

    // Para el resultado de la actualización de datos de TEXTO del perfil
    private final MutableLiveData<Boolean> _updateSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> updateSuccess = _updateSuccess;

    // Para el estado de carga general
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    // Para errores de la actualización de DATOS DE TEXTO del perfil
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    // LiveData para el resultado de la subida de la imagen de perfil
    // Cambiado a UserResponseDTO asumiendo que el backend devuelve el perfil actualizado
    private final MutableLiveData<UserResponseDTO> _profileImageUploadResult = new MutableLiveData<>();
    public final LiveData<UserResponseDTO> profileImageUploadResult = _profileImageUploadResult;

    // LiveData para errores específicos de la subida de imagen
    private final MutableLiveData<String> _imageUploadError = new MutableLiveData<>();
    public final LiveData<String> imageUploadError = _imageUploadError;

    // Observadores
    private Observer<UserResponseDTO> profileDataObserver;
    private LiveData<UserResponseDTO> currentProfileFetchLiveData;
    private Observer<UserResponseDTO> profileUpdateObserver;
    private LiveData<UserResponseDTO> currentProfileUpdateLiveData;
    private Observer<UserResponseDTO> profileImageUploadApiObserver; // Espera UserResponseDTO
    private LiveData<UserResponseDTO> currentProfileImageUploadApiLiveData;
    private final Observer<String> userProfileErrorObserver; // Para errores de fetch de perfil
    private final Observer<String> userUpdateErrorObserver;  // Para errores de update de perfil (texto)
    private final Observer<String> imageUploadErrorObserverFromRepo;


    public EditProfileViewModel(Application application) {
        super(application);
        // Asegúrate que UserRepository se inicialice con el contexto de la aplicación
        userRepository = new UserRepository(application.getApplicationContext());
        tokenManager = ApiClient.getTokenManager(application.getApplicationContext());

        // Observador para errores al cargar el perfil
        userProfileErrorObserver = errorMsg -> {
            if (errorMsg != null && Boolean.TRUE.equals(_isLoading.getValue())) {
                _isLoading.setValue(false);
                _errorMessage.postValue(errorMsg); // Usar el _errorMessage general
                _userData.postValue(null); // Indicar que la carga de datos falló
            }
        };
        userRepository.getUserProfileErrorLiveData().observeForever(userProfileErrorObserver);

        // Observador para errores al actualizar datos de texto del perfil
        userUpdateErrorObserver = errorMsg -> {
            if (errorMsg != null && Boolean.TRUE.equals(_isLoading.getValue())) {
                _isLoading.setValue(false);
                _errorMessage.postValue(errorMsg);
                _updateSuccess.postValue(false); // Indicar que la actualización de texto falló
            }
        };
        userRepository.getUpdateUserErrorLiveData().observeForever(userUpdateErrorObserver);

        // Observador para errores de subida de imagen desde el repositorio
        imageUploadErrorObserverFromRepo = errorMsg -> {
            if (errorMsg != null && Boolean.TRUE.equals(_isLoading.getValue())) {
                _isLoading.setValue(false);
                _imageUploadError.postValue(errorMsg);
                _profileImageUploadResult.postValue(null);
            }
        };
        userRepository.getProfileImageUploadErrorLiveData().observeForever(imageUploadErrorObserverFromRepo);
    }

    public void fetchCurrentUserData() {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _userData.setValue(null);

        String userEmail = tokenManager.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            _isLoading.setValue(false);
            _errorMessage.postValue("No se pudo obtener el email del usuario. Inicie sesión de nuevo.");
            Log.e(TAG, "Email de usuario no encontrado en TokenManager para fetchCurrentUserData.");
            return;
        }
        Log.d(TAG, "Fetching profile for email: " + userEmail);

        if (currentProfileFetchLiveData != null && profileDataObserver != null && currentProfileFetchLiveData.hasObservers()) {
            currentProfileFetchLiveData.removeObserver(profileDataObserver);
        }
        currentProfileFetchLiveData = userRepository.getUserProfile(userEmail);
        profileDataObserver = updatedUser -> {
            if (currentProfileFetchLiveData != null) currentProfileFetchLiveData.removeObserver(profileDataObserver);
            _isLoading.setValue(false);
            if (updatedUser != null) {
                _userData.postValue(updatedUser);
            } else {
                if (_errorMessage.getValue() == null) _errorMessage.postValue("Error al cargar datos del perfil.");
            }
        };
        currentProfileFetchLiveData.observeForever(profileDataObserver);
    }

    public void updateUserProfile(Long userId, UserDTO userUpdateRequest) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _updateSuccess.setValue(null);

        if (currentProfileUpdateLiveData != null && profileUpdateObserver != null && currentProfileUpdateLiveData.hasObservers()) {
            currentProfileUpdateLiveData.removeObserver(profileUpdateObserver);
        }
        currentProfileUpdateLiveData = userRepository.updateUser(userId, userUpdateRequest);
        profileUpdateObserver = updatedUser -> {
            if (currentProfileUpdateLiveData != null) currentProfileUpdateLiveData.removeObserver(profileUpdateObserver);
            _isLoading.setValue(false);
            if (updatedUser != null) {
                _userData.postValue(updatedUser);
                _updateSuccess.postValue(true);
                Log.d(TAG, "Datos de texto del perfil actualizados para userId: " + userId);
                if (updatedUser.getPublicName() != null) {
                    tokenManager.savePublicName(updatedUser.getPublicName());
                }
            } else {
                if (_errorMessage.getValue() == null) _errorMessage.postValue("Error al actualizar datos del perfil.");
                _updateSuccess.postValue(false);
            }
        };
        currentProfileUpdateLiveData.observeForever(profileUpdateObserver);
    }

    /**
     * Sube la imagen de perfil seleccionada.
     * Se espera que el backend devuelva el UserResponseDTO actualizado.
     * @param userId El ID del usuario.
     * @param imageUri La URI de la imagen seleccionada.
     */
    public void uploadProfileImage(Long userId, Uri imageUri) {
        if (userId == null || imageUri == null) {
            _imageUploadError.postValue("ID de usuario o URI de imagen no válidos para la subida.");
            return;
        }
        _isLoading.setValue(true);
        _imageUploadError.setValue(null);
        _profileImageUploadResult.setValue(null);

        if (currentProfileImageUploadApiLiveData != null && profileImageUploadApiObserver != null && currentProfileImageUploadApiLiveData.hasObservers()) {
            currentProfileImageUploadApiLiveData.removeObserver(profileImageUploadApiObserver);
        }

        currentProfileImageUploadApiLiveData = userRepository.uploadProfileImage(userId, imageUri);

        profileImageUploadApiObserver = userResponseAfterUpload -> {
            if (currentProfileImageUploadApiLiveData != null) currentProfileImageUploadApiLiveData.removeObserver(profileImageUploadApiObserver);

            if (userResponseAfterUpload != null && userResponseAfterUpload.getProfileImageUrl() != null) {
                _isLoading.setValue(false);
                Log.d(TAG, "Imagen de perfil subida, nueva URL: " + userResponseAfterUpload.getProfileImageUrl());
                _profileImageUploadResult.postValue(userResponseAfterUpload);
                _userData.postValue(userResponseAfterUpload); //
                if (userResponseAfterUpload.getPublicName() != null) {
                    tokenManager.savePublicName(userResponseAfterUpload.getPublicName());
                }
            } else {
                _isLoading.setValue(false); // Asegurar que se detenga la carga
                if (_imageUploadError.getValue() == null) {
                    _imageUploadError.postValue("Error al subir la imagen de perfil o respuesta inesperada del servidor.");
                }
                _profileImageUploadResult.postValue(null);
            }
        };
        currentProfileImageUploadApiLiveData.observeForever(profileImageUploadApiObserver);
    }

    public void clearErrorMessage() { _errorMessage.setValue(null); }
    public void clearImageUploadError() { _imageUploadError.setValue(null); }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentProfileFetchLiveData != null && profileDataObserver != null && currentProfileFetchLiveData.hasObservers()) {
            currentProfileFetchLiveData.removeObserver(profileDataObserver);
        }
        if (currentProfileUpdateLiveData != null && profileUpdateObserver != null && currentProfileUpdateLiveData.hasObservers()) {
            currentProfileUpdateLiveData.removeObserver(profileUpdateObserver);
        }
        if (currentProfileImageUploadApiLiveData != null && profileImageUploadApiObserver != null && currentProfileImageUploadApiLiveData.hasObservers()) {
            currentProfileImageUploadApiLiveData.removeObserver(profileImageUploadApiObserver);
        }
        if (userProfileErrorObserver != null && userRepository.getUserProfileErrorLiveData().hasObservers()) {
            userRepository.getUserProfileErrorLiveData().removeObserver(userProfileErrorObserver);
        }
        if (userUpdateErrorObserver != null && userRepository.getUpdateUserErrorLiveData().hasObservers()) {
            userRepository.getUpdateUserErrorLiveData().removeObserver(userUpdateErrorObserver);
        }
        if (imageUploadErrorObserverFromRepo != null && userRepository.getProfileImageUploadErrorLiveData().hasObservers()) {
            userRepository.getProfileImageUploadErrorLiveData().removeObserver(imageUploadErrorObserverFromRepo);
        }
    }
}