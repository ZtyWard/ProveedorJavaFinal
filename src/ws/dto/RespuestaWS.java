package ws.dto;

public class RespuestaWS {

    private boolean resultado;
    private String mensaje;

    public RespuestaWS() {
    }

    public RespuestaWS(
            boolean resultado,
            String mensaje) {

        this.resultado = resultado;
        this.mensaje = mensaje;
    }

    public boolean isResultado() {
        return resultado;
    }

    public void setResultado(boolean resultado) {
        this.resultado = resultado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
