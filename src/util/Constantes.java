package util;

public final class Constantes {

    public static final String RESPUESTA_OK =
            "OK";

    public static final String RESPUESTA_ERROR =
            "ERROR";

    public static final String RESPUESTA_TRAMA_INVALIDA =
            "TRAMA_INVALIDA";

    public static final String RESPUESTA_DATOS_INCOMPLETOS =
            "Datos Incompletos";

    public static final String RESPUESTA_TELEFONO_EN_USO =
            "Telefono en uso";

    public static final String RESPUESTA_TELEFONO_NO_CORRESPONDE =
            "Telefono no corresponde";

    public static final String RESPUESTA_INSUFICIENTE =
            "INSUF";

    public static final String TIPO_SERVICIO_PREPAGO =
            "PREPAGO";

    public static final String TIPO_SERVICIO_POSTPAGO =
            "POSTPAGO";

    public static final String ESTADO_DISPONIBLE =
            "DISPONIBLE";

    public static final String ESTADO_ACTIVO =
            "ACTIVO";

    public static final String ESTADO_INACTIVO =
            "INACTIVO";

    public static final String ESTADO_TARJETA_DISPONIBLE =
            "DISPONIBLE";

    public static final String ESTADO_TARJETA_ACTIVA =
            "ACTIVA";

    public static final String ESTADO_TARJETA_INACTIVA =
            "INACTIVA";

    public static final String TARIFA_MISMO_PROVEEDOR =
            "MISMO_PROVEEDOR";

    public static final String TARIFA_OTRO_PROVEEDOR =
            "OTRO_PROVEEDOR";

    public static final String TARIFA_INTERNACIONAL =
            "INTERNACIONAL";

    public static final String TIPO_TRANSACCION_LLAMADA =
            "1";

    public static final String TIPO_TRANSACCION_SALDO =
            "2";

    public static final int LONGITUD_TELEFONO =
            8;

    public static final int LONGITUD_IDENTIFICADOR_TELEFONO =
            16;

    public static final int LONGITUD_IDENTIFICADOR_TARJETA =
            19;

    public static final int LONGITUD_DURACION =
            6;

    public static final int PUERTO_PROVEEDOR_DEFAULT =
            6000;

    public static final String RUTA_LOG_DEFAULT =
            "logs/proveedor.log";

    private Constantes() {
    }
}
