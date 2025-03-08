package com.example.gamelend.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamelend.R;

public class MainActivity extends AppCompatActivity {
    private EditText editTextUsuario, editTextContrasena;
    private Button buttonEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editTextUsuario = findViewById(R.id.editTextUsuario);
        editTextContrasena = findViewById(R.id.editTextContrasena);
        buttonEntrar = findViewById(R.id.buttonEntrar);

        buttonEntrar.setOnClickListener(v -> {
            ValidarUsuario();
        });
    }
    private void ValidarUsuario() {
        String usuario = editTextUsuario.getText().toString();
        String contrasena = editTextContrasena.getText().toString();

        //CONTROLAMOS CAMPOS VACIOS
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "No se admiten campos vacios.", Toast.LENGTH_SHORT).show();
            return;
        }else{
            Intent intent = new Intent(this, ListaUsuarios.class);
            startActivity(intent);
        }

        //CONTROLAMOS USUARIO Y CONTRASEÑA
    }
}