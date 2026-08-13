package dao;

import conexion.ConexionBD;
import modelo.Telefono;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TelefonoDAO {

    // ==========================================================
    // CONSULTAS SQL
    // ==========================================================

    private static final String SQL_EXISTE_TELEFONO =
            """
            SELECT COUNT(*)
            FROM telefonos
            WHERE telefono = ?
            """;

    private static final String SQL_BUSCAR_POR_ID =
            """
            SELECT
                t.id,
                t.telefono,
                t.identificador_telefono,
                ta.identificador_tarjeta,
                t.tipo_servicio,
                t.estado,
                c.identificacion AS identificacion_cliente,
                t.saldo,
                t.fecha_activacion
            FROM telefonos t
            LEFT JOIN clientes c ON c.id = t.cliente_id
            LEFT JOIN tarjetas ta ON ta.telefono_id = t.id
            WHERE t.id = ?
            """;

    private static final String SQL_BUSCAR_POR_TELEFONO =
            """
            SELECT
                t.id,
                t.telefono,
                t.identificador_telefono,
                ta.identificador_tarjeta,
                t.tipo_servicio,
                t.estado,
                c.identificacion AS identificacion_cliente,
                t.saldo,
                t.fecha_activacion
            FROM telefonos t
            LEFT JOIN clientes c ON c.id = t.cliente_id
            LEFT JOIN tarjetas ta ON ta.telefono_id = t.id
            WHERE t.telefono = ?
            """;

    private static final String SQL_LISTAR =
            """
            SELECT
                t.id,
                t.telefono,
                t.identificador_telefono,
                ta.identificador_tarjeta,
                t.tipo_servicio,
                t.estado,
                c.identificacion AS identificacion_cliente,
                t.saldo,
                t.fecha_activacion
            FROM telefonos t
            LEFT JOIN clientes c ON c.id = t.cliente_id
            LEFT JOIN tarjetas ta ON ta.telefono_id = t.id
            ORDER BY t.id
            """;

    private static final String SQL_INSERTAR =
            """
            INSERT INTO telefonos (
                telefono,
                identificador_telefono,
                tipo_servicio,
                estado,
                cliente_id,
                saldo,
                fecha_activacion
            )
            VALUES (
                ?,
                ?,
                ?,
                ?,
                (
                    SELECT id
                    FROM clientes
                    WHERE identificacion = ?
                ),
                ?,
                ?
            )
            """;

    private static final String SQL_ACTUALIZAR =
            """
            UPDATE telefonos
            SET
                identificador_telefono = ?,
                tipo_servicio = ?,
                estado = ?,
                cliente_id = (
                    SELECT id
                    FROM clientes
                    WHERE identificacion = ?
                ),
                saldo = ?,
                fecha_activacion = ?
            WHERE telefono = ?
            """;

    private static final String SQL_ACTUALIZAR_ESTADO =
            """
            UPDATE telefonos
            SET estado = ?
            WHERE telefono = ?
            """;

    private static final String SQL_ACTUALIZAR_SALDO =
            """
            UPDATE telefonos
            SET saldo = ?
            WHERE telefono = ?
            """;

    private static final String SQL_ELIMINAR =
            """
            DELETE FROM telefonos
            WHERE telefono = ?
            """;

    // ==========================================================
    // EXISTE TELEFONO
    // ==========================================================

    public boolean existeTelefono(String telefono) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_EXISTE_TELEFONO)) {

            ps.setString(1, telefono);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ==========================================================
    // BUSCAR TELEFONO
    // ==========================================================

    public Telefono buscarPorId(int id) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return mapearTelefono(rs);
            }
        }
    }

    public Telefono buscarTelefono(String telefono) throws SQLException {

        return buscarPorTelefono(telefono);
    }

    public Telefono buscarPorTelefono(String telefono) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_TELEFONO)) {

            ps.setString(1, telefono);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return mapearTelefono(rs);
            }
        }
    }

    // ==========================================================
    // LISTAR TELEFONOS
    // ==========================================================

    public List<Telefono> listarTelefonos() throws SQLException {

        List<Telefono> telefonos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                telefonos.add(mapearTelefono(rs));
            }
        }

        return telefonos;
    }

    // ==========================================================
    // GUARDAR TELEFONO
    // ==========================================================

    public void guardarTelefono(Telefono telefono) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR)) {

            ps.setString(1, telefono.getTelefono());
            ps.setString(2, telefono.getIdentificadorTelefono());
            ps.setString(3, telefono.getTipoServicio());
            ps.setString(4, telefono.getEstado());
            ps.setString(5, telefono.getIdentificacionCliente());
            ps.setDouble(6, telefono.getSaldo());
            setFechaActivacion(ps, 7, telefono);

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // ACTUALIZAR TELEFONO
    // ==========================================================

    public void actualizarTelefono(Telefono telefono) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {

            ps.setString(1, telefono.getIdentificadorTelefono());
            ps.setString(2, telefono.getTipoServicio());
            ps.setString(3, telefono.getEstado());
            ps.setString(4, telefono.getIdentificacionCliente());
            ps.setDouble(5, telefono.getSaldo());
            setFechaActivacion(ps, 6, telefono);
            ps.setString(7, telefono.getTelefono());

            ps.executeUpdate();
        }
    }

    public void actualizarEstado(String telefono, String estado) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR_ESTADO)) {

            ps.setString(1, estado);
            ps.setString(2, telefono);

            ps.executeUpdate();
        }
    }

    public void actualizarSaldo(String telefono, double saldo) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR_SALDO)) {

            ps.setDouble(1, saldo);
            ps.setString(2, telefono);

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // ELIMINAR TELEFONO
    // ==========================================================

    public void eliminarTelefono(String telefono) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {

            ps.setString(1, telefono);

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // MAPEO
    // ==========================================================

    private Telefono mapearTelefono(ResultSet rs) throws SQLException {

        Telefono telefono = new Telefono();

        telefono.setId(rs.getInt("id"));
        telefono.setTelefono(rs.getString("telefono"));
        telefono.setIdentificadorTelefono(rs.getString("identificador_telefono"));
        telefono.setIdentificadorTarjeta(rs.getString("identificador_tarjeta"));
        telefono.setTipoServicio(rs.getString("tipo_servicio"));
        telefono.setEstado(rs.getString("estado"));
        telefono.setIdentificacionCliente(rs.getString("identificacion_cliente"));
        telefono.setSaldo(rs.getDouble("saldo"));

        Timestamp fechaActivacion =
                rs.getTimestamp("fecha_activacion");

        if (fechaActivacion != null) {
            telefono.setFechaActivacion(
                    fechaActivacion.toLocalDateTime());
        }

        return telefono;
    }

    private void setFechaActivacion(
            PreparedStatement ps,
            int indice,
            Telefono telefono) throws SQLException {

        if (telefono.getFechaActivacion() == null) {
            ps.setNull(indice, Types.TIMESTAMP);
            return;
        }

        ps.setTimestamp(
                indice,
                Timestamp.valueOf(
                        telefono.getFechaActivacion()));
    }
}
