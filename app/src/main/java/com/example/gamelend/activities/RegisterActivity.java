package com.example.gamelend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem; // Importante para onOptionsItemSelected
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull; // Importante para onOptionsItemSelected
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Importar Toolbar
import androidx.lifecycle.ViewModelProvider;

import com.example.gamelend.R;
import com.example.gamelend.dto.RegisterRequestDTO;
import com.example.gamelend.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    // Variables para los campos de entrada (nombres en inglés)
    private EditText nameEditText, publicNameEditText, passwordEditText,
            emailEditText, provinceEditText, cityEditText;
    private Button registerButtonSubmit;
    private ProgressBar loadingProgressBarRegister;

    private RegisterViewModel registerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Tu layout XML con la Toolbar

        // 1. Configurar la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarRegister); // ID de tu Toolbar en el XML
        setSupportActionBar(toolbar); // Establece la Toolbar como la ActionBar de la Activity

        // Habilitar el botón de "atrás" (flecha) en la Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // El título se establece desde el XML (app:title="@string/title_register_user")
        }

        // Inicializar vistas usando los IDs en inglés de tu XML
        nameEditText = findViewById(R.id.nameEditText);
        publicNameEditText = findViewById(R.id.publicNameEditText);
        passwordEditText = findViewById(R.id.passwordEditTextRegister);
        emailEditText = findViewById(R.id.emailEditTextRegister);
        provinceEditText = findViewById(R.id.provinceEditText);
        cityEditText = findViewById(R.id.cityEditText);
        registerButtonSubmit = findViewById(R.id.registerButtonSubmit);
        loadingProgressBarRegister = findViewById(R.id.loadingProgressBarRegister);

        // Obtener el ViewModel
        registerViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(RegisterViewModel.class);

        // Configurar observadores para la respuesta del ViewModel
        setupObservers();

        // Evento del botón de registrar
        registerButtonSubmit.setOnClickListener(v -> attemptRegistration());
    }

    // 2. Manejar el clic en el botón de "atrás" de la Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // El ID android.R.id.home corresponde al botón de "atrás" o "up" en la Toolbar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Llama al comportamiento estándar del botón "atrás" (finaliza la Activity)
            // Alternativamente, podrías usar finish(); directamente si es el comportamiento deseado.
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void attemptRegistration() {
        String name = nameEditText.getText().toString().trim();
        String publicName = publicNameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String province = provinceEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();

        if (name.isEmpty() || publicName.isEmpty() || password.isEmpty() || email.isEmpty() || province.isEmpty() || city.isEmpty()) {
            Toast.makeText(RegisterActivity.this, R.string.error_complete_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequestDTO request = new RegisterRequestDTO(name, publicName, password, email, province, city);
        registerViewModel.performRegistration(request);
    }

    private void setupObservers() {
        registerViewModel.isLoadingLiveData.observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                loadingProgressBarRegister.setVisibility(View.VISIBLE);
                registerButtonSubmit.setEnabled(false);
            } else {
                loadingProgressBarRegister.setVisibility(View.GONE);
                registerButtonSubmit.setEnabled(true);
            }
        });

        registerViewModel.registrationResultLiveData.observe(this, tokenResponse -> {
            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                Toast.makeText(RegisterActivity.this, R.string.registration_successful_login, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        registerViewModel.errorLiveData.observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                registerViewModel.clearRegistrationError();
            }
        });
    }
}