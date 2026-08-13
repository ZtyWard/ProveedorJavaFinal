package service;

import client.IdentificadorClient;
import dao.ClienteDAO;
import dao.TarjetaDAO;
import dao.TelefonoDAO;
import modelo.Tarjeta;
import modelo.Telefono;
import util.Validador;


import java.sql.SQLException;
import java.time.LocalDateTime;

public class ActivacionService {

    private static final String RESPUESTA_OK =
            "OK";

    private static final String RESPUESTA_ERROR =
            "ERROR";

    private static final String RESPUESTA_ACTIVACION_FALLIDA =
        "Activación fallida";

    private static final String RESPUESTA_DATOS_INCOMPLETOS =
            "Datos Incompletos";

    private static final String RESPUESTA_TELEFONO_EN_USO =
            "Telefono en uso";

    private static final String RESPUESTA_TELEFONO_NO_CORRESPONDE =
            "Telefono no corresponde";

    private static final String TIPO_SERVICIO_PREPAGO =
            "PREPAGO";

    private static final String TIPO_SERVICIO_POSTPAGO =
            "POSTPAGO";

    private static final String ESTADO_DISPONIBLE =
            "DISPONIBLE";

    private static final String ESTADO_ACTIVO =
            "ACTIVO";

    private static final String ESTADO_INACTIVO =
            "INACTIVO";

    private static final String ESTADO_TARJETA_DISPONIBLE =
            "DISPONIBLE";

    private static final String ESTADO_TARJETA_ACTIVA =
            "ACTIVA";

    private static final String ESTADO_TARJETA_INACTIVA =
            "INACTIVA";

    private static final double SALDO_INICIAL_PREPAGO =
            1000.00;

    private final ClienteDAO clienteDAO;
    private final TelefonoDAO telefonoDAO;
    private final TarjetaDAO tarjetaDAO;
    private final IdentificadorClient identificadorClient;

   public ActivacionService() {

    this.clienteDAO =
            new ClienteDAO();

    this.telefonoDAO =
            new TelefonoDAO();

    this.tarjetaDAO =
            new TarjetaDAO();

    /*
     * Cliente que permite comunicar el Proveedor Java
     * con el Identificador Python por el puerto 5000.
     */
    this.identificadorClient =
            new IdentificadorClient();
}

    // ==========================================================
    // REGISTRAR LINEA DISPONIBLE
    // ==========================================================

    public String registrarLineaDisponible(
            String telefono,
            String identificadorTelefono,
            String identificadorTarjeta,
            String tipoServicio) {

        try {

            if (datosNuevaLineaIncompletos(
                    telefono,
                    identificadorTelefono,
                    identificadorTarjeta,
                    tipoServicio)) {

                return RESPUESTA_DATOS_INCOMPLETOS;
            }

            if (telefonoDAO.existeTelefono(telefono)) {
                return RESPUESTA_TELEFONO_EN_USO;
            }

            if (tarjetaDAO.existeTarjeta(identificadorTarjeta)) {
                return RESPUESTA_ERROR;
            }

            Telefono nuevaLinea =
                    construirLineaDisponible(
                            telefono,
                            identificadorTelefono,
                            tipoServicio);

            telefonoDAO.guardarTelefono(
                    nuevaLinea);

            Tarjeta tarjeta =
                    construirTarjetaDisponible(
                            telefono,
                            identificadorTarjeta);

            tarjetaDAO.guardarTarjeta(
                    tarjeta);

            return RESPUESTA_OK;

        } catch (SQLException e) {

            return RESPUESTA_ERROR;
        }
    }

