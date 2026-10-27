package weatherreport_mvc;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import weatherreport_mvc.database.ConexionBD;
import weatherreport_mvc.views.LoginView;

/**
 * Punto de entrada de la aplicación de escritorio Weather Report.
 * Arquitectura MVC con paquetes: models, views, controllers, database y utils.
 */
public class WeatherReport_MVC {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el Look & Feel: " + e.getMessage());
        }

        // La migración del esquema se hace fuera del hilo de la interfaz.
        ConexionBD.asegurarEsquema();
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}

