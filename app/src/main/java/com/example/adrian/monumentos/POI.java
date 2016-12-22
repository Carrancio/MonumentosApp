package com.example.adrian.monumentos;

/**
 * Esta clase representa a los Puntos de Interés (POI de sus siglas en inglés) que la aplicación utiliza durante toda su
 * ejecución. Además de su creación, esta clase sólo posee métodos "get" para acceder a sus campos.
 *
 * @author Adrián Muñoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 * @version 1.0
 */
public class POI {

    //Nombre del POI
    private final String nombre;

    //Descripcion del POI
    private final String descripcion;

    //Latitud del POI
    private final double latitud;

    //Longitud del POI
    private final double longitud;

    //URL de la imagen del POI que almacena la WikiPedia
    private final String url_imagen;

    //Enlace al artículo de la versión móvil de la WikiPedia referente al POI
    private final String enlace;

    /**
     * Constructor publico del POI
     *
     * @param nombre      Nombre del POI
     * @param descripcion Descripción del POI
     * @param latitud     Latitud del POI
     * @param longitud    Longitud del POI
     * @param url_imagen  URL de la imagen del POI
     * @param enlace      Enlace del POI
     */
    POI(String nombre, String descripcion, double latitud, double longitud, String url_imagen, String enlace) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.url_imagen = url_imagen;
        this.enlace = enlace;
    }

    /**
     * Obtiene el nombre del POI
     *
     * @return nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la descripción del POI
     *
     * @return descripcion
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene la latitud del POI
     *
     * @return latitud
     */
    public double getLatitud() {
        return latitud;
    }

    /**
     * Obtiene la longitud del POI
     *
     * @return longitud
     */
    public double getLongitud() {
        return longitud;
    }

    /**
     * Obtiene la imagen del POI
     *
     * @return url_imagen
     */
    String getUrl_imagen() {
        return url_imagen;
    }

    /**
     * Obtiene el enlace del POI
     *
     * @return enlace
     */
    String getEnlace() { return enlace; }
}