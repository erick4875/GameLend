package com.example.gamelend.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.dto.ErrorResponseDTO;
import com.example.gamelend.dto.GameResponseDTO;
import com.example.gamelend.models.GameStatus;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.repository.GameRepository;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddGameViewModel extends AndroidViewModel {

    private static final String TAG = "AddGameViewModel";

    private GameRepository gameRepository;
    private TokenManager tokenManager;

    private final MutableLiveData<GameResponseDTO> _gameSaveResultLiveData = new MutableLiveData<>();
    public final LiveData<GameResponseDTO> gameSaveResultLiveData = _gameSaveResultLiveData;

    private final MutableLiveData<String> _errorLiveData = new MutableLiveData<>();
    public final LiveData<String> errorLiveData = _errorLiveData;

    private final MutableLiveData<Boolean> _isLoadingLiveData = new MutableLiveData<>();
    public final LiveData<Boolean> isLoadingLiveData = _isLoadingLiveData;

    public AddGameViewModel(Application application) {
        super(application);
        this.gameRepository = new GameRepository(application.getApplicationContext());
        this.tokenManager = ApiClient.getTokenManager(application.getApplicationContext());
    }

    /**
     * Intenta guardar un nuevo juego.
     * @param title Título del juego.
     * @param platform Plataforma del juego.
     * @param genre Género del juego.
     * @param description Descripción opcional.
     * @param status Estado del juego (del enum de Android).
     * @param isCatalog Si es un juego de catálogo.
     * @param catalogGameId ID del juego de catálogo base (si aplica).
     */
    public void saveNewGame(String title, String platform, String genre, String description,
                            GameStatus status, // Este es com.example.gamelend.models.GameStatus
                            boolean isCatalog, Long catalogGameId) {

        _isLoadingLiveData.setValue(true);
        _errorLiveData.setValue(null);
        _gameSaveResultLiveData.setValue(null);

        Long currentUserId = tokenManager.getUserId();
        if (currentUserId == null) {
            _isLoadingLiveData.setValue(false);
            _errorLiveData.postValue("Error: Usuario no autenticado. No se puede guardar el juego.");
            Log.e(TAG, "UserID es null, no se puede guardar el juego.");
            return;
        }

        // Convertir el GameStatus del modelo de UI (Android) al tipo que espera GameDTO si son diferentes.
        // Si tu GameDTO POJO en Android usa el mismo enum com.example.gamelend.models.GameStatus,
        // entonces puedes pasarlo directamente.
        // Si GameDTO espera un String para status, harías status.name().
        // Por ahora, asumimos que GameDTO puede tomar el GameStatus del modelo de Android.
        com.example.gamelend.dto.GameDTO gameDtoToSend = new com.example.gamelend.dto.GameDTO(
                null, // id es null para creación
                title,
                platform,
                genre,
                description, // Ya se maneja el isEmpty en la Activity o aquí si prefieres
                status, // Pasando el GameStatus de Android directamente
                currentUserId,
                null, // imageId
                null, // imageUrl
                isCatalog,
                catalogGameId
        );

        Log.d(TAG, "Intentando guardar juego para userId: " + currentUserId + " con DTO: " + gameDtoToSend.getTitle());

        Call<GameResponseDTO> call = gameRepository.createGame(gameDtoToSend);

        call.enqueue(new Callback<GameResponseDTO>() {
            @Override
            public void onResponse(Call<GameResponseDTO> call, Response<GameResponseDTO> response) {
                _isLoadingLiveData.postValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Juego guardado exitosamente. ID: " + response.body().getId());
                    _gameSaveResultLiveData.postValue(response.body());
                } else {
                    String errorMessage = "Error al guardar juego (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            Log.e(TAG, "Cuerpo del error al guardar juego: " + errorBodyString);
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyString, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getDetails() != null && !errorResponse.getDetails().isEmpty()) {
                                errorMessage = String.join("\n", errorResponse.getDetails());
                            } else if (errorResponse != null && errorResponse.getMessage() != null) {
                                errorMessage = errorResponse.getMessage();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error al parsear errorBody al guardar juego", e);
                        }
                    }
                    _errorLiveData.postValue(errorMessage);
                    _gameSaveResultLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<GameResponseDTO> call, Throwable t) {
                _isLoadingLiveData.postValue(false);
                Log.e(TAG, "Fallo en conexión al guardar juego: " + t.getMessage(), t);
                _errorLiveData.postValue("Error de conexión al guardar juego: " + t.getMessage());
                _gameSaveResultLiveData.postValue(null);
            }
        });
    }

    public void clearSaveError() {
        _errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}