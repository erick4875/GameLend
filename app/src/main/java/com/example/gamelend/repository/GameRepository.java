package com.example.gamelend.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamelend.dto.GameDTO; // Para createGame
import com.example.gamelend.dto.GameResponseDTO;
import com.example.gamelend.dto.GameSummaryDTO; // Para getAllGames
import com.example.gamelend.dto.ErrorResponseDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.remote.api.ApiService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameRepository {
    private static final String TAG = "GameRepository";
    private ApiService apiService;

    // LiveData para errores de la operación createGame
    private MutableLiveData<String> createGameErrorLiveData = new MutableLiveData<>();
    // LiveData para errores de la operación getGameDetails
    private MutableLiveData<String> gameDetailErrorLiveData = new MutableLiveData<>();
    // LiveData para errores de la operación getAllGames
    private MutableLiveData<String> gameListErrorLiveData = new MutableLiveData<>();


    public GameRepository(Context context) {
        this.apiService = ApiClient.getRetrofitInstance(context.getApplicationContext()).create(ApiService.class);
    }

    // Getters para los LiveData de error
    public LiveData<String> getCreateGameErrorLiveData() {
        return createGameErrorLiveData;
    }
    public LiveData<String> getGameDetailErrorLiveData() {
        return gameDetailErrorLiveData;
    }
    public LiveData<String> getGameListErrorLiveData() {
        return gameListErrorLiveData;
    }


    public LiveData<GameResponseDTO> createGame(GameDTO gameDTO) {
        MutableLiveData<GameResponseDTO> creationResultLiveData = new MutableLiveData<>();
        createGameErrorLiveData.postValue(null); // Limpiar error anterior

        apiService.createGame(gameDTO).enqueue(new Callback<GameResponseDTO>() {
            @Override
            public void onResponse(Call<GameResponseDTO> call, Response<GameResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    creationResultLiveData.postValue(response.body());
                } else {
                    String errorMessage = "Error al crear juego (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            Log.e(TAG, "Cuerpo del error al crear juego: " + errorBodyString);
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyString, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getDetails() != null && !errorResponse.getDetails().isEmpty()) {
                                errorMessage = String.join("\n", errorResponse.getDetails());
                            } else if (errorResponse != null && errorResponse.getMessage() != null) {
                                errorMessage = errorResponse.getMessage();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error al parsear errorBody al crear juego", e);
                        }
                    }
                    createGameErrorLiveData.postValue(errorMessage);
                    creationResultLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<GameResponseDTO> call, Throwable t) {
                Log.e(TAG, "Fallo en conexión al crear juego: " + t.getMessage(), t);
                createGameErrorLiveData.postValue("Error de conexión al crear juego: " + t.getMessage());
                creationResultLiveData.postValue(null);
            }
        });
        return creationResultLiveData;
    }

    /**
     * Obtiene los detalles de un juego específico por su ID.
     * El AuthInterceptor se encarga de añadir el token de acceso si es necesario.
     * @param gameId El ID del juego a obtener.
     * @return LiveData que emitirá el GameResponseDTO con los detalles del juego,
     * o null en caso de error (el error específico se posteará a gameDetailErrorLiveData).
     */
    public LiveData<GameResponseDTO> getGameDetails(Long gameId) {
        MutableLiveData<GameResponseDTO> gameDetailResultLiveData = new MutableLiveData<>();
        gameDetailErrorLiveData.postValue(null); // Limpiar error anterior

        // Asumimos que ApiService tiene: Call<GameResponseDTO> getGameDetailsById(@Path("id") Long gameId);
        apiService.getGameDetailsById(gameId).enqueue(new Callback<GameResponseDTO>() {
            @Override
            public void onResponse(Call<GameResponseDTO> call, Response<GameResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    gameDetailResultLiveData.postValue(response.body());
                } else {
                    String errorMessage = "Error al cargar detalles del juego (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            Log.e(TAG, "Cuerpo del error en detalles juego: " + errorBodyString);
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyString, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                errorMessage = errorResponse.getMessage();
                            } else if (errorResponse != null && errorResponse.getDetails() != null && !errorResponse.getDetails().isEmpty()) {
                                errorMessage = String.join("\n", errorResponse.getDetails());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear errorBody en detalles juego", e);
                        }
                    }
                    gameDetailErrorLiveData.postValue(errorMessage);
                    gameDetailResultLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<GameResponseDTO> call, Throwable t) {
                Log.e(TAG, "Fallo en conexión al cargar detalles juego: " + t.getMessage(), t);
                gameDetailErrorLiveData.postValue("Error de conexión al cargar detalles: " + t.getMessage());
                gameDetailResultLiveData.postValue(null);
            }
        });
        return gameDetailResultLiveData;
    }

    /**
     * Obtiene la lista de todos los juegos (resúmenes) desde la API.
     * El AuthInterceptor se encarga de añadir el token de acceso si es necesario.
     * @return LiveData que emitirá List<GameSummaryDTO> si la llamada es exitosa,
     * o una lista vacía en caso de error (el error específico se posteará a gameListErrorLiveData).
     */
    public LiveData<List<GameSummaryDTO>> getAllGames() {
        MutableLiveData<List<GameSummaryDTO>> gamesListResultLiveData = new MutableLiveData<>();
        gameListErrorLiveData.postValue(null); // Limpiar error anterior

        // Asumimos que ApiService tiene: Call<List<GameSummaryDTO>> getAllGames();
        apiService.getAllGames().enqueue(new Callback<List<GameSummaryDTO>>() {
            @Override
            public void onResponse(Call<List<GameSummaryDTO>> call, Response<List<GameSummaryDTO>> response) {
                if (response.isSuccessful()) { // Para GET, un 204 (No Content) también es isSuccessful
                    if (response.code() == 204 || response.body() == null) {
                        gamesListResultLiveData.postValue(new ArrayList<>()); // Lista vacía si no hay contenido
                    } else {
                        gamesListResultLiveData.postValue(response.body());
                    }
                } else {
                    String errorMessage = "Error al obtener lista de juegos (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            Log.e(TAG, "Cuerpo del error al obtener juegos: " + errorBodyString);
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyString, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                errorMessage = errorResponse.getMessage();
                            } else if (errorResponse != null && errorResponse.getDetails() != null && !errorResponse.getDetails().isEmpty()) {
                                errorMessage = String.join("\n", errorResponse.getDetails());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear errorBody al obtener juegos", e);
                        }
                    }
                    gameListErrorLiveData.postValue(errorMessage);
                    gamesListResultLiveData.postValue(new ArrayList<>()); // Lista vacía en caso de error
                }
            }

            @Override
            public void onFailure(Call<List<GameSummaryDTO>> call, Throwable t) {
                Log.e(TAG, "Fallo en conexión al obtener juegos: " + t.getMessage(), t);
                gameListErrorLiveData.postValue("Error de conexión al obtener juegos: " + t.getMessage());
                gamesListResultLiveData.postValue(new ArrayList<>());
            }
        });
        return gamesListResultLiveData;
    }

    // Aquí irían los métodos para updateGame y deleteGame si los necesitas,
    // cada uno con su propio LiveData para el resultado y para el error.
}