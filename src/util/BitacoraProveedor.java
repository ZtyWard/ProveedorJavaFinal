package util;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class BitacoraProveedor {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private BitacoraProveedor() {
    }

    public static void registrar(
            String tipo,
            String mensaje) {

        registrar(
                rutaLog(),
                tipo,
                mensaje);
    }

    public static void registrar(
            String rutaArchivo,
            String tipo,
            String mensaje) {

        try {

            File archivo =
                    new File(rutaArchivo);

            File carpeta =
                    archivo.getParentFile();

            if (carpeta != null) {
                carpeta.mkdirs();
            }

            try (FileWriter writer =
                         new FileWriter(archivo, true)) {

                writer.write(
                        construirRegistro(
                                tipo,
                                mensaje));

                writer.write(
                        System.lineSeparator());
            }

        } catch (Exception ignored) {
            // La bitacora no debe interrumpir el flujo principal.
        }
    }

    public static String construirRegistro(
            String tipo,
            String mensaje) {

        String fecha =
                LocalDateTime.now()
                        .format(FORMATO_FECHA);

        return "{\"fecha\":\"" + escapar(fecha) + "\"," +
                "\"tipo\":\"" + escapar(tipo) + "\"," +
                "\"mensaje\":\"" + escapar(mensaje) + "\"}";
    }

    public static String construirRegistroTransaccion(
            String tipo,
            String trama,
            String respuesta) {

        String fecha =
                LocalDateTime.now()
                        .format(FORMATO_FECHA);

        return "{\"fecha\":\"" + escapar(fecha) + "\"," +
                "\"tipo\":\"" + escapar(tipo) + "\"," +
                "\"trama\":\"" + escapar(trama) + "\"," +
                "\"respuesta\":\"" + escapar(respuesta) + "\"}";
    }

    public static String escapar(String valor) {

        if (valor == null) {
            return "";
        }

        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static String rutaLog() {

        return System.getenv()
                .getOrDefault(
                        "PROVEEDOR_LOG_PATH",
                        Constantes.RUTA_LOG_DEFAULT);
    }
}
