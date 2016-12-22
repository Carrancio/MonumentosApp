package com.example.adrian.monumentos;

/**
 * Esta clase sirve para obtener datos de cada POI
 *
 * @author Adrian Munoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 */
public class POI {

    /**
     * Nombre del POi
     */
    private final String nombre;

    /**
     * Descripcion del POI
     */
    private final String descripcion;

    /**
     * Latitud del POI
     */
    private final double latitud;

    /**
     * Longitud del POI
     */
    private final double longitud;

    /**
     *
     */
    private final String url_imagen;

    /**
     *
     */
    private final String enlace;

    /**
     * Constructor publico
     *
     * @param nombre      de cada punto de interes
     * @param descripcion de cada punto de interes
     * @param latitud     de cada punto de interes
     * @param longitud    de cada punto de interes
     * @param url_imagen  de cada punto de interes
     * @param enlace      de cada punto de interes
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
     * @return El nombre del POI
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la descripción del POI
     *
     * @return La descripcion del POI
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene la latitud del POI
     *
     * @return La latidud del POI
     */
    public double getLatitud() {
        return latitud;
    }

    /**
     * Obtiene la longitud del POI
     *
     * @return La longitud del POI
     */
    public double getLongitud() {
        return longitud;
    }

    /**
     * Obtiene la imagen del POI
     *
     * @return La imagen del POI
     */
    public String getUrl_imagen() {
        return url_imagen;
    }

    /**
     * Obtiene el enlace del POI
     *
     * @return El enlace del POI
     */
    public String getEnlace() {
        return enlace;
    }
}