    // ==========================================================
// ACTIVAR LINEA
// ==========================================================

public String activarLinea(
        String telefono,
        String identificadorTelefono,
        String identificadorTarjeta,
        String tipoServicio,
        String identificacionCliente) {

    try {

        // ------------------------------------------------------
        // Validar datos obligatorios.
        // ------------------------------------------------------
        if (datosActivacionIncompletos(
                telefono,
                identificadorTelefono,
                identificadorTarjeta,
                tipoServicio,
                identificacionCliente)) {

            return RESPUESTA_DATOS_INCOMPLETOS;
        }

        // ------------------------------------------------------
        // Buscar la línea y la tarjeta en SQL Server.
        // ------------------------------------------------------
        Telefono linea =
                telefonoDAO.buscarPorTelefono(
                        telefono);

        Tarjeta tarjeta =
                tarjetaDAO.buscarPorIdentificador(
                        identificadorTarjeta);

        // ------------------------------------------------------
        // La línea y la tarjeta deben estar disponibles,
        // y sus identificadores deben coincidir.
        // ------------------------------------------------------
        if (!lineaDisponible(linea)
                || !tarjetaDisponible(tarjeta)
                || !datosLineaCoinciden(
                        linea,
                        tarjeta,
                        identificadorTelefono,
                        tipoServicio)) {

            return RESPUESTA_TELEFONO_EN_USO;
        }

        // ------------------------------------------------------
        // Crear el cliente en SQL Server si aún no existe.
        // ------------------------------------------------------
        asegurarCliente(
                identificacionCliente);

        // ------------------------------------------------------
        // Activar primero en la base del Proveedor.
        // ------------------------------------------------------
        linea.setEstado(
                ESTADO_ACTIVO);

        linea.setIdentificacionCliente(
                identificacionCliente);

        linea.setFechaActivacion(
                LocalDateTime.now());

        linea.setSaldo(
                saldoInicial(tipoServicio));

        telefonoDAO.actualizarTelefono(
                linea);

        tarjetaDAO.actualizarEstado(
                identificadorTarjeta,
                ESTADO_TARJETA_ACTIVA);

        // ------------------------------------------------------
        // Notificar al Identificador Python.
        //
        // El cliente cifra:
        // - teléfono
        // - identificador del teléfono
        // - identificador de tarjeta
        //
        // Y envía una trama IDENTIFICADOR6.
        // ------------------------------------------------------
        String respuestaIdentificador =
                identificadorClient.notificarCambioLinea(
                        telefono,
                        identificadorTelefono,
                        identificadorTarjeta,
                        tipoServicio,
                        identificacionCliente,
                        "activo"
                );

        // ------------------------------------------------------
        // Si el Identificador falla, se revierte la activación
        // en SQL Server para no dejar las bases diferentes.
        // ------------------------------------------------------
        if (!RESPUESTA_OK.equalsIgnoreCase(
                respuestaIdentificador)) {

            linea.setEstado(
                    ESTADO_DISPONIBLE);

            linea.setIdentificacionCliente(
                    null);

            linea.setFechaActivacion(
                    null);

            linea.setSaldo(
                    0);

            telefonoDAO.actualizarTelefono(
                    linea);

            tarjetaDAO.actualizarEstado(
                    identificadorTarjeta,
                    ESTADO_TARJETA_DISPONIBLE);

            return RESPUESTA_ACTIVACION_FALLIDA;
        }

        return RESPUESTA_OK;

    } catch (SQLException e) {

        System.out.println(
                "Error SQL al activar la línea:");

        e.printStackTrace();

        return RESPUESTA_ERROR;

    } catch (Exception e) {

        System.out.println(
                "Error inesperado al activar la línea:");

        e.printStackTrace();

        return RESPUESTA_ERROR;
    }
}

