package server;

import service.ActivacionService;
import service.FacturacionService;
import service.ProveedorService;
import util.CifradoWSProveedor;
import util.Constantes;
import util.TramaParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Locale;

public class SocketServer {

    private static final String TIPO_LOG_TRANSACCION =
            "transaccion";

    private final ProveedorService proveedorService;
    private final ActivacionService activacionService;
    private final FacturacionService facturacionService;
    private final AsyncLogger logger;

    public SocketServer() {

        this.proveedorService =
                new ProveedorService();

        this.activacionService =
                new ActivacionService();

        this.facturacionService =
                new FacturacionService();

        this.logger =
                new AsyncLogger();
    }

    // ==========================================================
    // INICIO DEL SERVIDOR
    // ==========================================================

    public void iniciarServidor() {

        int puerto =
                obtenerPuerto();

        try (ServerSocket servidor =
                     new ServerSocket(puerto)) {

            mostrarInicio(
                    puerto);

            while (true) {

                atenderCliente(
                        servidor.accept());
            }

        } catch (Exception e) {

            System.out.println(
                    "Error servidor");

            e.printStackTrace();
        }
    }

    // ==========================================================
    // ATENCION DE CLIENTES
    // ==========================================================

    private void atenderCliente(Socket cliente) {

        try (Socket socket = cliente;
             BufferedReader entrada =
                     new BufferedReader(
                             new InputStreamReader(
                                     socket.getInputStream()));
             PrintWriter salida =
                     new PrintWriter(
                             socket.getOutputStream(),
                             true)) {

            String trama =
                    entrada.readLine();

            System.out.println(
                    "Trama recibida: "
                            + trama);

            String respuesta =
                    procesarTrama(
                            trama);

            logger.log(
                    obtenerTipoLog(trama),
                    trama,
                    respuesta);

            salida.println(
                    respuesta);

        } catch (Exception e) {

            System.out.println(
                    "Error atendiendo cliente:");

            e.printStackTrace();

            logger.log(
                    "error_servidor",
                    "",
                    Constantes.RESPUESTA_ERROR);
        }
    }

    // ==========================================================
    // ENRUTAMIENTO DE TRAMAS
    // ==========================================================

    private String procesarTrama(String trama) {

        if (trama == null
                || trama.trim().isEmpty()) {

            return Constantes.RESPUESTA_TRAMA_INVALIDA;
        }

        if ("PING".equalsIgnoreCase(trama)) {
            return Constantes.RESPUESTA_OK;
        }

        if (trama.startsWith("PROVEEDOR4|")) {
            return procesarProveedor4(
                    trama);
        }

        if (trama.startsWith("PROVEEDOR5|")) {
            return procesarProveedor5(
                    trama);
        }

        if (trama.startsWith("PROVEEDOR6|")) {
            return procesarProveedor6(
                    trama);
        }

        if (!TramaParser.tramaBaseValida(trama)) {
            return Constantes.RESPUESTA_TRAMA_INVALIDA;
        }

        if (TramaParser.esAutorizacionLlamada(trama)) {
            return procesarAutorizacionLlamada(
                    trama);
        }

        if (TramaParser.esConsultaSaldo(trama)) {
            return proveedorService.consultarSaldo(
                    TramaParser.obtenerTelefono(trama));
        }

        if (TramaParser.esRegistroMovimiento(trama)) {
            return proveedorService.registrarMovimiento(
                    TramaParser.obtenerTelefono(trama),
                    TramaParser.obtenerDetalle(trama));
        }

        return Constantes.RESPUESTA_TRAMA_INVALIDA;
    }

    // ==========================================================
    // PROVEEDOR4 - REGISTRAR LINEA DISPONIBLE
    // ==========================================================

    private String procesarProveedor4(String trama) {

        try {

            String[] partes =
                    trama.split("\\|", -1);

            if (partes.length != 6) {
                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }

            String telefono =
                    CifradoWSProveedor.descifrar(
                            partes[1]).trim();

            String identificadorTelefono =
                    CifradoWSProveedor.descifrar(
                            partes[2]).trim();

            String identificadorTarjeta =
                    CifradoWSProveedor.descifrar(
                            partes[3]).trim();

            String tipoServicio =
                    partes[4]
                            .trim()
                            .toUpperCase(Locale.ROOT);

            String estado =
                    partes[5]
                            .trim()
                            .toUpperCase(Locale.ROOT);

            if (valorVacio(telefono)
                    || valorVacio(identificadorTelefono)
                    || valorVacio(identificadorTarjeta)
                    || valorVacio(tipoServicio)
                    || !Constantes.ESTADO_DISPONIBLE.equals(estado)) {

                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }

            return activacionService.registrarLineaDisponible(
                    telefono,
                    identificadorTelefono,
                    identificadorTarjeta,
                    tipoServicio
            );

        } catch (Exception e) {

            System.out.println(
                    "Error procesando PROVEEDOR4:");

            e.printStackTrace();

            return Constantes.RESPUESTA_ERROR;
        }
    }

    // ==========================================================
    // PROVEEDOR5 - ACTIVAR / DESACTIVAR LINEA
    // ==========================================================

