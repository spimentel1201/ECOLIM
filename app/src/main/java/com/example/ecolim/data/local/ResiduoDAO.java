package com.example.ecolim.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ecolim.data.model.Residuo;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para gestionar las operaciones de la tabla Residuos.
 */
public class ResiduoDAO {
    private DatabaseHelper dbHelper;

    public ResiduoDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Guarda un nuevo registro de residuo.
     */
    public long guardar(Residuo residuo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_RES_TIPO, residuo.getTipo());
        values.put(DatabaseHelper.COL_RES_PESO, residuo.getPeso());
        values.put(DatabaseHelper.COL_RES_ZONA, residuo.getZona());
        values.put(DatabaseHelper.COL_RES_CANTIDAD, residuo.getCantidad());
        values.put(DatabaseHelper.COL_RES_FECHA, residuo.getFecha());
        return db.insert(DatabaseHelper.TABLE_RESIDUOS, null, values);
    }

    /**
     * Obtiene el peso total recolectado hoy.
     */
    public double obtenerTotalPesoHoy() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + DatabaseHelper.COL_RES_PESO + ") FROM " + DatabaseHelper.TABLE_RESIDUOS +
                " WHERE date(" + DatabaseHelper.COL_RES_FECHA + ") = date('now', 'localtime')", null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Obtiene el peso total histórico.
     */
    public double obtenerTotalPesoHistorico() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + DatabaseHelper.COL_RES_PESO + ") FROM " + DatabaseHelper.TABLE_RESIDUOS, null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Obtiene los totales agrupados por tipo para el Dashboard.
     */
    public Cursor obtenerResumenPorTipo(String tipo) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT SUM(peso), SUM(cantidad), MAX(fecha_registro) FROM " + 
                DatabaseHelper.TABLE_RESIDUOS + " WHERE tipo = ?", new String[]{tipo});
    }

    /**
     * Obtiene registros filtrados para reportes.
     */
    public Cursor obtenerFiltrados(String fechaInicio, String fechaFin, String tipo) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        StringBuilder query = new StringBuilder("SELECT * FROM " + DatabaseHelper.TABLE_RESIDUOS + " WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (fechaInicio != null && !fechaInicio.isEmpty() && fechaFin != null && !fechaFin.isEmpty()) {
            query.append(" AND date(" + DatabaseHelper.COL_RES_FECHA + ") BETWEEN date(?) AND date(?)");
            args.add(fechaInicio);
            args.add(fechaFin);
        }

        if (tipo != null && !tipo.equals("TODOS")) {
            query.append(" AND " + DatabaseHelper.COL_RES_TIPO + " = ?");
            args.add(tipo);
        }

        query.append(" ORDER BY " + DatabaseHelper.COL_RES_FECHA + " DESC");
        return db.rawQuery(query.toString(), args.toArray(new String[0]));
    }
}
