package weatherreport_mvc.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import weatherreport_mvc.database.ConexionBD;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.utils.Bitacora;
import weatherreport_mvc.views.ConfiguracionPanel;

/**
 * Controlador del módulo de Configuración. Carga y persiste los ajustes
 * de la estación meteorológica y las rutas de salida.
 */
public class ConfiguracionController {

    private final ConfiguracionPanel view;

    public ConfiguracionController(ConfiguracionPanel view) {
        this.view = view;
    }

    public void cargar() {
        view.mostrarConfiguracion(ConfiguracionSistema.cargar());
    }

    /** @return null si fue exitoso, o un mensaje de error. */
    public String aplicar(ConfiguracionSistema cfg) {
        if (cfg.getNombreEstacion() == null || cfg.getNombreEstacion().isBlank()) {
            return "El nombre de la estación es obligatorio.";
        }
        // Se valida antes de guardar: una ruta inválida antes se aceptaba en silencio
        // y luego el selector de archivos abría en otra carpeta sin avisar
        String error = validarCarpeta("La carpeta de reportes", cfg.getRutaReportes());
        if (error == null) {
            error = validarCarpeta("La carpeta de respaldos", cfg.getRutaRespaldos());
        }
        if (error != null) {
            return error;
        }
        try {
            cfg.guardar();
            ConexionBD.aplicarConfiguracion(cfg);
            return null;
        } catch (IOException e) {
            Bitacora.error("ConfiguracionController.aplicar", e);
            return "No se pudieron guardar los cambios en " + ConfiguracionSistema.ARCHIVO_CONFIG
                    + ":\n" + Bitacora.mensaje(e);
        }
    }

    /**
     * Comprueba que la ruta sea absoluta, que la carpeta exista (o se pueda
     * crear) y que se pueda escribir en ella. Devuelve el problema o null.
     */
    private static String validarCarpeta(String nombre, String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return nombre + " es obligatoria.";
        }
        Path carpeta;
        try {
            carpeta = Paths.get(ruta.trim());
        } catch (InvalidPathException e) {
            return nombre + " no es una ruta válida: " + ruta;
        }
        if (!carpeta.isAbsolute()) {
            return nombre + " debe ser una ruta completa (p. ej. C:\\Users\\...\\Reportes).";
        }
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            return nombre + " no existe y no se pudo crear:\n" + ruta + "\n" + Bitacora.mensaje(e);
        }
        if (!Files.isWritable(carpeta)) {
            return nombre + " no permite guardar archivos (sin permiso de escritura):\n" + ruta;
        }
        return null;
    }
}
