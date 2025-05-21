package com.example.gamelend.Activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelend.Models.Game;
import com.example.gamelend.Models.GameAdapter;
import com.example.gamelend.R;

import java.util.ArrayList;
import java.util.List;

public class ListaJuegos extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GameAdapter gameAdapter;
    private List<Game> gameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_juegos);

        recyclerView = findViewById(R.id.recyclerViewJuegos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Configuración del LayoutManager

        // Cargar los juegos de manera local
        cargarJuegos();
    }

    private void cargarJuegos() {
        // Crear una lista de juegos estática
        gameList = new ArrayList<>();

        // Aquí agregamos algunos juegos de ejemplo a la lista
        gameList.add(new Game("Juego 1", R.drawable.mando));
        gameList.add(new Game("Juego 2", R.drawable.mando));
        gameList.add(new Game("Juego 3", R.drawable.mando));

        gameAdapter = new GameAdapter(ListaJuegos.this, gameList);

        recyclerView.setAdapter(gameAdapter);
    }


}