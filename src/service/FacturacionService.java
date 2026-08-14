package service;

import dao.FacturacionDAO;
import modelo.Factura;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class FacturacionService {

    private static final String RESPUESTA_OK =
            "OK";

    private static final String RESPUESTA_ERROR =
            "ERROR";

    private final FacturacionDAO facturacionDAO;

    public FacturacionService() {

        this.facturacionDAO =
                new FacturacionDAO();
    }

    // ==========================================================
    // CALCULAR FACTURACION
    // ==========================================================

    public String calcularFacturacionPostpago(
            LocalDate fechaCalculo,
            LocalDate fechaMaximaPago) {

        try {

            if (fechasInvalidas(
                    fechaCalculo,
                    fechaMaximaPago)) {

                return RESPUESTA_ERROR;
            }

            System.out.println(
                    "Calculando facturacion: "
                            + fechaCalculo
                            + " -> "
                            + fechaMaximaPago);

            facturacionDAO.calcularFacturacionPostpago(
                    fechaCalculo,
                    fechaMaximaPago);

            System.out.println(
                    "Facturacion ejecutada correctamente.");

            return RESPUESTA_OK;

        } catch (SQLException e) {

            System.out.println(
                    "ERROR SQL EN FACTURACION:");

            e.printStackTrace();

            return RESPUESTA_ERROR;
        }
    }

    // ==========================================================
    // OBTENER ULTIMA FECHA DE FACTURACION
    // ==========================================================

    public LocalDate obtenerUltimaFechaFacturacion() {

        try {

            return facturacionDAO
                    .obtenerUltimaFechaFacturacion();

        } catch (SQLException e) {

            System.out.println(
                    "ERROR SQL OBTENIENDO ULTIMA FACTURACION:");

            e.printStackTrace();

            return null;
        }
    }

    // ==========================================================
    // CONSULTAR FACTURAS
    // ==========================================================

    public List<Factura> listarFacturasPorTelefono(
            String telefono) {

        try {

            if (valorVacio(telefono)) {
                return Collections.emptyList();
            }

            return facturacionDAO.listarPorTelefono(
                    telefono);

        } catch (SQLException e) {

            System.out.println(
                    "ERROR SQL LISTANDO FACTURAS:");

            e.printStackTrace();

            return Collections.emptyList();
        }
    }

    public Factura buscarFactura(int id) {

        try {

            if (id <= 0) {
                return null;
            }

            return facturacionDAO.buscarPorId(
                    id);

        } catch (SQLException e) {

            System.out.println(
                    "ERROR SQL BUSCANDO FACTURA:");

            e.printStackTrace();

            return null;
        }
    }

    // ==========================================================
    // PAGOS
    // ==========================================================

    public String marcarFacturaPagada(int id) {

        return actualizarEstadoPago(
                id,
                true);
    }

    public String marcarFacturaPendiente(int id) {

        return actualizarEstadoPago(
                id,
                false);
    }

    private String actualizarEstadoPago(
            int id,
            boolean pagada) {

        try {

            if (id <= 0) {
                return RESPUESTA_ERROR;
            }

            facturacionDAO.actualizarEstadoPago(
                    id,
                    pagada);

            return RESPUESTA_OK;

        } catch (SQLException e) {

            System.out.println(
                    "ERROR SQL ACTUALIZANDO PAGO:");

            e.printStackTrace();

            return RESPUESTA_ERROR;
        }
    }

    // ==========================================================
    // VALIDACIONES
    // ==========================================================

    private boolean fechasInvalidas(
            LocalDate fechaCalculo,
            LocalDate fechaMaximaPago) {

        return fechaCalculo == null
                || fechaMaximaPago == null
                || fechaMaximaPago.isBefore(fechaCalculo);
    }

    private boolean valorVacio(String valor) {

        return valor == null
                || valor.trim().isEmpty();
    }
}