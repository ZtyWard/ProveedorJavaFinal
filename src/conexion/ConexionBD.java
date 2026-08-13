package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionBD {

    private static final String SERVIDOR =
            System.getenv().getOrDefault(
                    "SQLSERVER_HOST",
                    "localhost\\SQLEXPRESS");

    private static final String BASE_DATOS =
            System.getenv().getOrDefault(
                    "SQLSERVER_DB",
                    "ProveedorTelefonicoDB");

    private static final String USUARIO =
            System.getenv().getOrDefault(
                    "SQLSERVER_USER",
                    "javauser");

    private static final String CLAVE =
            System.getenv().getOrDefault(
                    "SQLSERVER_PASSWORD",
                    "Java123456");

    private static final String URL =
            "jdbc:sqlserver://" + SERVIDOR + ";" +
            "databaseName=" + BASE_DATOS + ";" +
            "user=" + USUARIO + ";" +
            "password=" + CLAVE + ";" +
            "encrypt=false;" +
            "trustServerCertificate=true;";

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {

        return DriverManager.getConnection(URL);

    }

}