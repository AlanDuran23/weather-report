package weatherreport_mvc.controllers;

import weatherreport_mvc.database.ConexionBD;
import weatherreport_mvc.models.ConfiguracionSistema;
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
        try {
            cfg.guardar();
            ConexionBD.aplicarConfiguracion(cfg);
            return null;
        } catch (Exception e) {
            return "No se pudieron guardar los cambios: " + e.getMessage();
        }
    }
}
