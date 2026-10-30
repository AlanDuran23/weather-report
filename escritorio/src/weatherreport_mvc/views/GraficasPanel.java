package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.DateFormatter;
import org.jfree.chart.ChartPanel;
import weatherreport_mvc.controllers.GraficasController;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.GraficaUtil.PeriodoGrafica;
import weatherreport_mvc.utils.GraficaUtil.VariableMeteorologica;
import weatherreport_mvc.utils.UIFactory;

/**
 * Módulo 4 - Gráficas. Una única gráfica construida con JFreeChart cuya
 * variable meteorológica (humedad, viento o lluvia) y periodo de agregación
 * (semanal, mensual o anual) se eligen mediante barras de pestañas. Además
 * permite filtrar por año, mes o rango de fechas (máximo 7 días) según el
 * periodo activo.
 */
public class GraficasPanel extends JPanel {

    public static final String KEY = "graficas";

    private final GraficasController controller;

    private final JPanel zonaGrafica = new JPanel(new BorderLayout());

    private final JComboBox<Integer> cmbAnio = new JComboBox<>();
    private final JComboBox<String> cmbMes = new JComboBox<>();
    private final JFormattedTextField txtDesde = crearCampoFecha();
    private final JFormattedTextField txtHasta = crearCampoFecha();
    private final JPanel filaFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    private final JLabel lblAnio = UIFactory.crearLabel("Año:");
    private final JLabel lblMes = UIFactory.crearLabel("Mes:");
    private final JLabel lblDesde = UIFactory.crearLabel("Desde:");
    private final JLabel lblHasta = UIFactory.crearLabel("Hasta:");

    private boolean ajustandoRango;
    private LocalDate ultimaDesde;
    private LocalDate ultimaHasta;

    public GraficasPanel() {
        super(new BorderLayout());
        this.controller = new GraficasController(this);
        setBackground(ColoresUI.FONDO_RAIZ);
        construirInterfaz();
        controller.cargar();
    }

    private void construirInterfaz() {
        JPanel encabezado = EstiloUI.crearEncabezadoModulo("Gráficas Meteorológicas",
                "Comportamiento de una variable por semana, mes o año");

        // Recargar no es la acción principal de la vista: va como secundario
        JPanel botonera = new JPanel(new java.awt.GridBagLayout());
        botonera.setOpaque(false);
        JButton btnRefrescar = UIFactory.crearBotonSecundario("Actualizar gráficas");
        btnRefrescar.addActionListener(e -> controller.cargar());
        botonera.add(btnRefrescar);
        encabezado.add(botonera, BorderLayout.EAST);

        zonaGrafica.setBackground(ColoresUI.SUPERFICIE);
        zonaGrafica.add(placeholder("Cargando gráfica..."), BorderLayout.CENTER);

        JPanel barraTipo = crearBarraPestañas(
                PeriodoGrafica.SEMANAL.ordinal(),
                PeriodoGrafica.values(),
                p -> {
                    PeriodoGrafica periodo = (PeriodoGrafica) p;
                    actualizarFiltrosSegunPeriodo(periodo);
                    controller.seleccionarPeriodo(periodo);
                });
        JPanel barraVariable = crearBarraPestañas(
                VariableMeteorologica.HUMEDAD.ordinal(),
                VariableMeteorologica.values(),
                v -> controller.seleccionarVariable((VariableMeteorologica) v));

        JPanel pestañas = new JPanel(new BorderLayout(8, 0));
        pestañas.setOpaque(false);
        pestañas.add(barraTipo, BorderLayout.WEST);
        pestañas.add(barraVariable, BorderLayout.EAST);

        crearFilaFiltros();
        conectarFiltros();

        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        superior.add(pestañas, BorderLayout.NORTH);
        superior.add(filaFiltros, BorderLayout.SOUTH);

        JPanel panelGrafica = new JPanel(new BorderLayout());
        panelGrafica.setOpaque(false);
        panelGrafica.add(superior, BorderLayout.NORTH);
        panelGrafica.add(zonaGrafica, BorderLayout.CENTER);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(ColoresUI.FONDO_RAIZ);
        contenedor.setBorder(BorderFactory.createEmptyBorder(18, 20, 20, 20));
        contenedor.add(UIFactory.crearTarjeta(panelGrafica), BorderLayout.CENTER);

        add(encabezado, BorderLayout.NORTH);
        add(contenedor, BorderLayout.CENTER);
    }

