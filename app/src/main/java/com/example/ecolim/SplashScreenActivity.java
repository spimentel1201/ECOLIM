package com.example.ecolim;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 5000; // 5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Ocultar la barra de acción para una experiencia de pantalla completa
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Usar un Handler para retrasar el inicio de la siguiente actividad
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Crear un Intent para iniciar LoginActivity
            Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(intent);

            // Cerrar la actividad actual para que el usuario no pueda volver a ella
            finish();
        }, SPLASH_DELAY);
    }
}
