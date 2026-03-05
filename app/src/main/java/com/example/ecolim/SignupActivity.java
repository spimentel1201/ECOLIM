package com.example.ecolim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText nameEditText, dniEditText, passwordEditText;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = new DatabaseHelper(this);

        nameEditText = findViewById(R.id.nameEditText);
        dniEditText = findViewById(R.id.dniEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button signupButton = findViewById(R.id.signupButton);

        signupButton.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = nameEditText.getText().toString().trim();
        String dni = dniEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (nombre.isEmpty() || dni.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dni.length() != 8) {
            Toast.makeText(this, "El DNI debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = dbHelper.registrarUsuario(dni, nombre, password);

        if (id != -1) {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignupActivity.this, SignupConfirmationActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error al registrar: DNI ya existe", Toast.LENGTH_SHORT).show();
        }
    }
}
