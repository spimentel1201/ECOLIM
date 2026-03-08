package com.example.ecolim.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Clase encargada únicamente de la estructura y creación de la base de datos local SQLite.
 * La lógica de acceso a datos se ha movido a las clases DAO.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ecolim.db";
    private static final int DATABASE_VERSION = 1;

    // Constantes para la tabla Usuarios
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COL_USER_ID = "id_usuario";
    public static final String COL_USER_DNI = "dni";
    public static final String COL_USER_NOMBRE = "nombre";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_ROL = "rol";

    // Constantes para la tabla Residuos
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
}
