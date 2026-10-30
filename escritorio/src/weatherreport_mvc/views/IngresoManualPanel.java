package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import weatherreport_mvc.controllers.RegistroController;
import weatherreport_mvc.database.RegistroDAO;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.models.RegistroMeteorologico;
import weatherreport_mvc.utils.Estadistica;
import weatherreport_mvc.utils.AlertaUtil;
import weatherreport_mvc.utils.Bitacora;
import weatherreport_mvc.utils.AlertaUtil.Nivel;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.UIFactory;

/**
 * Módulo 2 - Ingreso de Datos. Formulario con JTextField manuales para
 * fecha, hora y humedad, JTextField para viento y lluvia, y JTextArea para
 * observaciones. Incluye un panel de resumen en vivo con alertas y una lista
 * de los últimos registros que enlaza al Histórico.
 */
public class IngresoManualPanel extends JPanel {

    public static final String KEY = "ingreso";

    private final RegistroController controller;
    private final Consumer<String> navegador;

    private final JTextField txtFecha;
    private final JTextField txtHora;
    private final JTextField txtHumedad;
    private final JTextField txtViento;
    private final JTextField txtLluvia;
    private final JTextField txtId;
    private final JTextField txtTemp;
    private final JTextField txtHiTemp;
    private final JTextField txtLowTemp;
    private final JTextField txtDew;
    private final JTextField txtChill;
    private final JTextField txtHeat;
    private final JTextField txtThw;
    private final JTextField txtDirViento;
    private final JTextField txtRecorrido;
    private final JTextField txtRafaga;
    private final JTextField txtDirRafaga;
    private final JTextField txtBar;
    private final JTextField txtRainRate;
    private final JTextField txtHeatDD;
    private final JTextField txtCoolDD;
    private final JTextField txtInTemp;
    private final JTextField txtInHum;
    private final JTextField txtInDew;
    private final JTextField txtInHeat;
    private final JTextField txtInEmc;
    private final JTextField txtInDensidad;
    private final JTextField txtWindSamp;
    private final JTextField txtIssTx;
    private final JTextField txtArcInt;
    private final JTextField txtRecept;
    private final JTextArea txtObservaciones;
    private final JLabel lblMensaje;
    private JButton btnGuardar;

    private final JLabel resumenFecha = new JLabel("--");
    private final JLabel resumenHumedad = new JLabel("--");
    private final JLabel resumenViento = new JLabel("--");
    private final JLabel resumenLluvia = new JLabel("--");
    private final JLabel resumenObservaciones = new JLabel("--");
    private final JLabel alertaHumedad = new JLabel(" ");
    private final JLabel alertaViento = new JLabel(" ");

    private JPanel listaRecientes;
    private ConfiguracionSistema cfg;

