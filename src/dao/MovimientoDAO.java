package dao;

import conexion.ConexionBD;
import modelo.Movimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO {

    // ==========================================================
    // CONSULTAS SQL
    // ==========================================================

    private static final String SQL_BUSCAR_POR_ID =
            """
            SELECT
                m.id,
                t.telefono,
                m.fecha_llamada,
                m.telefono_destino,
                m.costo,
                m.duracion
            FROM movimientos m
            INNER JOIN telefonos t ON t.id = m.telefono_id
            WHERE m.id = ?
            """;

    private static final String SQL_LISTAR_POR_TELEFONO =
            """
            SELECT
                m.id,
                t.telefono,
                m.fecha_llamada,
                m.telefono_destino,
                m.costo,
                m.duracion
            FROM movimientos m
            INNER JOIN telefonos t ON t.id = m.telefono_id
            WHERE t.telefono = ?
            ORDER BY m.fecha_llamada DESC
            """;

    private static final String SQL_INSERTAR =
            """
            INSERT INTO movimientos (
                telefono_id,
                tarifa_id,
                fecha_llamada,
                telefono_destino,
                costo,
                duracion
            )
            VALUES (
                (
                    SELECT id
                    FROM telefonos
                    WHERE telefono = ?
                ),
                (
                    SELECT id
                    FROM tarifas
                    WHERE tipo_llamada = ?
                ),
                ?,
                ?,
                ?,
                ?
            )
            """;

    private static final String SQL_ACTUALIZAR =
            """
            UPDATE movimientos
            SET
                fecha_llamada = ?,
                telefono_destino = ?,
                costo = ?,
                duracion = ?
            WHERE id = ?
            """;

    private static final String SQL_ELIMINAR =
            """
            DELETE FROM movimientos
            WHERE id = ?
            """;

    // ==========================================================
    // BUSCAR MOVIMIENTO
    // ==========================================================

    public Movimiento buscarPorId(int id) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return mapearMovimiento(rs);
            }
        }
    }

    // ==========================================================
    // LISTAR MOVIMIENTOS
    // ==========================================================

    public List<Movimiento> listarPorTelefono(String telefono)
            throws SQLException {

        List<Movimiento> movimientos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps =
                     conexion.prepareStatement(SQL_LISTAR_POR_TELEFONO)) {

            ps.setString(1, telefono);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    movimientos.add(mapearMovimiento(rs));
                }
            }
        }

        return movimientos;
    }

    // ==========================================================
    // GUARDAR MOVIMIENTO
    // ==========================================================

    public void guardarMovimiento(
            Movimiento movimiento,
            String tipoLlamada) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR)) {

            ps.setString(1, movimiento.getTelefono());
            ps.setString(2, tipoLlamada);
            setFechaLlamada(ps, 3, movimiento);
            ps.setString(4, movimiento.getTelefonoDestino());
            ps.setDouble(5, movimiento.getCosto());
            ps.setString(6, movimiento.getDuracion());

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // ACTUALIZAR MOVIMIENTO
    // ==========================================================

    public void actualizarMovimiento(Movimiento movimiento)
            throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {

            setFechaLlamada(ps, 1, movimiento);
            ps.setString(2, movimiento.getTelefonoDestino());
            ps.setDouble(3, movimiento.getCosto());
            ps.setString(4, movimiento.getDuracion());
            ps.setInt(5, movimiento.getId());

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // ELIMINAR MOVIMIENTO
    // ==========================================================

    public void eliminarMovimiento(int id) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {

            ps.setInt(1, id);

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // MAPEO
    // ==========================================================

    private Movimiento mapearMovimiento(ResultSet rs) throws SQLException {

        Movimiento movimiento = new Movimiento();

        movimiento.setId(rs.getInt("id"));
        movimiento.setTelefono(rs.getString("telefono"));
        movimiento.setTelefonoDestino(rs.getString("telefono_destino"));
        movimiento.setCosto(rs.getDouble("costo"));
        movimiento.setDuracion(rs.getString("duracion"));

        Timestamp fechaLlamada =
                rs.getTimestamp("fecha_llamada");

        if (fechaLlamada != null) {
            movimiento.setFechaLlamada(
                    fechaLlamada.toLocalDateTime());
        }

        return movimiento;
    }

    private void setFechaLlamada(
            PreparedStatement ps,
            int indice,
            Movimiento movimiento) throws SQLException {

        if (movimiento.getFechaLlamada() == null) {
            ps.setNull(indice, Types.TIMESTAMP);
            return;
        }

        ps.setTimestamp(
                indice,
                Timestamp.valueOf(
                        movimiento.getFechaLlamada()));
    }
}
