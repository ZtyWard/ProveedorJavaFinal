package modelo;

import java.time.LocalDateTime;

public class Telefono {

    private int id;
    private String telefono;
    private String identificadorTelefono;
    private String identificadorTarjeta;
    private String tipoServicio;
    private String estado;
    private String identificacionCliente;
    private double saldo;
    private LocalDateTime fechaActivacion;

    public Telefono() {
    }

    public Telefono(int id, String telefono, String identificadorTelefono,
                     String identificadorTarjeta, String tipoServicio,
                     String estado, String identificacionCliente,
                     double saldo, LocalDateTime fechaActivacion) {

        this.id = id;
        this.telefono = telefono;
        this.identificadorTelefono = identificadorTelefono;
        this.identificadorTarjeta = identificadorTarjeta;
        this.tipoServicio = tipoServicio;
        this.estado = estado;
        this.identificacionCliente = identificacionCliente;
        this.saldo = saldo;
        this.fechaActivacion = fechaActivacion;
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

    public String getIdentificadorTelefono() {
        return identificadorTelefono;
    }

    public void setIdentificadorTelefono(String identificadorTelefono) {
        this.identificadorTelefono = identificadorTelefono;
    }

    public String getIdentificadorTarjeta() {
        return identificadorTarjeta;
    }

    public void setIdentificadorTarjeta(String identificadorTarjeta) {
        this.identificadorTarjeta = identificadorTarjeta;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getIdentificacionCliente() {
        return identificacionCliente;
    }

    public void setIdentificacionCliente(String identificacionCliente) {
        this.identificacionCliente = identificacionCliente;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public LocalDateTime getFechaActivacion() {
        return fechaActivacion;
    }

    public void setFechaActivacion(LocalDateTime fechaActivacion) {
        this.fechaActivacion = fechaActivacion;
    }

    @Override
    public String toString() {
        return "Telefono{" +
                "id=" + id +
                ", telefono='" + telefono + '\'' +
                ", identificadorTelefono='" + identificadorTelefono + '\'' +
                ", identificadorTarjeta='" + identificadorTarjeta + '\'' +
                ", tipoServicio='" + tipoServicio + '\'' +
                ", estado='" + estado + '\'' +
                ", identificacionCliente='" + identificacionCliente + '\'' +
                ", saldo=" + saldo +
                ", fechaActivacion=" + fechaActivacion +
                '}';
    }
}