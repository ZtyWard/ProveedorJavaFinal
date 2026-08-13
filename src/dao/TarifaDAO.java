package dao;

import conexion.ConexionBD;
import modelo.Tarifa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TarifaDAO {

    // ==========================================================
    // CONSULTAS SQL
    // ==========================================================

    private static final String SQL_BUSCAR_POR_ID =
            """
            SELECT
                id,
                tipo_llamada,
                costo_minuto
            FROM tarifas
            WHERE id = ?
            """;

    private static final String SQL_BUSCAR_POR_TIPO =
            """
            SELECT
                id,
                tipo_llamada,
                costo_minuto
            FROM tarifas
            WHERE tipo_llamada = ?
            """;

    private static final String SQL_LISTAR =
            """
            SELECT
                id,
                tipo_llamada,
                costo_minuto
            FROM tarifas
            ORDER BY id
            """;

    private static final String SQL_INSERTAR =
            """
            INSERT INTO tarifas (
                tipo_llamada,
                costo_minuto
            )
            VALUES (?, ?)
            """;

    private static final String SQL_ACTUALIZAR =
            """
            UPDATE tarifas
            SET
                tipo_llamada = ?,
                costo_minuto = ?
            WHERE id = ?
            """;

    private static final String SQL_ELIMINAR =
            """
            DELETE FROM tarifas
            WHERE id = ?
            """;

    // ==========================================================
    // BUSCAR TARIFA
    // ==========================================================

    public Tarifa buscarPorId(int id) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return mapearTarifa(rs);
            }
        }
    }

    public Tarifa obtenerTarifa(String tipoLlamada) throws SQLException {

        return buscarPorTipo(tipoLlamada);
    }

    public Tarifa buscarPorTipo(String tipoLlamada) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_TIPO)) {

            ps.setString(1, tipoLlamada);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return mapearTarifa(rs);
            }
        }
    }

    // ==========================================================
    // LISTAR TARIFAS
    // ==========================================================

    public List<Tarifa> listarTarifas() throws SQLException {

        List<Tarifa> tarifas = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tarifas.add(mapearTarifa(rs));
            }
        }

        return tarifas;
    }

    // ==========================================================
    // GUARDAR TARIFA
    // ==========================================================

    public void guardarTarifa(Tarifa tarifa) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR)) {

            ps.setString(1, tarifa.getTipoLlamada());
            ps.setDouble(2, tarifa.getCostoMinuto());

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // ACTUALIZAR TARIFA
    // ==========================================================

    public void actualizarTarifa(Tarifa tarifa) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {

            ps.setString(1, tarifa.getTipoLlamada());
            ps.setDouble(2, tarifa.getCostoMinuto());
            ps.setInt(3, tarifa.getId());

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // ELIMINAR TARIFA
    // ==========================================================

    public void eliminarTarifa(int id) throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {

            ps.setInt(1, id);

            ps.executeUpdate();
        }
    }

    // ==========================================================
    // MAPEO
    // ==========================================================

    private Tarifa mapearTarifa(ResultSet rs) throws SQLException {

        Tarifa tarifa = new Tarifa();

        tarifa.setId(rs.getInt("id"));
        tarifa.setTipoLlamada(rs.getString("tipo_llamada"));
        tarifa.setCostoMinuto(rs.getDouble("costo_minuto"));

        return tarifa;
    }
}
