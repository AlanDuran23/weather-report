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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import weatherreport_mvc.utils.AlertaUtil.Nivel;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.EstiloUI.PanelRedondeado;
import weatherreport_mvc.utils.UIFactory;
import javax.swing.JScrollBar;

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
        PanelRedondeado encabezado = EstiloUI.crearPanelDegradadoVino();
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));

        JLabel lblTitulo = new JLabel("Ingreso de Datos");
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);
        lblTitulo.setForeground(ColoresUI.BLANCO);
        encabezado.add(lblTitulo, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setBackground(ColoresUI.FONDO_RAIZ);
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        gc.weightx = 1.4;
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

    private JPanel construirFormulario() {
        JPanel tarjeta = EstiloUI.crearPanelTarjeta();
        tarjeta.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int fila = 0;
        gbc.gridx = 0;
        gbc.gridy = fila;
        grid.add(UIFactory.crearLabel("ID del registro:"), gbc);
        gbc.gridx = 1;
        grid.add(txtId, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(6, 24, 6, 8);
        JButton btnActualizarRegistro = UIFactory.crearBotonResaltado("Actualizar registro");
        btnActualizarRegistro.addActionListener(e -> {
            if (controller.isEnEdicion()) {
                controller.actualizar();
            } else {
                controller.cargarPorId();
            }
        });
        grid.add(btnActualizarRegistro, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 8, 6, 8);

        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        grid.add(UIFactory.crearLabel("Fecha:"), gbc);
        gbc.gridx = 1;
        grid.add(txtFecha, gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(6, 24, 6, 8);
        grid.add(UIFactory.crearLabel("Hora:"), gbc);
        gbc.gridx = 3;
        grid.add(txtHora, gbc);

        fila++;
        agregarGrupo(grid, gbc, fila, "EXTERIOR");

        fila++;
        agregarCampo(grid, gbc, fila, "Temperatura exterior (°C):", txtTemp);
        fila++;
        agregarCampo(grid, gbc, fila, "Temperatura máxima (°C):", txtHiTemp);
        fila++;
        agregarCampo(grid, gbc, fila, "Temperatura mínima (°C):", txtLowTemp);
        fila++;
        agregarCampo(grid, gbc, fila, "Punto de rocío (°C):", txtDew);
        fila++;
        agregarCampo(grid, gbc, fila, "Sensación térmica (°C):", txtChill);
        fila++;
        agregarCampo(grid, gbc, fila, "Índice de calor (°C):", txtHeat);
        fila++;
        agregarCampo(grid, gbc, fila, "Índice THW (°C):", txtThw);
        fila++;
        agregarCampo(grid, gbc, fila, "Humedad relativa (%):", txtHumedad);

        fila++;
        agregarGrupo(grid, gbc, fila, "VIENTO");

        fila++;
        agregarCampo(grid, gbc, fila, "Velocidad promedio (km/h):", txtViento);
        fila++;
        agregarCampo(grid, gbc, fila, "Dirección:", txtDirViento);
        fila++;
        agregarCampo(grid, gbc, fila, "Recorrido del viento (km):", txtRecorrido);
        fila++;
        agregarCampo(grid, gbc, fila, "Ráfaga máxima (km/h):", txtRafaga);
        fila++;
        agregarCampo(grid, gbc, fila, "Dirección de la ráfaga:", txtDirRafaga);

        fila++;
        agregarGrupo(grid, gbc, fila, "PRESIÓN Y LLUVIA");

        fila++;
        agregarCampo(grid, gbc, fila, "Presión barométrica (hPa):", txtBar);
        fila++;
        agregarCampo(grid, gbc, fila, "Lluvia acumulada (mm):", txtLluvia);
        fila++;
        agregarCampo(grid, gbc, fila, "Tasa de lluvia (mm/h):", txtRainRate);
        fila++;
        agregarCampo(grid, gbc, fila, "Grados día calefacción:", txtHeatDD);
        fila++;
        agregarCampo(grid, gbc, fila, "Grados día refrigeración:", txtCoolDD);

        fila++;
        agregarGrupo(grid, gbc, fila, "INTERIOR");

        fila++;
        agregarCampo(grid, gbc, fila, "Temperatura interior (°C):", txtInTemp);
        fila++;
        agregarCampo(grid, gbc, fila, "Humedad interior (%):", txtInHum);
        fila++;
        agregarCampo(grid, gbc, fila, "Punto de rocío interior (°C):", txtInDew);
        fila++;
        agregarCampo(grid, gbc, fila, "Índice de calor interior (°C):", txtInHeat);
        fila++;
        agregarCampo(grid, gbc, fila, "EMC interior (%):", txtInEmc);
        fila++;
        agregarCampo(grid, gbc, fila, "Densidad del aire (kg/m³):", txtInDensidad);

        fila++;
        agregarGrupo(grid, gbc, fila, "SENSORES");

        fila++;
        agregarCampo(grid, gbc, fila, "Muestras de viento:", txtWindSamp);
        fila++;
        agregarCampo(grid, gbc, fila, "Estado ISS (iss_tx):", txtIssTx);
        fila++;
        agregarCampo(grid, gbc, fila, "Intervalo de archivo (min):", txtArcInt);
        fila++;
        agregarCampo(grid, gbc, fila, "Calidad de recepción (%):", txtRecept);

        fila++;
        agregarGrupo(grid, gbc, fila, "OBSERVACIONES");

        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 8, 6, 8);

        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        scrollObs.setPreferredSize(new Dimension(460, 120));
        scrollObs.setMinimumSize(new Dimension(460, 90));
        estilizarBarraScroll(scrollObs);
        grid.add(scrollObs, gbc);

        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        fila++;
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botones.setOpaque(false);
        btnGuardar = UIFactory.crearBotonPrimario("Guardar Registro");
        btnGuardar.setPreferredSize(new Dimension(180, 40));
        btnGuardar.addActionListener(e -> controller.guardar());
        JButton btnImportar = UIFactory.crearBotonSecundario("Importar TXT/Excel");
        btnImportar.setPreferredSize(new Dimension(150, 40));
        btnImportar.addActionListener(e -> controller.importarDesdeArchivo());
        JButton btnLimpiar = UIFactory.crearBotonSecundario("Limpiar");
        btnLimpiar.setPreferredSize(new Dimension(120, 40));
        btnLimpiar.addActionListener(e -> {
            limpiarFormulario();
            actualizarResumen();
        });
        botones.add(btnGuardar);
        botones.add(btnImportar);
        botones.add(btnLimpiar);
        grid.add(botones, gbc);
        gbc.gridwidth = 1;

        fila++;
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        lblMensaje.setFont(UIFactory.FUENTE_TEXTO_NEGRITA);
        grid.add(lblMensaje, gbc);
        gbc.gridwidth = 1;

        JScrollPane scrollForm = new JScrollPane(grid);
        scrollForm.setBorder(null);
        scrollForm.setBackground(ColoresUI.SUPERFICIE);
        scrollForm.getViewport().setBackground(ColoresUI.SUPERFICIE);
        scrollForm.setOpaque(true);
        scrollForm.setVerticalScrollBarPolicy(
                javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollForm.setHorizontalScrollBarPolicy(
                javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        estilizarBarraScroll(scrollForm);

        tarjeta.add(scrollForm, BorderLayout.CENTER);
        return tarjeta;
    }

    /** Fila de encabezado de grupo que ocupa las 4 columnas. */
    private void agregarGrupo(JPanel grid, GridBagConstraints gbc, int fila, String titulo) {
        JLabel grupo = UIFactory.crearLabel(titulo);
        grupo.setFont(UIFactory.FUENTE_TEXTO_NEGRITA);
        grupo.setForeground(ColoresUI.TEXTO_VINO);
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(14, 8, 2, 8);
        grid.add(grupo, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 8, 6, 8);
    }

    /** Fila etiqueta + campo a la izquierda del formulario. */
    private void agregarCampo(JPanel grid, GridBagConstraints gbc, int fila,
                              String etiqueta, JTextField campo) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        grid.add(UIFactory.crearLabel(etiqueta), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        grid.add(campo, gbc);
        gbc.gridwidth = 1;
    }

    private void estilizarBarraScroll(JScrollPane scroll) {
        scroll.setBackground(ColoresUI.SUPERFICIE);
        scroll.getViewport().setBackground(ColoresUI.SUPERFICIE);
        scroll.getViewport().setOpaque(true);
        JScrollBar barra = scroll.getVerticalScrollBar();
        barra.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = ColoresUI.VINO_OSCURO;
                this.trackColor = ColoresUI.SUPERFICIE;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return crearBotonVacio();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return crearBotonVacio();
            }

            private JButton crearBotonVacio() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
        });
        barra.setBackground(ColoresUI.SUPERFICIE);
        barra.setUnitIncrement(16);
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
        JPanel lateral = new JPanel(new GridLayout(2, 1, 0, 16));
        lateral.setOpaque(false);
        lateral.add(construirTarjetaResumen());
        lateral.add(construirTarjetaRecientes());
        return lateral;
    }

    private JPanel construirTarjetaResumen() {
        JPanel tarjeta = EstiloUI.crearPanelTarjeta();
        tarjeta.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel lblTitulo = UIFactory.crearLabelTitulo("Resumen del Registro");
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);

        resumenFecha.setFont(UIFactory.FUENTE_TEXTO);
        resumenFecha.setForeground(ColoresUI.TEXTO_PRIMARIO);
        resumenFecha.setHorizontalAlignment(JLabel.CENTER);

        configurarValorResumen(resumenHumedad);
        configurarValorResumen(resumenViento);
        configurarValorResumen(resumenLluvia);

        alertaHumedad.setFont(UIFactory.FUENTE_TEXTO_NEGRITA);
        alertaHumedad.setForeground(ColoresUI.ALERTA_NORMAL_OSC);
        alertaHumedad.setHorizontalAlignment(JLabel.CENTER);
        alertaViento.setFont(UIFactory.FUENTE_TEXTO_NEGRITA);
        alertaViento.setForeground(ColoresUI.ALERTA_NORMAL_OSC);
        alertaViento.setHorizontalAlignment(JLabel.CENTER);

        resumenObservaciones.setFont(UIFactory.FUENTE_TEXTO);
        resumenObservaciones.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        resumenObservaciones.setHorizontalAlignment(JLabel.CENTER);

        JPanel cuerpo = new JPanel();
        cuerpo.setOpaque(false);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.add(Box.createVerticalGlue());
        cuerpo.add(crearLineaIcono("Humedad_Relativa.png", resumenHumedad));
        cuerpo.add(Box.createVerticalStrut(10));
        cuerpo.add(crearLineaIcono("Velocidad_del_Viento.png", resumenViento));
        cuerpo.add(Box.createVerticalStrut(10));
        cuerpo.add(crearLineaIcono("Precipitacion.png", resumenLluvia));
        cuerpo.add(Box.createVerticalStrut(14));
        cuerpo.add(crearLineaTexto("Fecha y hora:", resumenFecha));
        cuerpo.add(Box.createVerticalStrut(10));
        cuerpo.add(crearLineaTexto("Observaciones:", resumenObservaciones));
        cuerpo.add(Box.createVerticalStrut(12));
        cuerpo.add(crearLineaTexto("", alertaHumedad));
        cuerpo.add(Box.createVerticalStrut(8));
        cuerpo.add(crearLineaTexto("", alertaViento));
        cuerpo.add(Box.createVerticalGlue());

        tarjeta.add(construirEncabezadoTarjeta(lblTitulo), BorderLayout.NORTH);
        tarjeta.add(cuerpo, BorderLayout.CENTER);
        return tarjeta;
    }

    private void configurarValorResumen(JLabel valor) {
        valor.setFont(UIFactory.FUENTE_TEXTO_NEGRITA);
        valor.setForeground(ColoresUI.TEXTO_PRIMARIO);
        valor.setHorizontalAlignment(JLabel.CENTER);
    }

    private JPanel crearLineaIcono(String icono, JLabel valor) {
        JPanel linea = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        linea.setOpaque(false);
        linea.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblIcono = new JLabel();
        ImageIcon img = UIFactory.cargarIconoProporcional(icono, 32, 48);
        if (img != null) {
            lblIcono.setIcon(img);
        }
        linea.add(lblIcono);
        linea.add(valor);
        return linea;
    }

    private JPanel crearLineaTexto(String etiqueta, JLabel valor) {
        JPanel linea = new JPanel();
        linea.setOpaque(false);
        linea.setLayout(new BoxLayout(linea, BoxLayout.Y_AXIS));
        linea.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (!etiqueta.isEmpty()) {
            JLabel lblEtiqueta = UIFactory.crearLabel(etiqueta);
            lblEtiqueta.setFont(UIFactory.FUENTE_TEXTO_NEGRITA);
            lblEtiqueta.setForeground(ColoresUI.TEXTO_SECUNDARIO);
            lblEtiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
            linea.add(lblEtiqueta);
        }
        valor.setAlignmentX(Component.CENTER_ALIGNMENT);
        linea.add(valor);
        return linea;
    }

    private JPanel construirEncabezadoTarjeta(JLabel lblTitulo) {
        lblTitulo.setHorizontalAlignment(JLabel.CENTER);
        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        JLabel espacio = new JLabel(" ");
        espacio.setPreferredSize(new Dimension(1, 8));
        superior.add(espacio, BorderLayout.NORTH);
        superior.add(lblTitulo, BorderLayout.CENTER);
        return superior;
    }

    private JPanel construirTarjetaRecientes() {
        JPanel tarjeta = EstiloUI.crearPanelTarjeta();
        tarjeta.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel lblTitulo = UIFactory.crearLabelTitulo("Últimos Registros");
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);

        listaRecientes = new JPanel(new GridLayout(6, 1, 0, 4));
        listaRecientes.setOpaque(false);

        tarjeta.add(construirEncabezadoTarjeta(lblTitulo), BorderLayout.NORTH);
        tarjeta.add(listaRecientes, BorderLayout.CENTER);
        return tarjeta;
    }

    public void recargarRecientes() {
        List<RegistroMeteorologico> recientes = RegistroDAO.obtenerTodos();
        listaRecientes.removeAll();
        int max = Math.min(6, recientes.size());
        for (int i = 0; i < 6; i++) {
            final int indice = i;
            JLabel fila = new JLabel();
            fila.setFont(UIFactory.FUENTE_TEXTO.deriveFont(15f));
            fila.setHorizontalAlignment(JLabel.CENTER);
            fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            fila.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    navegador.accept(HistoricoPanel.KEY);
                }
            });
            if (indice < max) {
                RegistroMeteorologico r = recientes.get(indice);
                fila.setText("• " + DateUtil.formatoFechaSeguro(r.getFecha())
                        + " " + DateUtil.formatoHoraSeguro(r.getHora())
                        + "  |  " + Estadistica.fmt("%.1f %%", r.getHumedad())
                        + "  |  " + Estadistica.fmt("%.1f km/h", r.getVelocidadViento()));
                fila.setForeground(ColoresUI.TEXTO_PRIMARIO);
            } else {
                fila.setText("• Sin datos");
                fila.setForeground(ColoresUI.TEXTO_SUAVE);
            }
            listaRecientes.add(fila);
        }
        listaRecientes.revalidate();
        listaRecientes.repaint();
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
        resumenObservaciones.setText(obs.isEmpty() ? "--" : obs);
    }

    private String envolverHtml(String texto) {
        return "<html><div style='text-align:center;'>" + texto + "</div></html>";
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
