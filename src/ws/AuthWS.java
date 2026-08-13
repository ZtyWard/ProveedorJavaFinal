package ws;

import util.Validador;
import ws.dto.RespuestaWS;

public class AuthWS {

    private static final String MENSAJE_EXITOSO =
            "Exitoso";

    private static final String MENSAJE_CREDENCIALES_INVALIDAS =
            "Usuario y/o contrasena incorrectos.";

    private static final String MENSAJE_DATOS_INVALIDOS =
            "Usuario ya existe o datos incorrectos o incompletos.";

    public RespuestaWS autenticar(
            String usuario,
            String contrasena,
            int tipoUsuario) {

        if (!credencialesBasicasValidas(
                usuario,
                contrasena,
                tipoUsuario)) {

            return new RespuestaWS(
                    false,
                    MENSAJE_CREDENCIALES_INVALIDAS);
        }

        return new RespuestaWS(
                true,
                MENSAJE_EXITOSO);
    }

    public RespuestaWS registrarUsuario(
            String identificacion,
            String nombre,
            String primerApellido,
            String segundoApellido,
            String correo,
            String usuario,
            String contrasena,
            int tipoUsuario) {

        if (!datosUsuarioValidos(
                identificacion,
                nombre,
                primerApellido,
                segundoApellido,
                correo,
                usuario,
                contrasena,
                tipoUsuario)) {

            return new RespuestaWS(
                    false,
                    MENSAJE_DATOS_INVALIDOS);
        }

        return new RespuestaWS(
                true,
                MENSAJE_EXITOSO);
    }

    public RespuestaWS cambiarEstadoUsuario(
            String identificacion,
            boolean activo) {

        if (!Validador.valorPresente(identificacion)) {
            return new RespuestaWS(
                    false,
                    "Usuario no existe o datos incorrectos.");
        }

        return new RespuestaWS(
                true,
                MENSAJE_EXITOSO);
    }

    private boolean credencialesBasicasValidas(
            String usuario,
            String contrasena,
            int tipoUsuario) {

        return Validador.valorPresente(usuario)
                && Validador.valorPresente(contrasena)
                && tipoUsuarioValido(tipoUsuario);
    }

    private boolean datosUsuarioValidos(
            String identificacion,
            String nombre,
            String primerApellido,
            String segundoApellido,
            String correo,
            String usuario,
            String contrasena,
            int tipoUsuario) {

        return Validador.valorPresente(identificacion)
                && Validador.valorPresente(nombre)
                && Validador.valorPresente(primerApellido)
                && Validador.valorPresente(segundoApellido)
                && Validador.correoValido(correo)
                && Validador.valorPresente(usuario)
                && Validador.contrasenaValida(contrasena)
                && tipoUsuarioValido(tipoUsuario);
    }

    private boolean tipoUsuarioValido(int tipoUsuario) {

        return tipoUsuario == 1
                || tipoUsuario == 2;
    }
}
