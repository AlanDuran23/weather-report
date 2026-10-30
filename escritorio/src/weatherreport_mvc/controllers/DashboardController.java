package weatherreport_mvc.controllers;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.ToDoubleFunction;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import weatherreport_mvc.database.ConexionBD;
import weatherreport_mvc.database.RegistroDAO;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.models.RegistroMeteorologico;
import weatherreport_mvc.utils.Bitacora;
import weatherreport_mvc.utils.Estadistica;
import weatherreport_mvc.views.DashboardPanel;

/**
 * Controlador del módulo de Panel Principal (Dashboard). Actualiza
 * periódicamente las lecturas, su resumen de 24 h y las alertas.
 */
public class DashboardController {

    /** Lecturas que dibuja la tendencia de cada tarjeta. */
    public static final int REGISTROS_TENDENCIA = 24;

    /** Resumen de una variable: últimas lecturas (cronológicas) y estadísticas de la ventana de 24 h. */
    public record Resumen24h(double[] serie, double promedio, double min, double max) {

        public boolean hayDatos() {
            return Estadistica.hay(promedio);
        }
    }

    private final DashboardPanel view;
    private final Timer temporizador;
    private boolean ejecutando;
    /** Hubo fallo de conexión en el refresco anterior (para no repetir el log). */
    private boolean sinConexion;

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
            private Resumen24h humedad;
            private Resumen24h viento;
            private Resumen24h temperatura;
            private ConfiguracionSistema cfg;

            @Override
            protected Void doInBackground() throws SQLException {
                cfg = ConfiguracionSistema.cargar();
                List<RegistroMeteorologico> todos = RegistroDAO.obtenerTodos();
                ultimo = todos.isEmpty() ? null : todos.get(0);
                // Ventana de 24 h que termina en el último registro, no en la hora actual:
                // si la estación dejó de enviar datos el resumen sigue mostrando su último día
                List<RegistroMeteorologico> ventana = new ArrayList<>();
                if (ultimo != null) {
                    LocalDateTime desde = LocalDateTime.of(ultimo.getFecha(), ultimo.getHora())
                            .minusHours(24);
                    for (RegistroMeteorologico r : todos) {
                        if (!LocalDateTime.of(r.getFecha(), r.getHora()).isAfter(desde)) {
                            break;
                        }
                        ventana.add(0, r); // la lista viene del más reciente al más antiguo
                    }
                }
                // La tendencia usa los últimos registros y no la ventana de 24 h: con pocas
                // lecturas por día, 24 h daban 2 o 3 puntos y la línea no decía nada
                List<RegistroMeteorologico> ultimos = new ArrayList<>(
                        todos.subList(0, Math.min(REGISTROS_TENDENCIA, todos.size())));
                Collections.reverse(ultimos);
                humedad = resumir(ventana, ultimos, RegistroMeteorologico::getHumedad);
                viento = resumir(ventana, ultimos, RegistroMeteorologico::getVelocidadViento);
                temperatura = resumir(ventana, ultimos, RegistroMeteorologico::getTempExterior);
                return null;
            }

            @Override
            protected void done() {
                ejecutando = false;
                try {
                    get();
                    if (sinConexion) {
                        sinConexion = false;
                        Bitacora.aviso("DashboardController", "conexión con la base de datos restablecida");
                    }
                    view.mostrarDatos(ultimo, humedad, viento, temperatura, cfg);
                } catch (ExecutionException e) {
                    // Cada 5 s se reintenta: solo se registra el primer fallo de la racha
                    if (!sinConexion) {
                        Bitacora.error("DashboardController.refrescar", e.getCause());
                    }
                    sinConexion = true;
                    boolean conexion = e.getCause() instanceof SQLException sql
                            && ConexionBD.esFalloDeConexion(sql);
                    view.mostrarErrorLectura(
                            conexion ? "Sin conexión con la base de datos" : "No se pudieron leer los datos",
                            e.getCause() instanceof SQLException sql
                                    ? ConexionBD.mensajeError(sql) : Bitacora.mensaje(e));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private static Resumen24h resumir(List<RegistroMeteorologico> ventana,
                                      List<RegistroMeteorologico> ultimos,
                                      ToDoubleFunction<RegistroMeteorologico> campo) {
        Estadistica e = new Estadistica();
        for (RegistroMeteorologico r : ventana) {
            e.agregar(campo.applyAsDouble(r));
        }
        double[] serie = new double[ultimos.size()];
        for (int i = 0; i < serie.length; i++) {
            serie[i] = campo.applyAsDouble(ultimos.get(i));
        }
        return new Resumen24h(serie, e.promedio(), e.min(), e.max());
    }
}