    /** Barra de pestañas genérica: periodo o variable meteorológica. */
    private JPanel crearBarraPestañas(int seleccionada, Object[] opciones,
            Consumer<Object> alSeleccionar) {
        JPanel grupo = UIFactory.crearGrupoSegmentado();
        JButton[] botones = new JButton[opciones.length];
        for (int i = 0; i < opciones.length; i++) {
            Object opcion = opciones[i];
            String nombre = opcion instanceof VariableMeteorologica v
                    ? v.getNombre()
                    : ((PeriodoGrafica) opcion).getNombre();
            JButton boton = UIFactory.crearPestania(nombre);
            final Object opcionFinal = opcion;
            boton.addActionListener(e -> {
                for (JButton b : botones) {
                    UIFactory.aplicarEstiloPestania(b, b == boton);
                }
                alSeleccionar.accept(opcionFinal);
            });
            botones[i] = boton;
            grupo.add(boton);
        }
        UIFactory.aplicarEstiloPestania(botones[seleccionada], true);

        // El grupo conserva su ancho natural: FlowLayout no lo estira
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barra.setOpaque(false);
        barra.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        barra.add(grupo);
        return barra;
    }

    /** Fila de filtros (año, mes y rango de fechas) según el periodo activo. */
    private void crearFilaFiltros() {
        filaFiltros.setOpaque(false);
        filaFiltros.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        for (Integer anio : controller.obtenerAnios()) {
            cmbAnio.addItem(anio);
        }
        if (cmbAnio.getItemCount() == 0) {
            cmbAnio.addItem(controller.getAnio());
        }
        cmbAnio.setSelectedItem(controller.getAnio());
        UIFactory.estilizarCombo(cmbAnio);

        for (int m = 1; m <= 12; m++) {
            cmbMes.addItem(DateUtil.nombreMesCorto(m));
        }
        cmbMes.setSelectedIndex(controller.getMes() - 1);
        UIFactory.estilizarCombo(cmbMes);

        txtDesde.setValue(aDate(controller.getInicio()));
        txtHasta.setValue(aDate(controller.getFin()));
        ultimaDesde = controller.getInicio();
        ultimaHasta = controller.getFin();

        filaFiltros.add(lblAnio);
        filaFiltros.add(cmbAnio);
        filaFiltros.add(lblMes);
        filaFiltros.add(cmbMes);
        filaFiltros.add(lblDesde);
        filaFiltros.add(txtDesde);
        filaFiltros.add(lblHasta);
        filaFiltros.add(txtHasta);

        actualizarFiltrosSegunPeriodo(PeriodoGrafica.SEMANAL);
    }

