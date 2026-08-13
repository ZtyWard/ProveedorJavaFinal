package util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Descifra los campos sensibles enviados por WS_PROVEEDOR.
 * El formato es Base64(IV de 16 bytes + texto AES-CBC cifrado).
 */
public final class CifradoWSProveedor {

    private static final String CLAVE_PREDETERMINADA =
            "12345678901234567890123456789012";

    private static final int TAMANO_IV = 16;

    private CifradoWSProveedor() {
    }

    public static String descifrar(String valorCifrado) {

        if (valorCifrado == null || valorCifrado.trim().isEmpty()) {
            return "";
        }

        try {
            byte[] datos = Base64.getDecoder().decode(valorCifrado.trim());

            if (datos.length <= TAMANO_IV) {
                return "";
            }

            byte[] iv = new byte[TAMANO_IV];
            byte[] contenido = new byte[datos.length - TAMANO_IV];

            ByteBuffer.wrap(datos).get(iv).get(contenido);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(obtenerLlave(), "AES"),
                    new IvParameterSpec(iv));

            return new String(cipher.doFinal(contenido), StandardCharsets.UTF_8);

        } catch (Exception e) {
            return "";
        }
    }

    private static byte[] obtenerLlave() {

        String valor = System.getenv().getOrDefault(
                "WS_PROVEEDOR_AES_KEY",
                CLAVE_PREDETERMINADA);

        byte[] llave = valor.getBytes(StandardCharsets.UTF_8);

        if (llave.length != 16 && llave.length != 24 && llave.length != 32) {
            throw new IllegalStateException(
                    "WS_PROVEEDOR_AES_KEY debe tener 16, 24 o 32 bytes.");
        }

        return llave;
    }
}
