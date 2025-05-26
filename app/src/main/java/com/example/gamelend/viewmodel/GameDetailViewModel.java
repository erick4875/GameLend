package com.example.gamelend.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.gamelend.dto.GameResponseDTO;
import com.example.gamelend.repository.GameRepository;
// Importa LoanDTO y LoanRepository si manejas la solicitud de préstamo aquí
// import com.example.gamelend.dto.LoanDTO;
// import com.example.gamelend.repository.LoanRepository;

public class GameDetailViewModel extends AndroidViewModel {

    private static final String TAG = "GameDetailViewModel";

    private GameRepository gameRepository;
    // private LoanRepository loanRepository; // Para solicitar préstamos

    private final MutableLiveData<GameResponseDTO> _gameDetails = new MutableLiveData<>();
    public final LiveData<GameResponseDTO> gameDetails = _gameDetails;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    // Para el resultado de la solicitud de préstamo
    private final MutableLiveData<Boolean> _loanRequestSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> loanRequestSuccess = _loanRequestSuccess;


    private Observer<GameResponseDTO> gameDetailsObserver;
    private LiveData<GameResponseDTO> currentGameDetailsLiveData;

    public GameDetailViewModel(Application application) {
        super(application);
        this.gameRepository = new GameRepository(application.getApplicationContext());
        // this.loanRepository = new LoanRepository(application.getApplicationContext());

        // Observar errores de carga de detalles del GameRepository
        this.gameRepository.getGameDetailErrorLiveData().observeForever(errorMsg -> {
            if (errorMsg != null && Boolean.TRUE.equals(_isLoading.getValue())) {
                _isLoading.setValue(false);
                _errorMessage.postValue(errorMsg);
                _gameDetails.postValue(null);
            }
        });

        // Observar errores de solicitud de préstamo del LoanRepository (si lo tienes)
        /*
        this.loanRepository.getLoanRequestErrorLiveData().observeForever(errorMsg -> {
             if (errorMsg != null && Boolean.TRUE.equals(_isLoading.getValue())) { // Podrías necesitar un isLoading separado para préstamos
                _isLoading.setValue(false);
                _errorMessage.postValue(errorMsg);
                _loanRequestSuccess.postValue(false);
            }
        });
        */
    }

    public void fetchGameDetails(Long gameId) {
        if (gameId == null || gameId <= 0) {
            _errorMessage.postValue("ID de juego inválido.");
            _gameDetails.postValue(null);
            return;
        }

        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _gameDetails.setValue(null); // Limpiar datos anteriores

        if (currentGameDetailsLiveData != null && gameDetailsObserver != null && currentGameDetailsLiveData.hasObservers()) {
            currentGameDetailsLiveData.removeObserver(gameDetailsObserver);
        }

        currentGameDetailsLiveData = gameRepository.getGameDetails(gameId);

        gameDetailsObserver = new Observer<GameResponseDTO>() {
            @Override
            public void onChanged(GameResponseDTO gameResponse) {
                if (currentGameDetailsLiveData != null) {
                    currentGameDetailsLiveData.removeObserver(this);
                }
                // _isLoading.setValue(false); // Se maneja en el observador de error del repo también

                if (gameResponse != null) {
                    _isLoading.setValue(false); // Detener carga en caso de éxito
                    Log.d(TAG, "Detalles del juego obtenidos: " + gameResponse.getTitle());
                    _gameDetails.postValue(gameResponse);
                } else {
                    // El error ya debería haber sido posteado por la observación
                    // de gameRepository.getGameDetailErrorLiveData().
                    if (_errorMessage.getValue() == null) {
                        _isLoading.setValue(false); // Asegurar que el loading se detenga
                        _errorMessage.postValue("Error desconocido al cargar detalles del juego.");
                    }
                }
            }
        };
        currentGameDetailsLiveData.observeForever(gameDetailsObserver);
    }

    public void requestLoan(Long gameId /*, otros datos necesarios para LoanDTO */) {
        _isLoading.setValue(true); // Podrías tener un _isRequestingLoanLiveData
        _errorMessage.setValue(null);
        _loanRequestSuccess.setValue(null);

        // TODO: Obtener el ID del usuario actual (borrowerId) desde TokenManager
        // Long borrowerId = tokenManager.getUserId();
        // if (borrowerId == null) { ... manejar error ... }

        // Crear el LoanDTO
        // LoanDTO loanRequest = new LoanDTO(null, gameId, null /*lenderId se establece en backend?*/,
        //                                 borrowerId, /*fechas, notas*/);

        // Llamar a loanRepository.requestLoan(loanRequest)
        /*
        LiveData<Boolean> loanApiResponse = loanRepository.requestLoan(loanRequest); // Asume que devuelve LiveData<Boolean>
        loanApiResponse.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                loanApiResponse.removeObserver(this);
                _isLoading.setValue(false);
                _loanRequestSuccess.postValue(success);
                if (Boolean.FALSE.equals(success) && _errorMessage.getValue() == null) {
                    _errorMessage.postValue("No se pudo solicitar el préstamo.");
                }
            }
        });
        */
        // Placeholder
        Log.d(TAG, "Lógica para solicitar préstamo para el juego ID: " + gameId + " pendiente.");
        // Simular éxito/fallo para probar la UI
        new android.os.Handler().postDelayed(() -> {
            _isLoading.setValue(false);
            _loanRequestSuccess.postValue(true); // Simular éxito
            // _errorMessage.postValue("Error simulado al solicitar préstamo."); // Simular error
        }, 1500);

    }


    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentGameDetailsLiveData != null && gameDetailsObserver != null && currentGameDetailsLiveData.hasObservers()) {
            currentGameDetailsLiveData.removeObserver(gameDetailsObserver);
        }
        // Des-observar otros LiveData del repositorio si es necesario
    }
}


