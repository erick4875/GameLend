package com.example.gamelend.Activitys;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelend.Clases.ListAdapter;
import com.example.gamelend.Clases.Usuario;
import com.example.gamelend.Conexion.ApiService;
import com.example.gamelend.Conexion.ApiClient;
import com.example.gamelend.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaUsuarios extends AppCompatActivity {
//    List<Usuario> usuarios;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_lista_usuarios);
//        CargarUsuarios();
//
//    }
//
//    private void CargarUsuarios() {
//
//        usuarios = new ArrayList<>();
//        usuarios.add(new Usuario("User1", "1234", "Erick","Barcelona"));
//        usuarios.add(new Usuario("User2", "1234", "Juan","Madrid"));
//        usuarios.add(new Usuario("User3", "1234", "Pedro","Valencia"));
//        usuarios.add(new Usuario("User4", "1234", "Maria","Sevilla"));
//        usuarios.add(new Usuario("User5", "1234", "Ana","Bilbao"));
//        usuarios.add(new Usuario("User6", "1234", "Luis","Malaga"));
//
//        ListAdapter listAdapter = new ListAdapter(usuarios, this);
//        RecyclerView recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(listAdapter);
//
//    }

    private ApiService apiService;  // Asegúrate de tener la referencia al ApiService

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_usuarios);

        apiService = ApiClient.getConexion(this).create(ApiService.class); // Inicializamos apiService
        CargarUsuarios();
    }

    private void CargarUsuarios() {
        apiService.obtenerUsuarios().enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful()) {
                    List<Usuario> usuarios = response.body();
                    // Actualizamos la lista de usuarios en el RecyclerView
                    ListAdapter listAdapter = new ListAdapter(usuarios, ListaUsuarios.this);
                    RecyclerView recyclerView = findViewById(R.id.recyclerView);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ListaUsuarios.this));
                    recyclerView.setAdapter(listAdapter);
                } else {
                    Toast.makeText(ListaUsuarios.this, "Error al cargar los usuarios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                Toast.makeText(ListaUsuarios.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

}