    // ==========================================================
// DESACTIVAR LINEA
// ==========================================================

public String desactivarLinea(
        String telefono,
        String identificadorTelefono,
        String identificadorTarjeta,
        String identificacionCliente) {

    try {

        // ------------------------------------------------------
        // Validar datos obligatorios.
        // ------------------------------------------------------
        if (valorVacio(telefono)
                || valorVacio(identificadorTelefono)
                || valorVacio(identificadorTarjeta)
                || valorVacio(identificacionCliente)
                || !Validador.telefonoValido(telefono)
                || !Validador.identificadorTelefonoValido(identificadorTelefono)
                || !Validador.identificadorTarjetaValido(identificadorTarjeta)) {

            return RESPUESTA_DATOS_INCOMPLETOS;
        }

        // ------------------------------------------------------
        // Buscar línea y tarjeta en SQL Server.
        // ------------------------------------------------------
        Telefono linea =
                telefonoDAO.buscarPorTelefono(
                        telefono);

        Tarjeta tarjeta =
                tarjetaDAO.buscarPorIdentificador(
                        identificadorTarjeta);

        // ------------------------------------------------------
        // Verificar que la línea esté activa y pertenezca
        // al cliente indicado.
        // ------------------------------------------------------
        if (!lineaActivaDeCliente(
                linea,
                identificadorTelefono,
                identificacionCliente)
                || !tarjetaDeLinea(
                        tarjeta,
                        telefono)) {

            return RESPUESTA_TELEFONO_NO_CORRESPONDE;
        }

        /*
         * Guardamos los valores anteriores.
         * Se utilizarán para revertir el cambio si Python falla.
         */
        String tipoServicioAnterior =
                linea.getTipoServicio();

        String clienteAnterior =
                linea.getIdentificacionCliente();

        double saldoAnterior =
                linea.getSaldo();

        LocalDateTime fechaActivacionAnterior =
                linea.getFechaActivacion();

        // ------------------------------------------------------
        // Desactivar en la base de datos del Proveedor.
        // ------------------------------------------------------
        linea.setEstado(
                ESTADO_INACTIVO);

        linea.setIdentificacionCliente(
                null);

        linea.setSaldo(
                0);

        linea.setFechaActivacion(
                null);

        telefonoDAO.actualizarTelefono(
                linea);

        tarjetaDAO.actualizarEstado(
                identificadorTarjeta,
                ESTADO_TARJETA_INACTIVA);

        // ------------------------------------------------------
        // Notificar la desactivación al Identificador Python.
        // ------------------------------------------------------
        String respuestaIdentificador =
                identificadorClient.notificarCambioLinea(
                        telefono,
                        identificadorTelefono,
                        identificadorTarjeta,
                        tipoServicioAnterior,
                        identificacionCliente,
                        "inactivo"
                );

        // ------------------------------------------------------
        // Si Python falla, restaurar los valores anteriores
        // en la base del Proveedor.
        // ------------------------------------------------------
        if (!RESPUESTA_OK.equalsIgnoreCase(
                respuestaIdentificador)) {

            linea.setEstado(
                    ESTADO_ACTIVO);

            linea.setIdentificacionCliente(
                    clienteAnterior);

            linea.setSaldo(
                    saldoAnterior);

            linea.setFechaActivacion(
                    fechaActivacionAnterior);

            telefonoDAO.actualizarTelefono(
                    linea);

            tarjetaDAO.actualizarEstado(
                    identificadorTarjeta,
                    ESTADO_TARJETA_ACTIVA);

            return RESPUESTA_ACTIVACION_FALLIDA;
        }

        return RESPUESTA_OK;

    } catch (SQLException e) {

        System.out.println(
                "Error SQL al desactivar la línea:");

        e.printStackTrace();

        return RESPUESTA_ERROR;

    } catch (Exception e) {

        System.out.println(
                "Error inesperado al desactivar la línea:");

        e.printStackTrace();

        return RESPUESTA_ERROR;
    }
}

    // ==========================================================
    // REGLAS DE NEGOCIO
    // ==========================================================

    private void asegurarCliente(String identificacionCliente)
            throws SQLException {

        if (!clienteDAO.existeCliente(identificacionCliente)) {
            clienteDAO.guardarCliente(
                    identificacionCliente,
                    true);
        }
    }

