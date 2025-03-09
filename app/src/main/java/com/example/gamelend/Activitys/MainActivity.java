package com.example.gamelend.Activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelend.Conexion.ApiClient;
import com.example.gamelend.Conexion.ApiService;
import com.example.gamelend.R;
import com.example.gamelend.dto.LoginRequest;
import com.example.gamelend.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUsuario, editTextContrasena;
    private Button buttonEntrar;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editTextUsuario = findViewById(R.id.editTextUsuario);
        editTextContrasena = findViewById(R.id.editTextContrasena);
        buttonEntrar = findViewById(R.id.buttonEntrar);

        apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);

        buttonEntrar.setOnClickListener(v -> {
            validarUsuario();
        });
    }

    private void validarUsuario() {
        String usuario = editTextUsuario.getText().toString().trim();
        String contrasena = editTextContrasena.getText().toString().trim();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "No se admiten campos vacíos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Llamada al método que verifica en la API
        loginEnApi(usuario, contrasena);
    }

    private void loginEnApi(String usuario, String contrasena) {
        LoginRequest request = new LoginRequest(usuario, contrasena);

        Call<LoginResponse> call = apiService.login(request);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();

                    String nombreUsuario = loginResponse.getNombreUsuario();
                    String token = loginResponse.getToken();

                    // Guardamos el token en SharedPreferences (opcional)
                    SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("token", token);
                    editor.apply();

                    Toast.makeText(MainActivity.this, "Bienvenido " + nombreUsuario, Toast.LENGTH_SHORT).show();

                    // Ir a la siguiente pantalla
                    Intent intent = new Intent(MainActivity.this, ListaUsuarios.class);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
