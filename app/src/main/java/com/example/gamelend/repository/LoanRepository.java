package com.example.gamelend.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamelend.dto.ErrorResponseDTO;
import com.example.gamelend.dto.LoanRequestDTO;
import com.example.gamelend.dto.LoanResponseDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.remote.api.ApiService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList; // Para el método requestLoan si devuelve LiveData y falla

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoanRepository {
    private static final String TAG = "LoanRepository";
    private ApiService apiService;
    private Context appContext;

    private MutableLiveData<String> loanRequestErrorLiveData = new MutableLiveData<>();

    public LoanRepository(Context context) {
        this.appContext = context.getApplicationContext(); // Usar applicationContext para evitar fugas
        this.apiService = ApiClient.getRetrofitInstance(appContext).create(ApiService.class);
    }

    public LiveData<String> getLoanRequestErrorLiveData() {
        return loanRequestErrorLiveData;
    }

    /**
     * Devuelve el objeto Call para solicitar un préstamo.
     * El ViewModel se encargará de hacer enqueue y manejar el Callback.
     * @param loanRequest DTO con la información de la solicitud (ej. gameId).
     * @return Call<LoanResponseDTO> para ejecutar la petición.
     */
    public Call<LoanResponseDTO> requestLoanApiCall(LoanRequestDTO loanRequest) {
        if (loanRequest == null || loanRequest.getGameId() == null || loanRequest.getGameId() <= 0) {
            Log.e(TAG, "LoanRequestDTO o Game ID inválido para requestLoanApiCall: " + loanRequest);
        }
        return apiService.requestLoan(loanRequest);
    }


    /**
     * Solicita un préstamo para un juego.
     * @param loanRequest DTO con la información de la solicitud.
     * @return LiveData que emitirá LoanResponseDTO si la solicitud es exitosa,
     * o null en caso de error (el error específico se posteará a loanRequestErrorLiveData).
     */
    public LiveData<LoanResponseDTO> requestLoanWithLiveData(LoanRequestDTO loanRequest) {
        MutableLiveData<LoanResponseDTO> loanResultLiveData = new MutableLiveData<>();
        loanRequestErrorLiveData.postValue(null); // Limpiar error anterior

        if (loanRequest == null || loanRequest.getGameId() == null || loanRequest.getGameId() <= 0) {
            Log.e(TAG, "LoanRequestDTO o Game ID inválido para solicitar préstamo: " + loanRequest);
            loanRequestErrorLiveData.postValue("Datos de solicitud de préstamo no válidos.");
            loanResultLiveData.postValue(null);
            return loanResultLiveData;
        }

        apiService.requestLoan(loanRequest).enqueue(new Callback<LoanResponseDTO>() {
            @Override
            public void onResponse(Call<LoanResponseDTO> call, Response<LoanResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Solicitud de préstamo exitosa (LiveData). Loan ID: " + response.body().getId());
                    loanResultLiveData.postValue(response.body());
                } else {
                    String errorMessage = "Error al solicitar préstamo (Cód: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyStr = response.errorBody().string();
                            Log.e(TAG, "Cuerpo del error de solicitud de préstamo: " + errorBodyStr);
                            Gson gson = new Gson();
                            ErrorResponseDTO errorResponse = gson.fromJson(errorBodyStr, ErrorResponseDTO.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                errorMessage = errorResponse.getMessage();
                            } else if (errorResponse != null && errorResponse.getDetails() != null && !errorResponse.getDetails().isEmpty()){
                                errorMessage = String.join("; ", errorResponse.getDetails());
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error al parsear errorBody de solicitud de préstamo", e);
                        }
                    }
                    loanRequestErrorLiveData.postValue(errorMessage);
                    loanResultLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<LoanResponseDTO> call, Throwable t) {
                Log.e(TAG, "Fallo de conexión al solicitar préstamo (LiveData): " + t.getMessage(), t);
                loanRequestErrorLiveData.postValue("Error de conexión al solicitar préstamo: " + t.getMessage());
                loanResultLiveData.postValue(null);
            }
        });
        return loanResultLiveData;
    }
}