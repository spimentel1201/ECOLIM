package com.example.ecolim.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ecolim.data.model.Usuario;

/**
 * Clase DAO para gestionar las operaciones de la tabla Usuarios.
 */
public class UsuarioDAO {
    private DatabaseHelper dbHelper;

    public UsuarioDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Registra un nuevo operario.
     */
    public long registrar(Usuario usuario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_DNI, usuario.getDni());
        values.put(DatabaseHelper.COL_USER_NOMBRE, usuario.getNombre());
        values.put(DatabaseHelper.COL_USER_PASSWORD, usuario.getPassword());
        return db.insert(DatabaseHelper.TABLE_USUARIOS, null, values);
    }

    /**
     * Valida credenciales de login.
     */
    public boolean login(String dni, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USUARIOS, 
                new String[]{DatabaseHelper.COL_USER_ID},
                DatabaseHelper.COL_USER_DNI + "=? AND " + DatabaseHelper.COL_USER_PASSWORD + "=?",
                new String[]{dni, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Obtiene un usuario por DNI.
     */
    public Usuario obtenerPorDni(String dni) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USUARIOS, null, 
                DatabaseHelper.COL_USER_DNI + "=?", new String[]{dni}, 
                null, null, null);
        
        Usuario usuario = null;
        if (cursor != null && cursor.moveToFirst()) {
            usuario = new Usuario();
            usuario.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)));
            usuario.setDni(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_DNI)));
            usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NOMBRE)));
            usuario.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)));
            usuario.setRol(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROL)));
            cursor.close();
        }
        return usuario;
    }
}
