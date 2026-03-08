package com.example.ecolim;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase encargada de la gestión de la base de datos local SQLite.
 * Proporciona métodos para la creación de tablas y operaciones CRUD (Crear, Leer, Actualizar, Borrar).
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

    /**
     * Constructor de la clase.
     * @param context Contexto de la aplicación.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Crea las tablas necesarias al inicializar la base de datos por primera vez.
     * Se implementó para asegurar que la estructura de datos esté disponible desde el primer uso.
     */
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

    /**
     * Maneja la actualización de la base de datos si cambia la versión.
     * Actualmente elimina y recrea las tablas (política de desarrollo).
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESIDUOS);
        onCreate(db);
    }

    // --- MÉTODOS PARA USUARIOS ---

    /**
     * Registra un nuevo operario en la base de datos.
     * @return ID del registro insertado o -1 si falla.
     */
    public long registrarUsuario(String dni, String nombre, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_DNI, dni);
        values.put(COL_USER_NOMBRE, nombre);
        values.put(COL_USER_PASSWORD, password);
        return db.insert(TABLE_USUARIOS, null, values);
    }

    /**
     * Valida las credenciales de un usuario.
     * @return true si el DNI y password coinciden con un registro.
     */
    public boolean login(String dni, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USUARIOS, new String[]{COL_USER_ID},
                COL_USER_DNI + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{dni, password}, null, null, null);
        boolean success = cursor.getCount() > 0;
        cursor.close();
        return success;
    }

    /**
     * Busca un usuario por su DNI para obtener información de perfil.
     */
    public Cursor obtenerUsuarioPorDni(String dni) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USUARIOS, null, COL_USER_DNI + "=?", new String[]{dni}, null, null, null);
    }

    // --- MÉTODOS PARA RESIDUOS ---

    /**
     * Inserta un nuevo registro de recolección de residuos.
     * Implementado para el almacenamiento local "Offline-First".
     */
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

    /**
     * Obtiene el resumen de recolección agrupado por tipo de residuo.
     * Utilizado para alimentar las tarjetas del Dashboard.
     */
    public Cursor obtenerTotalesPorTipo() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COL_RES_TIPO + ", SUM(" + COL_RES_PESO + ") as total_peso, " +
                "SUM(" + COL_RES_CANTIDAD + ") as total_cantidad, " +
                "MAX(" + COL_RES_FECHA + ") as ultima_fecha " +
                "FROM " + TABLE_RESIDUOS + " GROUP BY " + COL_RES_TIPO, null);
    }

    /**
     * Suma el peso total de todos los residuos registrados en la fecha actual.
     * Se implementó para mostrar el KPI principal en la HomeActivity.
     */
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

    /**
     * Suma el peso total de todos los residuos registrados históricamente.
     * Implementado para mostrar el acumulado total en el Dashboard.
     */
    public double obtenerTotalPesoHistorico() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_RES_PESO + ") FROM " + TABLE_RESIDUOS, null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Obtiene registros de residuos filtrados para la generación de reportes.
     * @param fechaInicio Fecha inicial (YYYY-MM-DD).
     * @param fechaFin Fecha final (YYYY-MM-DD).
     * @param tipo Tipo de residuo o "TODOS".
     * @return Cursor con los resultados filtrados.
     */
    public Cursor obtenerResiduosFiltrados(String fechaInicio, String fechaFin, String tipo) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_RESIDUOS + " WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (fechaInicio != null && !fechaInicio.isEmpty() && fechaFin != null && !fechaFin.isEmpty()) {
            query.append(" AND date(" + COL_RES_FECHA + ") BETWEEN date(?) AND date(?)");
            args.add(fechaInicio);
            args.add(fechaFin);
        }

        if (tipo != null && !tipo.equals("TODOS")) {
            query.append(" AND " + COL_RES_TIPO + " = ?");
            args.add(tipo);
        }

        query.append(" ORDER BY " + COL_RES_FECHA + " DESC");
        return db.rawQuery(query.toString(), args.toArray(new String[0]));
    }

    /**
     * Elimina un registro de residuo específico por ID.
     */
    public int eliminarResiduo(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_RESIDUOS, COL_RES_ID + "=?", new String[]{String.valueOf(id)});
    }
}
