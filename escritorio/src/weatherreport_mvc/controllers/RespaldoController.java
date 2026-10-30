package weatherreport_mvc.controllers;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import weatherreport_mvc.database.ConexionBD;
import weatherreport_mvc.database.RespaldoBD;
import weatherreport_mvc.utils.Bitacora;
import weatherreport_mvc.views.RespaldoPanel;

/**
 * Controlador del módulo de Respaldo y Restauración. Respalda y restaura por la misma
 * conexión JDBC de la aplicación ({@link RespaldoBD}), así funciona igual con MySQL local
 * que con el servidor en la nube y no depende de mysqldump.exe ni mysql.exe.
 */
public class RespaldoController {

    private final RespaldoPanel view;

    public RespaldoController(RespaldoPanel view) {
        this.view = view;
    }

    public void realizarRespaldo(File destino) {
        view.mostrarProgreso("Iniciando respaldo de la base de datos...");
        new SwingWorker<String, Void>() {
            private File temporal;

            private void borrarTemporal() {
                if (temporal == null) {
                    return;
                }
                try {
                    Files.deleteIfExists(temporal.toPath());
                } catch (IOException e) {
                    Bitacora.error("RespaldoController: no se pudo borrar " + temporal, e);
                }
            }

            /** null si todo salió bien; si no, el motivo para el usuario. */
            @Override
            protected String doInBackground() {
                try {
                    // Se escribe a un temporal para no dañar un respaldo previo si el volcado falla.
                    File carpeta = destino.getAbsoluteFile().getParentFile();
                    temporal = File.createTempFile("respaldo_", ".sql.tmp", carpeta);
                    try (Connection conn = ConexionBD.getConnection();
                            Writer out = Files.newBufferedWriter(temporal.toPath(), StandardCharsets.UTF_8)) {
                        RespaldoBD.exportar(conn, out);
                    }
                    Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    return null;
                } catch (SQLException e) {
                    Bitacora.error("RespaldoController.realizarRespaldo: " + destino, e);
                    return ConexionBD.mensajeError(e);
                } catch (IOException e) {
                    Bitacora.error("RespaldoController.realizarRespaldo: " + destino, e);
                    return Bitacora.mensaje(e);
                } finally {
                    // Cualquier salida que no llegó al destino (error, excepción) borra el temporal:
                    // antes quedaban respaldo_*.sql.tmp huérfanos en la carpeta de respaldos
                    borrarTemporal();
                }
            }

            @Override
            protected void done() {
                try {
                    String error = get();
                    if (error == null) {
                        view.mostrarExito("Respaldo completado.", destino);
                    } else {
                        view.mostrarError("El respaldo falló. " + error);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Bitacora.error("RespaldoController.realizarRespaldo", e.getCause());
                    view.mostrarError("Error al ejecutar el respaldo: " + Bitacora.mensaje(e));
                }
            }
        }.execute();
    }

    public void restaurarRespaldo(File archivo) {
        view.mostrarProgreso("Restaurando respaldo " + archivo.getName() + " ...");
        new SwingWorker<String, Void>() {
            /** null si todo salió bien; si no, el motivo para el usuario. */
            @Override
            protected String doInBackground() {
                try {
                    String sql = Files.readString(archivo.toPath(), StandardCharsets.UTF_8);
                    try (Connection conn = ConexionBD.getConnection()) {
                        RespaldoBD.importar(conn, sql);
                    }
                    return null;
                } catch (SQLException e) {
                    Bitacora.error("RespaldoController.restaurarRespaldo: " + archivo, e);
                    return ConexionBD.esFalloDeConexion(e) ? ConexionBD.mensajeError(e) : e.getMessage();
                } catch (IOException e) {
                    Bitacora.error("RespaldoController.restaurarRespaldo: " + archivo, e);
                    return Bitacora.mensaje(e);
                }
            }

            @Override
            protected void done() {
                try {
                    String error = get();
                    if (error == null) {
                        view.mostrarExito("Restauración completada.", archivo);
                    } else {
                        view.mostrarError("La restauración falló. " + error);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Bitacora.error("RespaldoController.restaurarRespaldo", e.getCause());
                    view.mostrarError("Error al restaurar: " + Bitacora.mensaje(e));
                }
            }
        }.execute();
    }
}