    private boolean lineaDisponible(Telefono linea) {

        return linea != null
                && ESTADO_DISPONIBLE.equals(linea.getEstado());
    }

    private boolean tarjetaDisponible(Tarjeta tarjeta) {

        return tarjeta != null
                && ESTADO_TARJETA_DISPONIBLE.equals(tarjeta.getEstado());
    }

    private boolean datosLineaCoinciden(
            Telefono linea,
            Tarjeta tarjeta,
            String identificadorTelefono,
            String tipoServicio) {

        return identificadorTelefono.equals(
                linea.getIdentificadorTelefono())
                && tipoServicio.equals(
                linea.getTipoServicio())
                && linea.getTelefono().equals(
                tarjeta.getTelefono());
    }

    private boolean lineaActivaDeCliente(
            Telefono linea,
            String identificadorTelefono,
            String identificacionCliente) {

        return linea != null
                && ESTADO_ACTIVO.equals(linea.getEstado())
                && identificadorTelefono.equals(linea.getIdentificadorTelefono())
                && identificacionCliente.equals(linea.getIdentificacionCliente());
    }

    private boolean tarjetaDeLinea(
            Tarjeta tarjeta,
            String telefono) {

        return tarjeta != null
                && telefono.equals(tarjeta.getTelefono());
    }

    private double saldoInicial(String tipoServicio) {

        if (TIPO_SERVICIO_PREPAGO.equals(tipoServicio)) {
            return SALDO_INICIAL_PREPAGO;
        }

        return 0;
    }

    // ==========================================================
    // CONSTRUCCION DE MODELOS
    // ==========================================================

    private Telefono construirLineaDisponible(
            String telefono,
            String identificadorTelefono,
            String tipoServicio) {

        Telefono linea =
                new Telefono();

        linea.setTelefono(telefono);
        linea.setIdentificadorTelefono(identificadorTelefono);
        linea.setTipoServicio(tipoServicio);
        linea.setEstado(ESTADO_DISPONIBLE);
        linea.setIdentificacionCliente(null);
        linea.setSaldo(0);
        linea.setFechaActivacion(null);

        return linea;
    }

    private Tarjeta construirTarjetaDisponible(
            String telefono,
            String identificadorTarjeta) {

        Tarjeta tarjeta =
                new Tarjeta();

        tarjeta.setTelefono(telefono);
        tarjeta.setIdentificadorTarjeta(identificadorTarjeta);
        tarjeta.setEstado(ESTADO_TARJETA_DISPONIBLE);

        return tarjeta;
    }

    // ==========================================================
    // VALIDACIONES
    // ==========================================================

    private boolean datosNuevaLineaIncompletos(
            String telefono,
            String identificadorTelefono,
            String identificadorTarjeta,
            String tipoServicio) {

        return valorVacio(telefono)
                || valorVacio(identificadorTelefono)
                || valorVacio(identificadorTarjeta)
                || !Validador.telefonoValido(telefono)
                || !Validador.identificadorTelefonoValido(identificadorTelefono)
                || !Validador.identificadorTarjetaValido(identificadorTarjeta)
                || !tipoServicioValido(tipoServicio);
    }

    private boolean datosActivacionIncompletos(
            String telefono,
            String identificadorTelefono,
            String identificadorTarjeta,
            String tipoServicio,
            String identificacionCliente) {

        return datosNuevaLineaIncompletos(
                telefono,
                identificadorTelefono,
                identificadorTarjeta,
                tipoServicio)
                || valorVacio(identificacionCliente);
    }

    private boolean tipoServicioValido(String tipoServicio) {

        return TIPO_SERVICIO_PREPAGO.equals(tipoServicio)
                || TIPO_SERVICIO_POSTPAGO.equals(tipoServicio);
    }

    private boolean valorVacio(String valor) {

        return valor == null
                || valor.trim().isEmpty();
    }
}