    public IngresoManualPanel(Consumer<String> navegador) {
        super(new BorderLayout());
        this.navegador = navegador;
        this.controller = new RegistroController(this);
        this.cfg = ConfiguracionSistema.cargar();
        setBackground(ColoresUI.FONDO_RAIZ);

        txtFecha = UIFactory.crearTextField(10);
        txtFecha.setText(LocalDate.now().format(DateUtil.FORMATO_FECHA));
        txtFecha.setPreferredSize(new Dimension(110, 32));
        txtFecha.setMinimumSize(new Dimension(110, 32));

        txtHora = UIFactory.crearTextField(5);
        txtHora.setText(LocalTime.now().format(DateUtil.FORMATO_HORA));
        txtHora.setPreferredSize(new Dimension(110, 32));
        txtHora.setMinimumSize(new Dimension(110, 32));

        txtHumedad = UIFactory.crearTextField(6);
        txtHumedad.setText("60");
        txtHumedad.setPreferredSize(new Dimension(90, 32));
        txtHumedad.setMinimumSize(new Dimension(90, 32));

        txtViento = UIFactory.crearTextField(12);
        txtLluvia = UIFactory.crearTextField(12);
        txtId = UIFactory.crearTextField(8);
        txtTemp = UIFactory.crearTextField(12);
        txtHiTemp = UIFactory.crearTextField(12);
        txtLowTemp = UIFactory.crearTextField(12);
        txtDew = UIFactory.crearTextField(12);
        txtChill = UIFactory.crearTextField(12);
        txtHeat = UIFactory.crearTextField(12);
        txtThw = UIFactory.crearTextField(12);
        txtDirViento = UIFactory.crearTextField(12);
        txtRecorrido = UIFactory.crearTextField(12);
        txtRafaga = UIFactory.crearTextField(12);
        txtDirRafaga = UIFactory.crearTextField(12);
        txtBar = UIFactory.crearTextField(12);
        txtRainRate = UIFactory.crearTextField(12);
        txtHeatDD = UIFactory.crearTextField(12);
        txtCoolDD = UIFactory.crearTextField(12);
        txtInTemp = UIFactory.crearTextField(12);
        txtInHum = UIFactory.crearTextField(12);
        txtInDew = UIFactory.crearTextField(12);
        txtInHeat = UIFactory.crearTextField(12);
        txtInEmc = UIFactory.crearTextField(12);
        txtInDensidad = UIFactory.crearTextField(12);
        txtWindSamp = UIFactory.crearTextField(12);
        txtIssTx = UIFactory.crearTextField(12);
        txtArcInt = UIFactory.crearTextField(12);
        txtRecept = UIFactory.crearTextField(12);
        txtObservaciones = new JTextArea(14, 34);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setFont(UIFactory.FUENTE_TEXTO);
        UIFactory.estilizarCampo(txtObservaciones);

        UIFactory.estilizarCampo(txtFecha);
        UIFactory.estilizarCampo(txtHora);
        UIFactory.estilizarCampo(txtHumedad);
        estilizarCamposNuevos();
        lblMensaje = new JLabel(" ");

        construirInterfaz();
        escucharCambios();
        actualizarResumen();
        recargarRecientes();
    }

    private void construirInterfaz() {
        JPanel encabezado = EstiloUI.crearEncabezadoModulo("Ingreso de Datos",
                "Capture una lectura de la estación o cargue un registro por su ID para corregirlo");

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setBackground(ColoresUI.FONDO_RAIZ);
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        gc.weightx = 1.6;
        gc.gridx = 0;
        gc.insets = new Insets(0, 0, 0, 8);
        contenido.add(construirFormulario(), gc);

        gc.gridx = 1;
        gc.weightx = 0.6;
        gc.insets = new Insets(0, 8, 0, 0);
        contenido.add(construirPanelLateral(), gc);

        add(encabezado, BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);
    }

