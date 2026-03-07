package com.example.ecolim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Actividad encargada de la autenticación de usuarios.
 * Permite a los operarios ingresar al sistema validando su DNI y contraseña.
 */
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

        // Listener para procesar el inicio de sesión
        loginButton.setOnClickListener(v -> login());
        
        // Listener para navegar a la pantalla de registro
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Valida los datos ingresados y realiza la autenticación.
     * Se implementó con SharedPreferences para mantener una sesión básica del usuario.
     */
    private void login() {
        String dni = dniEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validación de campos vacíos
        if (dni.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese DNI y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        // Consulta a la base de datos local para verificar credenciales
        if (dbHelper.login(dni, password)) {
            // Persistencia de sesión: Guardar datos del usuario logueado
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

            // Redirección al Dashboard tras éxito
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Finaliza LoginActivity para evitar volver atrás
        } else {
            Toast.makeText(this, "DNI o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
}
