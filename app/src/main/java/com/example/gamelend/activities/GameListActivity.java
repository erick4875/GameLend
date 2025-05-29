package com.example.gamelend.activities;

import android.content.Intent;
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
import com.example.gamelend.dto.GameResponseDTO; // Cambiado de GameSummaryDTO
import com.example.gamelend.models.Game;
import com.example.gamelend.models.GameAdapter;
import com.example.gamelend.viewmodel.GameListViewModel;

import java.util.ArrayList;
import java.util.List;

public class GameListActivity extends AppCompatActivity implements GameAdapter.OnGameItemClickListener {

    private static final String TAG = "GameListActivity";

    private RecyclerView gamesRecyclerView;
    private GameAdapter gameAdapter;
    private ProgressBar loadingProgressBar;
    private GameListViewModel gameListViewModel;

    public static final String EXTRA_GAME_ID = "com.example.gamelend.GAME_ID";

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

        gameAdapter = new GameAdapter(this, new ArrayList<>(), this);
        gamesRecyclerView.setAdapter(gameAdapter);

        gameListViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(GameListViewModel.class);

        setupViewModelObservers();

        // En lugar de fetchAllGames, podrías tener una lógica para decidir
        // si cargar todos los juegos o solo los de un usuario.
        // Por ahora, la modificaremos para cargar los del usuario actual si se llama
        // desde UserProfileActivity sin un EXTRA_USER_ID, o todos si se llama de otra forma.
        // O, si esta activity SIEMPRE muestra los juegos del usuario actual:
        Log.d(TAG, "Solicitando la lista de juegos del usuario actual...");
        gameListViewModel.fetchCurrentUserGames();
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

        // Observar gamesListResponseLiveData que ahora es List<GameResponseDTO>
        gameListViewModel.gamesListResponseLiveData.observe(this, gameResponseDTOs -> {
            if (gameResponseDTOs != null) {
                Log.d(TAG, "GamesList LiveData (GameResponseDTOs) changed, count: " + gameResponseDTOs.size());
                List<Game> uiGameList = mapGameResponseDTOsToGames(gameResponseDTOs);
                gameAdapter.submitList(uiGameList);
            } else {
                gameAdapter.submitList(new ArrayList<>());
            }
        });

        // Si también necesitas observar allGamesSummaryLiveData para una vista de "todos los juegos"
        // gameListViewModel.allGamesSummaryLiveData.observe(this, gameSummaryDTOs -> { ... });


        gameListViewModel.errorLiveData.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "errorMessage LiveData changed: " + error);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                gameListViewModel.clearFetchGamesError();
            }
        });
    }

    /**
     * Mapea una lista de GameResponseDTO (de la API) a una lista de Game (modelo de UI).
     * @param dtos Lista de GameResponseDTO.
     * @return Lista de objetos Game para la UI.
     */
    private List<Game> mapGameResponseDTOsToGames(List<GameResponseDTO> dtos) {
        List<Game> uiGames = new ArrayList<>();
        if (dtos == null) return uiGames;

        for (GameResponseDTO dto : dtos) {
            if (dto != null) {
                String title = dto.getTitle() != null ? dto.getTitle() : "Título no disponible";
                Long gameId = dto.getId();
                // Asumiendo que tu modelo Game de UI necesita id, título e imagen de placeholder
                uiGames.add(new Game(gameId, title, R.drawable.mando));
            }
        }
        return uiGames;
    }

    @Override
    public void onGameItemClick(Game clickedGame) {
        Log.d(TAG, "Juego clickeado: " + clickedGame.getName() + " (ID: " + clickedGame.getId() + ")");
        if (clickedGame.getId() != null) {
            Intent intent = new Intent(GameListActivity.this, GameDetailActivity.class);
            intent.putExtra(EXTRA_GAME_ID, clickedGame.getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "No se pudo obtener el ID del juego.", Toast.LENGTH_SHORT).show();
        }
    }
}