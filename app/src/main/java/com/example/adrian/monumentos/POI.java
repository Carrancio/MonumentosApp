package com.example.adrian.monumentos;

/**
 * Esta clase sirve para obtener datos de cada POI
 * @author Adrian Munoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 *
 */

public class POI {

    //Componentes de un POI (Point of Interest)
    private String nombre;
    private String descripcion;
    private double latitud;
    private double longitud;
    private String url_imagen;
    private String enlace;

    /**
     * Constructor publico
     * @param nombre de cada punto de interes
     * @param descripcion de cada punto de interes
     * @param latitud de cada punto de interes
     * @param longitud de cada punto de interes
     * @param url_imagen de cada punto de interes
     * @param enlace de cada punto de interes
     */
    public POI (String nombre, String descripcion, double latitud, double longitud, String url_imagen, String enlace){
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.url_imagen = url_imagen;
        this.enlace = enlace;

    }

    /**
     * Obtiene el nombre del POI
     * @return nombre
     */
    public String getNombre() { return nombre; }

    /**
     * Permite modificar el nombre de cada POI
     * @param nombre
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene la descripción de cada POI
     * @return descripción
     */
    public String getDescripcion() { return descripcion; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Obtiene la latitud de cada POI
     * @return latituf
     */

    public double getLatitud() { return latitud; }

    public void setLatitud(double latitud) { this.latitud = latitud; }

    /**
     * Obtiene la longitud de cada POI
     * @return longitud
     */
    public double getLongitud() { return longitud; }

    public void setLongitud(double longitud) { this.longitud = longitud; }

    /**
     * Obtiene la imagen de cada POI
     * @return imagen
     */
    public String getUrl_imagen() { return url_imagen; }

    public void setUrl_imagen(String url_imagen) { this.url_imagen = url_imagen; }

    /**
     * Obtiene el enlace de cada POI
     * @return
     */
    public String getEnlace() { return enlace; }

    public void setEnlace(String enlace) { this.enlace = enlace; }
}