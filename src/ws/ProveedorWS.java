package ws;

import service.ActivacionService;
import service.FacturacionService;
import service.ProveedorService;
import util.Constantes;
import ws.dto.ActivacionRequest;
import ws.dto.FacturacionRequest;
import ws.dto.NuevaLineaRequest;
import ws.dto.RespuestaWS;

public class ProveedorWS {

    private static final String MENSAJE_EXITOSO =
            "Exitoso";

    private static final String MENSAJE_NUEVA_LINEA_ERROR =
            "Problemas al incluir la informacion.";

    private static final String MENSAJE_ACTIVACION_ERROR =
            "Problemas al activar/desactivar la linea.";

    private static final String MENSAJE_FACTURACION_ERROR =
            "Problemas al realizar el calculo.";

    private final ProveedorService proveedorService;
    private final ActivacionService activacionService;
    private final FacturacionService facturacionService;

    public ProveedorWS() {

        this.proveedorService =
                new ProveedorService();

        this.activacionService =
                new ActivacionService();

        this.facturacionService =
                new FacturacionService();
    }

    // ==========================================================
    // OPERACIONES DE LINEAS
    // ==========================================================

    public RespuestaWS registrarNuevaLinea(
            NuevaLineaRequest request) {

        if (request == null) {
            return respuestaError(
                    MENSAJE_NUEVA_LINEA_ERROR);
        }

        String respuesta =
                activacionService.registrarLineaDisponible(
                        request.getTelefono(),
                        request.getIdentificadorTelefono(),
                        request.getIdentificadorTarjeta(),
                        request.getTipoServicio());

        return convertirRespuesta(
                respuesta,
                MENSAJE_NUEVA_LINEA_ERROR);
    }

    public RespuestaWS activarDesactivarLinea(
            ActivacionRequest request) {

        if (request == null) {
            return respuestaError(
                    MENSAJE_ACTIVACION_ERROR);
        }

        String respuesta;

        if (Constantes.ESTADO_ACTIVO.equals(request.getEstado())) {
            respuesta =
                    activacionService.activarLinea(
                            request.getTelefono(),
                            request.getIdentificadorTelefono(),
                            request.getIdentificadorTarjeta(),
                            request.getTipoServicio(),
                            request.getIdentificacionCliente());
        } else {
            respuesta =
                    activacionService.desactivarLinea(
                            request.getTelefono(),
                            request.getIdentificadorTelefono(),
                            request.getIdentificadorTarjeta(),
                            request.getIdentificacionCliente());
        }

        return convertirRespuesta(
                respuesta,
                MENSAJE_ACTIVACION_ERROR);
    }

    // ==========================================================
    // OPERACIONES DE FACTURACION
    // ==========================================================

    public RespuestaWS calcularFacturacion(
            FacturacionRequest request) {

        if (request == null) {
            return respuestaError(
                    MENSAJE_FACTURACION_ERROR);
        }

        String respuesta =
                facturacionService.calcularFacturacionPostpago(
                        request.getFechaCalculo(),
                        request.getFechaMaximaPago());

        return convertirRespuesta(
                respuesta,
                MENSAJE_FACTURACION_ERROR);
    }

    // ==========================================================
    // OPERACIONES EXISTENTES DEL PROVEEDOR
    // ==========================================================

    public String autorizarLlamada(
            String telefono,
            int tipoLlamada) {

        return proveedorService.autorizarLlamada(
                telefono,
                tipoLlamada);
    }

    public String consultarSaldo(String telefono) {

        return proveedorService.consultarSaldo(
                telefono);
    }

    public String registrarMovimiento(
            String telefono,
            String detalle) {

        return proveedorService.registrarMovimiento(
                telefono,
                detalle);
    }

    // ==========================================================
    // RESPUESTAS
    // ==========================================================

    private RespuestaWS convertirRespuesta(
            String respuestaServicio,
            String mensajeError) {

        if (Constantes.RESPUESTA_OK.equals(respuestaServicio)) {
            return new RespuestaWS(
                    true,
                    MENSAJE_EXITOSO);
        }

        return respuestaError(
                mensajeError);
    }

    private RespuestaWS respuestaError(String mensaje) {

        return new RespuestaWS(
                false,
                mensaje);
    }
}
