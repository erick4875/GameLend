package com.example.gamelend.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.dto.GameResponseDTO;
import com.example.gamelend.dto.LoanRequestDTO; // DTO para la petición de préstamo (solo gameId desde Android)
import com.example.gamelend.dto.LoanResponseDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.repository.GameRepository;
import com.example.gamelend.repository.LoanRepository;

import java.io.IOException; // Para el errorBody
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameDetailViewModel extends AndroidViewModel {

    private static final String TAG = "GameDetailViewModel";

    private GameRepository gameRepository;
    private LoanRepository loanRepository;
    private TokenManager tokenManager;

    private final MutableLiveData<GameResponseDTO> _gameDetails = new MutableLiveData<>();
    public final LiveData<GameResponseDTO> gameDetails = _gameDetails;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _loanRequestSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> loanRequestSuccess = _loanRequestSuccess;

    private final MutableLiveData<String> _loanRequestError = new MutableLiveData<>();
    public final LiveData<String> loanRequestError = _loanRequestError;

    private Observer<GameResponseDTO> gameDetailsObserver;
    private LiveData<GameResponseDTO> currentGameDetailsApiLiveData;
    private Observer<String> loanRequestErrorObserverFromRepo;


    public GameDetailViewModel(@NonNull Application application) {
        super(application);
        gameRepository = new GameRepository(application.getApplicationContext());
        loanRepository = new LoanRepository(application.getApplicationContext());
        tokenManager = ApiClient.getTokenManager(application.getApplicationContext());

        loanRequestErrorObserverFromRepo = errorMsg -> {
            if (errorMsg != null && Boolean.TRUE.equals(_isLoading.getValue())) {
                _isLoading.postValue(false);
                _loanRequestError.postValue(errorMsg);
                _loanRequestSuccess.postValue(false);
            }
        };
        // Si LoanRepository tiene un LiveData específico para errores de solicitud de préstamo, obsérvalo.
        // loanRepository.getLoanRequestErrorLiveData().observeForever(loanRequestErrorObserverFromRepo);
    }

    public void fetchGameDetails(Long gameId) {
        if (gameId == null || gameId <= 0) {
            _errorMessage.postValue("ID de juego no válido.");
            _isLoading.postValue(false);
            _gameDetails.postValue(null);
            return;
        }
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _gameDetails.setValue(null);

        if (currentGameDetailsApiLiveData != null && gameDetailsObserver != null && currentGameDetailsApiLiveData.hasObservers()) {
            currentGameDetailsApiLiveData.removeObserver(gameDetailsObserver);
        }
        Log.d(TAG, "Fetching game details for ID: " + gameId);
        currentGameDetailsApiLiveData = gameRepository.getGameDetailsById(gameId);

        gameDetailsObserver = gameResponse -> {
            if (currentGameDetailsApiLiveData != null) {
                currentGameDetailsApiLiveData.removeObserver(gameDetailsObserver);
            }
            _isLoading.setValue(false);
            if (gameResponse != null) {
                _gameDetails.postValue(gameResponse);
                Log.d(TAG, "Game details loaded: " + gameResponse.getTitle());
            } else {
                if (_errorMessage.getValue() == null) {
                    _errorMessage.postValue("No se pudieron cargar los detalles del juego.");
                }
                _gameDetails.postValue(null);
                Log.d(TAG, "Game details were null after fetch.");
            }
        };
        currentGameDetailsApiLiveData.observeForever(gameDetailsObserver);
    }

    /**
     * Inicia una solicitud para tomar prestado un juego.
     * @param gameId El ID del juego a solicitar.
     * @param lenderId El ID del usuario que posee el juego.
     */
    public void requestLoan(Long gameId, Long lenderId) { // <-- CORREGIDO: Eliminado borrowerId como parámetro
        Long borrowerId = tokenManager.getUserId(); // Obtener borrowerId aquí

        if (gameId == null || lenderId == null || borrowerId == null || borrowerId == 0L) {
            String errorMsg = "IDs inválidos para la solicitud de préstamo. GameID: " + gameId + ", LenderID: " + lenderId + ", BorrowerID: " + borrowerId;
            Log.e(TAG, errorMsg);
            _loanRequestError.postValue(errorMsg);
            _isLoading.postValue(false);
            return;
        }
        if (lenderId.equals(borrowerId)) {
            _loanRequestError.postValue("No puedes solicitar un préstamo de tu propio juego.");
            _isLoading.postValue(false);
            return;
        }

        _isLoading.setValue(true);
        _loanRequestError.setValue(null);
        _loanRequestSuccess.setValue(null);

        LoanRequestDTO androidLoanRequest = new LoanRequestDTO(gameId);

        Log.d(TAG, "Solicitando préstamo para juego ID: " + gameId + " (Lender: " + lenderId + ", Borrower: " + borrowerId + ")");

        Call<LoanResponseDTO> call = loanRepository.requestLoanApiCall(androidLoanRequest);

        if (call != null) {
            call.enqueue(new Callback<LoanResponseDTO>() {
                @Override
                public void onResponse(@NonNull Call<LoanResponseDTO> call, @NonNull Response<LoanResponseDTO> response) {
                    _isLoading.postValue(false);
                    if (response.isSuccessful() && response.body() != null) {
                        _loanRequestSuccess.postValue(true);
                        Log.d(TAG, "Solicitud de préstamo exitosa, respuesta: " + response.body().getId());
                        fetchGameDetails(gameId); // Refrescar detalles para actualizar estado del juego
                    } else {
                        String errorMsg = "Error al solicitar el préstamo (Cód: " + response.code() + ")";
                        if(response.errorBody() != null) {
                            try { errorMsg += ": " + response.errorBody().string(); }
                            catch (IOException e) { Log.e(TAG, "Error al leer errorBody", e); }
                        }
                        Log.e(TAG, errorMsg);
                        _loanRequestError.postValue(errorMsg);
                        _loanRequestSuccess.postValue(false);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LoanResponseDTO> call, @NonNull Throwable t) {
                    _isLoading.postValue(false);
                    Log.e(TAG, "Fallo de red en solicitud de préstamo: " + t.getMessage(), t);
                    _loanRequestError.postValue("Error de red: " + t.getMessage());
                    _loanRequestSuccess.postValue(false);
                }
            });
        } else {
            _isLoading.postValue(false);
            _loanRequestError.postValue("No se pudo iniciar la solicitud de préstamo (llamada nula desde el repositorio).");
        }
    }

    public void clearErrorMessage() { _errorMessage.setValue(null); }
    public void clearLoanRequestError() { _loanRequestError.setValue(null); }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentGameDetailsApiLiveData != null && gameDetailsObserver != null && currentGameDetailsApiLiveData.hasObservers()) {
            currentGameDetailsApiLiveData.removeObserver(gameDetailsObserver);
        }
        // if (loanRequestErrorObserverFromRepo != null && loanRepository.getLoanRequestErrorLiveData().hasObservers()) {
        //    loanRepository.getLoanRequestErrorLiveData().removeObserver(loanRequestErrorObserverFromRepo);
        // }
    }
}