package com.example.gamelend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem; // Para la Toolbar
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // Para las imágenes de perfil y logo
import android.widget.ProgressBar; // Para el feedback de carga/guardado
import android.widget.TextView; // Para el nombre de usuario
import android.widget.Toast;

import androidx.annotation.NonNull; // Para onOptionsItemSelected
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Para la Toolbar
import androidx.lifecycle.ViewModelProvider; // Importar ViewModelProvider

// import com.bumptech.glide.Glide; // Descomentar si usas Glide
import com.example.gamelend.R;
import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.dto.UserDTO;
import com.example.gamelend.dto.UserResponseDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.viewmodel.EditProfileViewModel; // Importar tu ViewModel

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private ImageView userProfileImageViewEdit, logoImageViewEditProfile;
    private TextView userNameTextViewEdit; // Para mostrar el publicName actual (no editable)
    private EditText nameEditTextProfile; // Para editar el nombre real
    private EditText publicNameEditTextProfile, passwordEditTextProfile; // Para editar el nombre público
    private EditText provinceEditTextProfile, cityEditTextProfile;

    private Button viewGamesButton, saveChangesButton;
    private ProgressBar editProfileLoadingProgressBar;

    private EditProfileViewModel editProfileViewModel;
    private TokenManager tokenManager;
    private Long currentEditingUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.editProfileToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        userProfileImageViewEdit = findViewById(R.id.userProfileImageViewEdit);
        logoImageViewEditProfile = findViewById(R.id.logoImageViewEditProfile);
        userNameTextViewEdit = findViewById(R.id.userNameTextViewEdit);

        nameEditTextProfile = findViewById(R.id.nameEditTextProfile); // EditText para el nombre real
        publicNameEditTextProfile = findViewById(R.id.publicNameEditTextProfile); // EditText para el nombre público
        provinceEditTextProfile = findViewById(R.id.provinceEditTextProfile);
        cityEditTextProfile = findViewById(R.id.cityEditTextProfile);
        passwordEditTextProfile = findViewById(R.id.passwordEditTextProfile);

        viewGamesButton = findViewById(R.id.viewGamesButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        editProfileLoadingProgressBar = findViewById(R.id.editProfileLoadingProgressBar);

        tokenManager = ApiClient.getTokenManager(getApplicationContext());

        editProfileViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(EditProfileViewModel.class);

        setupViewModelObservers();

        Log.d(TAG, "Solicitando datos del perfil para editar...");
        editProfileViewModel.fetchCurrentUserData();


        viewGamesButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, GameListActivity.class);
            startActivity(intent);
        });

        saveChangesButton.setOnClickListener(v -> attemptSaveChanges());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateUI(UserResponseDTO user) {
        if (user == null) {
            Log.w(TAG, "populateUI llamado con usuario null.");
            userNameTextViewEdit.setText(R.string.user_profile_load_error);
            nameEditTextProfile.setText("");
            publicNameEditTextProfile.setText("");
            provinceEditTextProfile.setText("");
            cityEditTextProfile.setText("");
            return;
        }
        Log.d(TAG, "Poblando UI con datos de: " + user.getPublicName());
        currentEditingUserId = user.getId();
        userNameTextViewEdit.setText(user.getPublicName());

        // Poblar los EditText con los datos del usuario
        nameEditTextProfile.setText(user.getName() != null ? user.getName() : ""); // Nombre real
        publicNameEditTextProfile.setText(user.getPublicName() != null ? user.getPublicName() : ""); // Nombre público
        provinceEditTextProfile.setText(user.getProvince() != null ? user.getProvince() : "");
        cityEditTextProfile.setText(user.getCity() != null ? user.getCity() : "");

        // Cargar imagen de perfil
        // if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
        //    Glide.with(this).load(user.getProfileImageUrl()).placeholder(R.drawable.perfil_usuario).into(userProfileImageViewEdit);
        // } else {
        //    userProfileImageViewEdit.setImageResource(R.drawable.perfil_usuario);
        // }
    }

    private void attemptSaveChanges() {
        // Recoger datos de los EditText correctos
        String name = nameEditTextProfile.getText().toString().trim(); // Nombre real del EditText
        String publicName = publicNameEditTextProfile.getText().toString().trim(); // Nombre público del EditText
        String province = provinceEditTextProfile.getText().toString().trim();
        String city = cityEditTextProfile.getText().toString().trim();
        String newPassword = passwordEditTextProfile.getText().toString().trim();

        // Dentro de attemptSaveChanges, después de recoger los campos
        if (name.isEmpty() || publicName.isEmpty() || province.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, R.string.error_complete_all_editable_fields, Toast.LENGTH_SHORT).show();
            return;
        }

// Validación adicional para la nueva contraseña SI se introduce una
        if (!newPassword.isEmpty() && newPassword.length() < 8) { // Ejemplo: longitud mínima de 8
            Toast.makeText(this, R.string.error_password_length, Toast.LENGTH_SHORT).show(); // Necesitarás esta string
            return;
        }

        if (currentEditingUserId == null) {
            Toast.makeText(this, "Error: No se pudo identificar al usuario para actualizar.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "currentEditingUserId es null en attemptSaveChanges");
            return;
        }

        UserDTO updatedUserDTO = new UserDTO(
                name,
                publicName,
                null,       // email (no se actualiza aquí)
                province,
                city,
                newPassword.isEmpty() ? null : newPassword, // Pasar null si está vacía, sino la nueva contraseña
                null,       // registrationDate
                null,       // games
                null        // roles
        );

        Log.d(TAG, "Intentando guardar cambios para userId: " + currentEditingUserId + " con Name: " + name + ", PublicName: " + publicName);
        editProfileViewModel.updateUserProfile(currentEditingUserId, updatedUserDTO);
    }

    private void setupViewModelObservers() {
        editProfileViewModel.isLoading.observe(this, isLoading -> {
            Log.d(TAG, "isLoading LiveData changed: " + isLoading);
            if (isLoading != null) {
                editProfileLoadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                saveChangesButton.setEnabled(!isLoading);
                viewGamesButton.setEnabled(!isLoading);
            }
        });

        editProfileViewModel.userData.observe(this, user -> {
            if (user != null) {
                Log.d(TAG, "userData LiveData changed, poblando UI para: " + user.getPublicName());
                populateUI(user);
            } else {
                Log.d(TAG, "userData LiveData es null (posiblemente después de un error de carga inicial).");
                populateUI(null);
            }
        });

        editProfileViewModel.updateSuccess.observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(EditProfileActivity.this, R.string.profile_updated_successfully, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        editProfileViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "errorMessage LiveData changed: " + error);
                Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_LONG).show();
                editProfileViewModel.clearErrorMessage();
            }
        });
    }
}
