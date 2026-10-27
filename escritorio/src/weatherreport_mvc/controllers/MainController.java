package weatherreport_mvc.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.SwingWorker;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.models.Usuario;
import weatherreport_mvc.utils.DatosSeeder;
import weatherreport_mvc.views.DashboardPanel;
import weatherreport_mvc.views.GraficasPanel;
import weatherreport_mvc.views.HistoricoPanel;
import weatherreport_mvc.views.MainView;
import weatherreport_mvc.views.ReportesPanel;

/**
 * Controlador principal de la aplicación. Orquesta la navegación entre los
 * módulos (CardLayout), mantiene la sesión del usuario, controla el acceso
 * según el rol y coordina la generación de datos de prueba al iniciar la sesión.
 */
public class MainController {

    private final MainView view;
    private final Usuario usuario;

    public MainController(MainView view, Usuario usuario) {
        this.view = view;
        this.usuario = usuario;
    }

    public void navegar(String clave) {
        if (permiteModulo(clave)) {
            view.mostrarCard(clave);
        }
    }

    /** Indica si el rol del usuario puede acceder al módulo indicado. */
    public boolean permiteModulo(String clave) {
        if (usuario.esIngeniero()) {
            return true;
        }
        // Consulta o rol desconocido: acceso de solo lectura (nunca ventana en blanco).
        return DashboardPanel.KEY.equals(clave)
                || HistoricoPanel.KEY.equals(clave)
                || GraficasPanel.KEY.equals(clave)
                || ReportesPanel.KEY.equals(clave);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getHoraActual() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    /**
     * Genera datos de prueba en segundo plano si la BD está vacía. Solo se
     * ejecuta con {@code datosPrueba=true} en config.properties, para no
     * mezclar datos ficticios con los de la estación.
     */
    public void iniciarDatosPrueba() {
        if (!ConfiguracionSistema.cargar().isModoPrueba()) {
            return;
        }
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return DatosSeeder.asegurarDatosPrueba();
            }

            @Override
            protected void done() {
                view.refrescarModulos();
            }
        }.execute();
    }

    public void cerrarSesion() {
        view.abrirLogin();
    }
}
