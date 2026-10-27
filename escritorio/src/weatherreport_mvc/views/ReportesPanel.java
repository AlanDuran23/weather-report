package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import weatherreport_mvc.controllers.ReporteController;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.EstiloUI.PanelRedondeado;
import weatherreport_mvc.utils.UIFactory;

/**
 * Módulo 5 - Reportes. Panel único que permite seleccionar el tipo de reporte
 * (Diario, Semanal o Mensual), previsualizarlo y generarlo en PDF con PDFBox.
 */
public class ReportesPanel extends JPanel {

    public static final String KEY = "reportes";

    private enum TipoReporte {
        DIARIO("Diario", "reporte_diario_"),
        SEMANAL("Semanal", "resumen_semanal_"),
        MENSUAL("Mensual", "resumen_mensual_");

        private final String nombre;
        private final String prefijoArchivo;

        TipoReporte(String nombre, String prefijoArchivo) {
            this.nombre = nombre;
            this.prefijoArchivo = prefijoArchivo;
        }
    }

    private final ReporteController controller;

    private final JPanel zonaVista = new JPanel(new BorderLayout());
    private final JButton btnVisualizar = UIFactory.crearBotonSecundario("Visualizar reporte");
    private final JButton btnGenerar = UIFactory.crearBotonPrimario("Generar Reporte Diario");

    private final JTextField txtFecha = UIFactory.crearTextField(10);

    private JButton[] botonesPestania;
    private TipoReporte seleccion = TipoReporte.DIARIO;

    public ReportesPanel() {
        super(new BorderLayout());
        this.controller = new ReporteController();
        setBackground(ColoresUI.FONDO_RAIZ);
        txtFecha.setText(DateUtil.formatoFecha(controller.fechaInicial()));
        txtFecha.setToolTipText("dd/MM/yyyy. El semanal abarca los 7 días que terminan en esta fecha; "
                + "el mensual, el mes completo. Pulse Enter para actualizar.");
        txtFecha.addActionListener(e -> mostrarNarrativa());
        construirInterfaz();
        mostrarNarrativa();
    }

