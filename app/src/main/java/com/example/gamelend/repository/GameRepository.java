package com.example.gamelend.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamelend.dto.GameDTO;
import com.example.gamelend.dto.GameResponseDTO;
import com.example.gamelend.dto.GameSummaryDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.remote.api.ApiService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameRepository {
    private static final String TAG = "GameRepository";
    private ApiService apiService;
    private Context appContext;

    private MutableLiveData<String> fetchAllGamesErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> fetchGamesByUserIdErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> createGameErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> gameDetailErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> updateGameErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> deleteGameErrorLiveData = new MutableLiveData<>();


    public GameRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.apiService = ApiClient.getRetrofitInstance(appContext).create(ApiService.class);
    }

    public LiveData<String> getFetchAllGamesErrorLiveData() { return fetchAllGamesErrorLiveData; }
    public LiveData<String> getFetchGamesByUserIdErrorLiveData() { return fetchGamesByUserIdErrorLiveData; }
    public LiveData<String> getCreateGameErrorLiveData() { return createGameErrorLiveData; }
    public LiveData<String> getGameDetailErrorLiveData() { return gameDetailErrorLiveData; }
    public LiveData<String> getUpdateGameErrorLiveData() { return updateGameErrorLiveData; }
    public LiveData<String> getDeleteGameErrorLiveData() { return deleteGameErrorLiveData; }


    public LiveData<List<GameSummaryDTO>> getAllGames() {
        MutableLiveData<List<GameSummaryDTO>> gamesListLiveData = new MutableLiveData<>();
        fetchAllGamesErrorLiveData.postValue(null);
        apiService.getAllGames().enqueue(new Callback<List<GameSummaryDTO>>() {
            @Override
            public void onResponse(Call<List<GameSummaryDTO>> call, Response<List<GameSummaryDTO>> response) {
                if (response.isSuccessful()) {
                    gamesListLiveData.postValue(response.body() != null ? response.body() : new ArrayList<>());
                } else {
                    String errorMsg = "Error al cargar todos los juegos (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try { errorMsg += ": " + response.errorBody().string(); }
                        catch (IOException e) { Log.e(TAG, "Error al parsear errorBody", e); }
                    }
                    fetchAllGamesErrorLiveData.postValue(errorMsg);
                    gamesListLiveData.postValue(new ArrayList<>());
                }
            }
            @Override
            public void onFailure(Call<List<GameSummaryDTO>> call, Throwable t) {
                fetchAllGamesErrorLiveData.postValue("Fallo de red al cargar todos los juegos: " + t.getMessage());
                gamesListLiveData.postValue(new ArrayList<>());
            }
        });
        return gamesListLiveData;
    }

    public LiveData<List<GameResponseDTO>> getGamesByUserId(Long userId) {
        MutableLiveData<List<GameResponseDTO>> gamesListLiveData = new MutableLiveData<>();
        fetchGamesByUserIdErrorLiveData.postValue(null);

        if (userId == null || userId <= 0) { // Mejorar la validación del ID
            Log.e(TAG, "User ID es null o inválido, no se pueden obtener los juegos.");
            fetchGamesByUserIdErrorLiveData.postValue("ID de usuario no válido.");
            gamesListLiveData.postValue(new ArrayList<>());
            return gamesListLiveData;
        }

        apiService.getGamesByUserId(userId).enqueue(new Callback<List<GameResponseDTO>>() {
            @Override
            public void onResponse(Call<List<GameResponseDTO>> call, Response<List<GameResponseDTO>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Juegos por userId obtenidos, código: " + response.code());
                    gamesListLiveData.postValue(response.body() != null ? response.body() : new ArrayList<>());
                } else {
                    String errorMsg = "Error al cargar juegos del usuario (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try { errorMsg += ": " + response.errorBody().string(); }
                        catch (IOException e) { Log.e(TAG, "Error al parsear errorBody", e); }
                    }
                    Log.e(TAG, errorMsg);
                    fetchGamesByUserIdErrorLiveData.postValue(errorMsg);
                    gamesListLiveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<GameResponseDTO>> call, Throwable t) {
                Log.e(TAG, "Fallo de red al cargar juegos del usuario: " + t.getMessage(), t);
                fetchGamesByUserIdErrorLiveData.postValue("Fallo de red al cargar juegos del usuario: " + t.getMessage());
                gamesListLiveData.postValue(new ArrayList<>());
            }
        });
        return gamesListLiveData;
    }

    /**
     * Obtiene los detalles de un juego específico por su ID.
     * @param gameId El ID del juego.
     * @return LiveData que emitirá GameResponseDTO si la llamada es exitosa,
     * o null en caso de error (el error específico se posteará a gameDetailErrorLiveData).
     */
    public LiveData<GameResponseDTO> getGameDetailsById(Long gameId) {
        MutableLiveData<GameResponseDTO> gameDetailLiveData = new MutableLiveData<>();
        gameDetailErrorLiveData.postValue(null); // Limpiar error anterior

        if (gameId == null || gameId <= 0) {
            Log.e(TAG, "ID de juego inválido para getGameDetailsById: " + gameId);
            gameDetailErrorLiveData.postValue("ID de juego no válido.");
            gameDetailLiveData.postValue(null);
            return gameDetailLiveData;
        }

        Log.d(TAG, "GameRepository: Fetching details for game ID: " + gameId);
        apiService.getGameDetailsById(gameId).enqueue(new Callback<GameResponseDTO>() {
            @Override
            public void onResponse(Call<GameResponseDTO> call, Response<GameResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "GameRepository: Detalles del juego obtenidos para ID " + gameId + ": " + response.body().getTitle());
                    gameDetailLiveData.postValue(response.body());
                } else {
                    String errorMessage = "Error al cargar detalles del juego (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyStr = response.errorBody().string();
                            Log.e(TAG, "GameRepository: Cuerpo del error de detalles del juego: " + errorBodyStr);
                        } catch (IOException e) {
                            Log.e(TAG, "GameRepository: Error al parsear errorBody de detalles del juego", e);
                        }
                    }
                    gameDetailErrorLiveData.postValue(errorMessage);
                    gameDetailLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<GameResponseDTO> call, Throwable t) {
                Log.e(TAG, "GameRepository: Fallo de red al cargar detalles del juego ID " + gameId + ": " + t.getMessage(), t);
                gameDetailErrorLiveData.postValue("Error de conexión al cargar detalles del juego: " + t.getMessage());
                gameDetailLiveData.postValue(null);
            }
        });
        return gameDetailLiveData;
    }

    public LiveData<GameResponseDTO> createGame(GameDTO gameDTO) {
        MutableLiveData<GameResponseDTO> resultLiveData = new MutableLiveData<>();
        createGameErrorLiveData.postValue(null);

        apiService.createGame(gameDTO).enqueue(new Callback<GameResponseDTO>() {
            @Override
            public void onResponse(Call<GameResponseDTO> call, Response<GameResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    resultLiveData.postValue(response.body());
                    Log.d(TAG, "Juego creado exitosamente: " + response.body().getTitle());
                } else {
                    String errorMsg = "Error al crear juego (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ": " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Error leyendo errorBody al crear juego", e);
                        }
                    }
                    createGameErrorLiveData.postValue(errorMsg);
                    resultLiveData.postValue(null);
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<GameResponseDTO> call, Throwable t) {
                String errorMsg = "Fallo de red al crear juego: " + t.getMessage();
                createGameErrorLiveData.postValue(errorMsg);
                resultLiveData.postValue(null);
                Log.e(TAG, errorMsg, t);
            }
        });
        return resultLiveData;
    }
}