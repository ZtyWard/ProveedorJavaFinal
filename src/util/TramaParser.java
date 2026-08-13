package util;

public final class TramaParser {

    private TramaParser() {
    }

    public static boolean tramaBaseValida(String trama) {

        return trama != null
                && trama.length() >= 10;
    }

    public static String obtenerTipoTransaccion(String trama) {

        if (trama == null
                || trama.isEmpty()) {

            return "";
        }

        return trama.substring(0, 1);
    }

    public static String obtenerTelefono(String trama) {

        if (!tramaBaseValida(trama)) {
            return "";
        }

        return trama.substring(1, 9);
    }

    public static int obtenerTipoLlamada(String trama) {

        if (trama == null
                || trama.length() < 10) {

            return -1;
        }

        try {

            return Integer.parseInt(
                    trama.substring(9, 10));

        } catch (NumberFormatException e) {

            return -1;
        }
    }

    public static String obtenerDetalle(String trama) {

        if (trama == null
                || trama.length() <= 9) {

            return "";
        }

        return trama.substring(9);
    }

    public static boolean esConsultaSaldo(String trama) {

        return tramaBaseValida(trama)
                && Constantes.TIPO_TRANSACCION_SALDO.equals(
                obtenerTipoTransaccion(trama));
    }

    public static boolean esAutorizacionLlamada(String trama) {

        return tramaBaseValida(trama)
                && trama.length() == 10
                && Constantes.TIPO_TRANSACCION_LLAMADA.equals(
                obtenerTipoTransaccion(trama));
    }

    public static boolean esRegistroMovimiento(String trama) {

        return tramaBaseValida(trama)
                && trama.length() > 10
                && Constantes.TIPO_TRANSACCION_LLAMADA.equals(
                obtenerTipoTransaccion(trama));
    }
}
