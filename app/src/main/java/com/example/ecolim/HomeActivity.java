package com.example.ecolim;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView totalKgTextView;
    private LinearLayout recentCollectionsContainer;
    private DatabaseHelper dbHelper;
    private final String[] tiposResiduos = {"ORGANICO", "PLASTICO", "CARTON", "METAL", "PELIGROSO"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        totalKgTextView = findViewById(R.id.totalKgTextView);
        recentCollectionsContainer = findViewById(R.id.recentCollectionsContainer);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, NewWasteActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarDatos();
    }

    private void actualizarDatos() {
        // Actualizar Peso Total de Hoy
        double totalPeso = dbHelper.obtenerTotalPesoHoy();
        totalKgTextView.setText(String.format(Locale.getDefault(), "%,.1f Kg", totalPeso));

        // Limpiar contenedor
        recentCollectionsContainer.removeAllViews();

        // Mostrar resumen por cada tipo (incluso si es 0)
        for (String tipo : tiposResiduos) {
            double pesoTipo = 0;
            int cantidadTipo = 0;
            String ultimaFecha = "--:--";

            // Consultar DB para este tipo específico
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT SUM(peso), SUM(cantidad), MAX(fecha_registro) FROM residuos WHERE tipo = ?",
                    new String[]{tipo});

            if (cursor != null && cursor.moveToFirst()) {
                pesoTipo = cursor.getDouble(0);
                cantidadTipo = cursor.getInt(1);
                String fechaRaw = cursor.getString(2);
                if (fechaRaw != null && fechaRaw.length() >= 16) {
                    ultimaFecha = fechaRaw.substring(11, 16);
                }
            }
            if (cursor != null) cursor.close();

            agregarItemResumen(tipo, pesoTipo, cantidadTipo, ultimaFecha);
        }
    }

    @SuppressLint("DefaultLocale")
    private void agregarItemResumen(String tipo, double peso, int cantidad, String hora) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_recoleccion, recentCollectionsContainer, false);

        ImageView iconView = itemView.findViewById(R.id.itemIcon);
        TextView titleView = itemView.findViewById(R.id.itemTitle);
        TextView detailsView = itemView.findViewById(R.id.itemDetails);
        TextView weightView = itemView.findViewById(R.id.itemWeight);

        titleView.setText(tipo);
        detailsView.setText(String.format("Cant: %d | Último: %s", cantidad, hora));
        weightView.setText(String.format(Locale.getDefault(), "%.1f Kg", peso));

        // Asignar icono según tipo
        switch (tipo) {
            case "PLASTICO":
                iconView.setImageResource(R.drawable.ic_recycle);
                break;
            case "CARTON":
                iconView.setImageResource(R.drawable.ic_box);
                break;
            case "ORGANICO":
                iconView.setImageResource(R.drawable.ic_leaf);
                break;
            case "METAL":
                iconView.setImageResource(R.drawable.ic_scale_green);
                break;
            case "PELIGROSO":
                iconView.setImageResource(R.drawable.ic_info);
                break;
        }

        recentCollectionsContainer.addView(itemView);
    }
}
