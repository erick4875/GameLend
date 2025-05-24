package com.example.gamelend.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelend.R;
import com.example.gamelend.dto.GameSummaryDTO; // DTO de la API
import com.example.gamelend.models.Game;         // Modelo local para la UI (com.example.gamelend.models.Game)
import com.example.gamelend.models.GameAdapter;
import com.example.gamelend.models.GameStatus; // El enum de tu paquete models
import com.example.gamelend.viewmodel.GameListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Para el mapeo con streams (requiere API 24+)

public class GameListActivity extends AppCompatActivity {

    private static final String TAG = "GameListActivity";

    private RecyclerView gamesRecyclerView;
    private GameAdapter gameAdapter;
    private ProgressBar loadingProgressBar;

    private GameListViewModel gameListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        Toolbar toolbar = findViewById(R.id.gameListToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        gamesRecyclerView = findViewById(R.id.gamesRecyclerView);
        loadingProgressBar = findViewById(R.id.gameListProgressBar);

        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        gamesRecyclerView.setHasFixedSize(true);

        // Inicializar adaptador con una lista vacía de 'models.Game'
        // El constructor de GameAdapter espera List<com.example.gamelend.models.Game>
        gameAdapter = new GameAdapter(this, new ArrayList<Game>() /*, listener */);
        gamesRecyclerView.setAdapter(gameAdapter);

        gameListViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(GameListViewModel.class);

        setupViewModelObservers();

        Log.d(TAG, "Solicitando la lista de juegos...");
        gameListViewModel.fetchAllGames();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewModelObservers() {
        gameListViewModel.isLoadingLiveData.observe(this, isLoading -> {
            Log.d(TAG, "isLoading LiveData changed: " + isLoading);
            if (isLoading != null) {
                loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                gamesRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            }
        });

        gameListViewModel.gamesListLiveData.observe(this, gameSummaryDTOs -> {
            // gameSummaryDTOs es List<GameSummaryDTO>
            if (gameSummaryDTOs != null) {
                Log.d(TAG, "gamesListLiveData (DTOs) changed, count: " + gameSummaryDTOs.size());

                // === MAPEAR List<GameSummaryDTO> a List<com.example.gamelend.models.Game> ===
                List<Game> uiGameList = new ArrayList<>();
                for (GameSummaryDTO dto : gameSummaryDTOs) {
                    if (dto != null) {
                        // Asumimos que GameSummaryDTO tiene getTitle().
                        // Para la imagen, GameSummaryDTO no tiene un resource ID.
                        // Si GameSummaryDTO tuviera una imageUrl, la pasarías al constructor de models.Game
                        // y GameAdapter usaría Glide. Por ahora, usamos un placeholder.
                        // También, GameSummaryDTO tiene 'platform' y 'status'. Si quieres mostrarlos,
                        // tu clase 'models.Game' necesitaría esos campos y un constructor que los acepte.

                        String title = dto.getTitle() != null ? dto.getTitle() : "Título no disponible";
                        // String platform = dto.getPlatform(); // Si GameSummaryDTO tiene getPlatform()
                        // com.example.gamelend.models.GameStatus status = mapDtoStatusToModelStatus(dto.getStatus()); // Necesitarías un método de mapeo para el enum

                        // Usando el constructor actual de models.Game(String name, int imageResourceId)
                        uiGameList.add(new Game(title, R.drawable.mando)); // R.drawable.mando es un placeholder
                    }
                }
                // =======================================================================
                gameAdapter.submitList(uiGameList); // Pasar la lista mapeada (List<models.Game>) al adaptador
            } else {
                gameAdapter.submitList(new ArrayList<>()); // Enviar lista vacía si los DTOs son nulos
            }
        });

        gameListViewModel.errorLiveData.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "errorMessage LiveData changed: " + error);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                gameListViewModel.clearFetchGamesError();
            }
        });
    }

    // Método de ejemplo para mapear el enum de estado del DTO al enum del modelo (si son diferentes)
    // Necesitarías que tu GameSummaryDTO tenga un getter para su GameStatus.
    /*
    private com.example.gamelend.models.GameStatus mapDtoStatusToModelStatus(org.project.group5.gamelend.entity.Game.GameStatus dtoStatus) {
        if (dtoStatus == null) return com.example.gamelend.models.GameStatus.UNAVAILABLE; // O un valor por defecto
        switch (dtoStatus) {
            case AVAILABLE:
                return com.example.gamelend.models.GameStatus.AVAILABLE;
            case BORROWED:
                return com.example.gamelend.models.GameStatus.BORROWED;
            case UNAVAILABLE:
            default:
                return com.example.gamelend.models.GameStatus.UNAVAILABLE;
        }
    }
    */
}
