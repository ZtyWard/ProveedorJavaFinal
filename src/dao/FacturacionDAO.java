package dao;

import conexion.ConexionBD;
import modelo.Factura;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class FacturacionDAO {

    // ==========================================================
    // CONSULTAS SQL
    // ==========================================================

    private static final String SQL_BUSCAR_POR_ID =
            """
            SELECT
                f.id,
                t.telefono,
                f.fecha_calculo,
                f.fecha_maxima_pago,
                f.monto,
                f.pagada
            FROM facturas f
            INNER JOIN telefonos t ON t.id = f.telefono_id
            WHERE f.id = ?
            """;

    private static final String SQL_LISTAR_POR_TELEFONO =
            """
            SELECT
                f.id,
                t.telefono,
                f.fecha_calculo,
                f.fecha_maxima_pago,
                f.monto,
                f.pagada
            FROM facturas f
            INNER JOIN telefonos t ON t.id = f.telefono_id
            WHERE t.telefono = ?
            ORDER BY f.fecha_calculo DESC
            """;

    private static final String SQL_INSERTAR =
            """
            INSERT INTO facturas (
                telefono_id,
                fecha_calculo,
                fecha_maxima_pago,
                monto,
                pagada
            )
            VALUES (
                (
                    SELECT id
                    FROM telefonos
                    WHERE telefono = ?
                ),
                ?,
                ?,
                ?,
                ?
            )
            """;

    private static final String SQL_ACTUALIZAR =
            """
            UPDATE facturas
            SET
                fecha_calculo = ?,
                fecha_maxima_pago = ?,
                monto = ?,
                pagada = ?
            WHERE id = ?
            """;

    private static final String SQL_ACTUALIZAR_ESTADO_PAGO =
            """
            UPDATE facturas
            SET pagada = ?
            WHERE id = ?
            """;

    private static final String SQL_ELIMINAR =
            """
            DELETE FROM facturas
            WHERE id = ?
            """;

    private static final String SQL_CALCULAR_FACTURACION =
            "{call sp_calcular_facturacion_postpago(?, ?)}";

    private static final String SQL_ULTIMA_FECHA_FACTURACION =
            """
            SELECT MAX(fecha_calculo)
            FROM facturas
            """;

    // ==========================================================
    // BUSCAR FACTURA
    // ==========================================================

    public Factura buscarPorId(int id) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return mapearFactura(rs);
            }
        }
    }

    // ==========================================================
    // LISTAR FACTURAS
    // ==========================================================

    public List<Factura> listarPorTelefono(String telefono)
            throws SQLException {

        List<Factura> facturas = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps =
                     conexion.prepareStatement(SQL_LISTAR_POR_TELEFONO)) {

            ps.setString(1, telefono);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    facturas.add(mapearFactura(rs));
                }
            }
        }

        return facturas;
    }

    // ==========================================================
    // GUARDAR FACTURA
    // ==========================================================

    public void guardarFactura(Factura factura) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR)) {

            ps.setString(1, factura.getTelefono());
            setFecha(ps, 2, factura.getFechaCalculo());
            setFecha(ps, 3, factura.getFechaMaximaPago());
            ps.setDouble(4, factura.getMonto());
            ps.setBoolean(5, factura.isPagada());

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // ACTUALIZAR FACTURA
    // ==========================================================

    public void actualizarFactura(Factura factura) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {

            setFecha(ps, 1, factura.getFechaCalculo());
            setFecha(ps, 2, factura.getFechaMaximaPago());
            ps.setDouble(3, factura.getMonto());
            ps.setBoolean(4, factura.isPagada());
            ps.setInt(5, factura.getId());

            ps.executeUpdate();
        }
    }

    public void actualizarEstadoPago(int id, boolean pagada)
            throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps =
                     conexion.prepareStatement(SQL_ACTUALIZAR_ESTADO_PAGO)) {

            ps.setBoolean(1, pagada);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // CALCULAR FACTURACION
    // ==========================================================

    public void calcularFacturacionPostpago(
            java.time.LocalDate fechaCalculo,
            java.time.LocalDate fechaMaximaPago) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             CallableStatement cs =
                     conexion.prepareCall(SQL_CALCULAR_FACTURACION)) {

            setFecha(cs, 1, fechaCalculo);
            setFecha(cs, 2, fechaMaximaPago);

            cs.execute();
        }
    }

    // ==========================================================
    // OBTENER ULTIMA FECHA DE FACTURACION
    // ==========================================================

    public java.time.LocalDate obtenerUltimaFechaFacturacion()
            throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps =
                     conexion.prepareStatement(SQL_ULTIMA_FECHA_FACTURACION);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return null;
            }

            Date fecha = rs.getDate(1);

            if (fecha == null) {
                return null;
            }

            return fecha.toLocalDate();
        }
    }

    // ==========================================================
    // ELIMINAR FACTURA
    // ==========================================================

    public void eliminarFactura(int id) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {

            ps.setInt(1, id);

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // MAPEO
    // ==========================================================

    private Factura mapearFactura(ResultSet rs) throws SQLException {

        Factura factura = new Factura();

        factura.setId(rs.getInt("id"));
        factura.setTelefono(rs.getString("telefono"));

        Date fechaCalculo =
                rs.getDate("fecha_calculo");

        if (fechaCalculo != null) {
            factura.setFechaCalculo(
                    fechaCalculo.toLocalDate());
        }

        Date fechaMaximaPago =
                rs.getDate("fecha_maxima_pago");

        if (fechaMaximaPago != null) {
            factura.setFechaMaximaPago(
                    fechaMaximaPago.toLocalDate());
        }

        factura.setMonto(rs.getDouble("monto"));
        factura.setPagada(rs.getBoolean("pagada"));

        return factura;
    }

    private void setFecha(
            PreparedStatement ps,
            int indice,
            java.time.LocalDate fecha) throws SQLException {

        if (fecha == null) {
            ps.setNull(indice, Types.DATE);
            return;
        }

        ps.setDate(indice, Date.valueOf(fecha));
    }
}