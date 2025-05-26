package com.example.gamelend.activities;

import android.content.Intent; // Para iniciar GameDetailActivity
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
// import com.example.gamelend.models.GameStatus; // Si lo usas en el mapeo
import com.example.gamelend.viewmodel.GameListViewModel;

import java.util.ArrayList;
import java.util.List;
// import java.util.stream.Collectors; // Si usas streams para mapear

public class GameListActivity extends AppCompatActivity implements GameAdapter.OnGameItemClickListener {

    private static final String TAG = "GameListActivity";

    private RecyclerView gamesRecyclerView;
    private GameAdapter gameAdapter;
    private ProgressBar loadingProgressBar;
    private GameListViewModel gameListViewModel;

    public static final String EXTRA_GAME_ID = "com.example.gamelend.GAME_ID"; // Constante para el extra del Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list); // Tu XML actualizado

        Toolbar toolbar = findViewById(R.id.gameListToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Muestra la flecha "atrás"
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        gamesRecyclerView = findViewById(R.id.gamesRecyclerView);
        loadingProgressBar = findViewById(R.id.gameListProgressBar);

        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        gamesRecyclerView.setHasFixedSize(true);

        gameAdapter = new GameAdapter(this, new ArrayList<Game>(), this);
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
            if (gameSummaryDTOs != null) {
                Log.d(TAG, "GamesList LiveData (DTOs) changed, count: " + gameSummaryDTOs.size());
                List<Game> uiGameList = mapGameSummaryDTOsToGames(gameSummaryDTOs);
                gameAdapter.submitList(uiGameList);
            } else {
                gameAdapter.submitList(new ArrayList<>());
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

    /**
     * Mapea una lista de GameSummaryDTO (de la API) a una lista de Game (modelo de UI).
     * @param dtos Lista de GameSummaryDTO.
     * @return Lista de objetos Game para la UI.
     */
    private List<Game> mapGameSummaryDTOsToGames(List<GameSummaryDTO> dtos) {
        List<Game> uiGames = new ArrayList<>();
        if (dtos == null) return uiGames;

        for (GameSummaryDTO dto : dtos) {
            if (dto != null) {
                String title = dto.getTitle() != null ? dto.getTitle() : "Título no disponible";
                Long gameId = dto.getId(); // Asumimos que GameSummaryDTO tiene getId()

                // Usando el constructor de models.Game que ahora incluye id
                // public Game(Long id, String name, int imageResourceId)
                uiGames.add(new Game(gameId, title, R.drawable.mando)); // R.drawable.mando es un placeholder
            }
        }
        return uiGames;
    }

    /**
     * Se llama cuando un ítem de la lista de juegos es clickeado.
     * @param clickedGame El objeto 'Game' (de tu paquete models) que fue clickeado.
     */
    @Override
    public void onGameItemClick(Game clickedGame) {
        Log.d(TAG, "Juego clickeado: " + clickedGame.getName() + " (ID: " + clickedGame.getId() + ")");
        Toast.makeText(this, "Juego: " + clickedGame.getName(), Toast.LENGTH_SHORT).show();

        if (clickedGame.getId() != null) {
            Intent intent = new Intent(GameListActivity.this, GameDetailActivity.class);
            intent.putExtra(EXTRA_GAME_ID, clickedGame.getId()); // Pasar el ID del juego
            startActivity(intent);
        } else {
            Log.e(TAG, "ID del juego es null, no se puede navegar a detalles.");
            Toast.makeText(this, "No se pudo obtener el ID del juego para ver detalles.", Toast.LENGTH_SHORT).show();
        }
    }
}