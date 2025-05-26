package com.example.gamelend.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer; // Importar Observer

import com.example.gamelend.dto.GameSummaryDTO;
// ErrorResponseDTO y Gson no son necesarios aquí si el GameRepository los maneja internamente
// import com.example.gamelend.dto.ErrorResponseDTO;
// import com.google.gson.Gson;
import com.example.gamelend.repository.GameRepository;

// import java.io.IOException; // No es necesario aquí
import java.util.ArrayList;
import java.util.List;

// Retrofit Call, Callback, Response no son necesarios aquí
// import retrofit2.Call;
// import retrofit2.Callback;
// import retrofit2.Response;

public class GameListViewModel extends AndroidViewModel {

    private static final String TAG = "GameListViewModel";

    private GameRepository gameRepository;

    private final MutableLiveData<List<GameSummaryDTO>> _gamesListLiveData = new MutableLiveData<>();
    public final LiveData<List<GameSummaryDTO>> gamesListLiveData = _gamesListLiveData;

    private final MutableLiveData<String> _errorLiveData = new MutableLiveData<>();
    public final LiveData<String> errorLiveData = _errorLiveData;

    private final MutableLiveData<Boolean> _isLoadingLiveData = new MutableLiveData<>();
    public final LiveData<Boolean> isLoadingLiveData = _isLoadingLiveData;

    // Observador para el LiveData de resultado del repositorio
    private Observer<List<GameSummaryDTO>> gamesListApiObserver;
    private LiveData<List<GameSummaryDTO>> currentGamesListApiLiveData;

    // Observador para el LiveData de error del repositorio
    private Observer<String> gamesListErrorObserver;

    public GameListViewModel(Application application) {
        super(application);
        this.gameRepository = new GameRepository(application.getApplicationContext());

        // Crear el observador para los errores de carga de la lista de juegos del repositorio
        gamesListErrorObserver = errorMsg -> {
            if (errorMsg != null) {
                Log.d(TAG, "Error de carga de lista de juegos recibido del GameRepository: " + errorMsg);
                _isLoadingLiveData.postValue(false); // Detener carga si hay error del repo
                _errorLiveData.postValue(errorMsg);
                _gamesListLiveData.postValue(new ArrayList<>()); // Postear lista vacía en caso de error
            }
        };
        // Observar el LiveData de error del GameRepository
        this.gameRepository.getGameListErrorLiveData().observeForever(gamesListErrorObserver);
    }

    /**
     * Obtiene la lista de todos los juegos (resúmenes) desde el repositorio.
     */
    public void fetchAllGames() {
        _isLoadingLiveData.setValue(true);
        _errorLiveData.setValue(null);
        // _gamesListLiveData.setValue(null); // Opcional: limpiar lista anterior

        // Remover observador anterior si existe para la respuesta de la API
        if (currentGamesListApiLiveData != null && gamesListApiObserver != null && currentGamesListApiLiveData.hasObservers()) {
            currentGamesListApiLiveData.removeObserver(gamesListApiObserver);
        }

        // GameRepository.getAllGames() ahora devuelve LiveData<List<GameSummaryDTO>>
        currentGamesListApiLiveData = gameRepository.getAllGames();

        gamesListApiObserver = new Observer<List<GameSummaryDTO>>() {
            @Override
            public void onChanged(List<GameSummaryDTO> gameSummaries) {
                // Remover el observador después de la primera emisión
                if (currentGamesListApiLiveData != null) {
                    currentGamesListApiLiveData.removeObserver(this);
                }
                // El estado de carga se actualiza a false tanto en éxito como cuando el observador de error del repo se activa.
                // _isLoadingLiveData.setValue(false); // Se podría mover aquí si el errorLiveData del repo no lo hace

                if (gameSummaries != null) {
                    // Si la respuesta del repo es exitosa (incluyendo un 204 que el repo convierte a lista vacía),
                    // gameSummaries no será null.
                    _isLoadingLiveData.setValue(false); // Asegurar que el loading se detenga
                    Log.d(TAG, "Juegos obtenidos exitosamente: " + gameSummaries.size() + " juegos.");
                    _gamesListLiveData.postValue(gameSummaries);
                }
                // Si gameSummaries es null, significa que el repositorio indicó un fallo.
                // El observador de gameRepository.getGameListErrorLiveData() (en el constructor)
                // ya debería haber actualizado _errorLiveData y _isLoadingLiveData.
                else if (_errorLiveData.getValue() == null) { // Solo si el repo no posteó un error específico
                    _isLoadingLiveData.setValue(false);
                    _errorLiveData.postValue("Error desconocido al obtener la lista de juegos.");
                }
            }
        };
        currentGamesListApiLiveData.observeForever(gamesListApiObserver);
    }

    public void clearFetchGamesError() {
        _errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Limpiar observadores para evitar memory leaks
        if (currentGamesListApiLiveData != null && gamesListApiObserver != null && currentGamesListApiLiveData.hasObservers()) {
            currentGamesListApiLiveData.removeObserver(gamesListApiObserver);
        }
        if (gamesListErrorObserver != null && gameRepository.getGameListErrorLiveData().hasObservers()) {
            gameRepository.getGameListErrorLiveData().removeObserver(gamesListErrorObserver);
        }
    }
}