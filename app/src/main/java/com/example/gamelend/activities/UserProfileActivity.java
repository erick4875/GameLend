package com.example.gamelend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Para depuración
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
import androidx.core.graphics.Insets; // Mantén si usas el listener de insets
import androidx.core.view.ViewCompat; // Mantén si usas el listener de insets
import androidx.core.view.WindowInsetsCompat; // Mantén si usas el listener de insets
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Para cargar imágenes desde URL
import com.example.gamelend.R;
import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.models.GameAdapter; // Asume que tienes este adaptador
// import com.example.gamelend.models.Game; // Si GameAdapter usa una clase Game local
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.viewmodel.UserProfileViewModel; // Tu ViewModel

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";

    // UI Elements
    private ImageView userProfileImageView, logoImageViewProfile;
    private TextView userNameTextView;
    private Button editProfileButton, addGameButton, logoutButton;
    private RecyclerView gamesRecyclerView;
    private ProgressBar loadingProgressBarProfile;

    private GameAdapter gameAdapter;
    private TokenManager tokenManager;
    private UserProfileViewModel userProfileViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Descomenta si quieres flecha atrás
            getSupportActionBar().setTitle(R.string.title_activity_user_profile);
        }

        // Inicializar vistas (asegúrate que los IDs en XML sean estos)
        userProfileImageView = findViewById(R.id.userProfileImageView);
        logoImageViewProfile = findViewById(R.id.logoImageViewProfile);
        userNameTextView = findViewById(R.id.userNameTextView);
        editProfileButton = findViewById(R.id.editProfileButton);
        addGameButton = findViewById(R.id.addGameButton);
        logoutButton = findViewById(R.id.logoutButton);
        gamesRecyclerView = findViewById(R.id.gamesRecyclerView);
        loadingProgressBarProfile = findViewById(R.id.loadingProgressBarProfile);

        // Inicializar TokenManager
        tokenManager = ApiClient.getTokenManager(getApplicationContext());

        // Configurar RecyclerView
        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        gamesRecyclerView.setHasFixedSize(true);
        // Necesitarás un GameAdapter. Si no lo tienes, crea uno básico.
        gameAdapter = new GameAdapter(this, new ArrayList<>() /*, listener si es necesario */);
        gamesRecyclerView.setAdapter(gameAdapter);

        // Inicializar ViewModel
        userProfileViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(UserProfileViewModel.class);

        // Configurar observadores para los LiveData del ViewModel
        setupViewModelObservers();

        // Solicitar la carga de datos del perfil
        Log.d(TAG, "Solicitando datos del perfil del usuario...");
        userProfileViewModel.fetchUserProfileData();

        // Configuración de eventos para los botones
        editProfileButton.setOnClickListener(view -> {
            Intent intent = new Intent(UserProfileActivity.this, EditProfileActivity.class);
            // Podrías pasar el ID del usuario o el UserResponseDTO si EditProfileActivity lo necesita
            // intent.putExtra("USER_ID", userIdFromViewModel);
            startActivity(intent);
        });

        addGameButton.setOnClickListener(view -> {
            Intent intent = new Intent(UserProfileActivity.this, AddGameActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(view -> performLogout());

        // Manejo de Insets (si lo necesitas)
        View profileHeaderLayout = findViewById(R.id.profileHeaderLayout);
        if (profileHeaderLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(profileHeaderLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void setupViewModelObservers() {
        // Observar el estado de carga
        userProfileViewModel.isLoading.observe(this, isLoading -> {
            Log.d(TAG, "isLoading LiveData changed: " + isLoading);
            if (isLoading != null && isLoading) {
                loadingProgressBarProfile.setVisibility(View.VISIBLE);
                // Opcionalmente, ocultar contenido principal mientras carga
                // gamesRecyclerView.setVisibility(View.GONE);
                // userNameTextView.setVisibility(View.GONE); // etc.
            } else {
                loadingProgressBarProfile.setVisibility(View.GONE);
                // Mostrar contenido principal
                // gamesRecyclerView.setVisibility(View.VISIBLE);
                // userNameTextView.setVisibility(View.VISIBLE);
            }
        });

        // Observar los datos del perfil del usuario
        userProfileViewModel.userData.observe(this, user -> {
            if (user != null) {
                Log.d(TAG, "userData LiveData changed: " + user.getPublicName());
                userNameTextView.setText(user.getPublicName()); // <--- ACTUALIZAR EL NOMBRE
                // Aquí también actualizarías la imagen de perfil, etc.
                // Ejemplo con Glide (asegúrate de tener la dependencia y permiso de internet):
                // if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                //    Glide.with(this).load(user.getProfileImageUrl()).placeholder(R.drawable.perfil_usuario).into(userProfileImageView);
                // } else {
                //    userProfileImageView.setImageResource(R.drawable.perfil_usuario); // Placeholder
                // }
                // También podrías cargar los juegos del usuario aquí o tener un LiveData separado para ellos
                // if (user.getGames() != null) {
                //    gameAdapter.submitList(user.getGames()); // Asumiendo que UserResponseDTO tiene los juegos y GameAdapter los acepta
                // }
            } else {
                Log.d(TAG, "userData LiveData es null (posiblemente después de un error inicial)");
            }
        });

        // Observar los mensajes de error
        userProfileViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "errorMessage LiveData changed: " + error);
                Toast.makeText(UserProfileActivity.this, error, Toast.LENGTH_LONG).show();
                userProfileViewModel.clearErrorMessage(); // Limpiar el error después de mostrarlo
            }
        });

        // Observar la lista de juegos del usuario (si la manejas por separado en el ViewModel)
        /*
        userProfileViewModel.userGames.observe(this, games -> {
            if (games != null) {
                Log.d(TAG, "userGames LiveData changed, count: " + games.size());
                gameAdapter.submitList(games); // Asume que GameAdapter tiene submitList
            }
        });
        */
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
        // Manejar el clic en la flecha de atrás de la Toolbar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // onBackPressed() ya lo tenías, puedes mantenerlo o ajustarlo
    // @Override
    // public void onBackPressed() {
    //     super.onBackPressed();
    //     finish();
    // }
}