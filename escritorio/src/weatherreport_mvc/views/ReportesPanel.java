package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import weatherreport_mvc.controllers.ReporteController;
import weatherreport_mvc.database.ConexionBD;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.utils.Bitacora;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.SelectorArchivos;
import weatherreport_mvc.utils.UIFactory;

/**
 * Módulo 5 - Reportes. Panel único que permite seleccionar el tipo de reporte
 * (Diario, Semanal o Mensual), previsualizarlo y generarlo en PDF con PDFBox.
 * Para el rol Consulta se muestra como "Informe": solo la narrativa, con formato
 * de documento, y el botón para generarla.
 */
public class ReportesPanel extends JPanel {

    public static final String KEY = "reportes";

    /** Texto del informe: serif como un documento impreso (igual que la web). */
    private static final Font FUENTE_INFORME = new Font("Georgia", Font.PLAIN, 17);
    /** Ancho máximo de la columna del informe (como max-width de .informe en la web). */
    private static final int ANCHO_INFORME = 960;

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
    private final boolean modoInforme;

    private final JPanel zonaVista = new JPanel(new BorderLayout());
    private final JButton btnVisualizar = UIFactory.crearBotonSecundario("Visualizar reporte");
    private final JButton btnGenerar = UIFactory.crearBotonPrimario("Generar Reporte Diario");
    private final JLabel lblTituloInforme = UIFactory.crearEtiquetaGrupo("");

    private final JTextField txtFecha = UIFactory.crearTextField(10);

    private JButton[] botonesPestania;
    private TipoReporte seleccion = TipoReporte.DIARIO;

    public ReportesPanel() {
        this(false);
    }

