package weatherreport_mvc.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import weatherreport_mvc.database.RegistroDAO;
import weatherreport_mvc.models.RegistroMeteorologico;
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
        view.mostrarRegistros(RegistroDAO.obtenerTodos());
    }

    public void buscar(String texto, Integer mes, String campo) {
        view.mostrarRegistros(RegistroDAO.buscar(texto, mes, campo));
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
            ExcelExporter.mostrarError(view,
                    "No se pudo exportar el archivo:\n" + ex.getMessage());
        }
    }
}
