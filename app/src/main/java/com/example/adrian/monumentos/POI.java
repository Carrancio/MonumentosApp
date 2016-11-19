package com.example.adrian.monumentos;


public class POI {

    //Componentes de un POI (Point of Interest)
    private String nombre;
    private String descripcion;
    private double latitud;
    private double longitud;
    private String url_imagen;
    private String enlace;

    //Constructor público
    public POI (String nombre, String descripcion, double latitud, double longitud, String url_imagen, String enlace){
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.url_imagen = url_imagen;
        this.enlace = enlace;

    }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getLatitud() { return latitud; }

    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }

    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getUrl_imagen() { return url_imagen; }

    public void setUrl_imagen(String url_imagen) { this.url_imagen = url_imagen; }

    public String getEnlace() { return enlace; }

    public void setEnlace(String enlace) { this.enlace = enlace; }
}