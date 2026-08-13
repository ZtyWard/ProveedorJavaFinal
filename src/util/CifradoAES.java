package util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public final class CifradoAES {

    private static final String ALGORITMO_AES =
            "AES";

    private static final String TRANSFORMACION_AES =
            "AES/ECB/PKCS5Padding";

    private static final String CLAVE_AMBIENTE =
            "PROVEEDOR_AES_KEY";

    private static final String CLAVE_DEFAULT =
            "ProveedorJava2026";

    private CifradoAES() {
    }

    public static String cifrar(String texto) {

        if (texto == null) {
            return "";
        }

        try {

            Cipher cipher =
                    Cipher.getInstance(TRANSFORMACION_AES);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    construirLlave());

            byte[] cifrado =
                    cipher.doFinal(
                            texto.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder()
                    .encodeToString(cifrado);

        } catch (Exception e) {

            return "";
        }
    }

    public static String descifrar(String textoCifrado) {

        if (textoCifrado == null
                || textoCifrado.trim().isEmpty()) {

            return "";
        }

        try {

            Cipher cipher =
                    Cipher.getInstance(TRANSFORMACION_AES);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    construirLlave());

            byte[] bytes =
                    Base64.getDecoder()
                            .decode(textoCifrado);

            byte[] descifrado =
                    cipher.doFinal(bytes);

            return new String(
                    descifrado,
                    StandardCharsets.UTF_8);

        } catch (Exception e) {

            return "";
        }
    }

    private static SecretKeySpec construirLlave()
            throws Exception {

        String clave =
                System.getenv()
                        .getOrDefault(
                                CLAVE_AMBIENTE,
                                CLAVE_DEFAULT);

        MessageDigest sha =
                MessageDigest.getInstance("SHA-256");

        byte[] llave =
                sha.digest(
                        clave.getBytes(StandardCharsets.UTF_8));

        llave =
                Arrays.copyOf(
                        llave,
                        16);

        return new SecretKeySpec(
                llave,
                ALGORITMO_AES);
    }
}
