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
import com.example.gamelend.dto.RespuestaGeneral;
import com.example.gamelend.dto.UsuarioResponseDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaUsuarios extends AppCompatActivity {
    private ApiService apiService;  // Asegúrate de tener la referencia al ApiService

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_usuarios);

        apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class); // Inicializamos apiService
        CargarUsuarios();
    }

    private void CargarUsuarios() {
        apiService.getUsuarios().enqueue(new Callback<RespuestaGeneral<List<UsuarioResponseDTO>>>() {
            @Override
            public void onResponse(Call<RespuestaGeneral<List<UsuarioResponseDTO>>> call, Response<RespuestaGeneral<List<UsuarioResponseDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isExito()) {

                    List<UsuarioResponseDTO> usuarios = response.body().getCuerpo();

                    ListAdapter listAdapter = new ListAdapter(usuarios, ListaUsuarios.this, new ListAdapter.OnItemClickListener() {

                        public void onEdit(UsuarioResponseDTO usuario) {
                            // Acciones al editar (pendiente implementar)
                            Toast.makeText(ListaUsuarios.this, "Editar: " + usuario.getNombrePublico(), Toast.LENGTH_SHORT).show();
                        }

                        public void onDelete(UsuarioResponseDTO usuario) {
                            // Acciones al eliminar (pendiente implementar)
                            Toast.makeText(ListaUsuarios.this, "Eliminar: " + usuario.getNombrePublico(), Toast.LENGTH_SHORT).show();
                        }


                        public void onJuegosClick(UsuarioResponseDTO usuario) {
                            // Aquí manejas el evento click para los botones "Juegos"
                            Toast.makeText(ListaUsuarios.this, "Juegos: " + usuario.getNombrePublico(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    RecyclerView recyclerView = findViewById(R.id.recyclerView);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ListaUsuarios.this));
                    recyclerView.setAdapter(listAdapter);

                } else {
                    Toast.makeText(ListaUsuarios.this, "Error al obtener los usuarios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaGeneral<List<UsuarioResponseDTO>>> call, Throwable t) {
                Toast.makeText(ListaUsuarios.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}