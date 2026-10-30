package weatherreport_mvc;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import weatherreport_mvc.database.ConexionBD;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.utils.Bitacora;
import weatherreport_mvc.views.LoginView;

/**
 * Punto de entrada de la aplicación de escritorio Weather Report.
 * Arquitectura MVC con paquetes: models, views, controllers, database y utils.
 */
public class WeatherReport_MVC {

    /** Evita encadenar diálogos si un error se repite en cada repintado. */
    private static final long PAUSA_ENTRE_AVISOS_MS = 10_000;
    private static volatile long ultimoAviso;

    public static void main(String[] args) {
        instalarManejadorGlobal();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            Bitacora.error("WeatherReport_MVC.main: no se pudo aplicar el Look & Feel", e);
        }

        // La migración del esquema se hace fuera del hilo de la interfaz.
        ConexionBD.asegurarEsquema();
        SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
            avisarConfiguracionDanada();
        });
    }

    /**
     * Cualquier excepción no capturada (en el hilo de la interfaz o en otro)
     * queda en la bitácora y se avisa al usuario, en lugar de perderse: la
     * aplicación instalada no tiene consola.
     */
    private static void instalarManejadorGlobal() {
        Thread.setDefaultUncaughtExceptionHandler((hilo, e) -> {
            Bitacora.error("Excepción no controlada en el hilo " + hilo.getName(), e);
            long ahora = System.currentTimeMillis();
            if (ahora - ultimoAviso < PAUSA_ENTRE_AVISOS_MS) {
                return;
            }
            ultimoAviso = ahora;
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Ocurrió un error inesperado: " + Bitacora.mensaje(e)
                    + "\n\nLa operación pudo no completarse. El detalle quedó registrado en:\n"
                    + Bitacora.getArchivo(),
                    "Weather Report", JOptionPane.ERROR_MESSAGE));
        });
    }

    private static void avisarConfiguracionDanada() {
        String problema = ConfiguracionSistema.problemaDeCarga();
        if (problema != null) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo leer por completo la configuración:\n" + problema
                    + "\n\nSe usan los valores por defecto donde faltaron datos. "
                    + "Revise el módulo Información.",
                    "Configuración", JOptionPane.WARNING_MESSAGE);
        }
    }
}
