package weatherreport_mvc.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import weatherreport_mvc.database.ConexionBD;
import weatherreport_mvc.database.RegistroDAO;
import weatherreport_mvc.models.RegistroMeteorologico;
import weatherreport_mvc.utils.Bitacora;
import weatherreport_mvc.utils.ExcelExporter;
import weatherreport_mvc.views.HistoricoPanel;

/**
 * Controlador del módulo de Histórico. Gestiona la carga, búsqueda, filtrado
 * por mes, impresión y exportación a Excel de los registros meteorológicos.
 */
public class HistoricoController {

    private final HistoricoPanel view;

    public HistoricoController(HistoricoPanel view) {
        this.view = view;
    }

    public void cargarTodos() {
        try {
            view.mostrarRegistros(RegistroDAO.obtenerTodos());
        } catch (SQLException e) {
            Bitacora.error("HistoricoController.cargarTodos", e);
            view.mostrarErrorCarga(ConexionBD.mensajeError(e));
        }
    }

    public void buscar(String texto, Integer mes, String campo) {
        try {
            view.mostrarRegistros(RegistroDAO.buscar(texto, mes, campo));
        } catch (SQLException e) {
            Bitacora.error("HistoricoController.buscar", e);
            view.mostrarErrorCarga(ConexionBD.mensajeError(e));
        }
    }

    public void exportarExcel(List<RegistroMeteorologico> registros) {
        File destino = ExcelExporter.elegirDestino(view);
        if (destino == null) {
            return;
        }
        try {
            ExcelExporter.exportarExcel(registros, destino);
            JOptionPane.showMessageDialog(view,
                    "Se exportaron " + registros.size() + " registros a:\n" + destino.getPath(),
                    "Exportación completada", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            Bitacora.error("HistoricoController.exportarExcel: " + destino, ex);
            ExcelExporter.mostrarError(view, "No se pudo exportar el archivo:\n"
                    + Bitacora.mensaje(ex)
                    + "\nSi el archivo está abierto en Excel, ciérrelo e intente de nuevo.");
        }
    }
}
