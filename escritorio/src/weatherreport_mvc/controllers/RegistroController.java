package weatherreport_mvc.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import weatherreport_mvc.database.ConexionBD;
import weatherreport_mvc.database.RegistroDAO;
import weatherreport_mvc.models.RegistroMeteorologico;
import weatherreport_mvc.utils.Bitacora;
import weatherreport_mvc.utils.Estadistica;
import weatherreport_mvc.utils.ImportadorArchivos;
import weatherreport_mvc.views.IngresoManualPanel;

/**
 * Controlador del módulo de Ingreso Manual. Valida los datos del formulario y
 * los persiste en la base de datos.
 */
public class RegistroController {

    private final IngresoManualPanel view;

    private RegistroMeteorologico registroEnEdicion;

    public RegistroController(IngresoManualPanel view) {
        this.view = view;
    }

    public void cargarPorId() {
        Integer id = view.getIdEdicion();
        if (id == null) {
            view.mostrarError("Ingrese un ID de registro válido.");
            return;
        }
        RegistroMeteorologico registro;
        try {
            registro = RegistroDAO.obtenerPorId(id);
        } catch (SQLException e) {
            // Antes un fallo de conexión se leía como "No existe un registro con el ID"
            Bitacora.error("RegistroController.cargarPorId", e);
            view.mostrarError(ConexionBD.mensajeError(e));
            return;
        }
        if (registro == null) {
            view.mostrarError("No existe un registro con el ID " + id + ".");
            return;
        }
        this.registroEnEdicion = registro;
        view.cargarRegistro(registro);
    }

    public boolean isEnEdicion() {
        return registroEnEdicion != null;
    }

    public void salirDeEdicion() {
        this.registroEnEdicion = null;
    }

    public void guardar() {
        if (registroEnEdicion != null) {
            actualizar();
            return;
        }
        LocalDate fecha = view.getFecha();
        LocalTime hora = view.getHora();
        Double humedad = view.getHumedad();
        Double viento = view.getViento();
        Double lluvia = view.getLluvia();
        String observaciones = view.getObservaciones().trim();

        if (fecha == null || hora == null) {
            view.mostrarError("La fecha y la hora deben tener el formato dd/MM/yyyy HH:mm.");
            return;
        }
        if (humedad == null || humedad < 0 || humedad > 100) {
            view.mostrarError("La humedad debe estar entre 0 y 100 %.");
            return;
        }
        if (viento == null || viento < 0) {
            view.mostrarError("La velocidad del viento debe ser un número mayor o igual a 0.");
            return;
        }
        if (lluvia == null || lluvia < 0) {
            view.mostrarError("La lluvia debe ser un número mayor o igual a 0.");
            return;
        }

        RegistroMeteorologico registro = new RegistroMeteorologico();
        rellenarRegistro(registro);
        registro.setObservaciones(observaciones);

        try {
            if (RegistroDAO.existeFechaHora(fecha, hora, 0)) {
                view.mostrarError("Ya existe un registro con esa fecha y hora.");
                return;
            }
            RegistroDAO.insertar(registro);
            view.mostrarExito("Registro guardado correctamente.");
            view.limpiarFormulario();
        } catch (SQLException e) {
            Bitacora.error("RegistroController.guardar", e);
            view.mostrarError(ConexionBD.mensajeError(e));
        }
    }

    /** Actualiza en la BD el registro del ID cargado o el escrito en el campo ID. */
    public void actualizar() {
        Integer id = registroEnEdicion != null
                ? registroEnEdicion.getId()
                : view.getIdEdicion();
        if (id == null) {
            view.mostrarError("Ingrese un ID de registro válido.");
            return;
        }

        LocalDate fecha = view.getFecha();
        LocalTime hora = view.getHora();
        Double humedad = view.getHumedad();
        Double viento = view.getViento();
        Double lluvia = view.getLluvia();
        String observaciones = view.getObservaciones().trim();

        if (fecha == null || hora == null) {
            view.mostrarError("La fecha y la hora deben tener el formato dd/MM/yyyy HH:mm.");
            return;
        }
        if (humedad == null || humedad < 0 || humedad > 100) {
            view.mostrarError("La humedad debe estar entre 0 y 100 %.");
            return;
        }
        if (viento == null || viento < 0) {
            view.mostrarError("La velocidad del viento debe ser un número mayor o igual a 0.");
            return;
        }
        if (lluvia == null || lluvia < 0) {
            view.mostrarError("La lluvia debe ser un número mayor o igual a 0.");
            return;
        }

        RegistroMeteorologico registro = new RegistroMeteorologico();
        registro.setId(id);
        rellenarRegistro(registro);
        registro.setObservaciones(observaciones);

        try {
            if (RegistroDAO.existeFechaHora(fecha, hora, id)) {
                view.mostrarError("Ya existe otro registro con esa fecha y hora.");
                return;
            }
            RegistroDAO.actualizar(registro);
            salirDeEdicion();
            view.mostrarExito("Registro " + id + " actualizado correctamente.");
            view.limpiarFormulario();
        } catch (SQLException e) {
            Bitacora.error("RegistroController.actualizar", e);
            view.mostrarError(ConexionBD.mensajeError(e));
        }
    }

