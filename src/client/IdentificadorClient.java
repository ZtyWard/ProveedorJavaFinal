package client;

import util.CifradoIdentificador;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Cliente encargado de comunicar el Proveedor Java
 * con el Identificador desarrollado en Python.
 *
 * Envía la historia IDENTIFICADOR6 al puerto 5000.
 */
public class IdentificadorClient {

    private static final String HOST_PREDETERMINADO =
            "127.0.0.1";

    private static final int PUERTO_PREDETERMINADO =
            5000;

    private static final String CODIGO_PROVEEDOR_PREDETERMINADO =
            "P1";

    private static final String RESPUESTA_FALLIDA =
            "Activación fallida";

    private final String host;
    private final int puerto;
    private final String codigoProveedor;

    /**
     * Lee la configuración desde variables de ambiente.
     *
     * IDENTIFICADOR_HOST
     * IDENTIFICADOR_PORT
     * CODIGO_PROVEEDOR
     */
    public IdentificadorClient() {

        this.host =
                System.getenv().getOrDefault(
                        "IDENTIFICADOR_HOST",
                        HOST_PREDETERMINADO
                );

        this.puerto =
                obtenerPuerto();

        this.codigoProveedor =
                System.getenv().getOrDefault(
                        "CODIGO_PROVEEDOR",
                        CODIGO_PROVEEDOR_PREDETERMINADO
                );
    }

    /**
     * Cifra los datos sensibles, construye la trama
     * IDENTIFICADOR6 y la envía al Identificador Python.
     *
     * Formato:
     *
     * IDENTIFICADOR6|
     * telefonoCifrado|
     * identificadorTelefonoCifrado|
     * identificadorTarjetaCifrado|
     * tipoServicio|
     * identificacionCliente|
     * estado|
     * codigoProveedor
     */
    public String notificarCambioLinea(
            String telefono,
            String identificadorTelefono,
            String identificadorTarjeta,
            String tipoServicio,
            String identificacionCliente,
            String estado) {

        try {

            if (!datosValidos(
                    telefono,
                    identificadorTelefono,
                    identificadorTarjeta,
                    tipoServicio,
                    identificacionCliente,
                    estado)) {

                return RESPUESTA_FALLIDA;
            }

            String telefonoCifrado =
                    CifradoIdentificador.cifrar(
                            telefono.trim());

            String identificadorTelefonoCifrado =
                    CifradoIdentificador.cifrar(
                            identificadorTelefono.trim());

            String identificadorTarjetaCifrado =
                    CifradoIdentificador.cifrar(
                            identificadorTarjeta.trim());

            String tipoNormalizado =
                    tipoServicio
                            .trim()
                            .toUpperCase(Locale.ROOT);

            String estadoNormalizado =
                    normalizarEstado(estado);

            if (estadoNormalizado == null) {
                return RESPUESTA_FALLIDA;
            }

            String trama =
                    String.join(
                            "|",
                            "IDENTIFICADOR6",
                            telefonoCifrado,
                            identificadorTelefonoCifrado,
                            identificadorTarjetaCifrado,
                            tipoNormalizado,
                            identificacionCliente.trim(),
                            estadoNormalizado,
                            codigoProveedor
                                    .trim()
                                    .toUpperCase(Locale.ROOT)
                    );

            System.out.println(
                    "Enviando IDENTIFICADOR6 al Identificador Python...");

            String respuesta =
                    enviarTrama(trama);

            System.out.println(
                    "Respuesta del Identificador: "
                            + respuesta);

            return respuesta;

        } catch (Exception e) {

            System.out.println(
                    "Error comunicando con el Identificador Python:");

            e.printStackTrace();

            return RESPUESTA_FALLIDA;
        }
    }

    /**
     * Abre una conexión TCP con el Identificador Python.
     */
    private String enviarTrama(String trama)
            throws Exception {

        try (Socket socket = new Socket()) {

            socket.connect(
                    new InetSocketAddress(
                            host,
                            puerto
                    ),
                    5000
            );

            socket.setSoTimeout(
                    10000
            );

            try (
                    BufferedWriter salida =
                            new BufferedWriter(
                                    new OutputStreamWriter(
                                            socket.getOutputStream(),
                                            StandardCharsets.UTF_8
                                    )
                            );

                    BufferedReader entrada =
                            new BufferedReader(
                                    new InputStreamReader(
                                            socket.getInputStream(),
                                            StandardCharsets.UTF_8
                                    )
                            )
            ) {

                salida.write(
                        trama);

                salida.newLine();

                salida.flush();

                String respuesta =
                        entrada.readLine();

                if (respuesta == null
                        || respuesta.trim().isEmpty()) {

                    return RESPUESTA_FALLIDA;
                }

                return respuesta.trim();
            }
        }
    }

    /**
     * Validaciones básicas antes de enviar la trama.
     */
    private boolean datosValidos(
            String telefono,
            String identificadorTelefono,
            String identificadorTarjeta,
            String tipoServicio,
            String identificacionCliente,
            String estado) {

        if (valorVacio(telefono)
                || valorVacio(identificadorTelefono)
                || valorVacio(identificadorTarjeta)
                || valorVacio(tipoServicio)
                || valorVacio(identificacionCliente)
                || valorVacio(estado)) {

            return false;
        }

        if (!telefono.matches("\\d{8}")) {
            return false;
        }

        if (!identificadorTelefono.matches("\\d{16}")) {
            return false;
        }

        if (!identificadorTarjeta.matches("\\d{19}")) {
            return false;
        }

        String tipo =
                tipoServicio
                        .trim()
                        .toUpperCase(Locale.ROOT);

        return "PREPAGO".equals(tipo)
                || "POSTPAGO".equals(tipo);
    }

    /**
     * Convierte diferentes palabras al formato que espera Python.
     */
    private String normalizarEstado(String estado) {

        String valor =
                estado
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if ("ACTIVO".equals(valor)
                || "ACTIVAR".equals(valor)) {

            return "activo";
        }

        if ("INACTIVO".equals(valor)
                || "DESACTIVADO".equals(valor)
                || "DESACTIVAR".equals(valor)
                || "DISPONIBLE".equals(valor)) {

            return "inactivo";
        }

        return null;
    }

    private int obtenerPuerto() {

        try {

            return Integer.parseInt(
                    System.getenv().getOrDefault(
                            "IDENTIFICADOR_PORT",
                            String.valueOf(
                                    PUERTO_PREDETERMINADO)
                    )
            );

        } catch (NumberFormatException e) {

            return PUERTO_PREDETERMINADO;
        }
    }

    private boolean valorVacio(String valor) {

        return valor == null
                || valor.trim().isEmpty();
    }
}