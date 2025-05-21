package com.example.gamelend.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamelend.R;
import com.example.gamelend.dto.RegisterRequestDTO;
import com.example.gamelend.dto.TokenResponseDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.remote.api.ApiService;

import retrofit2.Call;
import retrofit2.Response;

public class Registro extends AppCompatActivity {

    EditText editTextNombre, editTextApellidos, editTextUsuario, editTextContrasena,
            editTextEmail, editTextTelefono, editTextProvincia, editTextCiudad;
    Button buttonRegistrar;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        // Inicializar vistas
        editTextNombre = findViewById(R.id.editTextText);
        editTextApellidos = findViewById(R.id.editTextText2);
        editTextUsuario = findViewById(R.id.editTextText3);
        editTextContrasena = findViewById(R.id.editTextTextPassword);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress);
        editTextTelefono = findViewById(R.id.editTextPhone);
//        editTextProvincia = findViewById(R.id.editTextProvince);
//        editTextCiudad = findViewById(R.id.editTextCity);
        buttonRegistrar = findViewById(R.id.buttonEntrar);

        // Inicializar Retrofit
        apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);

        // Evento del botón
        buttonRegistrar.setOnClickListener(v -> {
            String nombre = editTextNombre.getText().toString().trim();
            String apellidos = editTextApellidos.getText().toString().trim();
            String username = editTextUsuario.getText().toString().trim();
            String password = editTextContrasena.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String telefono = editTextTelefono.getText().toString().trim();
            String provincia = editTextProvincia.getText().toString().trim();
            String ciudad = editTextCiudad.getText().toString().trim();

            if (nombre.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(Registro.this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            RegisterRequestDTO request = new RegisterRequestDTO(
                    nombre,
                    username,
                    password,
                    email,
                    provincia,
                    ciudad
            );

            apiService.register(request).enqueue(new retrofit2.Callback<TokenResponseDTO>() {
                @Override
                public void onResponse(Call<TokenResponseDTO> call, Response<TokenResponseDTO> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(Registro.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(Registro.this, "Error: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<TokenResponseDTO> call, Throwable t) {
                    Toast.makeText(Registro.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
