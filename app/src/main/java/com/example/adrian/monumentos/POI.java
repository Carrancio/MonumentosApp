package com.example.adrian.monumentos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Esta clase sirve para obtener datos de cada POI
 *
 * @author Adrian Munoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 */
public class POI implements Parcelable {

    /**
     * Nombre del POi
     */
    private String nombre;

    /**
     * Descripcion del POI
     */
    private String descripcion;

    /**
     * Latitud del POI
     */
    private double latitud;

    /**
     * Longitud del POI
     */
    private double longitud;

    /**
     *
     */
    private String url_imagen;

    /**
     *
     */
    private String enlace;

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
    public POI(String nombre, String descripcion, double latitud, double longitud, String url_imagen, String enlace) {
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
     * Permite modificar el nombre del POI
     *
     * @param nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
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
     * Permite modificar la descripcion del POI
     *
     * @param descripcion
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
     * Permite modificar la latitud del POI
     *
     * @param latitud
     */
    public void setLatitud(double latitud) {
        this.latitud = latitud;
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
     * Permite modificar la longitud del POI
     *
     * @param longitud
     */
    public void setLongitud(double longitud) {
        this.longitud = longitud;
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
     * Permite modificar la imagen del POI
     *
     * @param url_imagen
     */
    public void setUrl_imagen(String url_imagen) {
        this.url_imagen = url_imagen;
    }

    /**
     * Obtiene el enlace del POI
     *
     * @return El enlace del POI
     */
    public String getEnlace() {
        return enlace;
    }

    /**
     * Permite modificar el enlace del POI
     *
     * @param enlace
     */
    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    /**
     * Seccion Parcelable
     *
     * @param in
     */
    private POI(Parcel in) {
        String[] datos = new String[6];

        in.readStringArray(datos);
        this.nombre = datos[0];
        this.descripcion = datos[1];
        this.latitud = Double.parseDouble(datos[2]);
        this.longitud = Double.parseDouble(datos[3]);
        this.url_imagen = datos[4];
        this.enlace = datos[5];
    }

    /**
     * @return
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @param parcel
     * @param i
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{this.nombre,
                this.descripcion, String.valueOf(this.latitud), String.valueOf(this.longitud), this.url_imagen, this.enlace});
    }

    /**
     *
     */
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public POI createFromParcel(Parcel in) {
            return new POI(in);
        }

        public POI[] newArray(int size) {
            return new POI[size];
        }
    };
}