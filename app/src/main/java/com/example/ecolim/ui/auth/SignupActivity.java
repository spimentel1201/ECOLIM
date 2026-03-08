package com.example.ecolim.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecolim.R;
import com.example.ecolim.data.local.DatabaseHelper;

/**
 * Actividad para el registro de nuevos operarios.
 * Permite crear una cuenta local para acceder a la aplicación.
 */
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

        // Listener para procesar el registro del nuevo usuario
        signupButton.setOnClickListener(v -> registrarUsuario());
    }

    /**
     * Valida los datos e inserta el nuevo usuario en la base de datos.
     * Incluye validaciones de negocio específicas como la longitud del DNI.
     */
    private void registrarUsuario() {
        String nombre = nameEditText.getText().toString().trim();
        String dni = dniEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validación de campos obligatorios
        if (nombre.isEmpty() || dni.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de formato de DNI (Regla de negocio: 8 dígitos)
        if (dni.length() != 8) {
            Toast.makeText(this, "El DNI debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Intento de registro en la base de datos local
        long id = dbHelper.registrarUsuario(dni, nombre, password);

        if (id != -1) {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
            // Navegación a la pantalla de confirmación tras éxito
            Intent intent = new Intent(SignupActivity.this, SignupConfirmationActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Manejo de error si el DNI ya está registrado (restricción UNIQUE en SQL)
            Toast.makeText(this, "Error al registrar: DNI ya existe", Toast.LENGTH_SHORT).show();
        }
    }
}