    /**
     * Formulario en dos columnas con la etiqueta sobre el campo: la mitad de
     * filas que antes. Las acciones quedan fijas al pie de la tarjeta para que
     * Guardar siempre esté a la vista, sin desplazarse.
     */
    private JPanel construirFormulario() {
        JPanel tarjeta = EstiloUI.crearPanelTarjeta();

        JPanel grid = new PanelAnchoVista();
        grid.setOpaque(false);
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
        grid.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Registro: ID para cargar/actualizar, fecha y hora de la lectura
        JButton btnActualizarRegistro = UIFactory.crearBotonResaltado("Actualizar registro");
        btnActualizarRegistro.setToolTipText("Carga el registro con este ID; si ya está cargado, guarda los cambios");
        btnActualizarRegistro.addActionListener(e -> {
            if (controller.isEnEdicion()) {
                controller.actualizar();
            } else {
                controller.cargarPorId();
            }
        });
        JPanel filaId = new JPanel(new BorderLayout(8, 0));
        filaId.setOpaque(false);
        filaId.add(txtId, BorderLayout.CENTER);
        filaId.add(btnActualizarRegistro, BorderLayout.EAST);

        // El ID comparte celda con su botón: recibe más ancho que fecha y hora
        JPanel registro = new JPanel(new GridBagLayout());
        registro.setOpaque(false);
        GridBagConstraints rc = new GridBagConstraints();
        rc.fill = GridBagConstraints.HORIZONTAL;
        rc.weightx = 1.6;
        registro.add(campo("ID del registro", filaId), rc);
        rc.insets = new Insets(0, 16, 0, 0);
        rc.weightx = 1;
        registro.add(campo("Fecha (dd/MM/yyyy)", txtFecha), rc);
        rc.weightx = 0.7;
        registro.add(campo("Hora (HH:mm)", txtHora), rc);
        agregarSeccion(grid, "Registro", registro);

        agregarSeccion(grid, "Exterior", columnas(
                "Temperatura exterior (°C)", txtTemp,
                "Humedad relativa (%)", txtHumedad,
                "Temperatura máxima (°C)", txtHiTemp,
                "Temperatura mínima (°C)", txtLowTemp,
                "Punto de rocío (°C)", txtDew,
                "Sensación térmica (°C)", txtChill,
                "Índice de calor (°C)", txtHeat,
                "Índice THW (°C)", txtThw));

        agregarSeccion(grid, "Viento", columnas(
                "Velocidad promedio (km/h)", txtViento,
                "Dirección", txtDirViento,
                "Ráfaga máxima (km/h)", txtRafaga,
                "Dirección de la ráfaga", txtDirRafaga,
                "Recorrido del viento (km)", txtRecorrido));

        agregarSeccion(grid, "Presión y lluvia", columnas(
                "Presión barométrica (hPa)", txtBar,
                "Lluvia acumulada (mm)", txtLluvia,
                "Tasa de lluvia (mm/h)", txtRainRate,
                "Grados día calefacción", txtHeatDD,
                "Grados día refrigeración", txtCoolDD));

        agregarSeccion(grid, "Interior", columnas(
                "Temperatura interior (°C)", txtInTemp,
                "Humedad interior (%)", txtInHum,
                "Punto de rocío interior (°C)", txtInDew,
                "Índice de calor interior (°C)", txtInHeat,
                "EMC interior (%)", txtInEmc,
                "Densidad del aire (kg/m³)", txtInDensidad));

        agregarSeccion(grid, "Sensores", columnas(
                "Muestras de viento", txtWindSamp,
                "Estado ISS (iss_tx)", txtIssTx,
                "Intervalo de archivo (min)", txtArcInt,
                "Calidad de recepción (%)", txtRecept));

        JScrollPane scrollObs = UIFactory.crearScrollCampo(txtObservaciones);
        scrollObs.setPreferredSize(new Dimension(460, 110));
        agregarSeccion(grid, "Observaciones", scrollObs);

        JScrollPane scrollForm = new JScrollPane(grid);
        scrollForm.setBorder(null);
        scrollForm.setOpaque(false);
        scrollForm.getViewport().setOpaque(false);
        scrollForm.setHorizontalScrollBarPolicy(
                javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        UIFactory.estilizarScroll(scrollForm);

        // Pie fijo: acción principal, acciones secundarias y el mensaje de resultado
        btnGuardar = UIFactory.crearBotonPrimario("Guardar Registro");
        btnGuardar.addActionListener(e -> controller.guardar());
        JButton btnImportar = UIFactory.crearBotonSecundario("Importar TXT/Excel");
        btnImportar.addActionListener(e -> controller.importarDesdeArchivo());
        JButton btnLimpiar = UIFactory.crearBotonTexto("Limpiar");
        btnLimpiar.addActionListener(e -> {
            limpiarFormulario();
            actualizarResumen();
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnLimpiar);
        botones.add(btnImportar);
        botones.add(btnGuardar);

        lblMensaje.setFont(UIFactory.FUENTE_NORMAL);
        JPanel pie = new JPanel(new BorderLayout(16, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ColoresUI.BORDE),
                BorderFactory.createEmptyBorder(14, 24, 14, 20)));
        pie.add(lblMensaje, BorderLayout.CENTER);
        pie.add(botones, BorderLayout.EAST);

        tarjeta.add(scrollForm, BorderLayout.CENTER);
        tarjeta.add(pie, BorderLayout.SOUTH);
        return tarjeta;
    }

    /** Contenido que se ajusta al ancho visible del scroll (solo desplaza en vertical). */
    private static final class PanelAnchoVista extends JPanel implements javax.swing.Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle r, int orientacion, int direccion) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle r, int orientacion, int direccion) {
            return r.height - 32;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Sección del formulario: etiqueta de grupo y su contenido debajo. */
    private static void agregarSeccion(JPanel grid, String titulo, JComponent contenido) {
        JLabel lbl = UIFactory.crearEtiquetaGrupo(titulo);
        lbl.setBorder(BorderFactory.createEmptyBorder(grid.getComponentCount() == 0 ? 0 : 24, 0, 10, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Alto fijo: si el formulario es más bajo que la tarjeta, BoxLayout no estira las secciones
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, lbl.getPreferredSize().height));
        contenido.setMaximumSize(new Dimension(Integer.MAX_VALUE, contenido.getPreferredSize().height));
        grid.add(lbl);
        grid.add(contenido);
    }

    /** Pares etiqueta/campo repartidos en dos columnas. */
    private static JPanel columnas(Object... pares) {
        JPanel p = new JPanel(new GridLayout(0, 2, 16, 12));
        p.setOpaque(false);
        for (int i = 0; i < pares.length; i += 2) {
            p.add(campo((String) pares[i], (JComponent) pares[i + 1]));
        }
        return p;
    }

    /** Etiqueta sobre el campo. */
    private static JPanel campo(String etiqueta, JComponent control) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(UIFactory.FUENTE_NORMAL);
        lbl.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(lbl, BorderLayout.NORTH);
        p.add(control, BorderLayout.CENTER);
        return p;
    }

    private void estilizarCamposNuevos() {
        for (JTextField campo : new JTextField[]{
                txtViento, txtLluvia, txtId, txtTemp, txtHiTemp, txtLowTemp,
                txtDew, txtChill, txtHeat, txtThw, txtDirViento, txtRecorrido,
                txtRafaga, txtDirRafaga, txtBar, txtRainRate, txtHeatDD,
                txtCoolDD, txtInTemp, txtInHum, txtInDew, txtInHeat, txtInEmc,
                txtInDensidad, txtWindSamp, txtIssTx, txtArcInt, txtRecept}) {
            UIFactory.estilizarCampo(campo);
        }
    }

    private JPanel construirPanelLateral() {
        JPanel lateral = new JPanel(new GridBagLayout());
        lateral.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 0;
        c.weighty = 0;
        lateral.add(construirTarjetaResumen(), c);
        c.gridy = 1;
        c.weighty = 1;
        c.insets = new Insets(16, 0, 0, 0);
        lateral.add(construirTarjetaRecientes(), c);
        return lateral;
    }

    /** Vista previa en vivo de lo que se va a guardar y su nivel de alerta. */
    private JPanel construirTarjetaResumen() {
        JPanel tarjeta = EstiloUI.crearPanelTarjeta();
        tarjeta.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        configurarValorResumen(resumenHumedad);
        configurarValorResumen(resumenViento);
        configurarValorResumen(resumenLluvia);
        configurarValorResumen(resumenFecha);

        alertaHumedad.setFont(UIFactory.FUENTE_NORMAL);
        alertaHumedad.setForeground(ColoresUI.ALERTA_NORMAL_OSC);
        alertaViento.setFont(UIFactory.FUENTE_NORMAL);
        alertaViento.setForeground(ColoresUI.ALERTA_NORMAL_OSC);

        resumenObservaciones.setFont(UIFactory.FUENTE_NORMAL);
        resumenObservaciones.setForeground(ColoresUI.TEXTO_SECUNDARIO);

        JPanel cuerpo = new JPanel();
        cuerpo.setOpaque(false);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.add(filaResumen("Humedad", resumenHumedad));
        cuerpo.add(filaResumen("Viento", resumenViento));
        cuerpo.add(filaResumen("Lluvia", resumenLluvia));
        cuerpo.add(filaResumen("Fecha y hora", resumenFecha));
        cuerpo.add(Box.createVerticalStrut(12));
        cuerpo.add(bloqueTexto("Observaciones", resumenObservaciones));
        cuerpo.add(Box.createVerticalStrut(12));
        cuerpo.add(alinear(alertaHumedad));
        cuerpo.add(Box.createVerticalStrut(4));
        cuerpo.add(alinear(alertaViento));

        JLabel lblTitulo = UIFactory.crearLabelTitulo("Resumen del registro");
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        tarjeta.add(lblTitulo, BorderLayout.NORTH);
        tarjeta.add(cuerpo, BorderLayout.CENTER);
        return tarjeta;
    }

    private void configurarValorResumen(JLabel valor) {
        valor.setFont(UIFactory.FUENTE_TEXTO_NEGRITA);
        valor.setForeground(ColoresUI.TEXTO_PRIMARIO);
        valor.setHorizontalAlignment(JLabel.RIGHT);
    }

    /**
     * Fila etiqueta a la izquierda y valor a la derecha. Sin iconos: el de
     * lluvia era un triángulo rojo y se leía como alerta.
     */
    private JPanel filaResumen(String etiqueta, JLabel valor) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(UIFactory.FUENTE_NORMAL.deriveFont(14f));
        lbl.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        JPanel fila = new JPanel(new BorderLayout(12, 0));
        fila.setOpaque(false);
        fila.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ColoresUI.LINEA_TABLA),
                BorderFactory.createEmptyBorder(9, 0, 9, 0)));
        fila.add(lbl, BorderLayout.WEST);
        fila.add(valor, BorderLayout.EAST);
        return alinear(fila);
    }

    private JPanel bloqueTexto(String etiqueta, JLabel valor) {
        JPanel bloque = new JPanel(new BorderLayout(0, 4));
        bloque.setOpaque(false);
        bloque.add(UIFactory.crearEtiquetaGrupo(etiqueta), BorderLayout.NORTH);
        bloque.add(valor, BorderLayout.CENTER);
        return alinear(bloque);
    }

    private static <T extends JComponent> T alinear(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    private JPanel construirTarjetaRecientes() {
        JPanel tarjeta = EstiloUI.crearPanelTarjeta();
        tarjeta.setBorder(BorderFactory.createEmptyBorder(20, 22, 16, 22));

        JLabel lblTitulo = UIFactory.crearLabelTitulo("Últimos registros");
        JButton btnVerTodo = UIFactory.crearBotonTexto("Ver histórico");
        btnVerTodo.addActionListener(e -> navegador.accept(HistoricoPanel.KEY));
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        cabecera.add(lblTitulo, BorderLayout.WEST);
        cabecera.add(btnVerTodo, BorderLayout.EAST);

        listaRecientes = new JPanel();
        listaRecientes.setOpaque(false);
        listaRecientes.setLayout(new BoxLayout(listaRecientes, BoxLayout.Y_AXIS));

        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(listaRecientes, BorderLayout.CENTER);
        return tarjeta;
    }

    /** Lista compacta: fecha y hora a la izquierda, humedad y viento alineados a la derecha. */
    public void recargarRecientes() {
        List<RegistroMeteorologico> recientes;
        String problema = null;
        try {
            recientes = RegistroDAO.obtenerTodos();
        } catch (SQLException e) {
            Bitacora.error("IngresoManualPanel.recargarRecientes", e);
            recientes = List.of();
            problema = "No se pudo leer la base de datos.";
        }
        listaRecientes.removeAll();
        int max = Math.min(6, recientes.size());
        if (problema != null) {
            JLabel aviso = new JLabel(problema);
            aviso.setFont(UIFactory.FUENTE_NORMAL);
            aviso.setForeground(ColoresUI.ALERTA_ATENCION_OSC);
            listaRecientes.add(alinear(aviso));
        } else if (max == 0) {
            JLabel vacio = new JLabel("Aún no hay registros guardados.");
            vacio.setFont(UIFactory.FUENTE_NORMAL);
            vacio.setForeground(ColoresUI.TEXTO_SUAVE);
            listaRecientes.add(alinear(vacio));
        }
        for (int i = 0; i < max; i++) {
            RegistroMeteorologico r = recientes.get(i);
            JLabel lblFecha = new JLabel(DateUtil.formatoFechaSeguro(r.getFecha())
                    + "  " + DateUtil.formatoHoraSeguro(r.getHora()));
            lblFecha.setFont(UIFactory.FUENTE_NORMAL.deriveFont(14f));
            lblFecha.setForeground(ColoresUI.TEXTO_SECUNDARIO);

            JPanel cifras = new JPanel(new GridLayout(1, 2, 12, 0));
            cifras.setOpaque(false);
            cifras.add(cifra(Estadistica.fmt("%.1f %%", r.getHumedad())));
            cifras.add(cifra(Estadistica.fmt("%.1f km/h", r.getVelocidadViento())));

            JPanel fila = new JPanel(new BorderLayout(12, 0));
            fila.setOpaque(false);
            fila.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, i < max - 1 ? 1 : 0, 0, ColoresUI.LINEA_TABLA),
                    BorderFactory.createEmptyBorder(9, 0, 9, 0)));
            fila.add(lblFecha, BorderLayout.WEST);
            fila.add(cifras, BorderLayout.EAST);
            fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            fila.setToolTipText("Ver en Histórico");
            fila.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    navegador.accept(HistoricoPanel.KEY);
                }
            });
            listaRecientes.add(alinear(fila));
        }
        listaRecientes.revalidate();
        listaRecientes.repaint();
    }

    private static JLabel cifra(String texto) {
        JLabel lbl = new JLabel(texto, JLabel.RIGHT);
        lbl.setFont(UIFactory.FUENTE_NORMAL.deriveFont(14f));
        lbl.setForeground(ColoresUI.TEXTO_PRIMARIO);
        lbl.setPreferredSize(new Dimension(78, lbl.getPreferredSize().height));
        return lbl;
    }

    private void escucharCambios() {
        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarResumen();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarResumen();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarResumen();
            }
        };
        txtFecha.getDocument().addDocumentListener(dl);
        txtHora.getDocument().addDocumentListener(dl);
        txtHumedad.getDocument().addDocumentListener(dl);
        txtViento.getDocument().addDocumentListener(dl);
        txtLluvia.getDocument().addDocumentListener(dl);
        txtObservaciones.getDocument().addDocumentListener(dl);
    }

    private void actualizarResumen() {
        LocalDate fecha = getFecha();
        LocalTime hora = getHora();
        if (fecha == null || hora == null) {
            resumenFecha.setText("--");
        } else {
            resumenFecha.setText(DateUtil.formatoFechaEstacion(fecha)
                    + " " + DateUtil.formatoHoraEstacion(hora));
        }

        Double humedad = getHumedad();
        if (humedad == null) {
            resumenHumedad.setText("--");
            alertaHumedad.setText(" ");
        } else {
            resumenHumedad.setText(String.format("%.1f %%", humedad));
            Nivel nivelHum = AlertaUtil.evaluarHumedad(humedad, cfg.getUmbralHumedad());
            alertaHumedad.setText(envolverHtml(
                    AlertaUtil.mensajeHumedad(humedad, cfg.getUmbralHumedad())));
            alertaHumedad.setForeground(nivelHum.getColor());
        }

        Double viento = getViento();
        if (viento == null) {
            resumenViento.setText("--");
            alertaViento.setText(" ");
        } else {
            resumenViento.setText(String.format("%.1f km/h", viento));
            Nivel nivelViento = AlertaUtil.evaluarViento(viento, cfg.getUmbralViento());
            alertaViento.setText(envolverHtml(
                    AlertaUtil.mensajeViento(viento, cfg.getUmbralViento())));
            alertaViento.setForeground(nivelViento.getColor());
        }

        Double lluvia = getLluvia();
        resumenLluvia.setText(lluvia == null ? "--" : String.format("%.1f mm/h", lluvia));

        String obs = getObservaciones().trim();
        resumenObservaciones.setText(obs.isEmpty() ? "--" : envolverHtml(obs));
    }

    private String envolverHtml(String texto) {
        // Ancho fijo para que el texto largo se parta en líneas dentro de la tarjeta
        String seguro = texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html><body style='width:260px'>" + seguro + "</body></html>";
    }

    public LocalDate getFecha() {
        try {
            return LocalDate.parse(txtFecha.getText().trim(), DateUtil.FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public LocalTime getHora() {
        try {
            return LocalTime.parse(txtHora.getText().trim(), DateUtil.FORMATO_HORA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public Double getHumedad() {
        return num(txtHumedad.getText());
    }

    public Double getViento() {
        return num(txtViento.getText());
    }

    public Double getLluvia() {
        return num(txtLluvia.getText());
    }

    public Double getTempExterior() {
        return num(txtTemp.getText());
    }

    public Double getHiTemp() {
        return num(txtHiTemp.getText());
    }

    public Double getLowTemp() {
        return num(txtLowTemp.getText());
    }

    public Double getPuntoRocio() {
        return num(txtDew.getText());
    }

    public Double getSensacionTermica() {
        return num(txtChill.getText());
    }

    public Double getIndiceCalor() {
        return num(txtHeat.getText());
    }

    public Double getIndiceThw() {
        return num(txtThw.getText());
    }

    public Double getRecorridoViento() {
        return num(txtRecorrido.getText());
    }

    public Double getRafagaMaxima() {
        return num(txtRafaga.getText());
    }

    public Double getPresionBarometrica() {
        return num(txtBar.getText());
    }

    public Double getTasaLluvia() {
        return num(txtRainRate.getText());
    }

    public Double getGradosDiaCalefaccion() {
        return num(txtHeatDD.getText());
    }

    public Double getGradosDiaRefrigeracion() {
        return num(txtCoolDD.getText());
    }

    public Double getTempInterior() {
        return num(txtInTemp.getText());
    }

    public Double getHumedadInterior() {
        return num(txtInHum.getText());
    }

    public Double getPuntoRocioInterior() {
        return num(txtInDew.getText());
    }

    public Double getIndiceCalorInterior() {
        return num(txtInHeat.getText());
    }

    public Double getEmcInterior() {
        return num(txtInEmc.getText());
    }

    public Double getDensidadAireInterior() {
        return num(txtInDensidad.getText());
    }

    public Integer getMuestrasViento() {
        return entero(txtWindSamp.getText());
    }

    public Integer getEstadoTransmision() {
        return entero(txtIssTx.getText());
    }

    public Integer getIntervaloArchivo() {
        return entero(txtArcInt.getText());
    }

    public Double getCalidadRecepcion() {
        return num(txtRecept.getText());
    }

    public String getDireccionViento() {
        return txtDirViento.getText().trim();
    }

    public String getDireccionRafaga() {
        return txtDirRafaga.getText().trim();
    }

    /** Acepta coma o punto decimal ("12,5" o "12.5"). */
    private static Double num(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer entero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getObservaciones() {
        return txtObservaciones.getText();
    }

    public Integer getIdEdicion() {
        try {
            return Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Carga un registro existente en el formulario y activa el modo edición. */
    public void cargarRegistro(RegistroMeteorologico r) {
        establecerFechaHora(r.getFecha(), r.getHora());
        txtId.setText(String.valueOf(r.getId()));
        txtTemp.setText(valor(r.getTempExterior()));
        txtHiTemp.setText(valor(r.getHiTemp()));
        txtLowTemp.setText(valor(r.getLowTemp()));
        txtDew.setText(valor(r.getPuntoRocio()));
        txtChill.setText(valor(r.getSensacionTermica()));
        txtHeat.setText(valor(r.getIndiceCalor()));
        txtThw.setText(valor(r.getIndiceThw()));
        txtHumedad.setText(valor(r.getHumedad()));
        txtViento.setText(valor(r.getVelocidadViento()));
        txtDirViento.setText(r.getDireccionViento());
        txtRecorrido.setText(valor(r.getRecorridoViento()));
        txtRafaga.setText(valor(r.getRafagaMaxima()));
        txtDirRafaga.setText(r.getDireccionRafaga());
        txtBar.setText(valor(r.getPresionBarometrica()));
        txtLluvia.setText(valor(r.getLluvia()));
        txtRainRate.setText(valor(r.getTasaLluvia()));
        txtHeatDD.setText(valor(r.getGradosDiaCalefaccion()));
        txtCoolDD.setText(valor(r.getGradosDiaRefrigeracion()));
        txtInTemp.setText(valor(r.getTempInterior()));
        txtInHum.setText(valor(r.getHumedadInterior()));
        txtInDew.setText(valor(r.getPuntoRocioInterior()));
        txtInHeat.setText(valor(r.getIndiceCalorInterior()));
        txtInEmc.setText(valor(r.getEmcInterior()));
        txtInDensidad.setText(valor(r.getDensidadAireInterior()));
        txtWindSamp.setText(String.valueOf(r.getMuestrasViento()));
        txtIssTx.setText(String.valueOf(r.getEstadoTransmision()));
        txtArcInt.setText(String.valueOf(r.getIntervaloArchivo()));
        txtRecept.setText(valor(r.getCalidadRecepcion()));
        txtObservaciones.setText(r.getObservaciones());
        btnGuardar.setText("Guardar cambios");
        lblMensaje.setText("Registro " + r.getId() + " cargado. Edite y guarde.");
        lblMensaje.setForeground(ColoresUI.ALERTA_NORMAL_OSC);
        actualizarResumen();
    }

    /** Texto del campo; vacío si el dato falta (un 0 real se muestra como 0). */
    private String valor(double num) {
        if (!Estadistica.hay(num)) {
            return "";
        }
        if (num == (long) num) {
            return String.valueOf((long) num);
        }
        return String.valueOf(num);
    }

    private void establecerFechaHora(LocalDate fecha, LocalTime hora) {
        txtFecha.setText(fecha.format(DateUtil.FORMATO_FECHA));
        txtHora.setText(hora.format(DateUtil.FORMATO_HORA));
    }

    public void limpiarFormulario() {
        txtViento.setText("");
        txtLluvia.setText("");
        txtObservaciones.setText("");
        txtHumedad.setText("60");
        txtTemp.setText("");
        txtHiTemp.setText("");
        txtLowTemp.setText("");
        txtDew.setText("");
        txtChill.setText("");
        txtHeat.setText("");
        txtThw.setText("");
        txtDirViento.setText("");
        txtRecorrido.setText("");
        txtRafaga.setText("");
        txtDirRafaga.setText("");
        txtBar.setText("");
        txtRainRate.setText("");
        txtHeatDD.setText("");
        txtCoolDD.setText("");
        txtInTemp.setText("");
        txtInHum.setText("");
        txtInDew.setText("");
        txtInHeat.setText("");
        txtInEmc.setText("");
        txtInDensidad.setText("");
        txtWindSamp.setText("");
        txtIssTx.setText("");
        txtArcInt.setText("");
        txtRecept.setText("");
        txtId.setText("");
        btnGuardar.setText("Guardar Registro");
        controller.salirDeEdicion();
    }

    public void mostrarExito(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setForeground(ColoresUI.ALERTA_NORMAL_OSC);
        recargarRecientes();
    }

    public void mostrarError(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setForeground(ColoresUI.ALERTA_CRITICA_OSC);
    }

    /** Abre el selector de archivo para importar registros desde TXT o Excel. */
    public File elegirArchivoImportacion() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Importar registros desde archivo");
        selector.addChoosableFileFilter(new FileNameExtensionFilter(
                "Archivos de texto (*.txt, *.csv)", "txt", "csv"));
        selector.addChoosableFileFilter(new FileNameExtensionFilter(
                "Archivos Excel (*.xlsx, *.xls)", "xlsx", "xls"));
        selector.setAcceptAllFileFilterUsed(false);
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return selector.getSelectedFile();
    }
}
