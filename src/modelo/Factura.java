package modelo;

import java.time.LocalDate;

public class Factura {

    private int id;
    private String telefono;
    private LocalDate fechaCalculo;
    private LocalDate fechaMaximaPago;
    private double monto;
    private boolean pagada;

    public Factura() {
    }

    public Factura(int id, String telefono, LocalDate fechaCalculo,
                   LocalDate fechaMaximaPago, double monto, boolean pagada) {

        this.id = id;
        this.telefono = telefono;
        this.fechaCalculo = fechaCalculo;
        this.fechaMaximaPago = fechaMaximaPago;
        this.monto = monto;
        this.pagada = pagada;
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

    public LocalDate getFechaCalculo() {
        return fechaCalculo;
    }

    public void setFechaCalculo(LocalDate fechaCalculo) {
        this.fechaCalculo = fechaCalculo;
    }

    public LocalDate getFechaMaximaPago() {
        return fechaMaximaPago;
    }

    public void setFechaMaximaPago(LocalDate fechaMaximaPago) {
        this.fechaMaximaPago = fechaMaximaPago;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public boolean isPagada() {
        return pagada;
    }

    public void setPagada(boolean pagada) {
        this.pagada = pagada;
    }

    @Override
    public String toString() {
        return "Factura{" +
                "id=" + id +
                ", telefono='" + telefono + '\'' +
                ", fechaCalculo=" + fechaCalculo +
                ", fechaMaximaPago=" + fechaMaximaPago +
                ", monto=" + monto +
                ", pagada=" + pagada +
                '}';
    }
}