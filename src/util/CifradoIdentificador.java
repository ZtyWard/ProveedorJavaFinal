package util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifrado utilizado exclusivamente para comunicarse
 * con el Identificador Python.
 *
 * Es compatible con CryptoBox.encrypt() del main.py:
 *
 * Base64(
 *     nonce de 12 bytes
 *     + texto cifrado
 *     + etiqueta de autenticación GCM
 * )
 */
public final class CifradoIdentificador {

    /*
     * Misma llave AES Base64 configurada en el Identificador Python.
     */
    private static final String LLAVE_PREDETERMINADA =
            "XP2jwjvXFogsw3DHWywVFIU2ZS1J6IYEDIzvoyve/dg=";

    private static final int TAMANO_NONCE = 12;
    private static final int TAMANO_TAG_BITS = 128;

    private CifradoIdentificador() {
        // Evita que se creen objetos de esta clase.
    }

    /**
     * Cifra un texto usando AES-GCM.
     *
     * @param textoPlano dato que se desea cifrar
     * @return resultado cifrado y convertido a Base64
     */
    public static String cifrar(String textoPlano) {

        if (textoPlano == null || textoPlano.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El texto que se desea cifrar está vacío."
            );
        }

        try {
            /*
             * Primero intenta leer la llave desde una variable de ambiente.
             * Si no existe, usa la misma llave predeterminada del main.py.
             */
            String llaveBase64 =
                    System.getenv().getOrDefault(
                            "AES_KEY_BASE64",
                            LLAVE_PREDETERMINADA
                    );

            byte[] llave =
                    Base64.getDecoder().decode(llaveBase64);

            if (llave.length != 16
                    && llave.length != 24
                    && llave.length != 32) {

                throw new IllegalStateException(
                        "La llave AES debe contener 16, 24 o 32 bytes."
                );
            }

            /*
             * Python AESGCM utiliza un nonce de 12 bytes.
             */
            byte[] nonce = new byte[TAMANO_NONCE];
            new SecureRandom().nextBytes(nonce);

            Cipher cipher =
                    Cipher.getInstance("AES/GCM/NoPadding");

            SecretKeySpec claveAES =
                    new SecretKeySpec(llave, "AES");

            GCMParameterSpec parametrosGCM =
                    new GCMParameterSpec(
                            TAMANO_TAG_BITS,
                            nonce
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    claveAES,
                    parametrosGCM
            );

            /*
             * En Java, doFinal devuelve:
             * texto cifrado + tag de autenticación.
             */
            byte[] datosCifrados =
                    cipher.doFinal(
                            textoPlano.getBytes(StandardCharsets.UTF_8)
                    );

            /*
             * El main.py espera:
             * nonce + datos cifrados + tag.
             */
            byte[] resultado =
                    ByteBuffer
                            .allocate(
                                    nonce.length + datosCifrados.length
                            )
                            .put(nonce)
                            .put(datosCifrados)
                            .array();

            return Base64.getEncoder()
                    .encodeToString(resultado);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "No fue posible cifrar el dato para el Identificador.",
                    e
            );
        }
    }
}