    /** @param modoInforme true para el rol Consulta: vista "Informe" (texto + Generar informe) */
    public ReportesPanel(boolean modoInforme) {
        super(new BorderLayout());
        this.modoInforme = modoInforme;
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
        if (modoInforme) {
            construirInforme();
            return;
        }
        JPanel encabezado = EstiloUI.crearEncabezadoModulo("Reportes en PDF",
                "Revise el resumen del periodo y genere el documento para archivar o enviar");

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

    /**
     * Vista "Informe" (rol Consulta): periodo y fecha en una línea discreta, la
     * narrativa como documento en una tarjeta y un solo botón para generarla.
     */
    private void construirInforme() {
        JPanel encabezado = EstiloUI.crearEncabezadoModulo("Informe",
                "Resumen meteorológico del periodo elegido");

        zonaVista.setOpaque(false);
        btnGenerar.setText(textoBotonGenerar());
        btnGenerar.addActionListener(e -> generarSeleccionado());

        lblTituloInforme.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ColoresUI.BORDE),
                BorderFactory.createEmptyBorder(16, 0, 0, 0)));
        pie.add(btnGenerar);

        JPanel documento = new JPanel(new BorderLayout());
        documento.setOpaque(false);
        documento.add(lblTituloInforme, BorderLayout.NORTH);
        documento.add(zonaVista, BorderLayout.CENTER);
        documento.add(pie, BorderLayout.SOUTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setOpaque(false);
        contenido.add(crearBarraTipo(), BorderLayout.NORTH);
        contenido.add(EstiloUI.crearTarjeta(documento, 28), BorderLayout.CENTER);

        // Columna de lectura centrada: en pantallas anchas las líneas de 1500 px cansan
        JPanel columna = new JPanel(null) {
            @Override
            public void doLayout() {
                int ancho = Math.min(getWidth(), ANCHO_INFORME);
                contenido.setBounds((getWidth() - ancho) / 2, 0, ancho, getHeight());
            }
        };
        columna.setBackground(ColoresUI.FONDO_RAIZ);
        columna.add(contenido);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(ColoresUI.FONDO_RAIZ);
        contenedor.setBorder(BorderFactory.createEmptyBorder(18, 20, 20, 20));
        contenedor.add(columna, BorderLayout.CENTER);

        add(encabezado, BorderLayout.NORTH);
        add(contenedor, BorderLayout.CENTER);
    }

    private String textoBotonGenerar() {
        return modoInforme ? "Generar informe" : "Generar Reporte " + seleccion.nombre;
    }

    /** Barra de pestañas para elegir Diario, Semanal o Mensual. */
    private JPanel crearBarraTipo() {
        JPanel grupo = UIFactory.crearGrupoSegmentado();
        TipoReporte[] tipos = TipoReporte.values();
        botonesPestania = new JButton[tipos.length];
        for (int i = 0; i < tipos.length; i++) {
            TipoReporte tipo = tipos[i];
            JButton boton = UIFactory.crearPestania(tipo.nombre);
            final TipoReporte tipoSeleccion = tipo;
            boton.addActionListener(e -> {
                seleccion = tipoSeleccion;
                for (JButton b : botonesPestania) {
                    UIFactory.aplicarEstiloPestania(b, b == boton);
                }
                btnGenerar.setText(textoBotonGenerar());
                mostrarNarrativa();
            });
            botonesPestania[i] = boton;
            grupo.add(boton);
        }
        UIFactory.aplicarEstiloPestania(botonesPestania[0], true);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barra.setOpaque(false);
        barra.add(grupo);
        barra.add(Box.createHorizontalStrut(24));
        JLabel lblFecha = UIFactory.crearLabel(modoInforme ? "Fecha" : "Fecha del reporte");
        lblFecha.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        barra.add(lblFecha);
        barra.add(txtFecha);
        return barra;
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
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Bitacora.error("ReportesPanel.visualizarSeleccionado", e.getCause());
                    mostrarError("No se pudo generar la vista previa: " + Bitacora.mensaje(e));
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
        try {
            switch (seleccion) {
                case SEMANAL -> texto = controller.narrativaSemanal(fecha);
                case MENSUAL -> texto = controller.narrativaMensual(fecha);
                default -> texto = controller.narrativaDiaria(fecha);
            }
        } catch (SQLException e) {
            Bitacora.error("ReportesPanel.mostrarNarrativa", e);
            mostrarAvisoVista(ConexionBD.mensajeError(e));
            return;
        }
        if (modoInforme) {
            lblTituloInforme.setText(("Informe " + seleccion.nombre + " · " + periodoTexto(fecha)).toUpperCase());
            mostrarEnVista(crearTextoInforme(texto), false);
            return;
        }
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        // Texto de lectura: 15 px en gris claro, no blanco puro, con margen generoso
        area.setFont(UIFactory.FUENTE_TEXTO);
        area.setBackground(ColoresUI.SUPERFICIE);
        area.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        area.setCaretColor(ColoresUI.TEXTO_PRIMARIO);
        area.setSelectionColor(ColoresUI.SELECCION);
        area.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        mostrarEnVista(area, true);
    }

    /** Pone el texto en la zona de vista con scroll vertical (con fondo de superficie o transparente). */
    private void mostrarEnVista(JComponent texto, boolean conFondo) {
        JScrollPane scroll = new JScrollPane(texto);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        if (conFondo) {
            scroll.getViewport().setBackground(ColoresUI.SUPERFICIE);
        } else {
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
        }
        UIFactory.estilizarScroll(scroll);
        zonaVista.removeAll();
        zonaVista.add(scroll, BorderLayout.CENTER);
        zonaVista.revalidate();
        zonaVista.repaint();
    }

    /**
     * Narrativa con formato de documento: serif, interlineado amplio y espacio entre
     * párrafos; el primero (la apertura) más grande y claro, como en la web.
     */
    private static JTextPane crearTextoInforme(String texto) {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.setCaretColor(ColoresUI.TEXTO_PRIMARIO);
        pane.setSelectionColor(ColoresUI.SELECCION);
        pane.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 8));
        StyledDocument doc = pane.getStyledDocument();
        String[] parrafos = texto.split("\\n\\s*\\n");
        try {
            for (int i = 0; i < parrafos.length; i++) {
                boolean apertura = i == 0 && parrafos.length > 1;
                SimpleAttributeSet estilo = new SimpleAttributeSet();
                StyleConstants.setFontFamily(estilo, FUENTE_INFORME.getFamily());
                StyleConstants.setFontSize(estilo, apertura ? 20 : FUENTE_INFORME.getSize());
                StyleConstants.setForeground(estilo, apertura ? ColoresUI.TEXTO_PRIMARIO : ColoresUI.TEXTO_SECUNDARIO);
                StyleConstants.setLineSpacing(estilo, 0.3f);
                StyleConstants.setSpaceBelow(estilo, i == parrafos.length - 1 ? 0 : 16);
                int inicio = doc.getLength();
                String p = parrafos[i].strip();
                doc.insertString(inicio, i == parrafos.length - 1 ? p : p + "\n", estilo);
                doc.setParagraphAttributes(inicio, p.length(), estilo, false);
            }
        } catch (BadLocationException e) {
            // No ocurre: siempre se inserta al final del documento
            Bitacora.error("ReportesPanel.crearTextoInforme", e);
        }
        pane.setCaretPosition(0);
        return pane;
    }

    /** Periodo del informe en palabras: el día, la semana o el mes. */
    private String periodoTexto(LocalDate fecha) {
        return switch (seleccion) {
            case SEMANAL -> DateUtil.formatoFecha(fecha.minusDays(6)) + " al " + DateUtil.formatoFecha(fecha);
            case MENSUAL -> fecha.getMonth().getDisplayName(TextStyle.FULL, Locale.of("es", "MX"))
                    + " " + fecha.getYear();
            default -> DateUtil.formatoFecha(fecha);
        };
    }

    private void generarSeleccionado() {
        LocalDate fecha = leerFecha();
        if (fecha == null) {
            return;
        }
        String prefijo = modoInforme ? "informe_" + seleccion.nombre.toLowerCase() + "_" : seleccion.prefijoArchivo;
        File destino = seleccionarDestino(prefijo + DateUtil.selloArchivo());
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
                btnGenerar.setText(textoBotonGenerar());
                try {
                    get();
                    mostrarExito(destino);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Bitacora.error("ReportesPanel.generarSeleccionado: " + destino, e.getCause());
                    mostrarError("No se pudo generar el reporte: " + Bitacora.mensaje(e)
                            + ". Si el archivo está abierto en otro programa, ciérrelo e intente de nuevo.");
                }
            }
        }.execute();
    }

    /** Aviso en ámbar en el área de vista previa (p. ej. sin conexión). */
    private void mostrarAvisoVista(String texto) {
        zonaVista.removeAll();
        JLabel aviso = UIFactory.crearLabel(texto);
        aviso.setForeground(ColoresUI.ALERTA_ATENCION_OSC);
        aviso.setHorizontalAlignment(JLabel.CENTER);
        zonaVista.add(aviso, BorderLayout.CENTER);
        zonaVista.revalidate();
        zonaVista.repaint();
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
            UIFactory.estilizarScroll(scroll);
            zonaVista.add(scroll, BorderLayout.CENTER);
        }
        zonaVista.revalidate();
        zonaVista.repaint();
    }

    private File seleccionarDestino(String nombreBase) {
        return SelectorArchivos.elegirDestino(this, modoInforme ? "Guardar informe PDF" : "Guardar reporte PDF",
                ConfiguracionSistema.cargar().getRutaReportes(), nombreBase + ".pdf",
                "Documentos PDF (*.pdf)", "pdf");
    }

    public void mostrarExito(File archivo) {
        new MensajeExitoDialog(this, modoInforme ? "Informe generado con éxito" : "Reporte generado con éxito",
                archivo).setVisible(true);
    }

    public void mostrarError(String mensaje) {
        new MensajeExitoDialog(this, modoInforme ? "Error al generar el informe" : "Error al generar el reporte",
                mensaje).setVisible(true);
    }
}
