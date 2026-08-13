package ws.dto;

public class NuevaLineaRequest {

    private String telefono;
    private String identificadorTelefono;
    private String identificadorTarjeta;
    private String tipoServicio;

    public NuevaLineaRequest() {
    }

    public NuevaLineaRequest(
            String telefono,
            String identificadorTelefono,
            String identificadorTarjeta,
            String tipoServicio) {

        this.telefono = telefono;
        this.identificadorTelefono = identificadorTelefono;
        this.identificadorTarjeta = identificadorTarjeta;
        this.tipoServicio = tipoServicio;
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
}