    private void construirInterfaz() {
        PanelRedondeado encabezado = EstiloUI.crearPanelDegradadoVino();
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));

        JLabel lblTitulo = new JLabel("Reportes en PDF");
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);
        lblTitulo.setForeground(ColoresUI.BLANCO);
        encabezado.add(lblTitulo, BorderLayout.NORTH);

        zonaVista.setBackground(ColoresUI.SUPERFICIE);
        zonaVista.setBorder(BorderFactory.createLineBorder(ColoresUI.BORDE, 1));

        btnVisualizar.addActionListener(e -> visualizarSeleccionado());
        btnGenerar.addActionListener(e -> generarSeleccionado());

        JPanel botonera = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botonera.setOpaque(false);
        botonera.add(btnVisualizar);
        botonera.add(btnGenerar);

        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setOpaque(false);
        contenido.add(crearBarraTipo(), BorderLayout.NORTH);
        contenido.add(zonaVista, BorderLayout.CENTER);
        contenido.add(botonera, BorderLayout.SOUTH);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(ColoresUI.FONDO_RAIZ);
        contenedor.setBorder(BorderFactory.createEmptyBorder(18, 20, 20, 20));
        contenedor.add(UIFactory.crearTarjeta(contenido), BorderLayout.CENTER);

        add(encabezado, BorderLayout.NORTH);
        add(contenedor, BorderLayout.CENTER);
    }

    /** Barra de pestañas para elegir Diario, Semanal o Mensual. */
    private JPanel crearBarraTipo() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setOpaque(false);
        TipoReporte[] tipos = TipoReporte.values();
        botonesPestania = new JButton[tipos.length];
        for (int i = 0; i < tipos.length; i++) {
            TipoReporte tipo = tipos[i];
            JButton boton = UIFactory.crearBotonSecundario(tipo.nombre);
            boton.setPreferredSize(new Dimension(110, 34));
            final TipoReporte tipoSeleccion = tipo;
            boton.addActionListener(e -> {
                seleccion = tipoSeleccion;
                for (JButton b : botonesPestania) {
                    aplicarEstiloPestania(b, b == boton);
                }
                btnGenerar.setText("Generar Reporte " + seleccion.nombre);
                mostrarNarrativa();
            });
            botonesPestania[i] = boton;
            barra.add(boton);
        }
        aplicarEstiloPestania(botonesPestania[0], true);

        barra.add(Box.createHorizontalStrut(16));
        barra.add(UIFactory.crearLabel("Fecha del reporte:"));
        barra.add(txtFecha);
        return barra;
    }

    /** Alterna el estilo de una pestaña: activa con degradado vino + glow. */
    private void aplicarEstiloPestania(JButton boton, boolean seleccionada) {
        if (!(boton instanceof EstiloUI.BotonGlow glow)) {
            return;
        }
        if (seleccionada) {
            glow.configurarGradiente(ColoresUI.VINO_OSCURO, ColoresUI.VINO_NEON);
            glow.setGlow(ColoresUI.RUBI);
            glow.setBorde(null);
            glow.setForeground(ColoresUI.BLANCO);
        } else {
            glow.setRelleno(ColoresUI.SUPERFICIE_ELEVADA,
                    new Color(0x2E, 0x2E, 0x38));
            glow.setGlow(null);
            glow.setBorde(ColoresUI.BORDE_CLARO);
            glow.setForeground(ColoresUI.TEXTO_PRIMARIO);
        }
        glow.repaint();
    }

    /** Fecha escrita en el campo; null (con aviso) si no es válida. */
    private LocalDate leerFecha() {
        try {
            return LocalDate.parse(txtFecha.getText().trim(), DateUtil.FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            mostrarError("La fecha del reporte debe tener el formato dd/MM/yyyy.");
            return null;
        }
    }

    private void visualizarSeleccionado() {
        LocalDate fecha = leerFecha();
        if (fecha == null) {
            return;
        }
        TipoReporte tipo = seleccion;
        btnVisualizar.setEnabled(false);
        btnVisualizar.setText("Generando...");
        new SwingWorker<JComponent, Void>() {
            @Override
            protected JComponent doInBackground() throws Exception {
                return switch (tipo) {
                    case SEMANAL -> controller.visualizarResumenSemanal(fecha);
                    case MENSUAL -> controller.visualizarResumenMensual(fecha);
                    default -> controller.visualizarReporteDiario(fecha);
                };
            }

            @Override
            protected void done() {
                btnVisualizar.setEnabled(true);
                btnVisualizar.setText("Visualizar reporte");
                try {
                    mostrarVisualizacion(get());
                } catch (Exception e) {
                    mostrarError("No se pudo generar la vista previa: " + e.getMessage());
                }
            }
        }.execute();
    }

    /** Muestra la narrativa meteorológica del tipo de reporte seleccionado. */
    private void mostrarNarrativa() {
        LocalDate fecha = leerFecha();
        if (fecha == null) {
            return;
        }
        String texto;
        switch (seleccion) {
            case SEMANAL -> texto = controller.narrativaSemanal(fecha);
            case MENSUAL -> texto = controller.narrativaMensual(fecha);
            default -> texto = controller.narrativaDiaria(fecha);
        }
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UIFactory.FUENTE_NORMAL);
        area.setBackground(ColoresUI.SUPERFICIE);
        area.setForeground(ColoresUI.TEXTO_PRIMARIO);
        area.setCaretColor(ColoresUI.TEXTO_PRIMARIO);
        area.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ColoresUI.SUPERFICIE);
        zonaVista.removeAll();
        zonaVista.add(scroll, BorderLayout.CENTER);
        zonaVista.revalidate();
        zonaVista.repaint();
    }

    private void generarSeleccionado() {
        LocalDate fecha = leerFecha();
        if (fecha == null) {
            return;
        }
        File destino = seleccionarDestino(seleccion.prefijoArchivo + DateUtil.selloArchivo());
        if (destino == null) {
            return;
        }
        TipoReporte tipo = seleccion;
        btnGenerar.setEnabled(false);
        btnGenerar.setText("Generando...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                switch (tipo) {
                    case SEMANAL -> controller.generarResumenSemanal(fecha, destino);
                    case MENSUAL -> controller.generarResumenMensual(fecha, destino);
                    default -> controller.generarReporteDiario(fecha, destino);
                }
                return null;
            }

            @Override
            protected void done() {
                btnGenerar.setEnabled(true);
                btnGenerar.setText("Generar Reporte " + seleccion.nombre);
                try {
                    get();
                    mostrarExito(destino);
                } catch (Exception e) {
                    mostrarError("No se pudo generar el reporte: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void mostrarVisualizacion(JComponent contenido) {
        zonaVista.removeAll();
        if (contenido == null) {
            JLabel error = UIFactory.crearLabel("No se pudo generar la vista previa.");
            error.setForeground(ColoresUI.ALERTA_CRITICA_OSC);
            error.setHorizontalAlignment(JLabel.CENTER);
            zonaVista.add(error, BorderLayout.CENTER);
        } else {
            JScrollPane scroll = new JScrollPane(contenido);
            scroll.setBorder(null);
            scroll.getViewport().setBackground(ColoresUI.SUPERFICIE);
            scroll.setHorizontalScrollBarPolicy(
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.setVerticalScrollBarPolicy(
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            zonaVista.add(scroll, BorderLayout.CENTER);
        }
        zonaVista.revalidate();
        zonaVista.repaint();
    }

    private File seleccionarDestino(String nombreBase) {
        ConfiguracionSistema cfg = ConfiguracionSistema.cargar();
        File dir = new File(cfg.getRutaReportes());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setDialogTitle("Guardar reporte PDF");
        chooser.setSelectedFile(new File(dir, nombreBase + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("Documentos PDF (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
                archivo = new File(archivo.getParentFile(), archivo.getName() + ".pdf");
            }
            return archivo;
        }
        return null;
    }

    public void mostrarExito(File archivo) {
        new MensajeExitoDialog(this, "Reporte generado con éxito", archivo).setVisible(true);
    }

    public void mostrarError(String mensaje) {
        new MensajeExitoDialog(this, "Error al generar el reporte", mensaje).setVisible(true);
    }
}
