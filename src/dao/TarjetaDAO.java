package dao;

import conexion.ConexionBD;
import modelo.Tarjeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TarjetaDAO {

    // ==========================================================
    // CONSULTAS SQL
    // ==========================================================

    private static final String SQL_EXISTE_TARJETA =
            """
            SELECT COUNT(*)
            FROM tarjetas
            WHERE identificador_tarjeta = ?
            """;

    private static final String SQL_BUSCAR_POR_IDENTIFICADOR =
            """
            SELECT
                ta.id,
                ta.identificador_tarjeta,
                t.telefono,
                ta.estado
            FROM tarjetas ta
            INNER JOIN telefonos t ON t.id = ta.telefono_id
            WHERE ta.identificador_tarjeta = ?
            """;

    private static final String SQL_BUSCAR_POR_TELEFONO =
            """
            SELECT
                ta.id,
                ta.identificador_tarjeta,
                t.telefono,
                ta.estado
            FROM tarjetas ta
            INNER JOIN telefonos t ON t.id = ta.telefono_id
            WHERE t.telefono = ?
            """;

    private static final String SQL_LISTAR =
            """
            SELECT
                ta.id,
                ta.identificador_tarjeta,
                t.telefono,
                ta.estado
            FROM tarjetas ta
            INNER JOIN telefonos t ON t.id = ta.telefono_id
            ORDER BY ta.id
            """;

    private static final String SQL_INSERTAR =
            """
            INSERT INTO tarjetas (
                identificador_tarjeta,
                telefono_id,
                estado
            )
            VALUES (
                ?,
                (
                    SELECT id
                    FROM telefonos
                    WHERE telefono = ?
                ),
                ?
            )
            """;

    private static final String SQL_ACTUALIZAR =
            """
            UPDATE tarjetas
            SET
                telefono_id = (
                    SELECT id
                    FROM telefonos
                    WHERE telefono = ?
                ),
                estado = ?
            WHERE identificador_tarjeta = ?
            """;

    private static final String SQL_ACTUALIZAR_ESTADO =
            """
            UPDATE tarjetas
            SET estado = ?
            WHERE identificador_tarjeta = ?
            """;

    private static final String SQL_ELIMINAR =
            """
            DELETE FROM tarjetas
            WHERE identificador_tarjeta = ?
            """;

    // ==========================================================
    // EXISTE TARJETA
    // ==========================================================

    public boolean existeTarjeta(String identificadorTarjeta)
            throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_EXISTE_TARJETA)) {

            ps.setString(1, identificadorTarjeta);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ==========================================================
    // BUSCAR TARJETA
    // ==========================================================

    public Tarjeta buscarPorIdentificador(String identificadorTarjeta)
            throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps =
                     conexion.prepareStatement(SQL_BUSCAR_POR_IDENTIFICADOR)) {

            ps.setString(1, identificadorTarjeta);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return mapearTarjeta(rs);
            }
        }
    }

    public Tarjeta buscarPorTelefono(String telefono) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_TELEFONO)) {

            ps.setString(1, telefono);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return mapearTarjeta(rs);
            }
        }
    }

    // ==========================================================
    // LISTAR TARJETAS
    // ==========================================================

    public List<Tarjeta> listarTarjetas() throws SQLException {

        List<Tarjeta> tarjetas = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tarjetas.add(mapearTarjeta(rs));
            }
        }

        return tarjetas;
    }

    // ==========================================================
    // GUARDAR TARJETA
    // ==========================================================

    public void guardarTarjeta(Tarjeta tarjeta) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR)) {

            ps.setString(1, tarjeta.getIdentificadorTarjeta());
            ps.setString(2, tarjeta.getTelefono());
            ps.setString(3, tarjeta.getEstado());

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // ACTUALIZAR TARJETA
    // ==========================================================

    public void actualizarTarjeta(Tarjeta tarjeta) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {

            ps.setString(1, tarjeta.getTelefono());
            ps.setString(2, tarjeta.getEstado());
            ps.setString(3, tarjeta.getIdentificadorTarjeta());

            ps.executeUpdate();
        }
    }

    public void actualizarEstado(
            String identificadorTarjeta,
            String estado) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR_ESTADO)) {

            ps.setString(1, estado);
            ps.setString(2, identificadorTarjeta);

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // ELIMINAR TARJETA
    // ==========================================================

    public void eliminarTarjeta(String identificadorTarjeta)
            throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {

            ps.setString(1, identificadorTarjeta);

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // MAPEO
    // ==========================================================

    private Tarjeta mapearTarjeta(ResultSet rs) throws SQLException {

        Tarjeta tarjeta = new Tarjeta();

        tarjeta.setId(rs.getInt("id"));
        tarjeta.setIdentificadorTarjeta(
                rs.getString("identificador_tarjeta"));
        tarjeta.setTelefono(rs.getString("telefono"));
        tarjeta.setEstado(rs.getString("estado"));

        return tarjeta;
    }
}
