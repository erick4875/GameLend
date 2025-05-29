package com.example.gamelend.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.gamelend.auth.TokenManager; // Para obtener userId
import com.example.gamelend.dto.GameResponseDTO; // Cambiado de GameSummaryDTO
import com.example.gamelend.dto.GameSummaryDTO;
import com.example.gamelend.remote.api.ApiClient; // Para TokenManager
import com.example.gamelend.repository.GameRepository;
import java.util.List;
import java.util.ArrayList;

public class GameListViewModel extends AndroidViewModel {
    private static final String TAG = "GameListViewModel";
    private GameRepository gameRepository;
    private TokenManager tokenManager; // Para obtener el ID del usuario actual

    // Cambiado para manejar GameResponseDTO si getGamesByUserId devuelve eso
    private final MutableLiveData<List<GameResponseDTO>> _gamesListLiveData = new MutableLiveData<>();
    public final LiveData<List<GameResponseDTO>> gamesListResponseLiveData = _gamesListLiveData;

    // Si fetchAllGames devuelve GameSummaryDTO, necesitarás un LiveData separado o unificar
    private final MutableLiveData<List<GameSummaryDTO>> _allGamesSummaryLiveData = new MutableLiveData<>();
    public final LiveData<List<GameSummaryDTO>> allGamesSummaryLiveData = _allGamesSummaryLiveData;


    private final MutableLiveData<Boolean> _isLoadingLiveData = new MutableLiveData<>();
    public final LiveData<Boolean> isLoadingLiveData = _isLoadingLiveData;

    private final MutableLiveData<String> _errorLiveData = new MutableLiveData<>();
    public final LiveData<String> errorLiveData = _errorLiveData;

    private Observer<List<GameResponseDTO>> gamesByUserIdObserver;
    private LiveData<List<GameResponseDTO>> currentGamesByUserIdApiLiveData;
    private Observer<String> gamesByUserIdErrorObserver;

    private Observer<List<GameSummaryDTO>> allGamesObserver;
    private LiveData<List<GameSummaryDTO>> currentAllGamesApiLiveData;
    private Observer<String> allGamesErrorObserver;


    public GameListViewModel(@NonNull Application application) {
        super(application);
        gameRepository = new GameRepository(application.getApplicationContext());
        tokenManager = ApiClient.getTokenManager(application.getApplicationContext()); // Inicializar TokenManager

        // Observador para errores de fetchGamesByUserId
        gamesByUserIdErrorObserver = errorMsg -> {
            if (errorMsg != null) {
                _isLoadingLiveData.postValue(false);
                _errorLiveData.postValue(errorMsg);
                // _gamesListLiveData.postValue(new ArrayList<>()); // Opcional, el repositorio ya podría hacerlo
            }
        };
        gameRepository.getFetchGamesByUserIdErrorLiveData().observeForever(gamesByUserIdErrorObserver);

        // Observador para errores de fetchAllGames
        allGamesErrorObserver = errorMsg -> {
            if (errorMsg != null) {
                _isLoadingLiveData.postValue(false);
                _errorLiveData.postValue(errorMsg);
                // _allGamesSummaryLiveData.postValue(new ArrayList<>());
            }
        };
        gameRepository.getFetchAllGamesErrorLiveData().observeForever(allGamesErrorObserver);
    }

    public void fetchAllGames() {
        _isLoadingLiveData.setValue(true);
        _errorLiveData.setValue(null);

        if (currentAllGamesApiLiveData != null && allGamesObserver != null && currentAllGamesApiLiveData.hasObservers()) {
            currentAllGamesApiLiveData.removeObserver(allGamesObserver);
        }
        currentAllGamesApiLiveData = gameRepository.getAllGames();
        allGamesObserver = gameSummaryDTOs -> {
            if(currentAllGamesApiLiveData != null) currentAllGamesApiLiveData.removeObserver(allGamesObserver);
            _isLoadingLiveData.setValue(false);
            if (gameSummaryDTOs != null) {
                _allGamesSummaryLiveData.postValue(gameSummaryDTOs);
                Log.d(TAG, "Juegos (summary) obtenidos exitosamente: " + gameSummaryDTOs.size() + " juegos.");
            } else if (_errorLiveData.getValue() == null) {
                _errorLiveData.postValue("No se pudieron cargar todos los juegos.");
            }
        };
        currentAllGamesApiLiveData.observeForever(allGamesObserver);
    }

    /**
     * Obtiene los juegos para el usuario actualmente logueado.
     */
    public void fetchCurrentUserGames() {
        Long currentUserId = tokenManager.getUserId();
        if (currentUserId == null || currentUserId == 0L) { // 0L como ID inválido
            Log.e(TAG, "No se pudo obtener el ID del usuario actual para cargar sus juegos.");
            _errorLiveData.postValue("No se pudo identificar al usuario actual.");
            _isLoadingLiveData.postValue(false);
            _gamesListLiveData.postValue(new ArrayList<>()); // Lista vacía
            return;
        }

        _isLoadingLiveData.setValue(true);
        _errorLiveData.setValue(null); // Limpiar error anterior

        if (currentGamesByUserIdApiLiveData != null && gamesByUserIdObserver != null && currentGamesByUserIdApiLiveData.hasObservers()) {
            currentGamesByUserIdApiLiveData.removeObserver(gamesByUserIdObserver);
        }

        Log.d(TAG, "Fetching games for user ID: " + currentUserId);
        currentGamesByUserIdApiLiveData = gameRepository.getGamesByUserId(currentUserId);

        gamesByUserIdObserver = games -> {
            if (currentGamesByUserIdApiLiveData != null) currentGamesByUserIdApiLiveData.removeObserver(gamesByUserIdObserver);
            _isLoadingLiveData.setValue(false);
            if (games != null) {
                _gamesListLiveData.postValue(games); // Postea List<GameResponseDTO>
                Log.d(TAG, "Juegos del usuario obtenidos: " + games.size() + " juegos.");
            } else if (_errorLiveData.getValue() == null) {
                _errorLiveData.postValue("No se pudieron cargar los juegos del usuario.");
            }
        };
        currentGamesByUserIdApiLiveData.observeForever(gamesByUserIdObserver);
    }


    public void clearFetchGamesError() {
        _errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentGamesByUserIdApiLiveData != null && gamesByUserIdObserver != null) {
            currentGamesByUserIdApiLiveData.removeObserver(gamesByUserIdObserver);
        }
        if (currentAllGamesApiLiveData != null && allGamesObserver != null) {
            currentAllGamesApiLiveData.removeObserver(allGamesObserver);
        }
        if (gamesByUserIdErrorObserver != null) {
            gameRepository.getFetchGamesByUserIdErrorLiveData().removeObserver(gamesByUserIdErrorObserver);
        }
        if (allGamesErrorObserver != null) {
            gameRepository.getFetchAllGamesErrorLiveData().removeObserver(allGamesErrorObserver);
        }
    }
}