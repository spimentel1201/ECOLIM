package com.example.ecolim.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecolim.R;
import com.example.ecolim.data.local.UsuarioDAO;
import com.example.ecolim.data.model.Usuario;
import com.example.ecolim.ui.home.HomeActivity;

/**
 * Actividad encargada de la autenticación de usuarios.
 * Permite a los operarios ingresar al sistema validando su DNI y contraseña.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText dniEditText, passwordEditText;
    private UsuarioDAO usuarioDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usuarioDAO = new UsuarioDAO(this);

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

        // Consulta mediante DAO para verificar credenciales
        if (usuarioDAO.login(dni, password)) {
            // Persistencia de sesión: Obtener objeto Usuario del DAO
            Usuario usuario = usuarioDAO.obtenerPorDni(dni);
            if (usuario != null) {
                SharedPreferences preferences = getSharedPreferences("EcolimPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("user_dni", usuario.getDni());
                editor.putString("user_nombre", usuario.getNombre());
                editor.apply();
            }

            // Redirección al Dashboard tras éxito
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "DNI o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
}
