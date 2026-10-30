package weatherreport_mvc.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import weatherreport_mvc.database.RegistroDAO;
import weatherreport_mvc.database.RegistroDAO.AcumuladoAnio;
import weatherreport_mvc.database.RegistroDAO.AcumuladoMes;
import weatherreport_mvc.database.RegistroDAO.PuntoSemanal;
import weatherreport_mvc.models.RegistroMeteorologico;

/**
 * Constructor de gráficas JFreeChart a partir de datos reales de la base de datos.
 */
public final class GraficaUtil {

    /** Variable meteorológica seleccionable en las gráficas. */
    public enum VariableMeteorologica {
        HUMEDAD("Humedad", "%", ColoresUI.SERIE_HUMEDAD),
        VIENTO("Viento", "km/h", ColoresUI.SERIE_VIENTO),
        LLUVIA("Lluvia", "mm", ColoresUI.SERIE_LLUVIA),
        TEMPERATURA("Temperatura", "°C", ColoresUI.SERIE_TEMPERATURA),
        PRESION("Presión", "hPa", ColoresUI.SERIE_PRESION),
        ROCIO("Punto de rocío", "°C", ColoresUI.SERIE_ROCIO);

        private final String nombre;
        private final String unidad;
        private final Color color;

        VariableMeteorologica(String nombre, String unidad, Color color) {
            this.nombre = nombre;
            this.unidad = unidad;
            this.color = color;
        }

        public String getNombre() {
            return nombre;
        }

        public String getUnidad() {
            return unidad;
        }

        public String getEtiquetaEje() {
            return nombre + " (" + unidad + ")";
        }
    }

    /** Periodo de agregación de la gráfica: semanal, mensual o anual. */
    public enum PeriodoGrafica {
        SEMANAL("Semanal"),
        MENSUAL("Mensual"),
        ANUAL("Anual");

        private final String nombre;

        PeriodoGrafica(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }
    }

    private GraficaUtil() {
    }

