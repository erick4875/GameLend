
package com.example.gamelend.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelend.R;

public class EditarPerfil extends AppCompatActivity {

    EditText editNombre, editApellidos, editUsuario, editContrasena,
            editDireccion, editProvincia, editEmail, editTelefono;
    Button btnAceptarCambios, btnListaJuegos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        // Inicializar los EditText y el botón
        editNombre = findViewById(R.id.edit_nombre);
        editApellidos = findViewById(R.id.edit_apellidos);
        editUsuario = findViewById(R.id.edit_usuario);
        editContrasena = findViewById(R.id.edit_contrasena);
        editDireccion = findViewById(R.id.edit_direccion);
        editProvincia = findViewById(R.id.edit_provincia);
        editEmail = findViewById(R.id.edit_email);
        editTelefono = findViewById(R.id.edit_telefono);

        btnAceptarCambios = findViewById(R.id.btn_aceptar_cambios);
        btnListaJuegos = findViewById(R.id.btn_lista_juegos);

        // Botón para aceptar cambios
        btnAceptarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = editNombre.getText().toString();
                String apellidos = editApellidos.getText().toString();
                String usuario = editUsuario.getText().toString();
                String contrasena = editContrasena.getText().toString();
                String direccion = editDireccion.getText().toString();
                String provincia = editProvincia.getText().toString();
                String email = editEmail.getText().toString();
                String telefono = editTelefono.getText().toString();

                // Validar los campos
                if (nombre.isEmpty() || apellidos.isEmpty() || usuario.isEmpty() ||
                        contrasena.isEmpty() || direccion.isEmpty() || provincia.isEmpty() ||
                        email.isEmpty() || telefono.isEmpty()) {
                    Toast.makeText(EditarPerfil.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(EditarPerfil.this, "Cambios guardados", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón para ir a la lista de juegos
        btnListaJuegos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditarPerfil.this, ListaJuegos.class);
                startActivity(intent);
            }
        });
    }
}
