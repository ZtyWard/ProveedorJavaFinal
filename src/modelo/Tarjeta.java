package modelo;

public class Tarjeta {

    private int id;
    private String identificadorTarjeta;
    private String telefono;
    private String estado;

    public Tarjeta() {
    }

    public Tarjeta(int id, String identificadorTarjeta, String telefono, String estado) {
        this.id = id;
        this.identificadorTarjeta = identificadorTarjeta;
        this.telefono = telefono;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentificadorTarjeta() {
        return identificadorTarjeta;
    }

    public void setIdentificadorTarjeta(String identificadorTarjeta) {
        this.identificadorTarjeta = identificadorTarjeta;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Tarjeta{" +
                "id=" + id +
                ", identificadorTarjeta='" + identificadorTarjeta + '\'' +
                ", telefono='" + telefono + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}