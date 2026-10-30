package weatherreport_mvc.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.JComponent;
import weatherreport_mvc.database.RegistroDAO;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.utils.Bitacora;
import weatherreport_mvc.utils.NarrativaMeteorologica;
import weatherreport_mvc.utils.PDFGenerator;

/**
 * Controlador del módulo de Reportes. Genera el "Reporte Diario" y los
 * "Resumen Semanal"/"Resumen Mensual" en PDF mediante Apache PDFBox, tomando
 * como referencia la fecha elegida en la vista (el día del reporte, la semana
 * que termina ese día o el mes al que pertenece).
 */
public class ReporteController {

    public ReporteController() {
    }

    /** Fecha propuesta al abrir el módulo: el último día con datos, o hoy. */
    public LocalDate fechaInicial() {
        try {
            LocalDate ultima = RegistroDAO.obtenerUltimaFecha();
            return ultima == null ? LocalDate.now() : ultima;
        } catch (SQLException e) {
            // Sin conexión se propone hoy; la narrativa mostrará el error de la BD
            Bitacora.error("ReporteController.fechaInicial", e);
            return LocalDate.now();
        }
    }

    private static LocalDate inicioSemana(LocalDate fecha) {
        return fecha.minusDays(6);
    }

    private static LocalDate inicioMes(LocalDate fecha) {
        return fecha.withDayOfMonth(1);
    }

    private static LocalDate finMes(LocalDate fecha) {
        return fecha.withDayOfMonth(fecha.lengthOfMonth());
    }

    public File generarReporteDiario(LocalDate fecha, File destino) throws IOException {
        ConfiguracionSistema cfg = ConfiguracionSistema.cargar();
        return PDFGenerator.generarReporteDiario(
                cfg.getNombreEstacion(), cfg.getUbicacion(), fecha, destino);
    }

    public File generarResumenSemanal(LocalDate fecha, File destino) throws IOException {
        ConfiguracionSistema cfg = ConfiguracionSistema.cargar();
        return PDFGenerator.generarResumenSemanal(
                cfg.getNombreEstacion(), cfg.getUbicacion(),
                inicioSemana(fecha), fecha, destino);
    }

    public File generarResumenMensual(LocalDate fecha, File destino) throws IOException {
        ConfiguracionSistema cfg = ConfiguracionSistema.cargar();
        return PDFGenerator.generarResumenMensual(
                cfg.getNombreEstacion(), cfg.getUbicacion(),
                inicioMes(fecha), finMes(fecha), destino);
    }

    /** Genera el PDF diario a un archivo temporal y lo devuelve renderizado. */
    public JComponent visualizarReporteDiario(LocalDate fecha) throws IOException {
        File temporal = File.createTempFile("vista_previa_diario", ".pdf");
        try {
            generarReporteDiario(fecha, temporal);
            return PDFGenerator.crearVistaPrevia(temporal);
        } finally {
            temporal.delete();
        }
    }

    /** Genera el PDF semanal a un archivo temporal y lo devuelve renderizado. */
    public JComponent visualizarResumenSemanal(LocalDate fecha) throws IOException {
        File temporal = File.createTempFile("vista_previa_semanal", ".pdf");
        try {
            generarResumenSemanal(fecha, temporal);
            return PDFGenerator.crearVistaPrevia(temporal);
        } finally {
            temporal.delete();
        }
    }

    /** Genera el PDF mensual a un archivo temporal y lo devuelve renderizado. */
    public JComponent visualizarResumenMensual(LocalDate fecha) throws IOException {
        File temporal = File.createTempFile("vista_previa_mensual", ".pdf");
        try {
            generarResumenMensual(fecha, temporal);
            return PDFGenerator.crearVistaPrevia(temporal);
        } finally {
            temporal.delete();
        }
    }

    /** Narrativa del resumen meteorológico del día indicado. */
    public String narrativaDiaria(LocalDate fecha) throws SQLException {
        return NarrativaMeteorologica.construir(NarrativaMeteorologica.Periodo.DIARIO,
                fecha, fecha, RegistroDAO.obtenerPorDia(fecha));
    }

    /** Narrativa de los 7 días que terminan en la fecha indicada. */
    public String narrativaSemanal(LocalDate fecha) throws SQLException {
        LocalDate desde = inicioSemana(fecha);
        return NarrativaMeteorologica.construir(NarrativaMeteorologica.Periodo.SEMANAL,
                desde, fecha, RegistroDAO.obtenerEntre(
                        desde.atStartOfDay(), fecha.atTime(23, 59, 59)));
    }

    /** Narrativa del mes al que pertenece la fecha indicada. */
    public String narrativaMensual(LocalDate fecha) throws SQLException {
        LocalDate desde = inicioMes(fecha);
        LocalDate hasta = finMes(fecha);
        return NarrativaMeteorologica.construir(NarrativaMeteorologica.Periodo.MENSUAL,
                desde, hasta, RegistroDAO.obtenerEntre(
                        desde.atStartOfDay(), hasta.atTime(23, 59, 59)));
    }
}
