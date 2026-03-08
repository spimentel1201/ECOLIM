package com.example.ecolim;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;

import com.google.android.material.datepicker.MaterialDatePicker;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Actividad para la generación de reportes en formato Excel (.xlsx).
 * Permite filtrar por rango de fechas y tipo de residuo.
 */
public class ReportsActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_STORAGE = 103;
    
    private TextView tvSelectedDates;
    private Spinner reportTypeSpinner;
    private DatabaseHelper dbHelper;
    private String startDate = "", endDate = "";
    
    private final String[] typesForReport = {"TODOS", "ORGANICO", "PLASTICO", "PAPEL", "METAL", "VIDRIO", "CARTON", "PELIGROSO"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvSelectedDates = findViewById(R.id.tvSelectedDates);
        reportTypeSpinner = findViewById(R.id.reportTypeSpinner);
        Button btnSelectDateRange = findViewById(R.id.btnSelectDateRange);
        Button btnExportExcel = findViewById(R.id.btnExportExcel);

        setupSpinner();

        btnSelectDateRange.setOnClickListener(v -> showDatePicker());
        btnExportExcel.setOnClickListener(v -> checkPermissionAndExport());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typesForReport);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportTypeSpinner.setAdapter(adapter);
    }

    /**
     * Muestra un selector de rango de fechas de Material Design.
     */
    private void showDatePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Seleccione Rango de Fechas")
                .build();

        picker.show(getSupportFragmentManager(), "DATE_RANGE_PICKER");

        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            startDate = sdf.format(new Date(selection.first));
            endDate = sdf.format(new Date(selection.second));
            tvSelectedDates.setText("Rango: " + startDate + " al " + endDate);
        });
    }

    /**
     * Verifica permisos según la versión de Android antes de exportar.
     */
    private void checkPermissionAndExport() {
        // En Android 10 (API 29) y superiores, getExternalFilesDir no requiere permisos.
        // Solo solicitamos el permiso en versiones anteriores (API 28 o menos).
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            } else {
                generateExcelReport();
            }
        } else {
            // Android 10+ : Acceso directo a carpetas privadas de la app
            generateExcelReport();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateExcelReport();
            } else {
                Toast.makeText(this, "Se requiere permiso de almacenamiento para guardar el reporte", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Genera el archivo Excel utilizando la librería Apache POI.
     */
    private void generateExcelReport() {
        String tipoSeleccionado = reportTypeSpinner.getSelectedItem().toString();
        Cursor cursor = dbHelper.obtenerResiduosFiltrados(startDate, endDate, tipoSeleccionado);

        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No hay datos para exportar con estos filtros", Toast.LENGTH_LONG).show();
            if (cursor != null) cursor.close();
            return;
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte EcoLim");

        // Crear Encabezados
        String[] headers = {"ID", "Tipo", "Peso (Kg)", "Zona", "Cantidad", "Fecha Registro"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Llenar datos desde el Cursor
        int rowIdx = 1;
        while (cursor.moveToNext()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RES_ID)));
            row.createCell(1).setCellValue(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RES_TIPO)));
            row.createCell(2).setCellValue(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RES_PESO)));
            row.createCell(3).setCellValue(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RES_ZONA)));
            row.createCell(4).setCellValue(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RES_CANTIDAD)));
            row.createCell(5).setCellValue(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RES_FECHA)));
        }
        cursor.close();

        // Guardar archivo en la carpeta de documentos privada de la aplicación
        String fileName = "Reporte_EcoLim_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".xlsx";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
            workbook.close();
            Toast.makeText(this, "Reporte generado: " + file.getName(), Toast.LENGTH_LONG).show();
            shareFile(file);
        } catch (IOException e) {
            Log.e("ExcelExport", "Error guardando Excel", e);
            Toast.makeText(this, "Error al generar el reporte", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Permite compartir el archivo generado mediante otras aplicaciones (WhatsApp, Correo, etc.)
     */
    private void shareFile(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Compartir Reporte Excel"));
    }
}
