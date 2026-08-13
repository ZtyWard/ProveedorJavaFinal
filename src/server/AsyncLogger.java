package server;

import util.BitacoraProveedor;
import util.Constantes;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncLogger {

    private final BlockingQueue<String> cola =
            new LinkedBlockingQueue<>();

    private final String rutaArchivo;

    public AsyncLogger() {

        rutaArchivo =
                System.getenv().getOrDefault(
                        "PROVEEDOR_LOG_PATH",
                        Constantes.RUTA_LOG_DEFAULT);

        Thread hilo =
                new Thread(this::procesarCola);

        hilo.setDaemon(true);
        hilo.start();
    }

    public void log(
            String tipo,
            String trama,
            String respuesta) {

        String registro =
                BitacoraProveedor.construirRegistroTransaccion(
                        tipo,
                        trama,
                        respuesta);

        cola.offer(registro);
    }

    private void procesarCola() {

        while (true) {

            try {

                String registro =
                        cola.take();

                escribirRegistro(
                        registro);

            } catch (Exception ignored) {
                // La bitacora no debe interrumpir las operaciones del proveedor.
            }
        }
    }

    private void escribirRegistro(String registro)
            throws java.io.IOException {

        File archivo =
                new File(rutaArchivo);

        File carpeta =
                archivo.getParentFile();

        if (carpeta != null) {
            carpeta.mkdirs();
        }

        try (FileWriter writer =
                     new FileWriter(archivo, true)) {

            writer.write(registro);
            writer.write(System.lineSeparator());
        }
    }
}
