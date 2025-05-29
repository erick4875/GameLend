package com.example.gamelend.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gamelend.R;

public class UserDetailActivity extends AppCompatActivity {

    public static final String EXTRA_USER_PUBLIC_NAME = "EXTRA_USER_PUBLIC_NAME";
    public static final String EXTRA_USER_EMAIL = "EXTRA_USER_EMAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        Toolbar toolbar = findViewById(R.id.userDetailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle del Usuario");
        }

        TextView textViewUsername = findViewById(R.id.textViewUsername);
        TextView textViewEmail = findViewById(R.id.textViewEmail);

        String username = getIntent().getStringExtra(EXTRA_USER_PUBLIC_NAME);
        String email = getIntent().getStringExtra(EXTRA_USER_EMAIL);

        textViewUsername.setText("Usuario: " + username);
        textViewEmail.setText("Correo: " + email);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Para que al presionar la flecha atrás, se cierre esta actividad
        return true;
    }
}