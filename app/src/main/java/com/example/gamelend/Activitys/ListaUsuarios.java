package com.example.gamelend.Activitys;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelend.Clases.ListAdapter;
import com.example.gamelend.Clases.Usuario;
import com.example.gamelend.R;

import java.util.ArrayList;
import java.util.List;

public class ListaUsuarios extends AppCompatActivity {
    List<Usuario> usuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_usuarios);
        CargarUsuarios();

    }

    private void CargarUsuarios(){
        usuarios = new ArrayList<>();
        usuarios.add(new Usuario("User1", "1234", "Erick","Barcelona"));
        usuarios.add(new Usuario("User2", "1234", "Juan","Madrid"));
        usuarios.add(new Usuario("User3", "1234", "Pedro","Valencia"));
        usuarios.add(new Usuario("User4", "1234", "Maria","Sevilla"));
        usuarios.add(new Usuario("User5", "1234", "Ana","Bilbao"));
        usuarios.add(new Usuario("User6", "1234", "Luis","Malaga"));

        ListAdapter listAdapter = new ListAdapter(usuarios, this);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(listAdapter);

    }
}