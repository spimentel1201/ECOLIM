package com.example.ecolim;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewWasteActivity extends AppCompatActivity {

    private Spinner typeSpinner;
    private EditText weightEditText, zoneEditText, quantityEditText;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_waste);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        typeSpinner = findViewById(R.id.typeSpinner);
        weightEditText = findViewById(R.id.weightEditText);
        zoneEditText = findViewById(R.id.zoneEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        Button saveButton = findViewById(R.id.saveButton);

        setupSpinner();

        saveButton.setOnClickListener(v -> guardarRegistro());
    }

    private void setupSpinner() {
        String[] types = {"ORGANICO", "PLASTICO", "CARTON", "METAL", "PELIGROSO"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
    }

    private void guardarRegistro() {
        String tipo = typeSpinner.getSelectedItem().toString();
        String pesoStr = weightEditText.getText().toString().trim();
        String zona = zoneEditText.getText().toString().trim();
        String cantidadStr = quantityEditText.getText().toString().trim();

        if (pesoStr.isEmpty() || zona.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double peso = Double.parseDouble(pesoStr);
            int cantidad = Integer.parseInt(cantidadStr);
            String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            long id = dbHelper.registrarResiduo(tipo, peso, zona, cantidad, fecha);

            if (id != -1) {
                Toast.makeText(this, "Registro guardado correctamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al guardar el registro", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Peso o cantidad inválidos", Toast.LENGTH_SHORT).show();
        }
    }
}
