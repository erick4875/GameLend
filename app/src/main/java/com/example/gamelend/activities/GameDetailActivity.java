package com.example.gamelend.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.gamelend.R;
import com.example.gamelend.dto.GameResponseDTO;
import com.example.gamelend.models.GameStatus;
import com.example.gamelend.viewmodel.GameDetailViewModel;
import com.example.gamelend.activities.GameListActivity;

public class GameDetailActivity extends AppCompatActivity {

    private static final String TAG = "GameDetailActivity";

    private ImageView gameImageViewDetail;
    private TextView gameTitleTextView, platformTextViewDetail, genreTextViewDetail,
            statusTextViewDetail, ownerNameTextViewDetail, descriptionTextViewDetail;
    private Button requestLoanButton;
    private ProgressBar gameDetailLoadingProgressBar;

    private GameDetailViewModel gameDetailViewModel;
    private Long currentGameId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        setupToolbar();
        bindViews();
        extractGameIdFromIntent();

        if (currentGameId == null || currentGameId == -1L) {
            Toast.makeText(this, "ID de juego inválido o no especificado.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupViewModel();
        observeViewModel();

        gameDetailViewModel.fetchGameDetails(currentGameId);

        setupRequestLoanButtonListener();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.gameDetailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Puedes establecer título dinámicamente en populateUI()
        }
    }

    private void bindViews() {
        gameImageViewDetail = findViewById(R.id.gameImageViewDetail);
        gameTitleTextView = findViewById(R.id.gameTitleTextView);
        platformTextViewDetail = findViewById(R.id.platformTextViewDetail);
        genreTextViewDetail = findViewById(R.id.genreTextViewDetail);
        statusTextViewDetail = findViewById(R.id.statusTextViewDetail);
        ownerNameTextViewDetail = findViewById(R.id.ownerNameTextViewDetail);
        descriptionTextViewDetail = findViewById(R.id.descriptionTextViewDetail);
        requestLoanButton = findViewById(R.id.requestLoanButton);
        gameDetailLoadingProgressBar = findViewById(R.id.gameDetailLoadingProgressBar);
    }

    private void extractGameIdFromIntent() {
        if (getIntent() != null && getIntent().hasExtra(GameListActivity.EXTRA_GAME_ID)) {
            currentGameId = getIntent().getLongExtra(GameListActivity.EXTRA_GAME_ID, -1L);
            Log.d(TAG, "Game ID recibido: " + currentGameId);
        } else {
            Log.e(TAG, "No se recibió GAME_ID en el Intent.");
            currentGameId = -1L;
        }
    }

    private void setupViewModel() {
        gameDetailViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(GameDetailViewModel.class);
    }

    private void observeViewModel() {
        gameDetailViewModel.isLoading.observe(this, isLoading -> {
            gameDetailLoadingProgressBar.setVisibility(isLoading != null && isLoading ? View.VISIBLE : View.GONE);
            requestLoanButton.setEnabled(isLoading == null || !isLoading);
        });

        gameDetailViewModel.gameDetails.observe(this, this::populateUI);

        gameDetailViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                gameDetailViewModel.clearErrorMessage();
            }
        });

        gameDetailViewModel.loanRequestSuccess.observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(this, "Solicitud de préstamo enviada con éxito.", Toast.LENGTH_LONG).show();
                requestLoanButton.setEnabled(false);
                requestLoanButton.setText(R.string.button_loan_requested);
            } else if (isSuccess != null) {
                // Si no tuvo éxito pero no hay error específico
                if (gameDetailViewModel.errorMessage.getValue() == null || gameDetailViewModel.errorMessage.getValue().isEmpty()) {
                    Toast.makeText(this, "No se pudo solicitar el préstamo.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void populateUI(GameResponseDTO game) {
        if (game == null) return;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(game.getTitle() != null ? game.getTitle() : getString(R.string.title_activity_game_detail));
        }

        gameTitleTextView.setText(game.getTitle() != null ? game.getTitle() : "N/A");
        platformTextViewDetail.setText(game.getPlatform() != null ? game.getPlatform() : "N/A");
        genreTextViewDetail.setText(game.getGenre() != null ? game.getGenre() : "N/A");

        statusTextViewDetail.setText(game.getStatus() != null ? game.getStatus().toString() : "N/A");
        ownerNameTextViewDetail.setText(game.getUserName() != null ? game.getUserName() : "N/A");
        descriptionTextViewDetail.setText(game.getDescription() != null ? game.getDescription() : "Sin descripción.");

        if (game.getImageUrl() != null && !game.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(game.getImageUrl().startsWith("http") ? game.getImageUrl() : (com.example.gamelend.remote.api.ApiClient.BASE_URL + game.getImageUrl().replaceFirst("/api", "")))
                    .placeholder(R.drawable.mando)
                    .error(R.drawable.mando)
                    .into(gameImageViewDetail);
        } else {
            gameImageViewDetail.setImageResource(R.drawable.mando);
        }

        // Habilitar o deshabilitar botón préstamo según estado
        if (GameStatus.AVAILABLE.equals(game.getStatus())) {
            requestLoanButton.setEnabled(true);
            requestLoanButton.setText(R.string.button_request_loan);
        } else {
            requestLoanButton.setEnabled(false);
            requestLoanButton.setText(game.getStatus() != null ? game.getStatus().toString() : "No Disponible");
        }
    }

    private void setupRequestLoanButtonListener() {
        requestLoanButton.setOnClickListener(v -> {
            if (currentGameId != null && gameDetailViewModel.gameDetails.getValue() != null) {
                Long lenderId = gameDetailViewModel.gameDetails.getValue().getUserId(); // ID dueño
                Long borrowerId = getCurrentUserId(); // Debes implementar este método para obtener el usuario actual
                if (lenderId != null && borrowerId != null) {
                    Log.d(TAG, "Solicitando préstamo para juego ID: " + currentGameId);
                    gameDetailViewModel.requestLoan(currentGameId, lenderId);
                } else {
                    Toast.makeText(this, "IDs de usuario no disponibles.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Long getCurrentUserId() {
        return 1L;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}