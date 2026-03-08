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

import com.example.ecolim.R;
import com.example.ecolim.data.local.ResiduoDAO;
import com.example.ecolim.data.local.DatabaseHelper;
import com.example.ecolim.ui.reports.ReportsActivity;
import com.example.ecolim.ui.waste.NewWasteActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Locale;

/**
 * Pantalla principal (Dashboard) de la aplicación.
 */
public class HomeActivity extends AppCompatActivity {

    private TextView totalKgTextView, totalHistoricoTextView, welcomeTextView;
    private LinearLayout recentCollectionsContainer;
    private ResiduoDAO residuoDAO;
    
    private final String[] tiposResiduos = {"ORGANICO", "PLASTICO", "PAPEL", "METAL", "VIDRIO", "CARTON", "PELIGROSO"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        residuoDAO = new ResiduoDAO(this);
        totalKgTextView = findViewById(R.id.totalKgTextView);
        totalHistoricoTextView = findViewById(R.id.totalHistoricoTextView);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        recentCollectionsContainer = findViewById(R.id.recentCollectionsContainer);

        mostrarBienvenida();

        ImageView btnGoToReports = findViewById(R.id.btnGoToReports);
        btnGoToReports.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReportsActivity.class);
            startActivity(intent);
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, NewWasteActivity.class);
            startActivity(intent);
        });
    }

    private void mostrarBienvenida() {
        SharedPreferences preferences = getSharedPreferences("EcolimPrefs", MODE_PRIVATE);
        String nombre = preferences.getString("user_nombre", "Usuario");
        welcomeTextView.setText("Bienvenido, " + toCamelCase(nombre));
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        actualizarDatos();
    }

    private void actualizarDatos() {
        double totalPesoHoy = residuoDAO.obtenerTotalPesoHoy();
        totalKgTextView.setText(String.format(Locale.getDefault(), "%,.1f Kg", totalPesoHoy));

        double totalPesoHistorico = residuoDAO.obtenerTotalPesoHistorico();
        totalHistoricoTextView.setText(String.format(Locale.getDefault(), "%,.1f Kg", totalPesoHistorico));

        recentCollectionsContainer.removeAllViews();

        for (String tipo : tiposResiduos) {
            double pesoTipo = 0;
            int cantidadTipo = 0;
            String ultimaFecha = "--:--";

            Cursor cursor = residuoDAO.obtenerResumenPorTipo(tipo);

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

        switch (tipo) {
            case "PLASTICO": iconView.setImageResource(R.drawable.ic_recycle); break;
            case "PAPEL":
            case "CARTON": iconView.setImageResource(R.drawable.ic_box); break;
            case "ORGANICO": iconView.setImageResource(R.drawable.ic_leaf); break;
            case "METAL": iconView.setImageResource(R.drawable.ic_scale_green); break;
            case "VIDRIO": iconView.setImageResource(R.drawable.ic_bottle); break;
            case "PELIGROSO": iconView.setImageResource(R.drawable.ic_info); break;
            default: iconView.setImageResource(R.drawable.ic_recycle); break;
        }

        recentCollectionsContainer.addView(itemView);
    }
}
