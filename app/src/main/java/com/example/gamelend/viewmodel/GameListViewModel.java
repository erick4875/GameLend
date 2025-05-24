package com.example.gamelend.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer; // Asegúrate de tener este import si lo usas
import com.example.gamelend.dto.GameSummaryDTO;
import com.example.gamelend.repository.GameRepository;
import com.example.gamelend.dto.ErrorResponseDTO; // Para parsear errores
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameListViewModel extends AndroidViewModel {

    private static final String TAG = "GameListViewModel";

    private GameRepository gameRepository;

    private final MutableLiveData<List<GameSummaryDTO>> _gamesListLiveData = new MutableLiveData<>();
    public final LiveData<List<GameSummaryDTO>> gamesListLiveData = _gamesListLiveData;

    private final MutableLiveData<String> _errorLiveData = new MutableLiveData<>();
    public final LiveData<String> errorLiveData = _errorLiveData;

    private final MutableLiveData<Boolean> _isLoadingLiveData = new MutableLiveData<>();
    public final LiveData<Boolean> isLoadingLiveData = _isLoadingLiveData;

    public GameListViewModel(Application application) {
        super(application);
        this.gameRepository = new GameRepository(application.getApplicationContext());

        // Si GameRepository expone un LiveData de error específico para la lista de juegos,
        // podrías observarlo aquí. Si no, el manejo de error se hace en el callback.
        /*
        gameListErrorObserver = errorMsg -> {
            if (errorMsg != null) {
                Log.d(TAG, "Error recibido del GameRepository: " + errorMsg);
                _isLoadingLiveData.postValue(false);
                _errorLiveData.postValue(errorMsg);
                _gamesListLiveData.postValue(new ArrayList<>());
            }
        };
        gameRepository.getGameListErrorLiveData().observeForever(gameListErrorObserver); // Asume que existe este método
        */
    }

    public void fetchAllGames() { // Renombrado de fetchGameSummaries para consistencia
        _isLoadingLiveData.setValue(true);
        _errorLiveData.setValue(null);
        // _gamesListLiveData.setValue(null); // Opcional

        // Remover observador anterior si currentGameListApiLiveData y gamesListApiObserver son campos
        // y se reutilizan. Si Call es nuevo cada vez, no es necesario para el Call en sí.

        Call<List<GameSummaryDTO>> call = gameRepository.getAllGames();

        call.enqueue(new Callback<List<GameSummaryDTO>>() {
            @Override
            public void onResponse(Call<List<GameSummaryDTO>> call, Response<List<GameSummaryDTO>> response) {
                _isLoadingLiveData.postValue(false);

                if (response.isSuccessful()) {
                    if (response.code() == 204 || response.body() == null) {
                        // Servidor respondió OK pero sin contenido (lista vacía)
                        Log.d(TAG, "Lista de juegos vacía recibida (Cód: " + response.code() + ")");
                        _gamesListLiveData.postValue(new ArrayList<>()); // Enviar lista vacía, no error
                        // _errorLiveData.postValue(null); // Asegurarse que no haya mensaje de error para 204
                    } else {
                        // Éxito con contenido
                        Log.d(TAG, "Juegos obtenidos exitosamente: " + response.body().size() + " juegos.");
                        _gamesListLiveData.postValue(response.body());
                    }
                } else {
                    // Error del servidor (4xx, 5xx)
                    String errorMessage = "Error al obtener la lista de juegos (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            Log.e(TAG, "Cuerpo del error al obtener juegos: " + errorBodyString);
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyString, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getDetails() != null && !errorResponse.getDetails().isEmpty()) {
                                errorMessage = String.join("\n", errorResponse.getDetails());
                            } else if (errorResponse != null && errorResponse.getMessage() != null) {
                                errorMessage = errorResponse.getMessage();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error al parsear errorBody al obtener juegos", e);
                        }
                    }
                    _errorLiveData.postValue(errorMessage);
                    _gamesListLiveData.postValue(new ArrayList<>()); // Lista vacía en caso de error
                }
            }

            @Override
            public void onFailure(Call<List<GameSummaryDTO>> call, Throwable t) {
                _isLoadingLiveData.postValue(false);
                Log.e(TAG, "Fallo en conexión al obtener juegos: " + t.getMessage(), t);
                _errorLiveData.postValue("Error de conexión al obtener juegos: " + t.getMessage());
                _gamesListLiveData.postValue(new ArrayList<>());
            }
        });
    }

    public void clearFetchGamesError() {
        _errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Limpiar observadores si se usó observeForever con campos de instancia
        /*
        if (currentGameListApiLiveData != null && gamesListApiObserver != null && currentGameListApiLiveData.hasObservers()) {
            currentGameListApiLiveData.removeObserver(gamesListApiObserver);
        }
        if (gameListErrorObserver != null && gameRepository.getGameListErrorLiveData().hasObservers()) { // Asume que existe
            gameRepository.getGameListErrorLiveData().removeObserver(gameListErrorObserver);
        }
        */
    }
}
