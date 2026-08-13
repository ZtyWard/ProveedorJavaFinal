package util;

import java.time.LocalDate;

public final class Validador {

    private Validador() {
    }

    public static boolean valorPresente(String valor) {

        return valor != null
                && !valor.trim().isEmpty();
    }

    public static boolean soloNumeros(String valor) {

        return valorPresente(valor)
                && valor.matches("\\d+");
    }

    public static boolean longitudExacta(
            String valor,
            int longitud) {

        return valorPresente(valor)
                && valor.length() == longitud;
    }

    public static boolean telefonoValido(String telefono) {

        return soloNumeros(telefono)
                && longitudExacta(
                telefono,
                Constantes.LONGITUD_TELEFONO);
    }

    public static boolean identificadorTelefonoValido(
            String identificadorTelefono) {

        return soloNumeros(identificadorTelefono)
                && longitudExacta(
                identificadorTelefono,
                Constantes.LONGITUD_IDENTIFICADOR_TELEFONO);
    }

    public static boolean identificadorTarjetaValido(
            String identificadorTarjeta) {

        return soloNumeros(identificadorTarjeta)
                && longitudExacta(
                identificadorTarjeta,
                Constantes.LONGITUD_IDENTIFICADOR_TARJETA);
    }

    public static boolean tipoServicioValido(String tipoServicio) {

        return Constantes.TIPO_SERVICIO_PREPAGO.equals(tipoServicio)
                || Constantes.TIPO_SERVICIO_POSTPAGO.equals(tipoServicio);
    }

    public static boolean estadoTelefonoValido(String estado) {

        return Constantes.ESTADO_DISPONIBLE.equals(estado)
                || Constantes.ESTADO_ACTIVO.equals(estado)
                || Constantes.ESTADO_INACTIVO.equals(estado);
    }

    public static boolean estadoTarjetaValido(String estado) {

        return Constantes.ESTADO_TARJETA_DISPONIBLE.equals(estado)
                || Constantes.ESTADO_TARJETA_ACTIVA.equals(estado)
                || Constantes.ESTADO_TARJETA_INACTIVA.equals(estado);
    }

    public static boolean fechasFacturacionValidas(
            LocalDate fechaCalculo,
            LocalDate fechaMaximaPago) {

        return fechaCalculo != null
                && fechaMaximaPago != null
                && !fechaMaximaPago.isBefore(fechaCalculo);
    }

    public static boolean correoValido(String correo) {

        return valorPresente(correo)
                && correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    public static boolean contrasenaValida(String contrasena) {

        return valorPresente(contrasena)
                && contrasena.length() == 14
                && contrasena.matches(".*[A-Z].*")
                && contrasena.matches(".*[a-z].*")
                && contrasena.matches(".*\\d.*")
                && contrasena.matches(".*[^A-Za-z0-9].*");
    }
}
