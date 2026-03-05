package com.example.ecolim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText dniEditText, passwordEditText;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        dniEditText = findViewById(R.id.dniEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> login());
        
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void login() {
        String dni = dniEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (dni.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese DNI y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.login(dni, password)) {
            // Guardar sesión
            Cursor cursor = dbHelper.obtenerUsuarioPorDni(dni);
            if (cursor != null && cursor.moveToFirst()) {
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NOMBRE));
                SharedPreferences preferences = getSharedPreferences("EcolimPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("user_dni", dni);
                editor.putString("user_nombre", nombre);
                editor.apply();
                cursor.close();
            }

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "DNI o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
}
