package com.example.gamelend.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.gamelend.R;
import com.example.gamelend.auth.TokenManager;
import com.example.gamelend.dto.UserDTO;
import com.example.gamelend.dto.UserResponseDTO;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.viewmodel.EditProfileViewModel;

import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private ImageView userProfileImageViewEdit, logoImageViewEditProfile;
    private TextView userNameTextViewEdit;
    private EditText nameEditTextProfile, publicNameEditTextProfile,
            passwordEditTextProfile,
            provinceEditTextProfile, cityEditTextProfile;

    private Button viewGamesButton, saveChangesButton;
    private ProgressBar editProfileLoadingProgressBar;

    private EditProfileViewModel editProfileViewModel;
    private TokenManager tokenManager;
    private Long currentEditingUserId;
    private Uri selectedImageUri;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private ActivityResultLauncher<Intent> legacyImagePickerLauncher;


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
        nameEditTextProfile = findViewById(R.id.nameEditTextProfile);
        publicNameEditTextProfile = findViewById(R.id.publicNameEditTextProfile);
        passwordEditTextProfile = findViewById(R.id.passwordEditTextProfile);
        provinceEditTextProfile = findViewById(R.id.provinceEditTextProfile);
        cityEditTextProfile = findViewById(R.id.cityEditTextProfile);
        viewGamesButton = findViewById(R.id.viewGamesButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        editProfileLoadingProgressBar = findViewById(R.id.editProfileLoadingProgressBar);

        tokenManager = ApiClient.getTokenManager(getApplicationContext());

        editProfileViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(EditProfileViewModel.class);

        setupImagePickerLaunchers();
        setupViewModelObservers();

        Log.d(TAG, "Solicitando datos del perfil para editar...");
        editProfileViewModel.fetchCurrentUserData();

        userProfileImageViewEdit.setOnClickListener(v -> openImageChooser());
        viewGamesButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, GameListActivity.class);
            startActivity(intent);
        });
        saveChangesButton.setOnClickListener(v -> attemptSaveChanges());
    }

    private void setupImagePickerLaunchers() {
        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                displaySelectedImage(selectedImageUri);
            }
        });

        legacyImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            selectedImageUri = uri;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                int intentFlags = result.getData().getFlags();
                                int takeFlags = 0;
                                if ((intentFlags & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) takeFlags |= Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                if ((intentFlags & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0) takeFlags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                                if (takeFlags != 0) {
                                    try { getContentResolver().takePersistableUriPermission(uri, takeFlags); }
                                    catch (SecurityException e) { Log.e(TAG, "Error al tomar permisos persistentes", e); }
                                }
                            }
                            displaySelectedImage(selectedImageUri);
                        }
                    }
                });
    }

    private void openImageChooser() {
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this)) {
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            legacyImagePickerLauncher.launch(intent);
        }
    }

    private void displaySelectedImage(Uri imageUri) {
        Glide.with(this).load(imageUri).placeholder(R.drawable.perfil_usuario)
                .error(R.drawable.perfil_usuario_error)
                .circleCrop().into(userProfileImageViewEdit);
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
            userNameTextViewEdit.setText(R.string.user_profile_load_error);
            nameEditTextProfile.setText("");
            publicNameEditTextProfile.setText("");
            provinceEditTextProfile.setText("");
            cityEditTextProfile.setText("");
            passwordEditTextProfile.setText("");
            if (userProfileImageViewEdit != null)
                userProfileImageViewEdit.setImageResource(R.drawable.perfil_usuario);
            return;
        }
        currentEditingUserId = user.getId();
        userNameTextViewEdit.setText(user.getPublicName());
        nameEditTextProfile.setText(user.getName() != null ? user.getName() : "");
        publicNameEditTextProfile.setText(user.getPublicName() != null ? user.getPublicName() : "");
        provinceEditTextProfile.setText(user.getProvince() != null ? user.getProvince() : "");
        cityEditTextProfile.setText(user.getCity() != null ? user.getCity() : "");
        passwordEditTextProfile.setText("");

        // En populateUI
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            String relativeUrl = user.getProfileImageUrl();
            String fullImageUrl = ApiClient.BASE_URL + relativeUrl;

            Log.d(TAG, "Cargando imagen de perfil con Glide desde: " + fullImageUrl);
            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.perfil_usuario)
                    .error(R.drawable.perfil_usuario_error) // Asegúrate que este drawable exista
                    .circleCrop()
                    .into(userProfileImageViewEdit);
        } else if (userProfileImageViewEdit != null) {
            userProfileImageViewEdit.setImageResource(R.drawable.perfil_usuario);
        }
    }

    private void attemptSaveChanges() {
        String name = nameEditTextProfile.getText().toString().trim();
        String publicName = publicNameEditTextProfile.getText().toString().trim();
        String newPassword = passwordEditTextProfile.getText().toString();
        String province = provinceEditTextProfile.getText().toString().trim();
        String city = cityEditTextProfile.getText().toString().trim();

        if (publicName.isEmpty() || name.isEmpty() || province.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, R.string.error_complete_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.isEmpty() && newPassword.length() < 8) {
            Toast.makeText(this, R.string.error_password_length, Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentEditingUserId == null) {
            Toast.makeText(this, "Error: No se pudo identificar al usuario.", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedImageUri != null) {
            Log.d(TAG, "Intentando subir imagen de perfil...");
            editProfileViewModel.uploadProfileImage(currentEditingUserId, selectedImageUri);
        } else {
            Log.d(TAG, "No se seleccionó nueva imagen, actualizando solo datos de texto.");
            UserDTO textDataUserDTO = new UserDTO(
                    name.isEmpty() ? null : name, publicName, null, province, city,
                    newPassword.isEmpty() ? null : newPassword,
                    null, null, null
            );
            editProfileViewModel.updateUserProfile(currentEditingUserId, textDataUserDTO);
        }
    }

    private void setupViewModelObservers() {
        editProfileViewModel.isLoading.observe(this, isLoading -> {
            if (isLoading != null) {
                editProfileLoadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                saveChangesButton.setEnabled(!isLoading);
                viewGamesButton.setEnabled(!isLoading);
                userProfileImageViewEdit.setEnabled(!isLoading);
            }
        });

        editProfileViewModel.userData.observe(this, user -> {
            if (user != null) populateUI(user); else populateUI(null);
        });

        editProfileViewModel.profileImageUploadResult.observe(this, userResponseAfterUpload -> {
            if (userResponseAfterUpload != null) {
                Toast.makeText(this, "Imagen de perfil subida con éxito.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Imagen subida, nueva URL de perfil: " +
                        (userResponseAfterUpload.getProfileImageUrl() != null ? userResponseAfterUpload.getProfileImageUrl() : "N/A"));

                String name = nameEditTextProfile.getText().toString().trim();
                String publicName = publicNameEditTextProfile.getText().toString().trim();
                String newPassword = passwordEditTextProfile.getText().toString();
                String province = provinceEditTextProfile.getText().toString().trim();
                String city = cityEditTextProfile.getText().toString().trim();

                UserDTO textDataUserDTO = new UserDTO(
                        name.isEmpty() ? null : name, publicName, null, province, city,
                        newPassword.isEmpty() ? null : newPassword,
                        null, null, null
                );
                editProfileViewModel.updateUserProfile(currentEditingUserId, textDataUserDTO);
            }
        });

        editProfileViewModel.imageUploadError.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error al subir imagen: " + error, Toast.LENGTH_LONG).show();
                editProfileViewModel.clearImageUploadError();
            }
        });

        editProfileViewModel.updateSuccess.observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(EditProfileActivity.this, R.string.profile_updated_successfully, Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        editProfileViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(EditProfileActivity.this, "Error al actualizar perfil: " + error, Toast.LENGTH_LONG).show();
                editProfileViewModel.clearErrorMessage();
            }
        });
    }
}