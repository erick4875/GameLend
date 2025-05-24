package com.example.gamelend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.gamelend.R;
import com.example.gamelend.dto.TokenResponseDTO; // Asegúrate que este DTO tenga getAccessToken()
import com.example.gamelend.viewmodel.LoginViewModel;

import java.security.PublicKey;

public class MainActivity extends AppCompatActivity { // Considera renombrar a LoginActivity

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private ProgressBar loadingProgressBar;

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main); // Usa tu layout con IDs en inglés

        // Inicializar vistas con los IDs en inglés de tu layout XML
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // Obtener el ViewModel
        loginViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(LoginViewModel.class);

        // Configurar observadores para la respuesta del ViewModel
        setupLoginObservers();

        // Listener para el botón de login
        loginButton.setOnClickListener(v -> attemptLogin());

        // Listener para el botón de registrarse
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email y contraseña no pueden estar vacíos.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Llamar al metodo correcto en el ViewModel
        loginViewModel.performLogin(email, password);
    }

    private void setupLoginObservers() {
        // Observar el estado de carga
        // Asumiendo que en LoginViewModel tienes: public LiveData<Boolean> isLoadingLiveData;
        loginViewModel.isLoadingLiveData.observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginButton.setEnabled(false);
                registerButton.setEnabled(false);
            } else {
                loadingProgressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
            }
        });

        // Observar el resultado del login
        // Asumiendo que en LoginViewModel tienes: public LiveData<TokenResponseDTO> loginResultLiveData;
        loginViewModel.loginResultLiveData.observe(this, tokenResponseDTO -> {
            // El null check para tokenResponseDTO ya se hace en el ViewModel antes de postear a _loginResultLiveData
            // pero una doble verificación aquí no hace daño si el LiveData pudiera ser null por otras razones.
            if (tokenResponseDTO != null && tokenResponseDTO.getAccessToken() != null) {
                // El ViewModel (a través de TokenManager) ya guardó los tokens
                Toast.makeText(MainActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                navigateToUserProfile(tokenResponseDTO.getPublicName());
            }
            // Si tokenResponseDTO es null o no tiene accessToken, significa que el login falló.
            // Ese caso se maneja principalmente observando errorLiveData.
        });

        // Observar errores de login
        // Asumiendo que en LoginViewModel tienes: public LiveData<String> errorLiveData;
        loginViewModel.errorLiveData.observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                // Limpiar el error en el ViewModel para que no se muestre de nuevo en un cambio de configuración
                loginViewModel.clearLoginError(); // Asegúrate de tener este método en LoginViewModel
            }
        });
    }

    private void navigateToUserProfile(String publicName) {
        Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
        intent.putExtra("USERNAME", publicName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}