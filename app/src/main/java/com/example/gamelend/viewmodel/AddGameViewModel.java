package com.example.gamelend.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.dto.GameDTO;
import com.example.gamelend.dto.GameResponseDTO;
import com.example.gamelend.models.GameStatus;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.repository.GameRepository;

public class AddGameViewModel extends AndroidViewModel {

    private static final String TAG = "AddGameViewModel";

    private GameRepository gameRepository;
    private TokenManager tokenManager;

    // LiveData para el resultado de guardar el juego (GameResponseDTO si es exitoso)
    private final MutableLiveData<GameResponseDTO> _gameSaveResultLiveData = new MutableLiveData<>();
    public final LiveData<GameResponseDTO> gameSaveResultLiveData = _gameSaveResultLiveData;

    // LiveData para mensajes de error (ahora observará el del repositorio)
    private final MutableLiveData<String> _errorLiveData = new MutableLiveData<>();
    public final LiveData<String> errorLiveData = _errorLiveData;

    // LiveData para el estado de carga
    private final MutableLiveData<Boolean> _isLoadingLiveData = new MutableLiveData<>();
    public final LiveData<Boolean> isLoadingLiveData = _isLoadingLiveData;

    // Observador para el LiveData de resultado del repositorio
    private Observer<GameResponseDTO> gameCreationApiObserver;
    private LiveData<GameResponseDTO> currentGameCreationApiLiveData;

    // Observador para el LiveData de error del repositorio
    private Observer<String> gameCreationErrorObserver;


    public AddGameViewModel(Application application) {
        super(application);
        this.gameRepository = new GameRepository(application.getApplicationContext());
        this.tokenManager = ApiClient.getTokenManager(application.getApplicationContext());

        // Crear el observador para los errores de creación de juegos del repositorio
        gameCreationErrorObserver = errorMsg -> {
            if (errorMsg != null) {
                Log.d(TAG, "Error de creación de juego recibido del GameRepository: " + errorMsg);
                _isLoadingLiveData.postValue(false);
                _errorLiveData.postValue(errorMsg);
                _gameSaveResultLiveData.postValue(null);
            }
        };
        this.gameRepository.getCreateGameErrorLiveData().observeForever(gameCreationErrorObserver);
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
                            GameStatus status,
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

        GameDTO gameDtoToSend = new GameDTO(
                null, title, platform, genre, description,
                status, currentUserId, null, null, isCatalog, catalogGameId
        );

        Log.d(TAG, "Intentando guardar juego para userId: " + currentUserId + " con DTO: " + gameDtoToSend.getTitle());

        // Remover observador anterior si existe para la respuesta de la API
        if (currentGameCreationApiLiveData != null && gameCreationApiObserver != null && currentGameCreationApiLiveData.hasObservers()) {
            currentGameCreationApiLiveData.removeObserver(gameCreationApiObserver);
        }

        currentGameCreationApiLiveData = gameRepository.createGame(gameDtoToSend);

        gameCreationApiObserver = new Observer<GameResponseDTO>() {
            @Override
            public void onChanged(GameResponseDTO gameResponse) {
                // Remover el observador después de la primera emisión, ya que el LiveData del repo es nuevo cada vez
                if (currentGameCreationApiLiveData != null) {
                    currentGameCreationApiLiveData.removeObserver(this);
                }

                if (gameResponse != null) { // El éxito es simplemente que no sea null
                    _isLoadingLiveData.setValue(false); // Asegurar que el loading se detenga
                    Log.d(TAG, "Juego creado/guardado exitosamente: " + (gameResponse.getTitle() != null ? gameResponse.getTitle() : "N/A"));
                    _gameSaveResultLiveData.postValue(gameResponse);
                }
                else if (_errorLiveData.getValue() == null) { // Solo si el repo no posteó un error específico
                    _isLoadingLiveData.setValue(false);
                    _errorLiveData.postValue("Error desconocido al guardar el juego.");
                }
            }
        };
        currentGameCreationApiLiveData.observeForever(gameCreationApiObserver);
    }

    public void clearSaveError() {
        _errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Limpiar observadores para evitar memory leaks
        if (currentGameCreationApiLiveData != null && gameCreationApiObserver != null && currentGameCreationApiLiveData.hasObservers()) {
            currentGameCreationApiLiveData.removeObserver(gameCreationApiObserver);
        }
        if (gameCreationErrorObserver != null && gameRepository.getCreateGameErrorLiveData().hasObservers()) {
            gameRepository.getCreateGameErrorLiveData().removeObserver(gameCreationErrorObserver);
        }
    }
}