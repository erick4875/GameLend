package com.example.gamelend.activities;

import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelend.R;
import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.dto.GameSummaryDTO;
import com.example.gamelend.dto.UserResponseDTO;
import com.example.gamelend.models.Game;
import com.example.gamelend.models.GameAdapter; // Importar GameAdapter
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.viewmodel.GameListViewModel;
import com.example.gamelend.viewmodel.UserProfileViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
// import java.util.stream.Collectors; // Solo si usas streams (API 24+)

// UserProfileActivity ahora implementa la interfaz OnGameItemClickListener de GameAdapter
public class UserProfileActivity extends AppCompatActivity implements GameAdapter.OnGameItemClickListener {

    private static final String TAG = "UserProfileActivity";

    private ImageView userProfileImageView, logoImageViewProfile;
    private TextView userNameTextView, registrationDateTextView;
    private Button editProfileButton, addGameButton, logoutButton;
    private RecyclerView gamesRecyclerView;
    private ProgressBar loadingProgressBarProfile;

    private GameAdapter gameAdapter;
    private TokenManager tokenManager;
    private UserProfileViewModel userProfileViewModel;
    private GameListViewModel gameListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_user_profile);
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Descomenta si quieres flecha atrás
        }

        userProfileImageView = findViewById(R.id.userProfileImageView);
        logoImageViewProfile = findViewById(R.id.logoImageViewProfile);
        userNameTextView = findViewById(R.id.userNameTextView);
        registrationDateTextView = findViewById(R.id.registrationDateTextView);
        editProfileButton = findViewById(R.id.editProfileButton);
        addGameButton = findViewById(R.id.addGameButton);
        logoutButton = findViewById(R.id.logoutButton);
        gamesRecyclerView = findViewById(R.id.gamesRecyclerView);
        loadingProgressBarProfile = findViewById(R.id.loadingProgressBarProfile);

        tokenManager = ApiClient.getTokenManager(getApplicationContext());

        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        gamesRecyclerView.setHasFixedSize(true);
        // Al crear el adaptador, pasamos 'this' como el listener
        gameAdapter = new GameAdapter(this, new ArrayList<>(), this);
        gamesRecyclerView.setAdapter(gameAdapter);

        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        gameListViewModel = new ViewModelProvider(this).get(GameListViewModel.class);

        setupViewModelObservers();
        setupButtonListeners(); // Mover listeners de botones a un método separado

        // La carga de datos se hará en onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Solicitando datos...");
        loadingProgressBarProfile.setVisibility(View.VISIBLE);
        setButtonsEnabled(false);

        if (userProfileViewModel != null) {
            userProfileViewModel.fetchUserProfileData();
        }
        if (gameListViewModel != null) {
            // Aquí decides si cargas todos los juegos o solo los del usuario.
            // Si son solo los del usuario, esta lógica podría estar en UserProfileViewModel
            // o GameListViewModel necesitaría el userId.
            gameListViewModel.fetchAllGames();
        }
    }

    private void setupButtonListeners() {
        editProfileButton.setOnClickListener(view -> {
            Log.d(TAG, "Botón Editar Perfil PRESIONADO");
            Intent intent = new Intent(UserProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        addGameButton.setOnClickListener(view -> {
            Log.d(TAG, "Botón Añadir Juego PRESIONADO");
            Intent intent = new Intent(UserProfileActivity.this, AddGameActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(view -> {
            Log.d(TAG, "Botón Cerrar Sesión PRESIONADO");
            performLogout();
        });
    }

    private void setupViewModelObservers() {
        userProfileViewModel.isLoading.observe(this, isLoading -> {
            Log.d(TAG, "UserProfile isLoading: " + isLoading);
            updateLoadingState();
        });

        gameListViewModel.isLoadingLiveData.observe(this, isLoading -> {
            Log.d(TAG, "GameList isLoading: " + isLoading);
            updateLoadingState();
        });

        userProfileViewModel.userData.observe(this, user -> {
            if (user != null) {
                Log.d(TAG, "userData LiveData changed: " + user.getPublicName());
                userNameTextView.setText(user.getPublicName());
                if (user.getRegistrationDate() != null && !user.getRegistrationDate().isEmpty()) {
                    String formattedDate = formatDateString(user.getRegistrationDate());
                    registrationDateTextView.setText(getString(R.string.registration_date_prefix) + formattedDate);
                } else {
                    registrationDateTextView.setText(getString(R.string.registration_date_prefix) + "No disponible");
                }
            } else {
                Log.d(TAG, "userData LiveData es null");
                userNameTextView.setText("");
                registrationDateTextView.setText("");
            }
        });

        userProfileViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "UserProfile errorMessage: " + error);
                Toast.makeText(UserProfileActivity.this, "Perfil: " + error, Toast.LENGTH_LONG).show();
                userProfileViewModel.clearErrorMessage();
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
                Log.e(TAG, "GameList errorMessage: " + error);
                Toast.makeText(UserProfileActivity.this, "Juegos: " + error, Toast.LENGTH_LONG).show();
                gameListViewModel.clearFetchGamesError();
            }
        });
    }

    private void setButtonsEnabled(boolean enabled) {
        editProfileButton.setEnabled(enabled);
        addGameButton.setEnabled(enabled);
        logoutButton.setEnabled(enabled);
    }

    private void updateLoadingState() {
        Boolean profileIsLoading = userProfileViewModel.isLoading.getValue();
        Boolean gamesAreLoading = gameListViewModel.isLoadingLiveData.getValue();

        boolean pLoading = profileIsLoading != null && profileIsLoading;
        boolean gLoading = gamesAreLoading != null && gamesAreLoading;
        boolean overallLoading = pLoading || gLoading;

        Log.d(TAG, "updateLoadingState - profileLoading: " + pLoading + ", gamesLoading: " + gLoading + ", overallLoading: " + overallLoading);

        loadingProgressBarProfile.setVisibility(overallLoading ? View.VISIBLE : View.GONE);
        setButtonsEnabled(!overallLoading);
    }

    private List<Game> mapGameSummaryDTOsToGames(List<GameSummaryDTO> dtos) {
        List<Game> uiGames = new ArrayList<>();
        if (dtos == null) return uiGames;
        for (GameSummaryDTO dto : dtos) {
            if (dto != null) {
                String title = dto.getTitle() != null ? dto.getTitle() : "N/A";
                Long gameId = dto.getId(); // Asumiendo que GameSummaryDTO tiene getId()
                // Usando el constructor de models.Game que ahora incluye id
                uiGames.add(new Game(gameId, title, R.drawable.mando));
            }
        }
        return uiGames;
    }

    private String formatDateString(String dateString) {
        if (dateString == null) return "N/A";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error al parsear la fecha: " + dateString, e);
            return dateString;
        }
        return dateString;
    }

    private void performLogout() {
        Log.d(TAG, "Realizando logout...");
        tokenManager.clearTokens();
        Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // === IMPLEMENTACIÓN DEL MÉTODO DE LA INTERFAZ OnGameItemClickListener ===
    @Override
    public void onGameItemClick(Game clickedGame) {
        Log.d(TAG, "Juego clickeado: " + clickedGame.getName() + " (ID: " + clickedGame.getId() + ")");
        Toast.makeText(this, "Juego: " + clickedGame.getName(), Toast.LENGTH_SHORT).show();

        if (clickedGame.getId() != null) {
            Intent intent = new Intent(UserProfileActivity.this, GameDetailActivity.class);
            intent.putExtra(GameListActivity.EXTRA_GAME_ID, clickedGame.getId()); // Usar la constante de GameListActivity
            startActivity(intent);
        } else {
            Log.e(TAG, "ID del juego es null, no se puede navegar a detalles.");
            Toast.makeText(this, "No se pudo obtener el ID del juego para ver detalles.", Toast.LENGTH_SHORT).show();
        }
    }
    // =====================================================================
}