package com.example.gamelend.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.gamelend.R;
import com.example.gamelend.models.GameStatus;
import com.example.gamelend.viewmodel.AddGameViewModel;

public class AddGameActivity extends AppCompatActivity {

    private static final String TAG = "AddGameActivity";

    private EditText titleEditText, platformEditText, genreEditText, descriptionEditText;
    private Spinner statusSpinner;
    private Button saveGameButton, cancelButton;
    private ProgressBar loadingProgressBar;

    private AddGameViewModel addGameViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.addGameToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Inicializar vistas
        titleEditText = findViewById(R.id.titleEditText);
        platformEditText = findViewById(R.id.platformEditText);
        genreEditText = findViewById(R.id.genreEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        statusSpinner = findViewById(R.id.statusSpinner);
        saveGameButton = findViewById(R.id.saveGameButton);
        cancelButton = findViewById(R.id.cancelButton);
        loadingProgressBar = findViewById(R.id.addGameLoadingProgressBar);

        // Configurar spinner con el enum GameStatus
        ArrayAdapter<GameStatus> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                GameStatus.values()
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Inicializar ViewModel
        addGameViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(AddGameViewModel.class);

        // Configurar observadores para los LiveData del ViewModel
        setupViewModelObservers();

        // Listener para el botón guardar
        saveGameButton.setOnClickListener(view -> performSaveGame());

        // Listener para el botón cancelar
        cancelButton.setOnClickListener(view -> {
            finish(); // Cierra esta actividad y regresa a la anterior
        });
    }

    // Maneja el clic en la flecha de "atrás" de la Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Recoge los datos del formulario, los valida y llama al ViewModel para guardarlos.
     */
    private void performSaveGame() {
        // Lee los valores de los campos de texto.
        String title = titleEditText.getText().toString().trim();
        String platform = platformEditText.getText().toString().trim();
        String genre = genreEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        GameStatus status = (GameStatus) statusSpinner.getSelectedItem();

        // Comprueba que los campos obligatorios no estén vacíos.
        if (title.isEmpty() || platform.isEmpty() || genre.isEmpty() || status == null) {
            Toast.makeText(this, R.string.error_complete_all_fields_game, Toast.LENGTH_SHORT).show();
            return;
        }

        // El ViewModel se encargará de obtener el currentUserId y construir el GameDTO final.
        // Pasamos los datos recolectados de la UI al ViewModel.
        Log.d(TAG, "Llamando a addGameViewModel.saveNewGame con título: " + title);
        addGameViewModel.saveNewGame(
                title,
                platform,
                genre,
                description.isEmpty() ? null : description,
                status,
                false,
                null   // catalogGameId - ajusta si es una instancia de un juego de catálogo
        );
    }

    /**
     * Configura los observadores para los LiveData expuestos por AddGameViewModel.
     */
    private void setupViewModelObservers() {
        // Observar el estado de carga
        addGameViewModel.isLoadingLiveData.observe(this, isLoading -> {
            Log.d(TAG, "isLoading LiveData changed: " + isLoading);
            if (isLoading != null) {
                loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                saveGameButton.setEnabled(!isLoading);
                cancelButton.setEnabled(!isLoading);
            }
        });

        // Observar el resultado de la creación del juego
        addGameViewModel.gameSaveResultLiveData.observe(this, gameResponse -> {
            // Si gameResponse no es null, el juego se guardó (la API devolvió una respuesta exitosa)
            // y el ViewModel ya manejó el _isLoadingLiveData a false.
            if (gameResponse != null) {
                Log.d(TAG, "Juego guardado exitosamente, respuesta ID: " + gameResponse.getId());
                Toast.makeText(AddGameActivity.this, R.string.game_added_successfully, Toast.LENGTH_SHORT).show();
                finish(); // Cierra la actividad y vuelve a la anterior
            }
            // Si gameResponse es null, significa que hubo un error.
            // Ese error se maneja observando 'errorLiveData'.
        });

        // Observar los mensajes de error
        addGameViewModel.errorLiveData.observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Log.e(TAG, "Error recibido del ViewModel: " + errorMessage);
                Toast.makeText(AddGameActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                addGameViewModel.clearSaveError(); // Limpiar el error después de mostrarlo
            }
        });
    }
}


