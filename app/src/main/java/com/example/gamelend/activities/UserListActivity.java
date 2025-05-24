package com.example.gamelend.activities;

import android.os.Bundle;
import android.view.View; // Para la visibilidad del ProgressBar
import android.widget.ProgressBar; // Para el indicador de carga
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider; // Para obtener el ViewModel correctamente
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Asegúrate de que ListAdapter y UserResponseDTO usen nombres en inglés si los has traducido
import com.example.gamelend.models.ListAdapter; // Asumiendo que este es tu adaptador de UI
import com.example.gamelend.R;
import com.example.gamelend.dto.UserResponseDTO; // El DTO que usa el ListAdapter
import com.example.gamelend.viewmodel.UserListViewModel; // Tu ViewModel

import java.util.ArrayList; // Para inicializar el adaptador con una lista vacía

public class UserListActivity extends AppCompatActivity {

    private UserListViewModel userListViewModel;
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;
    private ListAdapter.OnItemClickListener itemClickListener;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Mantenlo si lo estás usando
        setContentView(R.layout.activity_user_list);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBarUsers);

        // Configurar RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemClickListener = new ListAdapter.OnItemClickListener() {
            @Override
            public void onEdit(UserResponseDTO user) {
                Toast.makeText(UserListActivity.this, "Edit: " + user.getPublicName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(UserResponseDTO user) {
                Toast.makeText(UserListActivity.this, "Delete: " + user.getPublicName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGamesClick(UserResponseDTO user) {
                Toast.makeText(UserListActivity.this, "Games: " + user.getPublicName(), Toast.LENGTH_SHORT).show();
            }
        };

        // inicializar el adaptador con el contexto y el listener
        listAdapter = new ListAdapter(this, itemClickListener);
        recyclerView.setAdapter(listAdapter);
        // Observar los LiveData del ViewModel
        observeViewModel();
        // Solicitar la carga de usuarios
        userListViewModel.fetchUsers();
    }

    private void observeViewModel() {
        // Observar el estado de carga
        userListViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE); // Ocultar lista mientras carga
            } else {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE); // Mostrar lista cuando termina
            }
        });

        // Observar la lista de usuarios
        userListViewModel.getUsersLiveData().observe(this, users -> {
            if (users != null) {
                listAdapter.submitList(users);
            }
        });

        // Observar los errores
        userListViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(UserListActivity.this, error, Toast.LENGTH_LONG).show();
                userListViewModel.clearError();
            }
        });
    }
}