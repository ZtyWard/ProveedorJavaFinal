package modelo;

import java.time.LocalDateTime;

public class Movimiento {

    private int id;
    private String telefono;
    private LocalDateTime fechaLlamada;
    private String telefonoDestino;
    private double costo;
    private String duracion;

    public Movimiento() {
    }

    public Movimiento(int id,
                       String telefono,
                       LocalDateTime fechaLlamada,
                       String telefonoDestino,
                       double costo,
                       String duracion) {

        this.id = id;
        this.telefono = telefono;
        this.fechaLlamada = fechaLlamada;
        this.telefonoDestino = telefonoDestino;
        this.costo = costo;
        this.duracion = duracion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDateTime getFechaLlamada() {
        return fechaLlamada;
    }

    public void setFechaLlamada(LocalDateTime fechaLlamada) {
        this.fechaLlamada = fechaLlamada;
    }

    public String getTelefonoDestino() {
        return telefonoDestino;
    }

    public void setTelefonoDestino(String telefonoDestino) {
        this.telefonoDestino = telefonoDestino;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    @Override
    public String toString() {
        return "Movimiento{" +
                "id=" + id +
                ", telefono='" + telefono + '\'' +
                ", fechaLlamada=" + fechaLlamada +
                ", telefonoDestino='" + telefonoDestino + '\'' +
                ", costo=" + costo +
                ", duracion='" + duracion + '\'' +
                '}';
    }
}