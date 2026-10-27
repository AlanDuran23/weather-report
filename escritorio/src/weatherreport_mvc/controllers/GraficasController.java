package weatherreport_mvc.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import org.jfree.chart.ChartPanel;
import weatherreport_mvc.database.RegistroDAO;
import weatherreport_mvc.utils.GraficaUtil;
import weatherreport_mvc.utils.GraficaUtil.PeriodoGrafica;
import weatherreport_mvc.utils.GraficaUtil.VariableMeteorologica;
import weatherreport_mvc.views.GraficasPanel;

/**
 * Controlador del módulo de Gráficas. Construye una única gráfica cuya
 * variable meteorológica (humedad, viento o lluvia) y periodo de agregación
 * (semanal, mensual o anual) son seleccionables desde la vista, junto con el
 * año, el mes o el rango de fechas según el periodo activo.
 */
public class GraficasController {

    private final GraficasPanel view;

    private VariableMeteorologica variable = VariableMeteorologica.HUMEDAD;
    private PeriodoGrafica periodo = PeriodoGrafica.SEMANAL;

    private int anio;
    private int mes;
    private LocalDate inicio;
    private LocalDate fin;

    public GraficasController(GraficasPanel view) {
        this.view = view;
        definirValoresIniciales();
    }

    /** Ajusta el año, mes y rango inicial a partir de los datos disponibles. */
    private void definirValoresIniciales() {
        LocalDate ultima = RegistroDAO.obtenerUltimaFecha();
        if (ultima == null) {
            anio = LocalDate.now().getYear();
            mes = LocalDate.now().getMonthValue();
            inicio = LocalDate.now().minusDays(6);
            fin = LocalDate.now();
            return;
        }
        anio = ultima.getYear();
        mes = ultima.getMonthValue();
        fin = ultima;
        inicio = ultima.minusDays(6);
    }

    public void cargar() {
        view.mostrarGrafica(GraficaUtil.crearPanelGrafica(periodo, variable, anio, mes, inicio, fin));
    }

    public void seleccionarPeriodo(PeriodoGrafica periodo) {
        this.periodo = periodo;
        cargar();
    }

    public void seleccionarVariable(VariableMeteorologica variable) {
        this.variable = variable;
        cargar();
    }

    public void seleccionarAnio(int anio) {
        this.anio = anio;
        cargar();
    }

    public void seleccionarMes(int mes) {
        this.mes = mes;
        cargar();
    }

    public void seleccionarRango(LocalDate inicio, LocalDate fin) {
        this.inicio = inicio;
        this.fin = fin;
        cargar();
    }

    /** Años disponibles (descendentes): con datos más el año actual y el anterior. */
    public List<Integer> obtenerAnios() {
        int anioActual = LocalDate.now().getYear();
        LinkedHashSet<Integer> unicos = new LinkedHashSet<>(RegistroDAO.obtenerAnios());
        unicos.add(anioActual);
        unicos.add(anioActual - 1);
        List<Integer> anios = new ArrayList<>(unicos);
        anios.sort(Comparator.reverseOrder());
        return anios;
    }

    public int getAnio() {
        return anio;
    }

    public int getMes() {
        return mes;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFin() {
        return fin;
    }
}
