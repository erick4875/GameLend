package com.example.gamelend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelend.R;
import com.example.gamelend.dto.UserResponseDTO;
import com.example.gamelend.models.ListAdapter;
import com.example.gamelend.viewmodel.UserListViewModel;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity implements ListAdapter.OnItemClickListener {

    private static final String TAG = "UserListActivity";

    private UserListViewModel userListViewModel;
    private RecyclerView recyclerViewUsers;
    private ListAdapter userListAdapter;
    private ProgressBar progressBarUsers;
    private SearchView searchViewUsers;

    public static final String EXTRA_USER_ID = "com.example.gamelend.USER_ID_FOR_GAMES";
    public static final String EXTRA_USER_PUBLIC_NAME = "com.example.gamelend.USER_PUBLIC_NAME_FOR_GAMES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.userListToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_user_list);
        }

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        progressBarUsers = findViewById(R.id.progressBarUsers);
        searchViewUsers = findViewById(R.id.searchViewUsers);

        recyclerViewUsers.setHasFixedSize(true);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        userListAdapter = new ListAdapter(this, this);
        recyclerViewUsers.setAdapter(userListAdapter);

        userListViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(UserListViewModel.class);

        observeViewModel();
        setupSearchViewListener();

        Log.d(TAG, "onCreate: Solicitando lista de usuarios...");
        if (userListViewModel != null) {
            userListViewModel.fetchUsers();
        }
    }

    // Maneja el clic en el botón de "atrás" de la Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSearchViewListener() {

        searchViewUsers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "SearchView query submitted: " + query);
                Toast.makeText(UserListActivity.this, "Buscando: " + query, Toast.LENGTH_SHORT).show();
                searchViewUsers.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "SearchView text changed: " + newText);
                return true;
            }
        });
    }

    private void observeViewModel() {
        if (userListViewModel == null) {
            Log.e(TAG, "UserListViewModel no inicializado!");
            Toast.makeText(this, "Error al inicializar la vista de usuarios.", Toast.LENGTH_LONG).show();
            return;
        }

        userListViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            Log.d(TAG, "isLoading LiveData changed: " + isLoading);
            if (isLoading != null) {
                progressBarUsers.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                recyclerViewUsers.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            } else {
                progressBarUsers.setVisibility(View.GONE);
                recyclerViewUsers.setVisibility(View.VISIBLE);
            }
        });

        userListViewModel.getUsersLiveData().observe(this, users -> {
            Log.d(TAG, "Users LiveData changed, count: " + (users != null ? users.size() : "null"));
            if (users != null) {
                userListAdapter.submitList(users);
            } else {
                userListAdapter.submitList(new ArrayList<>());
            }
        });

        userListViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error LiveData changed: " + error);
                Toast.makeText(UserListActivity.this, error, Toast.LENGTH_LONG).show();
                if (userListViewModel != null) userListViewModel.clearError();
            }
        });
    }

    @Override
    public void onItemClick(UserResponseDTO user) {
        Log.d(TAG, "Usuario clickeado: " + user.getPublicName() + " (ID: " + user.getId() + ")");
        Intent intent = new Intent(UserListActivity.this, UserDetailActivity.class);
        intent.putExtra(UserDetailActivity.EXTRA_USER_PUBLIC_NAME, user.getPublicName());
        intent.putExtra(UserDetailActivity.EXTRA_USER_EMAIL, user.getEmail());
        startActivity(intent);
    }

    // --- Implementación de los métodos de OnItemClickListener ---
    @Override
    public void onEdit(UserResponseDTO user) {
        Toast.makeText(UserListActivity.this, "Acción Editar para: " + user.getPublicName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(UserResponseDTO user) {
        Toast.makeText(UserListActivity.this, "Acción Borrar para: " + user.getPublicName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGamesClick(UserResponseDTO user) {
        Log.d(TAG, "onGamesClick para usuario: " + user.getPublicName() + " (ID: " + user.getId() + ")");
        Toast.makeText(UserListActivity.this, "Viendo juegos de: " + user.getPublicName(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(UserListActivity.this, GameListActivity.class);
        intent.putExtra(EXTRA_USER_ID, user.getId());
        intent.putExtra(EXTRA_USER_PUBLIC_NAME, user.getPublicName());
        startActivity(intent);
    }
}

