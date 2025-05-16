package com.example.gamelend.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamelend.R;

public class InfoUsuario extends AppCompatActivity {
    // Variables de la interfaz
    private ImageView imageViewUsuario, imageViewLogo;
    private TextView tVNombre;
    private Button btnEditarPerfil, btnAñadirJuego;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_usuario);

        // Inicialización de los componentes de la interfaz
        imageViewUsuario = findViewById(R.id.imageViewUsuario);
        imageViewLogo = findViewById(R.id.imageViewLogo);
        tVNombre = findViewById(R.id.tVNombre);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnAñadirJuego = findViewById(R.id.btnAñadirJuego);

        // Ajustar padding para la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configuración de eventos
        btnEditarPerfil.setOnClickListener(view -> {
            Intent intent = new Intent(InfoUsuario.this, EditarPerfil.class);
            startActivity(intent);
        });

        btnAñadirJuego.setOnClickListener(view -> {
            Intent intent = new Intent(InfoUsuario.this, AgregarJuego.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