    private void conectarFiltros() {
        cmbAnio.addActionListener(e -> {
            Object seleccion = cmbAnio.getSelectedItem();
            if (seleccion instanceof Integer anio) {
                controller.seleccionarAnio(anio);
            }
        });
        cmbMes.addActionListener(e -> controller.seleccionarMes(cmbMes.getSelectedIndex() + 1));
        txtDesde.addActionListener(e -> ajustarRangoDesde());
        txtDesde.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                ajustarRangoDesde();
            }
        });
        txtHasta.addActionListener(e -> ajustarRangoHasta());
        txtHasta.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                ajustarRangoHasta();
            }
        });
    }

    /** Muestra u oculta los filtros según el periodo elegido. */
    private void actualizarFiltrosSegunPeriodo(PeriodoGrafica periodo) {
        boolean mensualOAnual = periodo != PeriodoGrafica.SEMANAL;
        boolean mensual = periodo == PeriodoGrafica.MENSUAL;
        boolean semanal = periodo == PeriodoGrafica.SEMANAL;

        lblAnio.setVisible(mensualOAnual);
        cmbAnio.setVisible(mensualOAnual);
        lblMes.setVisible(mensual);
        cmbMes.setVisible(mensual);
        lblDesde.setVisible(semanal);
        txtDesde.setVisible(semanal);
        lblHasta.setVisible(semanal);
        txtHasta.setVisible(semanal);
        filaFiltros.revalidate();
        filaFiltros.repaint();
    }

    /** Recalcula el rango al cambiar la fecha inicial (máximo 7 días). */
    private void ajustarRangoDesde() {
        if (ajustandoRango) {
            return;
        }
        LocalDate desde = comprometerFecha(txtDesde, ultimaDesde);
        LocalDate hasta = fecha(txtHasta);
        if (hasta == null) {
            hasta = ultimaHasta;
        }
        if (desde.isAfter(hasta)) {
            hasta = desde;
            ajustandoRango = true;
            txtHasta.setValue(aDate(desde));
            ajustandoRango = false;
        }
        LocalDate tope = desde.plusDays(6);
        if (hasta.isAfter(tope)) {
            ajustandoRango = true;
            txtHasta.setValue(aDate(tope));
            ajustandoRango = false;
            hasta = tope;
        }
        ultimaDesde = desde;
        ultimaHasta = hasta;
        controller.seleccionarRango(desde, hasta);
    }

    /** Recalcula el rango al cambiar la fecha final (máximo 7 días). */
    private void ajustarRangoHasta() {
        if (ajustandoRango) {
            return;
        }
        LocalDate desde = fecha(txtDesde);
        if (desde == null) {
            desde = ultimaDesde;
        }
        LocalDate hasta = comprometerFecha(txtHasta, ultimaHasta);
        if (hasta.isBefore(desde)) {
            ajustandoRango = true;
            txtDesde.setValue(aDate(hasta));
            ajustandoRango = false;
            desde = hasta;
        }
        LocalDate tope = desde.plusDays(6);
        if (hasta.isAfter(tope)) {
            ajustandoRango = true;
            txtHasta.setValue(aDate(tope));
            ajustandoRango = false;
            hasta = tope;
        }
        ultimaDesde = desde;
        ultimaHasta = hasta;
        controller.seleccionarRango(desde, hasta);
    }

    /** Campo de fecha editable manualmente con formato dd/MM/yyyy (sin flechas). */
    private static JFormattedTextField crearCampoFecha() {
        JFormattedTextField campo = new JFormattedTextField(
                new DateFormatter(new SimpleDateFormat("dd/MM/yyyy")));
        campo.setColumns(10);
        UIFactory.estilizarCampo(campo);
        return campo;
    }

    private static LocalDate fecha(JFormattedTextField campo) {
        Object valor = campo.getValue();
        if (valor instanceof Date d) {
            return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (valor instanceof LocalDate ld) {
            return ld;
        }
        return null;
    }

    private LocalDate comprometerFecha(JFormattedTextField campo, LocalDate respaldo) {
        try {
            campo.commitEdit();
        } catch (java.text.ParseException ex) {
            campo.setValue(aDate(respaldo));
        }
        LocalDate d = fecha(campo);
        return d == null ? respaldo : d;
    }

    private static Date aDate(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private JPanel placeholder(String texto) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(ColoresUI.SUPERFICIE);
        JLabel lbl = UIFactory.crearLabel(texto);
        lbl.setForeground(ColoresUI.TEXTO_SUAVE);
        lbl.setHorizontalAlignment(JLabel.CENTER);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    /** Sustituye la gráfica por un aviso en ámbar (p. ej. sin conexión). */
    public void mostrarError(String mensaje) {
        zonaGrafica.removeAll();
        JPanel aviso = placeholder(mensaje);
        aviso.getComponent(0).setForeground(ColoresUI.ALERTA_ATENCION_OSC);
        zonaGrafica.add(aviso, BorderLayout.CENTER);
        zonaGrafica.revalidate();
        zonaGrafica.repaint();
    }

    public void mostrarGrafica(ChartPanel grafica) {
        zonaGrafica.removeAll();
        grafica.setBackground(ColoresUI.SUPERFICIE);
        zonaGrafica.add(grafica, BorderLayout.CENTER);
        zonaGrafica.revalidate();
        zonaGrafica.repaint();
    }

    public void cargar() {
        controller.cargar();
    }
}
