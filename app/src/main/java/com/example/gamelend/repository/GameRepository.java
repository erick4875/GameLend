package com.example.gamelend.repository;

import android.content.Context;

import com.example.gamelend.dto.GameDTO;
import com.example.gamelend.dto.GameResponseDTO;
import com.example.gamelend.dto.GameSummaryDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.remote.api.ApiService;

import java.util.List;

import retrofit2.Call;

public class GameRepository {
    private final ApiService apiService;

    public GameRepository(Context context) {
        apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
    }

    public Call<GameResponseDTO> createGame(GameDTO gameDTO) {
        return apiService.createGame(gameDTO);
    }

    public Call<List<GameSummaryDTO>> getAllGames() {
        return apiService.getAllGames();
    }

    public Call<GameResponseDTO> getGameById(Long id) {
        return apiService.getGameById(id);
    }

    public Call<GameResponseDTO> updateGame(Long id, GameDTO gameDTO) {
        return apiService.updateGame(id, gameDTO);
    }

    public Call<Void> deleteGame(Long id) {
        return apiService.deleteGame(id);
    }
}
