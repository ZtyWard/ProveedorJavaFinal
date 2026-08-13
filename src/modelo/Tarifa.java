package modelo;

public class Tarifa {

    private int id;
    private String tipoLlamada;
    private double costoMinuto;

    public Tarifa() {
    }

    public Tarifa(int id, String tipoLlamada, double costoMinuto) {
        this.id = id;
        this.tipoLlamada = tipoLlamada;
        this.costoMinuto = costoMinuto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipoLlamada() {
        return tipoLlamada;
    }

    public void setTipoLlamada(String tipoLlamada) {
        this.tipoLlamada = tipoLlamada;
    }

    public double getCostoMinuto() {
        return costoMinuto;
    }

    public void setCostoMinuto(double costoMinuto) {
        this.costoMinuto = costoMinuto;
    }

    @Override
    public String toString() {
        return "Tarifa{" +
                "id=" + id +
                ", tipoLlamada='" + tipoLlamada + '\'' +
                ", costoMinuto=" + costoMinuto +
                '}';
    }
}