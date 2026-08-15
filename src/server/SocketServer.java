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
    // INICIO
    // ==========================================================

    public void iniciarServidor() {

        int puerto =
                obtenerPuerto();

        try (ServerSocket servidor =
                     new ServerSocket(puerto)) {

            mostrarInicio(puerto);

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
    // ATENDER CLIENTE
    // ==========================================================

    private void atenderCliente(
            Socket cliente) {

        try (
                Socket socket = cliente;

                BufferedReader entrada =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream()));

                PrintWriter salida =
                        new PrintWriter(
                                socket.getOutputStream(),
                                true)
        ) {

            String trama =
                    entrada.readLine();

            System.out.println(
                    "Trama recibida: "
                            + trama);

            String respuesta =
                    procesarTrama(trama);

            logger.log(
                    obtenerTipoLog(trama),
                    trama,
                    respuesta);

            salida.println(respuesta);

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
    // ENRUTAMIENTO
    // ==========================================================

    private String procesarTrama(
            String trama) {

        if (trama == null
                || trama.trim().isEmpty()) {

            return Constantes.RESPUESTA_TRAMA_INVALIDA;
        }


        if ("PING".equalsIgnoreCase(
                trama)) {

            return Constantes.RESPUESTA_OK;
        }


        // ------------------------------------------------------
        // CLIENTE5
        // ------------------------------------------------------

        if (trama.startsWith(
                Constantes.TIPO_TRANSACCION_RECARGA
                        + "|")) {

            return procesarRecarga(trama);
        }


        // ------------------------------------------------------
        // CLIENTE6
        // ------------------------------------------------------

        if (trama.startsWith(
                "PAGAR_FACTURA|")) {

            return procesarPagarFactura(
                    trama);
        }


        // ------------------------------------------------------
        // CLIENTE7
        // ------------------------------------------------------

        if (trama.startsWith(
                "CLIENTE7|")) {

            return procesarCliente7(
                    trama);
        }


        // ------------------------------------------------------
        // ADM3
        // ------------------------------------------------------

        if (trama.startsWith(
                "PROVEEDOR4|")) {

            return procesarProveedor4(
                    trama);
        }


        // ------------------------------------------------------
        // ADM4 / CLIENTE7
        //
        // Se mantiene el protocolo anterior para no romper
        // las operaciones que ya funcionan.
        // ------------------------------------------------------

        if (trama.startsWith(
                "PROVEEDOR5|")) {

            return procesarProveedor5(
                    trama);
        }


        // ------------------------------------------------------
        // ADM6
        // ------------------------------------------------------

        if (trama.startsWith(
                "PROVEEDOR6|")) {

            return procesarProveedor6(
                    trama);
        }


        // ------------------------------------------------------
        // TRAMAS TELEFÓNICAS
        // ------------------------------------------------------

        if (!TramaParser.tramaBaseValida(
                trama)) {

            return Constantes.RESPUESTA_TRAMA_INVALIDA;
        }


        if (TramaParser.esAutorizacionLlamada(
                trama)) {

            return procesarAutorizacionLlamada(
                    trama);
        }


        if (TramaParser.esConsultaSaldo(
                trama)) {

            return proveedorService.consultarSaldo(
                    TramaParser.obtenerTelefono(
                            trama));
        }


        if (TramaParser.esRegistroMovimiento(
                trama)) {

            return proveedorService.registrarMovimiento(
                    TramaParser.obtenerTelefono(
                            trama),
                    TramaParser.obtenerDetalle(
                            trama));
        }


        return Constantes.RESPUESTA_TRAMA_INVALIDA;
    }


    // ==========================================================
    // CLIENTE7 - DEVOLVER LINEA
    // ==========================================================

    private String procesarCliente7(
            String trama) {

        try {

            /*
             * Formato:
             *
             * CLIENTE7|DEVOLVER|88881234
             */

            String[] partes =
                    trama.split(
                            "\\|",
                            -1);


            if (partes.length != 3) {

                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }


            String operacion =
                    partes[1]
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT);


            String telefono =
                    partes[2].trim();


            if (!"DEVOLVER".equals(
                    operacion)) {

                return Constantes.RESPUESTA_TRAMA_INVALIDA;
            }


            if (valorVacio(telefono)) {

                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }


            System.out.println(
                    "CLIENTE7 - Devolviendo línea: "
                            + telefono);


            /*
             * Aquí está la parte importante:
             *
             * NO recibimos:
             *
             * - identificador del teléfono
             * - identificador de tarjeta
             * - identificación del cliente
             *
             * ActivacionService los obtiene directamente
             * desde la BD usando el teléfono.
             */

            String respuesta =
                    activacionService
                            .desactivarLineaPorTelefono(
                                    telefono);


            System.out.println(
                    "CLIENTE7 - Respuesta devolución: "
                            + respuesta);


            return respuesta;

        } catch (Exception e) {

            System.out.println(
                    "Error procesando CLIENTE7:");

            e.printStackTrace();

            return Constantes.RESPUESTA_ERROR;
        }
    }


    // ==========================================================
    // CLIENTE6 - PAGAR FACTURA
    // ==========================================================

    private String procesarPagarFactura(
            String trama) {

        try {

            String[] partes =
                    trama.split(
                            "\\|",
                            -1);


            /*
             * Formato:
             *
             * PAGAR_FACTURA|1
             */

            if (partes.length != 2) {

                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }


            String idTexto =
                    partes[1].trim();


            if (valorVacio(idTexto)) {

                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }


            int facturaId =
                    Integer.parseInt(
                            idTexto);


            if (facturaId <= 0) {

                return Constantes.RESPUESTA_ERROR;
            }


            System.out.println(
                    "CLIENTE6 - Pagando factura ID: "
                            + facturaId);


            String respuesta =
                    facturacionService
                            .marcarFacturaPagada(
                                    facturaId);


            System.out.println(
                    "CLIENTE6 - Respuesta pago: "
                            + respuesta);


            return respuesta;

        } catch (NumberFormatException e) {

            return Constantes.RESPUESTA_ERROR;

        } catch (Exception e) {

            System.out.println(
                    "Error procesando PAGAR_FACTURA:");

            e.printStackTrace();

            return Constantes.RESPUESTA_ERROR;
        }
    }


    // ==========================================================
    // CLIENTE5 - RECARGAR SALDO
    // ==========================================================

    private String procesarRecarga(
            String trama) {

        try {

            String[] partes =
                    trama.split(
                            "\\|",
                            -1);


            if (partes.length != 3) {

                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }


            String telefono =
                    partes[1].trim();


            String montoTexto =
                    partes[2].trim();


            if (valorVacio(telefono)
                    || valorVacio(montoTexto)) {

                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }


            double monto =
                    Double.parseDouble(
                            montoTexto);


            if (monto <= 0
                    || monto != Math.floor(monto)) {

                return Constantes.RESPUESTA_ERROR;
            }


            return proveedorService.recargarSaldo(
                    telefono,
                    monto);

        } catch (NumberFormatException e) {

            return Constantes.RESPUESTA_ERROR;

        } catch (Exception e) {

            System.out.println(
                    "Error procesando RECARGA:");

            e.printStackTrace();

            return Constantes.RESPUESTA_ERROR;
        }
    }


    // ==========================================================
    // PROVEEDOR4
    // ==========================================================

    private String procesarProveedor4(
            String trama) {

        try {

            String[] partes =
                    trama.split(
                            "\\|",
                            -1);


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
                            .toUpperCase(
                                    Locale.ROOT);


            String estado =
                    partes[5]
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT);


            if (valorVacio(telefono)
                    || valorVacio(
                    identificadorTelefono)
                    || valorVacio(
                    identificadorTarjeta)
                    || valorVacio(
                    tipoServicio)
                    || !Constantes.ESTADO_DISPONIBLE
                    .equals(estado)) {

                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }


            return activacionService
                    .registrarLineaDisponible(
                            telefono,
                            identificadorTelefono,
                            identificadorTarjeta,
                            tipoServicio);

        } catch (Exception e) {

            System.out.println(
                    "Error procesando PROVEEDOR4:");

            e.printStackTrace();

            return Constantes.RESPUESTA_ERROR;
        }
    }


    // ==========================================================
    // PROVEEDOR5
    // ==========================================================

    private String procesarProveedor5(
            String trama) {

        try {

            String[] partes =
                    trama.split(
                            "\\|",
                            -1);


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
                            .toUpperCase(
                                    Locale.ROOT);


            String identificacionCliente =
                    partes[5].trim();


            String estado =
                    partes[6]
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT);


            if (valorVacio(telefono)
                    || valorVacio(
                    identificadorTelefono)
                    || valorVacio(
                    identificadorTarjeta)
                    || valorVacio(tipoServicio)
                    || valorVacio(
                    identificacionCliente)
                    || valorVacio(estado)) {

                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }


            if (Constantes.ESTADO_ACTIVO.equals(
                    estado)
                    || "ACTIVAR".equals(
                    estado)) {

                return activacionService.activarLinea(
                        telefono,
                        identificadorTelefono,
                        identificadorTarjeta,
                        tipoServicio,
                        identificacionCliente);
            }


            if (Constantes.ESTADO_INACTIVO.equals(
                    estado)
                    || "DESACTIVADO".equals(
                    estado)
                    || "DESACTIVAR".equals(
                    estado)
                    || Constantes.ESTADO_DISPONIBLE.equals(
                    estado)) {

                return activacionService.desactivarLinea(
                        telefono,
                        identificadorTelefono,
                        identificadorTarjeta,
                        identificacionCliente);
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
    // PROVEEDOR6
    // ==========================================================

    private String procesarProveedor6(
            String trama) {

        try {

            String[] partes =
                    trama.split(
                            "\\|",
                            -1);


            if (partes.length == 2
                    && "ULTIMA_FECHA"
                    .equalsIgnoreCase(
                            partes[1].trim())) {

                LocalDate ultimaFecha =
                        facturacionService
                                .obtenerUltimaFechaFacturacion();


                if (ultimaFecha == null) {

                    return "SIN_FACTURACION";
                }


                return ultimaFecha.toString();
            }


            if (partes.length != 3) {

                return Constantes.RESPUESTA_DATOS_INCOMPLETOS;
            }


            LocalDate fechaCalculo =
                    LocalDate.parse(
                            partes[1].trim());


            LocalDate fechaMaximaPago =
                    LocalDate.parse(
                            partes[2].trim());


            return facturacionService
                    .calcularFacturacionPostpago(
                            fechaCalculo,
                            fechaMaximaPago);

        } catch (Exception e) {

            System.out.println(
                    "Error procesando PROVEEDOR6:");

            e.printStackTrace();

            return Constantes.RESPUESTA_ERROR;
        }
    }


    // ==========================================================
    // AUTORIZACIÓN
    // ==========================================================

    private String procesarAutorizacionLlamada(
            String trama) {

        int tipoLlamada =
                TramaParser.obtenerTipoLlamada(
                        trama);


        if (tipoLlamada <= 0) {

            return Constantes.RESPUESTA_TRAMA_INVALIDA;
        }


        return proveedorService.autorizarLlamada(
                TramaParser.obtenerTelefono(
                        trama),
                tipoLlamada);
    }


    // ==========================================================
    // LOG
    // ==========================================================

    private String obtenerTipoLog(
            String trama) {

        if (trama == null
                || trama.trim().isEmpty()) {

            return "trama_vacia";
        }


        if ("PING".equalsIgnoreCase(
                trama)) {

            return "prueba_conexion";
        }


        if (trama.startsWith(
                Constantes.TIPO_TRANSACCION_RECARGA
                        + "|")) {

            return "recarga_saldo";
        }


        if (trama.startsWith(
                "PAGAR_FACTURA|")) {

            return "cliente6_pagar_factura";
        }


        if (trama.startsWith(
                "CLIENTE7|")) {

            return "cliente7_devolver_linea";
        }


        if (trama.startsWith(
                "PROVEEDOR4|")) {

            return "proveedor4_ingresar_linea";
        }


        if (trama.startsWith(
                "PROVEEDOR5|")) {

            return "proveedor5_activar_desactivar_linea";
        }


        if (trama.startsWith(
                "PROVEEDOR6|")) {

            if (trama.toUpperCase(
                    Locale.ROOT)
                    .contains(
                            "ULTIMA_FECHA")) {

                return "proveedor6_ultima_fecha_facturacion";
            }


            return "proveedor6_calcular_facturacion";
        }


        if (!TramaParser.tramaBaseValida(
                trama)) {

            return "trama_invalida";
        }


        if (TramaParser.esAutorizacionLlamada(
                trama)) {

            return "autorizacion_llamada";
        }


        if (TramaParser.esConsultaSaldo(
                trama)) {

            return "consulta_saldo";
        }


        if (TramaParser.esRegistroMovimiento(
                trama)) {

            return "registro_movimiento";
        }


        return TIPO_LOG_TRANSACCION;
    }


    // ==========================================================
    // UTILIDADES
    // ==========================================================

    private boolean valorVacio(
            String valor) {

        return valor == null
                || valor.trim().isEmpty();
    }


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


    private void mostrarInicio(
            int puerto) {

        System.out.println(
                "Proveedor iniciado en puerto "
                        + puerto
                        + "...");

        System.out.println(
                "Esperando conexiones...");
    }
}