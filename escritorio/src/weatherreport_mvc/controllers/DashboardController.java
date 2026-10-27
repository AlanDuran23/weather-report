package weatherreport_mvc.controllers;

import java.time.LocalDateTime;
import java.util.List;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import weatherreport_mvc.database.RegistroDAO;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.models.RegistroMeteorologico;
import weatherreport_mvc.utils.Estadistica;
import weatherreport_mvc.views.DashboardPanel;

/**
 * Controlador del módulo de Panel Principal (Dashboard). Actualiza
 * periódicamente los indicadores de humedad y viento y sus alertas dinámicas.
 */
public class DashboardController {

    private final DashboardPanel view;
    private final Timer temporizador;
    private boolean ejecutando;

    public DashboardController(DashboardPanel view) {
        this.view = view;
        this.temporizador = new Timer(5000, e -> refrescar());
    }

    public void iniciar() {
        refrescar();
        temporizador.start();
    }

    public void detener() {
        temporizador.stop();
    }

    public void refrescar() {
        if (ejecutando) {
            return;
        }
        ejecutando = true;
        new SwingWorker<Void, Void>() {
            private RegistroMeteorologico ultimo;
            private double promedioHumedad;
            private double promedioViento;
            private double promedioTemperatura;
            private boolean hayDatos24h;
            private ConfiguracionSistema cfg;
            private List<RegistroMeteorologico> recientes;

            @Override
            protected Void doInBackground() {
                cfg = ConfiguracionSistema.cargar();
                recientes = RegistroDAO.obtenerTodos();
                ultimo = recientes.isEmpty() ? null : recientes.get(0);
                LocalDateTime hace24h = LocalDateTime.now().minusHours(24);
                Estadistica hum = new Estadistica();
                Estadistica viento = new Estadistica();
                Estadistica temp = new Estadistica();
                int contador = 0;
                for (RegistroMeteorologico r : recientes) {
                    LocalDateTime fechaHora = LocalDateTime.of(r.getFecha(), r.getHora());
                    if (fechaHora.isBefore(hace24h)) {
                        break;
                    }
                    hum.agregar(r.getHumedad());
                    viento.agregar(r.getVelocidadViento());
                    temp.agregar(r.getTempExterior());
                    contador++;
                }
                hayDatos24h = contador > 0;
                promedioHumedad = hum.promedio();
                promedioViento = viento.promedio();
                promedioTemperatura = temp.promedio();
                return null;
            }

            @Override
            protected void done() {
                ejecutando = false;
                view.mostrarDatos(ultimo, promedioHumedad, promedioViento,
                        promedioTemperatura, hayDatos24h, cfg);
                view.mostrarRecientes(recientes);
            }
        }.execute();
    }
}
