package dao;

import conexion.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    // ==========================================================
    // CONSULTAS SQL
    // ==========================================================

    private static final String SQL_EXISTE_CLIENTE =
            """
            SELECT COUNT(*)
            FROM clientes
            WHERE identificacion = ?
            """;

    private static final String SQL_BUSCAR_ID_POR_IDENTIFICACION =
            """
            SELECT id
            FROM clientes
            WHERE identificacion = ?
            """;

    private static final String SQL_BUSCAR_ESTADO =
            """
            SELECT activo
            FROM clientes
            WHERE identificacion = ?
            """;

    private static final String SQL_LISTAR_IDENTIFICACIONES =
            """
            SELECT identificacion
            FROM clientes
            ORDER BY id
            """;

    private static final String SQL_INSERTAR =
            """
            INSERT INTO clientes (
                identificacion,
                activo
            )
            VALUES (?, ?)
            """;

    private static final String SQL_ACTUALIZAR_ESTADO =
            """
            UPDATE clientes
            SET activo = ?
            WHERE identificacion = ?
            """;

    private static final String SQL_ELIMINAR =
            """
            DELETE FROM clientes
            WHERE identificacion = ?
            """;

    // ==========================================================
    // EXISTE CLIENTE
    // ==========================================================

    public boolean existeCliente(String identificacion) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_EXISTE_CLIENTE)) {

            ps.setString(1, identificacion);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ==========================================================
    // BUSCAR CLIENTE
    // ==========================================================

    public Integer buscarIdPorIdentificacion(String identificacion)
            throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps =
                     conexion.prepareStatement(SQL_BUSCAR_ID_POR_IDENTIFICACION)) {

            ps.setString(1, identificacion);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return rs.getInt("id");
            }
        }
    }

    public Boolean buscarEstado(String identificacion) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_ESTADO)) {

            ps.setString(1, identificacion);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return rs.getBoolean("activo");
            }
        }
    }

    // ==========================================================
    // LISTAR CLIENTES
    // ==========================================================

    public List<String> listarIdentificaciones() throws SQLException {

        List<String> identificaciones = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps =
                     conexion.prepareStatement(SQL_LISTAR_IDENTIFICACIONES);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                identificaciones.add(
                        rs.getString("identificacion"));
            }
        }

        return identificaciones;
    }

    // ==========================================================
    // GUARDAR CLIENTE
    // ==========================================================

    public void guardarCliente(
            String identificacion,
            boolean activo) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR)) {

            ps.setString(1, identificacion);
            ps.setBoolean(2, activo);

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // ACTUALIZAR CLIENTE
    // ==========================================================

    public void actualizarEstado(
            String identificacion,
            boolean activo) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR_ESTADO)) {

            ps.setBoolean(1, activo);
            ps.setString(2, identificacion);

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // ELIMINAR CLIENTE
    // ==========================================================

    public void eliminarCliente(String identificacion) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {

            ps.setString(1, identificacion);

            ps.executeUpdate();
        }
    }
}