    /** Gráfica de líneas: comportamiento semanal de la variable en el rango de 7 días. */
    public static ChartPanel crearPanelGraficaSemanal(LocalDate inicio, LocalDate fin,
                                                      VariableMeteorologica variable) throws SQLException {
        List<PuntoSemanal> puntos = RegistroDAO.obtenerSerieEntre(inicio, fin);
        DefaultCategoryDataset datos = new DefaultCategoryDataset();
        for (PuntoSemanal p : puntos) {
            String etiqueta = DateUtil.nombreMesCorto(p.dia().getMonthValue())
                    + " " + p.dia().getDayOfMonth();
            datos.addValue(Estadistica.serie(valor(p, variable)), variable.getNombre(), etiqueta);
        }
        JFreeChart chart = ChartFactory.createLineChart(
                "Comportamiento Semanal - " + DateUtil.formatoFecha(inicio)
                        + " a " + DateUtil.formatoFecha(fin),
                "Día",
                variable.getEtiquetaEje(),
                datos,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        estilizarPlot(chart);
        estilizarLinea(chart.getCategoryPlot(), variable);
        return crearChartPanel(chart);
    }

    /** Gráfica de líneas: valores diarios de la variable en el mes indicado. */
    public static ChartPanel crearPanelGraficaMensual(int anio, int mes,
                                                      VariableMeteorologica variable) throws SQLException {
        List<PuntoSemanal> puntos = RegistroDAO.obtenerSerieMensual(anio, mes);
        DefaultCategoryDataset datos = new DefaultCategoryDataset();
        for (PuntoSemanal p : puntos) {
            datos.addValue(Estadistica.serie(valor(p, variable)), variable.getNombre(),
                    String.valueOf(p.dia().getDayOfMonth()));
        }
        JFreeChart chart = ChartFactory.createLineChart(
                "Comportamiento Mensual - " + DateUtil.nombreMes(mes) + " " + anio,
                "Día",
                variable.getEtiquetaEje(),
                datos,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        estilizarPlot(chart);
        estilizarLinea(chart.getCategoryPlot(), variable);
        return crearChartPanel(chart);
    }

    /** Gráfica de líneas: acumulado mensual de la variable en el año indicado. */
    public static ChartPanel crearPanelGraficaAnual(int anio, VariableMeteorologica variable) throws SQLException {
        List<AcumuladoMes> acumulados = RegistroDAO.obtenerAcumuladoMensual(anio);
        DefaultCategoryDataset datos = new DefaultCategoryDataset();
        for (AcumuladoMes a : acumulados) {
            datos.addValue(Estadistica.serie(valor(a, variable)), variable.getNombre(),
                    DateUtil.nombreMesCorto(a.mes()));
        }
        JFreeChart chart = ChartFactory.createLineChart(
                "Acumulado Mensual - " + anio,
                "Mes",
                variable.getEtiquetaEje(),
                datos,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        estilizarPlot(chart);
        estilizarLinea(chart.getCategoryPlot(), variable);
        return crearChartPanel(chart);
    }

    /** Construye la gráfica del periodo, año, mes y rango de fechas indicados. */
    public static ChartPanel crearPanelGrafica(PeriodoGrafica periodo,
                                               VariableMeteorologica variable,
                                               int anio, int mes,
                                               LocalDate inicio, LocalDate fin) throws SQLException {
        return switch (periodo) {
            case SEMANAL -> crearPanelGraficaSemanal(inicio, fin, variable);
            case MENSUAL -> crearPanelGraficaMensual(anio, mes, variable);
            case ANUAL -> crearPanelGraficaAnual(anio, variable);
        };
    }

    /** Visualización del reporte diario: registros del día actual por hora. */
    public static ChartPanel crearPanelReporteDiario() throws SQLException {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(23, 59, 59);
        List<RegistroMeteorologico> registros = RegistroDAO.obtenerEntre(inicio, fin);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        DefaultCategoryDataset datos = new DefaultCategoryDataset();
        for (RegistroMeteorologico r : registros) {
            String etiqueta = r.getHora().format(fmt);
            datos.addValue(Estadistica.serie(r.getHumedad()), "Humedad %", etiqueta);
            datos.addValue(Estadistica.serie(r.getVelocidadViento()), "Viento km/h", etiqueta);
            datos.addValue(Estadistica.serie(r.getLluvia()), "Lluvia mm", etiqueta);
        }
        JFreeChart chart = ChartFactory.createLineChart(
                "Reporte Diario - " + DateUtil.formatoFecha(LocalDate.now()),
                "Hora",
                "Valor",
                datos,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        estilizarPlot(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, ColoresUI.SERIE_HUMEDAD);
        renderer.setSeriesPaint(1, ColoresUI.SERIE_VIENTO);
        renderer.setSeriesPaint(2, ColoresUI.SERIE_LLUVIA);
        for (int i = 0; i < 3; i++) {
            renderer.setSeriesStroke(i, new BasicStroke(2f));
        }
        return crearChartPanel(chart);
    }

    /** Visualización del resumen semanal: promedios de los últimos 7 días. */
    public static ChartPanel crearPanelReporteSemanal() throws SQLException {
        List<PuntoSemanal> puntos = RegistroDAO.obtenerSerieSemanal();
        DefaultCategoryDataset datos = new DefaultCategoryDataset();
        for (PuntoSemanal p : puntos) {
            String etiqueta = DateUtil.nombreMesCorto(p.dia().getMonthValue())
                    + " " + p.dia().getDayOfMonth();
            datos.addValue(Estadistica.serie(p.humedad()), "Humedad %", etiqueta);
            datos.addValue(Estadistica.serie(p.viento()), "Viento km/h", etiqueta);
            datos.addValue(Estadistica.serie(p.lluvia()), "Lluvia mm", etiqueta);
        }
        JFreeChart chart = ChartFactory.createLineChart(
                "Resumen Semanal (Últimos 7 días)",
                "Día",
                "Valor",
                datos,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        estilizarPlot(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, ColoresUI.SERIE_HUMEDAD);
        renderer.setSeriesPaint(1, ColoresUI.SERIE_VIENTO);
        renderer.setSeriesPaint(2, ColoresUI.SERIE_LLUVIA);
        for (int i = 0; i < 3; i++) {
            renderer.setSeriesStroke(i, new BasicStroke(2f));
        }
        return crearChartPanel(chart);
    }

    private static double valor(PuntoSemanal p, VariableMeteorologica variable) {
        return switch (variable) {
            case HUMEDAD -> p.humedad();
            case VIENTO -> p.viento();
            case LLUVIA -> p.lluvia();
            case TEMPERATURA -> p.temperatura();
            case PRESION -> p.presion();
            case ROCIO -> p.rocio();
        };
    }

    private static double valor(AcumuladoMes a, VariableMeteorologica variable) {
        return switch (variable) {
            case HUMEDAD -> a.humedad();
            case VIENTO -> a.viento();
            case LLUVIA -> a.lluvia();
            case TEMPERATURA -> a.temperatura();
            case PRESION -> a.presion();
            case ROCIO -> a.rocio();
        };
    }

    private static ChartPanel crearChartPanel(JFreeChart chart) {
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(ColoresUI.SUPERFICIE);
        return cp;
    }

    private static void estilizarPlot(JFreeChart chart) {
        chart.setBackgroundPaint(ColoresUI.SUPERFICIE);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        if (chart.getTitle() != null) {
            chart.getTitle().setPaint(ColoresUI.TEXTO_PRIMARIO);
            chart.getTitle().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(ColoresUI.SUPERFICIE);
            chart.getLegend().setItemPaint(ColoresUI.TEXTO_SECUNDARIO);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 12));
        }
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(ColoresUI.SUPERFICIE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(ColoresUI.LINEA_TABLA);
        plot.setRangeGridlineStroke(new BasicStroke(0.8f));
        plot.setDomainGridlinesVisible(false);
        plot.setDomainGridlineStroke(new BasicStroke(0.5f));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setAxisLinePaint(ColoresUI.BORDE_CLARO);
        domainAxis.setTickMarkPaint(ColoresUI.BORDE_CLARO);
        domainAxis.setTickLabelPaint(ColoresUI.TEXTO_SECUNDARIO);
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        domainAxis.setLabelPaint(ColoresUI.TEXTO_SECUNDARIO);
        domainAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAxisLinePaint(ColoresUI.BORDE_CLARO);
        rangeAxis.setTickMarkPaint(ColoresUI.BORDE_CLARO);
        rangeAxis.setTickLabelPaint(ColoresUI.TEXTO_SECUNDARIO);
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        rangeAxis.setLabelPaint(ColoresUI.TEXTO_SECUNDARIO);
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setUpperMargin(0.15);
        rangeAxis.setLowerMargin(0.06);
    }

    /** Estilo amigable de la serie: línea suave con puntos y etiquetas de valor. */
    private static void estilizarLinea(CategoryPlot plot, VariableMeteorologica variable) {
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, variable.color);
        renderer.setSeriesStroke(0, new BasicStroke(2f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShape(new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0));
        renderer.setUseFillPaint(true);
        renderer.setDefaultFillPaint(variable.color);
        renderer.setUseOutlinePaint(true);
        renderer.setDefaultOutlinePaint(ColoresUI.SUPERFICIE);
        renderer.setDefaultOutlineStroke(new BasicStroke(2f));

        String formato = variable == VariableMeteorologica.HUMEDAD ? "0" : "0.#";
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat(formato)));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelPaint(ColoresUI.TEXTO_PRIMARIO);
        renderer.setDefaultItemLabelFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        renderer.setComputeItemLabelContrastColor(false);
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
        renderer.setDefaultNegativeItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER));
    }
}
