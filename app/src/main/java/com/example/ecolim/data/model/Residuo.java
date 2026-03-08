package com.example.ecolim.data.model;

/**
 * Modelo que representa un registro de recolección de residuos.
 */
public class Residuo {
    private int id;
    private String tipo;
    private double peso;
    private String zona;
    private int cantidad;
    private String fecha;
    private int sincronizado;

    public Residuo() {}

    public Residuo(int id, String tipo, double peso, String zona, int cantidad, String fecha, int sincronizado) {
        this.id = id;
        this.tipo = tipo;
        this.peso = peso;
        this.zona = zona;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.sincronizado = sincronizado;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getPeso() { return peso; }
    public void setPeso(double peso) { this.peso = peso; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public int getSincronizado() { return sincronizado; }
    public void setSincronizado(int sincronizado) { this.sincronizado = sincronizado; }
}
