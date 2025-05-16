package com.example.gamelend.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.gamelend.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelend.dto.GameDTO;
import com.example.gamelend.dto.GameStatus;
import com.example.gamelend.repository.GameRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgregarJuego extends AppCompatActivity {

    private EditText etTitle, etPlatform, etGenre, etDescription;
    private Spinner spinnerStatus;
    private Button btnSaveGame;

    private GameRepository gameRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_juego);

        // Inicializar vistas
        etTitle = findViewById(R.id.etTitle);
        etPlatform = findViewById(R.id.etPlatform);
        etGenre = findViewById(R.id.etGenre);
        etDescription = findViewById(R.id.etDescription);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSaveGame = findViewById(R.id.btnSaveGame);

        // Configurar spinner con enum GameStatus
        ArrayAdapter<GameStatus> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                GameStatus.values()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Inicializar repositorio (pasa el context si tu ApiClient lo requiere)
        gameRepository = new GameRepository(getApplicationContext());

        // Listener para el botón guardar
        btnSaveGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarJuego();
            }
        });
    }

    private void guardarJuego() {
        String title = etTitle.getText().toString().trim();
        String platform = etPlatform.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        GameStatus status = (GameStatus) spinnerStatus.getSelectedItem();

        // Validaciones básicas
        if (title.isEmpty() || platform.isEmpty() || genre.isEmpty() || status == null) {
            Toast.makeText(this, "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear DTO para enviar
        GameDTO gameDTO = new GameDTO(
                null, // id null para creación
                title,
                platform,
                genre,
                description.isEmpty() ? null : description,
                status.name(),
                1L, // aquí deberías poner el userId actual (ejemplo 1L)
                null,
                null,
                null,
                null
        );

        // Llamar API para crear juego
        Call<com.example.gamelend.dto.GameResponseDTO> call = gameRepository.createGame(gameDTO);
        call.enqueue(new Callback<com.example.gamelend.dto.GameResponseDTO>() {
            @Override
            public void onResponse(Call<com.example.gamelend.dto.GameResponseDTO> call, Response<com.example.gamelend.dto.GameResponseDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AgregarJuego.this, "Juego agregado con éxito", Toast.LENGTH_SHORT).show();
                    finish(); // cerrar la activity
                } else {
                    Toast.makeText(AgregarJuego.this, "Error al agregar juego: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.gamelend.dto.GameResponseDTO> call, Throwable t) {
                Toast.makeText(AgregarJuego.this, "Error en la conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}