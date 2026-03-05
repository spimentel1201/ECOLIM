package com.example.ecolim;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ecolim.db";
    private static final int DATABASE_VERSION = 1;

    // Tabla Usuarios
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COL_USER_ID = "id_usuario";
    public static final String COL_USER_DNI = "dni";
    public static final String COL_USER_NOMBRE = "nombre";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_ROL = "rol";

    // Tabla Residuos
    public static final String TABLE_RESIDUOS = "residuos";
    public static final String COL_RES_ID = "id_residuo";
    public static final String COL_RES_TIPO = "tipo";
    public static final String COL_RES_PESO = "peso";
    public static final String COL_RES_ZONA = "origen_zona";
    public static final String COL_RES_CANTIDAD = "cantidad";
    public static final String COL_RES_FECHA = "fecha_registro";
    public static final String COL_RES_SINCRONIZADO = "sincronizado";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsuariosTable = "CREATE TABLE " + TABLE_USUARIOS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_DNI + " TEXT UNIQUE NOT NULL, " +
                COL_USER_NOMBRE + " TEXT NOT NULL, " +
                COL_USER_PASSWORD + " TEXT NOT NULL, " +
                COL_USER_ROL + " TEXT DEFAULT 'operario')";
        db.execSQL(createUsuariosTable);

        String createResiduosTable = "CREATE TABLE " + TABLE_RESIDUOS + " (" +
                COL_RES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RES_TIPO + " TEXT NOT NULL, " +
                COL_RES_PESO + " REAL NOT NULL, " +
                COL_RES_ZONA + " TEXT, " +
                COL_RES_CANTIDAD + " INTEGER, " +
                COL_RES_FECHA + " TEXT NOT NULL, " +
                COL_RES_SINCRONIZADO + " INTEGER DEFAULT 0)";
        db.execSQL(createResiduosTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESIDUOS);
        onCreate(db);
    }

    // --- CRUD USUARIOS ---

    public long registrarUsuario(String dni, String nombre, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_DNI, dni);
        values.put(COL_USER_NOMBRE, nombre);
        values.put(COL_USER_PASSWORD, password);
        return db.insert(TABLE_USUARIOS, null, values);
    }

    public boolean login(String dni, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USUARIOS, new String[]{COL_USER_ID},
                COL_USER_DNI + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{dni, password}, null, null, null);
        boolean success = cursor.getCount() > 0;
        cursor.close();
        return success;
    }

    public Cursor obtenerUsuarioPorDni(String dni) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USUARIOS, null, COL_USER_DNI + "=?", new String[]{dni}, null, null, null);
    }

    // --- CRUD RESIDUOS ---

    public long registrarResiduo(String tipo, double peso, String zona, int cantidad, String fecha) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RES_TIPO, tipo);
        values.put(COL_RES_PESO, peso);
        values.put(COL_RES_ZONA, zona);
        values.put(COL_RES_CANTIDAD, cantidad);
        values.put(COL_RES_FECHA, fecha);
        return db.insert(TABLE_RESIDUOS, null, values);
    }

    public Cursor obtenerTotalesPorTipo() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Agrupar por tipo y sumar pesos y cantidades
        return db.rawQuery("SELECT " + COL_RES_TIPO + ", SUM(" + COL_RES_PESO + ") as total_peso, " +
                "SUM(" + COL_RES_CANTIDAD + ") as total_cantidad, " +
                "MAX(" + COL_RES_FECHA + ") as ultima_fecha " +
                "FROM " + TABLE_RESIDUOS + " GROUP BY " + COL_RES_TIPO, null);
    }

    public double obtenerTotalPesoHoy() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_RES_PESO + ") FROM " + TABLE_RESIDUOS +
                " WHERE date(" + COL_RES_FECHA + ") = date('now', 'localtime')", null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public int eliminarResiduo(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_RESIDUOS, COL_RES_ID + "=?", new String[]{String.valueOf(id)});
    }
}
