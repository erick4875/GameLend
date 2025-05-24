package com.example.gamelend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    private TextView userNameTextViewEdit;
    private EditText publicNameEditTextProfile,
            provinceEditTextProfile, cityEditTextProfile;

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
        publicNameEditTextProfile = findViewById(R.id.publicNameEditTextProfile);
        provinceEditTextProfile = findViewById(R.id.provinceEditTextProfile);
        cityEditTextProfile = findViewById(R.id.cityEditTextProfile);
        viewGamesButton = findViewById(R.id.viewGamesButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        editProfileLoadingProgressBar = findViewById(R.id.editProfileLoadingProgressBar);

        tokenManager = ApiClient.getTokenManager(getApplicationContext());

        // Inicializar ViewModel
        editProfileViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(EditProfileViewModel.class);

        setupViewModelObservers();

        // Cargar datos del usuario actual
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
            userNameTextViewEdit.setText(R.string.user_profile_load_error); // Nueva string
            // Dejar los EditText vacíos o con un hint indicando error
            publicNameEditTextProfile.setText("");
            provinceEditTextProfile.setText("");
            cityEditTextProfile.setText("");
            return;
        }
        Log.d(TAG, "Poblando UI con datos de: " + user.getPublicName());
        currentEditingUserId = user.getId(); // Guardar el ID del usuario actual
        userNameTextViewEdit.setText(user.getPublicName()); // Mostrar el publicName actual
        publicNameEditTextProfile.setText(user.getPublicName()); // Permitir editar publicName
        provinceEditTextProfile.setText(user.getProvince() != null ? user.getProvince() : "");
        cityEditTextProfile.setText(user.getCity() != null ? user.getCity() : "");

        // Cargar imagen de perfil si tienes la URL y Glide
        // if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
        //    Glide.with(this).load(user.getProfileImageUrl()).placeholder(R.drawable.perfil_usuario).into(userProfileImageViewEdit);
        // } else {
        //    userProfileImageViewEdit.setImageResource(R.drawable.perfil_usuario); // Placeholder
        // }
    }

    private void attemptSaveChanges() {
        // String name = nameEditTextProfile.getText().toString().trim(); // Eliminado
        String publicName = publicNameEditTextProfile.getText().toString().trim();
        String province = provinceEditTextProfile.getText().toString().trim();
        String city = cityEditTextProfile.getText().toString().trim();

        // Solo validamos los campos que se editan
        if (publicName.isEmpty() || province.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, R.string.error_complete_all_editable_fields, Toast.LENGTH_SHORT).show(); // Nueva string
            return;
        }

        if (currentEditingUserId == null) {
            Toast.makeText(this, "Error: No se pudo identificar al usuario para actualizar.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "currentEditingUserId es null en attemptSaveChanges");
            return;
        }

        // UserDTO para la actualización:
        // 'name' (nombre real) no se envía o se envía como null si no se puede cambiar.
        // 'email' y 'password' tampoco se actualizan desde este formulario.
        UserDTO updatedUserDTO = new UserDTO(
                null, // name (nombre real) - no se edita aquí
                publicName,
                null, // email
                province,
                city,
                null, // password
                null, // registrationDate
                null, // games
                null  // roles
        );

        Log.d(TAG, "Intentando guardar cambios para userId: " + currentEditingUserId + " con publicName: " + publicName);
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
                // No es necesariamente un error si es la primera carga y aún no hay datos,
                // el error se manejaría con errorMessage LiveData.
                // Si el usuario es null después de un intento de carga, populateUI mostrará el error.
                populateUI(null); // Llamar para mostrar estado de error/vacío
            }
        });

        editProfileViewModel.updateSuccess.observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(EditProfileActivity.this, R.string.profile_updated_successfully, Toast.LENGTH_SHORT).show(); // Nueva string
                finish(); // Volver a la pantalla anterior (UserProfileActivity)
            }
            // Si isSuccess es false, el error se maneja con errorMessage LiveData
        });

        editProfileViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "errorMessage LiveData changed: " + error);
                Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_LONG).show();
                editProfileViewModel.clearErrorMessage(); // Limpiar el error después de mostrarlo
            }
        });
    }
}
