package weatherreport_mvc.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.utils.MySqlLocalizador;
import weatherreport_mvc.views.RespaldoPanel;

/**
 * Controlador del módulo de Respaldo y Restauración. Ejecuta los comandos
 * del sistema (mysqldump y mysql) mediante ProcessBuilder.
 */
public class RespaldoController {

    private final RespaldoPanel view;

    public RespaldoController(RespaldoPanel view) {
        this.view = view;
    }

    public void realizarRespaldo(File destino) {
        view.mostrarProgreso("Iniciando respaldo de la base de datos...");
        new SwingWorker<Integer, Void>() {
            private String salida = "";

            @Override
            protected Integer doInBackground() {
                try {
                    ConfiguracionSistema cfg = ConfiguracionSistema.cargar();
                    String rutaDump = MySqlLocalizador.localizar(cfg.getRutaMysqldump());
                    if (rutaDump == null) {
                        salida = "No se encontró mysqldump.exe. Verifique la instalación de "
                                + "MySQL (p. ej. C:\\xampp\\mysql\\bin) o agregue su carpeta "
                                + "bin al PATH.";
                        return -1;
                    }
                    List<String> cmd = new ArrayList<>();
                    cmd.add(rutaDump);
                    cmd.add("--host=" + cfg.getHostBD());
                    cmd.add("--port=" + cfg.getPuertoBD());
                    cmd.add("--user=" + cfg.getUsuarioBD());
                    cmd.add(cfg.getNombreBD());

                    // Se escribe a un temporal para no dañar un respaldo previo si el volcado falla.
                    File carpeta = destino.getAbsoluteFile().getParentFile();
                    File temporal = File.createTempFile("respaldo_", ".sql.tmp", carpeta);
                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    configurarCredencial(pb, cfg.getContrasenaBD());
                    pb.redirectOutput(temporal);
                    Process proceso = pb.start();
                    proceso.getOutputStream().close();
                    salida = leerSalida(proceso.getErrorStream());
                    int codigo = proceso.waitFor();
                    if (codigo != 0) {
                        Files.deleteIfExists(temporal.toPath());
                        return codigo;
                    }
                    Files.move(temporal.toPath(), destino.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    return codigo;
                } catch (IOException | InterruptedException e) {
                    salida = e.getMessage();
                    return -1;
                }
            }

            @Override
            protected void done() {
                try {
                    int codigo = get();
                    if (codigo == 0) {
                        view.mostrarExito("Respaldo completado.", destino);
                    } else {
                        view.mostrarError("El respaldo falló. " + salida);
                    }
                } catch (Exception e) {
                    view.mostrarError("Error al ejecutar el respaldo: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void restaurarRespaldo(File archivo) {
        view.mostrarProgreso("Restaurando respaldo " + archivo.getName() + " ...");
        new SwingWorker<Integer, Void>() {
            private String salida = "";

            @Override
            protected Integer doInBackground() {
                try {
                    ConfiguracionSistema cfg = ConfiguracionSistema.cargar();
                    String rutaMysql = MySqlLocalizador.localizar(cfg.getRutaMysql());
                    if (rutaMysql == null) {
                        salida = "No se encontró mysql.exe. Verifique la instalación de "
                                + "MySQL (p. ej. C:\\xampp\\mysql\\bin) o agregue su carpeta "
                                + "bin al PATH.";
                        return -1;
                    }
                    List<String> cmd = new ArrayList<>();
                    cmd.add(rutaMysql);
                    cmd.add("--host=" + cfg.getHostBD());
                    cmd.add("--port=" + cfg.getPuertoBD());
                    cmd.add("--user=" + cfg.getUsuarioBD());
                    cmd.add(cfg.getNombreBD());

                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    pb.redirectErrorStream(true);
                    configurarCredencial(pb, cfg.getContrasenaBD());
                    pb.redirectInput(archivo);
                    Process proceso = pb.start();
                    proceso.getOutputStream().close();
                    salida = leerSalida(proceso.getInputStream());
                    int codigo = proceso.waitFor();
                    return codigo;
                } catch (IOException | InterruptedException e) {
                    salida = e.getMessage();
                    return -1;
                }
            }

            @Override
            protected void done() {
                try {
                    int codigo = get();
                    if (codigo == 0) {
                        view.mostrarExito("Restauración completada.", archivo);
                    } else {
                        view.mostrarError("La restauración falló. " + salida);
                    }
                } catch (Exception e) {
                    view.mostrarError("Error al restaurar: " + e.getMessage());
                }
            }
        }.execute();
    }

    /** Pasa la contraseña de MySQL por variable de entorno (evita exponerla en argv). */
    private static void configurarCredencial(ProcessBuilder pb, String clave) {
        if (clave != null && !clave.isEmpty()) {
            pb.environment().put("MYSQL_PWD", clave);
        }
    }

    private static String leerSalida(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append('\n');
            }
        }
        return sb.toString().trim();
    }
}
