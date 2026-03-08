package com.example.ecolim.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecolim.ui.waste.NewWasteActivity;
import com.example.ecolim.R;
import com.example.ecolim.ui.reports.ReportsActivity;
import com.example.ecolim.data.local.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Locale;

/**
 * Pantalla principal (Dashboard) de la aplicación.
 * Muestra un resumen de las recolecciones del día y permite acceder al registro de nuevos residuos.
 */
public class HomeActivity extends AppCompatActivity {

    private TextView totalKgTextView, totalHistoricoTextView, welcomeTextView;
    private LinearLayout recentCollectionsContainer;
    private DatabaseHelper dbHelper;
    
    // Lista de tipos sincronizada con NewWasteActivity y el modelo IA
    private final String[] tiposResiduos = {"ORGANICO", "PLASTICO", "PAPEL", "METAL", "VIDRIO", "CARTON", "PELIGROSO"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        totalKgTextView = findViewById(R.id.totalKgTextView);
        totalHistoricoTextView = findViewById(R.id.totalHistoricoTextView);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        recentCollectionsContainer = findViewById(R.id.recentCollectionsContainer);

        mostrarBienvenida();

        // Botón para ir a la pantalla de Reportes
        ImageView btnGoToReports = findViewById(R.id.btnGoToReports);
        btnGoToReports.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReportsActivity.class);
            startActivity(intent);
        });

        // Botón Flotante para ir a la pantalla de Nuevo Registro
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, NewWasteActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Recupera el nombre del usuario de SharedPreferences y lo muestra en formato CamelCase.
     */
    private void mostrarBienvenida() {
        SharedPreferences preferences = getSharedPreferences("EcolimPrefs", MODE_PRIVATE);
        String nombre = preferences.getString("user_nombre", "Usuario");
        welcomeTextView.setText("Bienvenido, " + toCamelCase(nombre));
    }

    /**
     * Convierte una cadena a formato CamelCase (Primera letra de cada palabra en mayúscula).
     */
    private String toCamelCase(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder sb = new StringBuilder();
        boolean nextTitleCase = true;
        for (char c : text.toLowerCase().toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Se ejecuta cada vez que la actividad vuelve al primer plano.
     * Implementado para asegurar que los datos del Dashboard estén siempre actualizados.
     */
    @Override
    protected void onResume() {
        super.onResume();
        actualizarDatos();
    }

    /**
     * Consulta la base de datos para obtener el peso total del día y el resumen por tipo.
     * Refresca la interfaz de usuario con la información más reciente.
     */
    private void actualizarDatos() {
        // Actualizar Peso Total recolectado hoy (KPI principal)
        double totalPesoHoy = dbHelper.obtenerTotalPesoHoy();
        totalKgTextView.setText(String.format(Locale.getDefault(), "%,.1f Kg", totalPesoHoy));

        // Actualizar Peso Total Histórico
        double totalPesoHistorico = dbHelper.obtenerTotalPesoHistorico();
        totalHistoricoTextView.setText(String.format(Locale.getDefault(), "%,.1f Kg", totalPesoHistorico));

        // Limpiar contenedor de la lista antes de volver a llenarlo
        recentCollectionsContainer.removeAllViews();

        // Iterar sobre los tipos de residuos para mostrar su estado actual
        for (String tipo : tiposResiduos) {
            double pesoTipo = 0;
            int cantidadTipo = 0;
            String ultimaFecha = "--:--";

            // Consulta específica para obtener totales y última hora por categoría
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT SUM(peso), SUM(cantidad), MAX(fecha_registro) FROM residuos WHERE tipo = ?",
                    new String[]{tipo});

            if (cursor != null && cursor.moveToFirst()) {
                pesoTipo = cursor.getDouble(0);
                cantidadTipo = cursor.getInt(1);
                String fechaRaw = cursor.getString(2);
                if (fechaRaw != null && fechaRaw.length() >= 16) {
                    // Extraer solo la hora del formato ISO (HH:mm)
                    ultimaFecha = fechaRaw.substring(11, 16);
                }
            }
            if (cursor != null) cursor.close();

            agregarItemResumen(tipo, pesoTipo, cantidadTipo, ultimaFecha);
        }
    }

    /**
     * Infla y añade dinámicamente una tarjeta de resumen al contenedor visual.
     * @param tipo Categoría del residuo.
     * @param peso Peso total acumulado.
     * @param cantidad Cantidad de registros.
     * @param hora Hora de la última recolección.
     */
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

        // Selección de icono representativo según el tipo de residuo
        switch (tipo) {
            case "PLASTICO":
                iconView.setImageResource(R.drawable.ic_recycle);
                break;
            case "PAPEL":
            case "CARTON":
                iconView.setImageResource(R.drawable.ic_box);
                break;
            case "ORGANICO":
                iconView.setImageResource(R.drawable.ic_leaf);
                break;
            case "METAL":
                iconView.setImageResource(R.drawable.ic_scale_green);
                break;
            case "VIDRIO":
                iconView.setImageResource(R.drawable.ic_bottle);
                break;
            case "PELIGROSO":
                iconView.setImageResource(R.drawable.ic_info);
                break;
            default:
                iconView.setImageResource(R.drawable.ic_recycle);
                break;
        }

        recentCollectionsContainer.addView(itemView);
    }
}
