package ws.dto;

public class ActivacionRequest {

    private String telefono;
    private String identificadorTelefono;
    private String identificadorTarjeta;
    private String tipoServicio;
    private String identificacionCliente;
    private String estado;

    public ActivacionRequest() {
    }

    public ActivacionRequest(
            String telefono,
            String identificadorTelefono,
            String identificadorTarjeta,
            String tipoServicio,
            String identificacionCliente,
            String estado) {

        this.telefono = telefono;
        this.identificadorTelefono = identificadorTelefono;
        this.identificadorTarjeta = identificadorTarjeta;
        this.tipoServicio = tipoServicio;
        this.identificacionCliente = identificacionCliente;
        this.estado = estado;
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

    public String getIdentificacionCliente() {
        return identificacionCliente;
    }

    public void setIdentificacionCliente(String identificacionCliente) {
        this.identificacionCliente = identificacionCliente;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
