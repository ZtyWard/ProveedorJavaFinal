package service;

import dao.MovimientoDAO;
import dao.TarifaDAO;
import dao.TelefonoDAO;
import modelo.Movimiento;
import modelo.Tarifa;
import modelo.Telefono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProveedorService {

    private static final String RESPUESTA_OK =
            "OK";

    private static final String RESPUESTA_ERROR =
            "ERROR";

    private static final String RESPUESTA_INSUFICIENTE =
            "INSUF";

    private static final String TIPO_SERVICIO_PREPAGO =
            "PREPAGO";

    private static final String TIPO_SERVICIO_POSTPAGO =
            "POSTPAGO";

    private static final String ESTADO_ACTIVO =
            "ACTIVO";

    private static final String TARIFA_MISMO_PROVEEDOR =
            "MISMO_PROVEEDOR";

    private static final String TARIFA_OTRO_PROVEEDOR =
            "OTRO_PROVEEDOR";

    private static final String TARIFA_INTERNACIONAL =
            "INTERNACIONAL";

    private static final int ANCHO_COSTO_AUTORIZACION =
            10;

    private static final int ANCHO_SALDO =
            19;

    private static final int LONGITUD_MINIMA_DETALLE_MOVIMIENTO =
            28;

    private static final int SEGUNDOS_POR_MINUTO =
            60;

    private static final int TIEMPO_MAXIMO_SEGUNDOS =
            99 * 3600 + 59 * 60 + 59;

    private final TelefonoDAO telefonoDAO;
    private final TarifaDAO tarifaDAO;
    private final MovimientoDAO movimientoDAO;

    public ProveedorService() {

        this.telefonoDAO =
                new TelefonoDAO();

        this.tarifaDAO =
                new TarifaDAO();

        this.movimientoDAO =
                new MovimientoDAO();
    }

    // ==========================================================
    // AUTORIZAR LLAMADA
    // ==========================================================

    public String autorizarLlamada(
            String telefono,
            int tipoLlamada) {

        try {

            Telefono linea =
                    telefonoDAO.buscarPorTelefono(
                            telefono);

            if (!lineaActiva(linea)) {
                return RESPUESTA_ERROR;
            }

            String tipoTarifa =
                    obtenerTipoTarifa(
                            tipoLlamada);

            if (tipoTarifa == null) {
                return RESPUESTA_ERROR;
            }

            Tarifa tarifa =
                    tarifaDAO.obtenerTarifa(
                            tipoTarifa);

            if (tarifa == null
                    || tarifa.getCostoMinuto() <= 0) {
                return RESPUESTA_ERROR;
            }

            if (TIPO_SERVICIO_POSTPAGO.equals(linea.getTipoServicio())) {
                return RESPUESTA_OK
                        + "|9999999999"
                        + "|245959";
            }

            BigDecimal costoMinuto =
                    BigDecimal.valueOf(
                            tarifa.getCostoMinuto());

            BigDecimal saldo =
                    BigDecimal.valueOf(
                            linea.getSaldo());

            if (saldo.compareTo(costoMinuto) < 0) {
                return RESPUESTA_INSUFICIENTE;
            }

            int tiempoAutorizado =
                    calcularTiempoAutorizado(
                            saldo,
                            costoMinuto);

            return RESPUESTA_OK
                    + "|"
                    + formatoDineroProveedor(
                            costoMinuto,
                            ANCHO_COSTO_AUTORIZACION)
                    + "|"
                    + formatoTiempo(
                            tiempoAutorizado);

        } catch (SQLException e) {

            return RESPUESTA_ERROR;
        }
    }

    // ==========================================================
    // CONSULTAR SALDO
    // ==========================================================

    public String consultarSaldo(String telefono) {

        try {

            Telefono linea =
                    telefonoDAO.buscarPorTelefono(
                            telefono);

            if (!lineaActiva(linea)) {
                return RESPUESTA_ERROR;
            }

            if (TIPO_SERVICIO_POSTPAGO.equals(linea.getTipoServicio())) {
                return RESPUESTA_OK + "|-1";
            }

            return RESPUESTA_OK
                    + "|"
                    + formatoDineroProveedor(
                            BigDecimal.valueOf(
                                    linea.getSaldo()),
                            ANCHO_SALDO);

        } catch (SQLException e) {

            return RESPUESTA_ERROR;
        }
    }

    // ==========================================================
    // REGISTRAR MOVIMIENTO
    // ==========================================================

    public String registrarMovimiento(
            String telefono,
            String detalle) {

        try {

            Telefono linea =
                    telefonoDAO.buscarPorTelefono(
                            telefono);

            if (!lineaActiva(linea)
                    || detalle == null
                    || detalle.length() < LONGITUD_MINIMA_DETALLE_MOVIMIENTO) {

                return RESPUESTA_ERROR;
            }

            Movimiento movimiento =
                    construirMovimiento(
                            telefono,
                            detalle);

            String tipoTarifa =
                    determinarTipoTarifaMovimiento(
                            telefono,
                            movimiento.getTelefonoDestino());

            movimientoDAO.guardarMovimiento(
                    movimiento,
                    tipoTarifa);

            if (TIPO_SERVICIO_PREPAGO.equals(linea.getTipoServicio())) {
                descontarSaldo(
                        linea,
                        movimiento.getCosto());
            }

            return RESPUESTA_OK;

        } catch (Exception e) {

            return RESPUESTA_ERROR;
        }
    }

    // ==========================================================
    // REGLAS DE NEGOCIO
    // ==========================================================

    private boolean lineaActiva(Telefono linea) {

        return linea != null
                && ESTADO_ACTIVO.equals(linea.getEstado());
    }

    private int calcularTiempoAutorizado(
            BigDecimal saldo,
            BigDecimal costoMinuto) {

        return saldo
                .divide(costoMinuto, 0, RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(SEGUNDOS_POR_MINUTO))
                .intValue();
    }

    private void descontarSaldo(
            Telefono linea,
            double costo) throws SQLException {

        double nuevoSaldo =
                Math.max(
                        0,
                        linea.getSaldo() - costo);

        telefonoDAO.actualizarSaldo(
                linea.getTelefono(),
                nuevoSaldo);
    }

    private String obtenerTipoTarifa(int tipoLlamada) {

        if (tipoLlamada == 1) {
            return TARIFA_MISMO_PROVEEDOR;
        }

        if (tipoLlamada == 2) {
            return TARIFA_OTRO_PROVEEDOR;
        }

        if (tipoLlamada == 3) {
            return TARIFA_INTERNACIONAL;
        }

        return null;
    }

    private String determinarTipoTarifaMovimiento(
            String telefonoOrigen,
            String telefonoDestino) throws SQLException {

        if (esNumeroInternacional(telefonoDestino)) {
            return TARIFA_INTERNACIONAL;
        }

        if (telefonoDAO.existeTelefono(telefonoDestino)
                && !telefonoOrigen.equals(telefonoDestino)) {

            return TARIFA_MISMO_PROVEEDOR;
        }

        return TARIFA_OTRO_PROVEEDOR;
    }

    private boolean esNumeroInternacional(String telefonoDestino) {

        return telefonoDestino != null
                && (telefonoDestino.startsWith("00")
                || telefonoDestino.startsWith("+"));
    }

    // ==========================================================
    // CONSTRUCCION DE MOVIMIENTO
    // ==========================================================

    private Movimiento construirMovimiento(
            String telefono,
            String detalle) {

        String fecha =
                detalle.substring(0, 8);

        String hora =
                detalle.substring(8, 14);

        String destino =
                detalle.substring(
                        14,
                        detalle.length() - 14);

        String costoTexto =
                detalle.substring(
                        detalle.length() - 14,
                        detalle.length() - 6);

        String duracion =
                detalle.substring(
                        detalle.length() - 6);

        BigDecimal costo =
                new BigDecimal(costoTexto)
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP);

        LocalDateTime fechaLlamada =
                LocalDateTime.parse(
                        fecha + hora,
                        DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Movimiento movimiento =
                new Movimiento();

        movimiento.setTelefono(telefono);
        movimiento.setFechaLlamada(fechaLlamada);
        movimiento.setTelefonoDestino(destino);
        movimiento.setCosto(costo.doubleValue());
        movimiento.setDuracion(duracion);

        return movimiento;
    }

    // ==========================================================
    // FORMATOS DE RESPUESTA
    // ==========================================================

    private String formatoDineroProveedor(
            BigDecimal valor,
            int ancho) {

        BigDecimal centavos =
                valor.multiply(BigDecimal.valueOf(100))
                        .setScale(0, RoundingMode.HALF_UP);

        return String.format(
                "%0" + ancho + "d",
                centavos.longValue());
    }

    private String formatoTiempo(int segundos) {

        int segundosControlados =
                Math.max(
                        0,
                        Math.min(
                                segundos,
                                TIEMPO_MAXIMO_SEGUNDOS));

        int horas =
                segundosControlados / 3600;

        int minutos =
                (segundosControlados % 3600) / 60;

        int segundosRestantes =
                segundosControlados % 60;

        return String.format(
                "%02d%02d%02d",
                horas,
                minutos,
                segundosRestantes);
    }
}
