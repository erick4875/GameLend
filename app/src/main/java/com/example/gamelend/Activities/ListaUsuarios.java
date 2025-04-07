package com.example.gamelend.Activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelend.Models.ListAdapter;
import com.example.gamelend.remote.api.ApiService;
import com.example.gamelend.remote.api.ApiClient;
import com.example.gamelend.R;
import com.example.gamelend.dto.UsuarioResponseDTO;
import com.example.gamelend.repository.UserRepository;
import com.example.gamelend.viewmodel.ListaUsuariosViewModel;

public class ListaUsuarios extends AppCompatActivity {

    private ListaUsuariosViewModel viewModel;
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;
    private ApiService apiService;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_usuarios);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrofit + Repository + ViewModel manuales (luego te muestro con Hilt)
        apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
        userRepository = new UserRepository(apiService);
        viewModel = new ListaUsuariosViewModel(userRepository);

        observarUsuarios();
    }

    private void observarUsuarios() {
        viewModel.getUsuarios().observe(this, usuarios -> {
            if (usuarios != null) {
                listAdapter = new ListAdapter(usuarios, ListaUsuarios.this, new ListAdapter.OnItemClickListener() {
                    @Override
                    public void onEdit(UsuarioResponseDTO usuario) {
                        Toast.makeText(ListaUsuarios.this, "Editar: " + usuario.getNombrePublico(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDelete(UsuarioResponseDTO usuario) {
                        Toast.makeText(ListaUsuarios.this, "Eliminar: " + usuario.getNombrePublico(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onJuegosClick(UsuarioResponseDTO usuario) {
                        Toast.makeText(ListaUsuarios.this, "Juegos: " + usuario.getNombrePublico(), Toast.LENGTH_SHORT).show();
                    }
                });

                recyclerView.setAdapter(listAdapter);
            } else {
                Toast.makeText(ListaUsuarios.this, "Error al obtener los usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
