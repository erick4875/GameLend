package com.example.gamelend.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.remote.api.ApiService;
import com.example.gamelend.R;
import com.example.gamelend.repository.UserRepository;
import com.example.gamelend.viewmodel.MainViewModel;


public class MainActivity extends AppCompatActivity {

    private EditText editTextUsuario, editTextContrasena;
    private Button buttonEntrar;

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editTextUsuario = findViewById(R.id.editTextUsuario);
        editTextContrasena = findViewById(R.id.editTextContrasena);
        buttonEntrar = findViewById(R.id.buttonEntrar);

        // Crear el repository y el viewModel de forma manual por ahora
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
        UserRepository userRepository = new UserRepository(apiService);
        viewModel = new MainViewModel(userRepository);

        setupObservers();

        buttonEntrar.setOnClickListener(v -> validarUsuario());
    }

    private void validarUsuario() {
        String usuario = editTextUsuario.getText().toString().trim();
        String contrasena = editTextContrasena.getText().toString().trim();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "No se admiten campos vacíos.", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.login(usuario, contrasena);
    }

    private void setupObservers() {
        viewModel.getTokenResponse().observe(this, tokenResponseDTO -> {
            if (tokenResponseDTO != null) {
                // Obtenemos el AccessToken y el RefreshToken
                String accessToken = tokenResponseDTO.getAccessToken();
                String token = tokenResponseDTO.getRefreshToken();

                // Guardamos el token en SharedPreferences (opcional)
                SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("accessToken", accessToken);
                editor.putString("refreshToken", token);
                editor.apply();

                Toast.makeText(MainActivity.this, "Incicio de sesión exitoso " , Toast.LENGTH_SHORT).show();

                // Ir a la siguiente pantalla
                Intent intent = new Intent(MainActivity.this, ListaUsuarios.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Credenciales incorrectas o error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
