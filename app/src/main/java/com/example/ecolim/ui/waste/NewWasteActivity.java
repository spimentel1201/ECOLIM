package com.example.ecolim.ui.waste;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ecolim.R;
import com.example.ecolim.data.local.ResiduoDAO;
import com.example.ecolim.data.model.Residuo;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Actividad para el registro de nuevos residuos.
 * Integra el uso de la cámara y un modelo de Inteligencia Artificial (TensorFlow Lite)
 * para la clasificación automática de residuos.
 */
public class NewWasteActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int PERMISSION_REQUEST_CAMERA = 102;

    private Spinner typeSpinner;
    private EditText weightEditText, zoneEditText, quantityEditText;
    private ImageView wasteImageView;
    private ResiduoDAO residuoDAO;
    private ArrayAdapter<String> adapter;
    
    private final String[] types = {"ORGANICO", "PLASTICO", "PAPEL", "METAL", "VIDRIO", "CARTON", "PELIGROSO"};

    private Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_waste);

        residuoDAO = new ResiduoDAO(this);

        try {
            tflite = new Interpreter(loadModelFile());
            Log.d("TFLite", "Modelo cargado exitosamente");
        } catch (IOException e) {
            Log.e("TFLite", "Error leyendo el archivo modelo", e);
            Toast.makeText(this, "Error al cargar el modelo de IA", Toast.LENGTH_SHORT).show();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        wasteImageView = findViewById(R.id.wasteImageView);
        typeSpinner = findViewById(R.id.typeSpinner);
        weightEditText = findViewById(R.id.weightEditText);
        zoneEditText = findViewById(R.id.zoneEditText);
        quantityEditText = findViewById(R.id.quantityEditText);

        Button saveButton = findViewById(R.id.saveButton);
        Button scanButton = findViewById(R.id.btnScanWaste);

        setupSpinner();

        saveButton.setOnClickListener(v -> guardarRegistro());
        scanButton.setOnClickListener(v -> abrirCamara());
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("WasteModel.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void setupSpinner() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
    }

    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            lanzarIntentCamara();
        }
    }

    private void lanzarIntentCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No se encontró aplicación de cámara", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lanzarIntentCamara();
            } else {
                Toast.makeText(this, "Se requiere permiso de cámara para escanear residuos", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) (extras != null ? extras.get("data") : null);
            if (imageBitmap != null) {
                wasteImageView.setImageBitmap(imageBitmap);
                classifyImage(imageBitmap);
            }
        }
    }

    private void classifyImage(Bitmap bitmap) {
        if (tflite == null) return;

        try {
            Tensor inputTensor = tflite.getInputTensor(0);
            int inputWidth = inputTensor.shape()[1];
            int inputHeight = inputTensor.shape()[2];
            int inputChannels = inputTensor.shape()[3];

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true);
            Bitmap grayscaleBitmap = toGrayscale(resizedBitmap);

            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(1 * inputWidth * inputHeight * inputChannels * 4);
            inputBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[inputWidth * inputHeight];
            grayscaleBitmap.getPixels(intValues, 0, grayscaleBitmap.getWidth(), 0, 0, grayscaleBitmap.getWidth(), grayscaleBitmap.getHeight());

            int pixel = 0;
            for (int i = 0; i < inputWidth; ++i) {
                for (int j = 0; j < inputHeight; ++j) {
                    int val = intValues[pixel++];
                    float normalizedVal = ((val >> 16) & 0xFF) / 255.0f;
                    inputBuffer.putFloat(normalizedVal);
                    if (inputChannels == 3) {
                        inputBuffer.putFloat(normalizedVal);
                        inputBuffer.putFloat(normalizedVal);
                    }
                }
            }

            int numClasses = tflite.getOutputTensor(0).shape()[1];
            float[][] outputArray = new float[1][numClasses];
            tflite.run(inputBuffer, outputArray);
            
            float[] scores = outputArray[0];
            int maxIdx = 0;
            for (int i = 1; i < scores.length; i++) {
                if (scores[i] > scores[maxIdx]) maxIdx = i;
            }

            if (maxIdx < types.length) {
                final int selection = maxIdx;
                runOnUiThread(() -> {
                    typeSpinner.setSelection(selection);
                    Toast.makeText(this, "IA: " + types[selection], Toast.LENGTH_SHORT).show();
                });
            }

        } catch (Exception e) {
            Log.e("TFLite", "Error en clasificación", e);
        }
    }

    private Bitmap toGrayscale(Bitmap bmpOriginal) {
        Bitmap bmpGrayscale = Bitmap.createBitmap(bmpOriginal.getWidth(), bmpOriginal.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
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
            double pesoUnitario = Double.parseDouble(pesoStr);
            int cantidad = Integer.parseInt(cantidadStr);
            double pesoTotal = pesoUnitario * cantidad;
            String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            Residuo nuevoResiduo = new Residuo();
            nuevoResiduo.setTipo(tipo);
            nuevoResiduo.setPeso(pesoTotal);
            nuevoResiduo.setZona(zona);
            nuevoResiduo.setCantidad(cantidad);
            nuevoResiduo.setFecha(fecha);

            long id = residuoDAO.guardar(nuevoResiduo);

            if (id != -1) {
                Toast.makeText(this, "Registro guardado: " + String.format(Locale.getDefault(), "%.2f", pesoTotal) + " Kg totales", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Datos numéricos inválidos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (tflite != null) tflite.close();
        super.onDestroy();
    }
}
