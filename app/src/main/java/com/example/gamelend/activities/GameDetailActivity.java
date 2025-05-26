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

import com.bumptech.glide.Glide; // Para cargar imágenes
import com.example.gamelend.R;
import com.example.gamelend.dto.GameResponseDTO;
import com.example.gamelend.models.GameStatus; // Tu enum de Android
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.viewmodel.GameDetailViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        setContentView(R.layout.activity_game_detail); // Tu XML para esta activity

        Toolbar toolbar = findViewById(R.id.gameDetailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // El título se establece desde el XML o se puede poner dinámicamente
        }

        // Inicializar vistas
        gameImageViewDetail = findViewById(R.id.gameImageViewDetail);
        gameTitleTextView = findViewById(R.id.gameTitleTextView);
        platformTextViewDetail = findViewById(R.id.platformTextViewDetail);
        genreTextViewDetail = findViewById(R.id.genreTextViewDetail);
        statusTextViewDetail = findViewById(R.id.statusTextViewDetail);
        ownerNameTextViewDetail = findViewById(R.id.ownerNameTextViewDetail);
        descriptionTextViewDetail = findViewById(R.id.descriptionTextViewDetail);
        requestLoanButton = findViewById(R.id.requestLoanButton);
        gameDetailLoadingProgressBar = findViewById(R.id.gameDetailLoadingProgressBar);

        // Obtener el ID del juego del Intent
        if (getIntent() != null && getIntent().hasExtra(GameListActivity.EXTRA_GAME_ID)) {
            currentGameId = getIntent().getLongExtra(GameListActivity.EXTRA_GAME_ID, -1L);
        } else {
            Log.e(TAG, "No se recibió GAME_ID en el Intent.");
            Toast.makeText(this, "Error: No se especificó el juego.", Toast.LENGTH_LONG).show();
            finish(); // Cerrar si no hay ID
            return;
        }

        if (currentGameId == -1L) {
            Log.e(TAG, "GAME_ID inválido recibido.");
            Toast.makeText(this, "Error: ID de juego inválido.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Inicializar ViewModel
        gameDetailViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(GameDetailViewModel.class);

        setupViewModelObservers();

        // Solicitar datos del juego
        Log.d(TAG, "Solicitando detalles para el juego ID: " + currentGameId);
        gameDetailViewModel.fetchGameDetails(currentGameId);

        // Listener para el botón de solicitar préstamo
        requestLoanButton.setOnClickListener(v -> {
            if (currentGameId != null) {
                Log.d(TAG, "Botón Solicitar Préstamo presionado para juego ID: " + currentGameId);
                // Aquí llamarías a un método en el ViewModel para solicitar el préstamo
                // gameDetailViewModel.requestLoan(currentGameId);
                Toast.makeText(this, "Solicitud de préstamo (lógica pendiente)...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViewModelObservers() {
        gameDetailViewModel.isLoading.observe(this, isLoading -> {
            Log.d(TAG, "isLoading LiveData changed: " + isLoading);
            if (isLoading != null) {
                gameDetailLoadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                // Podrías deshabilitar el botón de préstamo mientras carga
                requestLoanButton.setEnabled(!isLoading);
            }
        });

        gameDetailViewModel.gameDetails.observe(this, game -> {
            if (game != null) {
                Log.d(TAG, "gameDetails LiveData changed: " + game.getTitle());
                populateUI(game);
            } else {
                Log.d(TAG, "gameDetails LiveData es null (posiblemente después de un error inicial)");
                // No limpiar la UI aquí si el error ya se mostró,
                // o si quieres mostrar un estado "no encontrado".
            }
        });

        gameDetailViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "errorMessage LiveData changed: " + error);
                Toast.makeText(GameDetailActivity.this, error, Toast.LENGTH_LONG).show();
                gameDetailViewModel.clearErrorMessage();
            }
        });

        // Observador para el resultado de la solicitud de préstamo
        gameDetailViewModel.loanRequestSuccess.observe(this, isSuccess -> {
            if (isSuccess != null) {
                if (isSuccess) {
                    Toast.makeText(this, "Solicitud de préstamo enviada con éxito.", Toast.LENGTH_LONG).show();
                    // Aquí podrías querer actualizar la UI (ej. deshabilitar el botón, cambiar texto)
                    // o incluso cerrar la actividad o refrescar los datos del juego.
                    requestLoanButton.setEnabled(false);
                    requestLoanButton.setText("Préstamo Solicitado");
                } else {
                    // El error específico ya debería haberse mostrado por el observador de errorMessage
                    if (gameDetailViewModel.errorMessage.getValue() == null || gameDetailViewModel.errorMessage.getValue().isEmpty()) {
                        Toast.makeText(this, "No se pudo solicitar el préstamo.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void populateUI(GameResponseDTO game) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(game.getTitle() != null ? game.getTitle() : getString(R.string.title_activity_game_detail));
        }
        gameTitleTextView.setText(game.getTitle() != null ? game.getTitle() : "N/A");
        platformTextViewDetail.setText(game.getPlatform() != null ? game.getPlatform() : "N/A");
        genreTextViewDetail.setText(game.getGenre() != null ? game.getGenre() : "N/A");

        // Asumimos que GameResponseDTO tiene un campo 'status' que es un String o un Enum compatible
        // con tu models.GameStatus o que puedes convertir.
        // Si es un String:
        statusTextViewDetail.setText(game.getStatus() != null ? game.getStatus().toString() : "N/A");
        // Si es un enum y quieres mostrar el nombre:
        // statusTextViewDetail.setText(game.getStatus() != null ? game.getStatus().name() : "N/A");

        ownerNameTextViewDetail.setText(game.getUserName() != null ? game.getUserName() : "N/A"); // Asume que GameResponseDTO tiene userName
        descriptionTextViewDetail.setText(game.getDescription() != null ? game.getDescription() : "Sin descripción.");

        // Cargar imagen del juego con Glide
        if (game.getImageUrl() != null && !game.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(ApiClient.BASE_URL + game.getImageUrl().replaceFirst("/api", "")) // Ajusta la URL si es relativa
                    .placeholder(R.drawable.mando)
                    .error(R.drawable.mando) // Crea un drawable mando_error
                    .into(gameImageViewDetail);
        } else {
            gameImageViewDetail.setImageResource(R.drawable.mando); // Placeholder
        }

        // Habilitar/deshabilitar botón de préstamo según el estado del juego
        // Asume que GameResponseDTO tiene un campo 'status' que es un enum
        // o un String que puedes comparar con tu enum models.GameStatus.
        if (game.getStatus() == com.example.gamelend.models.GameStatus.AVAILABLE) { // O game.getStatus().name().equals("AVAILABLE")
            requestLoanButton.setEnabled(true);
            requestLoanButton.setText(R.string.button_request_loan);
        } else {
            requestLoanButton.setEnabled(false);
            requestLoanButton.setText(game.getStatus() != null ? game.getStatus().toString() : "No Disponible");
        }
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