    private String procesarProveedor5(String trama) {

        try {

            String[] partes =
                    trama.split("\\|", -1);

            if (partes.length != 7) {
                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }

            String telefono =
                    CifradoWSProveedor.descifrar(
                            partes[1]).trim();

            String identificadorTelefono =
                    CifradoWSProveedor.descifrar(
                            partes[2]).trim();

            String identificadorTarjeta =
                    CifradoWSProveedor.descifrar(
                            partes[3]).trim();

            String tipoServicio =
                    partes[4]
                            .trim()
                            .toUpperCase(Locale.ROOT);

            String identificacionCliente =
                    partes[5].trim();

            String estado =
                    partes[6]
                            .trim()
                            .toUpperCase(Locale.ROOT);

            if (valorVacio(telefono)
                    || valorVacio(identificadorTelefono)
                    || valorVacio(identificadorTarjeta)
                    || valorVacio(tipoServicio)
                    || valorVacio(identificacionCliente)
                    || valorVacio(estado)) {

                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }

            if (Constantes.ESTADO_ACTIVO.equals(estado)
                    || "ACTIVAR".equals(estado)) {

                return activacionService.activarLinea(
                        telefono,
                        identificadorTelefono,
                        identificadorTarjeta,
                        tipoServicio,
                        identificacionCliente
                );
            }

            if (Constantes.ESTADO_INACTIVO.equals(estado)
                    || "DESACTIVADO".equals(estado)
                    || "DESACTIVAR".equals(estado)
                    || Constantes.ESTADO_DISPONIBLE.equals(estado)) {

                return activacionService.desactivarLinea(
                        telefono,
                        identificadorTelefono,
                        identificadorTarjeta,
                        identificacionCliente
                );
            }

            return Constantes.RESPUESTA_ERROR;

        } catch (Exception e) {

            System.out.println(
                    "Error procesando PROVEEDOR5:");

            e.printStackTrace();

            return Constantes.RESPUESTA_ERROR;
        }
    }

    // ==========================================================
    // PROVEEDOR6 - CALCULAR FACTURACION
    // ==========================================================

    private String procesarProveedor6(String trama) {

        try {

            String[] partes =
                    trama.split("\\|", -1);

            if (partes.length != 3) {
                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }

            LocalDate fechaCalculo =
                    LocalDate.parse(
                            partes[1].trim());

            LocalDate fechaMaximaPago =
                    LocalDate.parse(
                            partes[2].trim());

            return facturacionService.calcularFacturacionPostpago(
                    fechaCalculo,
                    fechaMaximaPago
            );

        } catch (Exception e) {

            System.out.println(
                    "Error procesando PROVEEDOR6:");

            e.printStackTrace();

            return Constantes.RESPUESTA_ERROR;
        }
    }

    // ==========================================================
    // AUTORIZACION DE LLAMADA
    // ==========================================================

    private String procesarAutorizacionLlamada(String trama) {

        int tipoLlamada =
                TramaParser.obtenerTipoLlamada(
                        trama);

        if (tipoLlamada <= 0) {
            return Constantes.RESPUESTA_TRAMA_INVALIDA;
        }

        return proveedorService.autorizarLlamada(
                TramaParser.obtenerTelefono(trama),
                tipoLlamada);
    }

    // ==========================================================
    // BITACORA
    // ==========================================================

    private String obtenerTipoLog(String trama) {

        if (trama == null
                || trama.trim().isEmpty()) {

            return "trama_vacia";
        }

        if ("PING".equalsIgnoreCase(trama)) {
            return "prueba_conexion";
        }

        if (trama.startsWith("PROVEEDOR4|")) {
            return "proveedor4_ingresar_linea";
        }

        if (trama.startsWith("PROVEEDOR5|")) {
            return "proveedor5_activar_desactivar_linea";
        }

        if (trama.startsWith("PROVEEDOR6|")) {
            return "proveedor6_calcular_facturacion";
        }

        if (!TramaParser.tramaBaseValida(trama)) {
            return "trama_invalida";
        }

        if (TramaParser.esAutorizacionLlamada(trama)) {
            return "autorizacion_llamada";
        }

        if (TramaParser.esConsultaSaldo(trama)) {
            return "consulta_saldo";
        }

        if (TramaParser.esRegistroMovimiento(trama)) {
            return "registro_movimiento";
        }

        return TIPO_LOG_TRANSACCION;
    }

    // ==========================================================
    // UTILIDADES
    // ==========================================================

    private boolean valorVacio(String valor) {

        return valor == null
                || valor.trim().isEmpty();
    }

    // ==========================================================
    // CONFIGURACION
    // ==========================================================

    private int obtenerPuerto() {

        try {

            return Integer.parseInt(
                    System.getenv()
                            .getOrDefault(
                                    "PROVEEDOR_PORT",
                                    String.valueOf(
                                            Constantes.PUERTO_PROVEEDOR_DEFAULT)));

        } catch (NumberFormatException e) {

            return Constantes.PUERTO_PROVEEDOR_DEFAULT;
        }
    }

    private void mostrarInicio(int puerto) {

        System.out.println(
                "Proveedor iniciado en puerto "
                        + puerto
                        + "...");

        System.out.println(
                "Esperando conexiones...");
    }
}