    /** Copia al registro todos los valores leídos del formulario. */
    private void rellenarRegistro(RegistroMeteorologico r) {
        r.setFecha(view.getFecha());
        r.setHora(view.getHora());
        r.setTempExterior(obtener(view.getTempExterior()));
        r.setHiTemp(obtener(view.getHiTemp()));
        r.setLowTemp(obtener(view.getLowTemp()));
        r.setPuntoRocio(obtener(view.getPuntoRocio()));
        r.setSensacionTermica(obtener(view.getSensacionTermica()));
        r.setIndiceCalor(obtener(view.getIndiceCalor()));
        r.setIndiceThw(obtener(view.getIndiceThw()));
        r.setHumedad(obtener(view.getHumedad()));
        r.setVelocidadViento(obtener(view.getViento()));
        r.setDireccionViento(view.getDireccionViento());
        r.setRecorridoViento(obtener(view.getRecorridoViento()));
        r.setRafagaMaxima(obtener(view.getRafagaMaxima()));
        r.setDireccionRafaga(view.getDireccionRafaga());
        r.setPresionBarometrica(obtener(view.getPresionBarometrica()));
        r.setLluvia(obtener(view.getLluvia()));
        r.setTasaLluvia(obtener(view.getTasaLluvia()));
        r.setGradosDiaCalefaccion(obtener(view.getGradosDiaCalefaccion()));
        r.setGradosDiaRefrigeracion(obtener(view.getGradosDiaRefrigeracion()));
        r.setTempInterior(obtener(view.getTempInterior()));
        r.setHumedadInterior(obtener(view.getHumedadInterior()));
        r.setPuntoRocioInterior(obtener(view.getPuntoRocioInterior()));
        r.setIndiceCalorInterior(obtener(view.getIndiceCalorInterior()));
        r.setEmcInterior(obtener(view.getEmcInterior()));
        r.setDensidadAireInterior(obtener(view.getDensidadAireInterior()));
        r.setMuestrasViento(view.getMuestrasViento() == null ? 0 : view.getMuestrasViento());
        r.setEstadoTransmision(view.getEstadoTransmision() == null ? 0 : view.getEstadoTransmision());
        r.setIntervaloArchivo(view.getIntervaloArchivo() == null ? 0 : view.getIntervaloArchivo());
        r.setCalidadRecepcion(obtener(view.getCalidadRecepcion()));
    }

    private static double obtener(Double valor) {
        return valor == null ? Estadistica.SIN_DATO : valor;
    }

    /** Lee un archivo TXT o Excel y guarda los registros importados en la base de datos. */
    public void importarDesdeArchivo() {
        File archivo = view.elegirArchivoImportacion();
        if (archivo == null) {
            return;
        }
        try {
            ImportadorArchivos.ResultadoImportacion resultado =
                    ImportadorArchivos.importar(archivo);
            List<RegistroMeteorologico> registros = resultado.registros();
            if (registros.isEmpty()) {
                view.mostrarError("El archivo no contiene datos válidos.\n"
                        + primerError(resultado));
                return;
            }
            int insertados = RegistroDAO.insertarMasivo(registros);
            String mensaje = "Se importaron " + insertados + " registros desde "
                    + archivo.getName() + ".";
            int duplicados = registros.size() - insertados;
            if (duplicados > 0) {
                mensaje += "\nSe omitieron " + duplicados
                        + " registros cuya fecha y hora ya existían.";
            }
            if (!resultado.errores().isEmpty()) {
                mensaje += "\nSe omitieron " + resultado.errores().size() + " filas:\n"
                        + primerError(resultado);
            }
            view.mostrarExito(mensaje);
            view.limpiarFormulario();
        } catch (IOException e) {
            Bitacora.error("RegistroController.importarDesdeArchivo: " + archivo, e);
            view.mostrarError("No se pudo leer " + archivo.getName() + ":\n" + Bitacora.mensaje(e));
        } catch (SQLException e) {
            Bitacora.error("RegistroController.importarDesdeArchivo", e);
            view.mostrarError(ConexionBD.mensajeError(e));
        }
    }

    private static String primerError(ImportadorArchivos.ResultadoImportacion resultado) {
        List<String> errores = resultado.errores();
        if (errores.isEmpty()) {
            return "";
        }
        return errores.stream().limit(3).reduce((a, b) -> a + "\n" + b).orElse("");
    }
}
