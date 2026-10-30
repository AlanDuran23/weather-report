package weatherreport_mvc.utils;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import weatherreport_mvc.utils.EstiloUI.BotonGlow;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxUI;


/**
 * Fábrica de componentes Swing con el tema oscuro premium (mate + vino/rubí).
 */
public final class UIFactory {

    public static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_NEGRITA = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FUENTE_INDICADOR = new Font("Segoe UI", Font.BOLD, 58);
    public static final Font FUENTE_MENU = new Font("Segoe UI Semibold", Font.PLAIN, 14);
    /** Título de cada módulo en su cabecera. */
    public static final Font FUENTE_ENCABEZADO = new Font("Segoe UI Semibold", Font.PLAIN, 22);
    /** Título de tarjeta o sección dentro de un módulo. */
    public static final Font FUENTE_SECCION = new Font("Segoe UI Semibold", Font.PLAIN, 16);
    /** Botones: semibold pesa menos que bold y sigue leyéndose como acción. */
    public static final Font FUENTE_BOTON = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    /** Etiqueta de grupo en mayúsculas con tracking (igual que el Panel Principal). */
    public static final Font FUENTE_ETIQUETA = new Font("Segoe UI", Font.BOLD, 11)
            .deriveFont(java.util.Map.of(java.awt.font.TextAttribute.TRACKING, 0.08f));
    /** Título de los slides de módulos: 18. */
    public static final Font FUENTE_TITULO_SLIDE = new Font("Segoe UI", Font.BOLD, 18);
    /** Texto de los slides de módulos: 15. */
    public static final Font FUENTE_TEXTO = new Font("Segoe UI", Font.PLAIN, 15);
    /** Texto destacado (negrita) de los slides: 15. */
    public static final Font FUENTE_TEXTO_NEGRITA = new Font("Segoe UI", Font.BOLD, 15);

    private UIFactory() {
    }

    /** Botón primario: vino plano. Uno por pantalla, para la acción principal. */
    public static JButton crearBotonPrimario(String texto) {
        BotonGlow b = new BotonGlow(texto, ColoresUI.ACCION, null, null, 8);
        b.setRelleno(ColoresUI.ACCION, ColoresUI.ACCION_HOVER);
        b.setRellenoPresionado(ColoresUI.ACCION_PRESIONADA);
        b.setFont(FUENTE_BOTON);
        b.setForeground(ColoresUI.BLANCO);
        return b;
    }

    /** Botón secundario: superficie con trazo fino. */
    public static JButton crearBotonSecundario(String texto) {
        BotonGlow b = new BotonGlow(texto, ColoresUI.SUPERFICIE_ELEVADA,
                ColoresUI.BORDE_CLARO, null, 8);
        b.setRelleno(ColoresUI.SUPERFICIE_ELEVADA, ColoresUI.HOVER);
        b.setRellenoPresionado(ColoresUI.SUPERFICIE);
        b.setFont(FUENTE_BOTON);
        b.setForeground(ColoresUI.TEXTO_PRIMARIO);
        return b;
    }

    /** Botón resaltado (acción secundaria importante): contorno vino, texto claro. */
    public static JButton crearBotonResaltado(String texto) {
        BotonGlow b = new BotonGlow(texto, ColoresUI.SUPERFICIE_ELEVADA,
                ColoresUI.ACCION, null, 8);
        b.setRelleno(ColoresUI.SUPERFICIE_ELEVADA, new java.awt.Color(0x33, 0x1A, 0x22));
        b.setRellenoPresionado(ColoresUI.SUPERFICIE);
        b.setForeground(ColoresUI.TEXTO_PRIMARIO);
        b.setFont(FUENTE_BOTON);
        return b;
    }

    /** Acción destructiva: contorno rojo y texto rojo; solo se llena al pasar el cursor. */
    public static JButton crearBotonPeligro(String texto) {
        java.awt.Color rojo = ColoresUI.ALERTA_CRITICA_OSC;
        BotonGlow b = new BotonGlow(texto, null,
                new java.awt.Color(rojo.getRed(), rojo.getGreen(), rojo.getBlue(), 110), null, 8);
        b.setRelleno(null, new java.awt.Color(rojo.getRed(), rojo.getGreen(), rojo.getBlue(), 36));
        b.setFont(FUENTE_BOTON);
        b.setForeground(rojo);
        return b;
    }

    /** Botón de texto, sin caja (enlaces dentro de un formulario). */
    public static JButton crearBotonTexto(String texto) {
        BotonGlow b = new BotonGlow(texto, null, null, null, 8);
        b.setRelleno(null, ColoresUI.HOVER);
        b.setFont(FUENTE_BOTON);
        b.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        b.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return b;
    }

    /** Contenedor de pestañas segmentadas (Diario/Semanal/..., variables). */
    public static JPanel crearGrupoSegmentado() {
        EstiloUI.PanelRedondeado grupo = new EstiloUI.PanelRedondeado(10,
                ColoresUI.CAMPO, ColoresUI.BORDE);
        grupo.setLayout(new javax.swing.BoxLayout(grupo, javax.swing.BoxLayout.X_AXIS));
        grupo.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        return grupo;
    }

    /** Pestaña de un grupo segmentado. */
    public static JButton crearPestania(String texto) {
        BotonGlow b = new BotonGlow(texto, null, null, null, 8);
        b.setFont(FUENTE_BOTON);
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        aplicarEstiloPestania(b, false);
        return b;
    }

    /** Pestaña activa: superficie elevada y texto claro; inactiva: transparente. */
    public static void aplicarEstiloPestania(JButton boton, boolean seleccionada) {
        if (!(boton instanceof BotonGlow b)) {
            return;
        }
        if (seleccionada) {
            b.setRelleno(ColoresUI.HOVER, ColoresUI.HOVER);
            b.setBorde(ColoresUI.BORDE_CLARO);
            b.setForeground(ColoresUI.TEXTO_PRIMARIO);
        } else {
            b.setRelleno(null, ColoresUI.SUPERFICIE);
            b.setBorde(null);
            b.setForeground(ColoresUI.TEXTO_SUAVE);
        }
        b.repaint();
    }

    public static JLabel crearLabelTitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FUENTE_SECCION);
        l.setForeground(ColoresUI.TEXTO_PRIMARIO);
        return l;
    }

    /** Etiqueta de grupo: mayúsculas pequeñas con tracking, en gris suave. */
    public static JLabel crearEtiquetaGrupo(String texto) {
        JLabel l = new JLabel(texto.toUpperCase());
        l.setFont(FUENTE_ETIQUETA);
        l.setForeground(ColoresUI.TEXTO_SUAVE);
        return l;
    }

    public static JLabel crearLabelSubtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FUENTE_SUBTITULO);
        l.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        return l;
    }

    public static JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FUENTE_TEXTO);
        l.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        return l;
    }

    public static JTextField crearTextField(int columnas) {
        JTextField t = new JTextField(columnas);
        t.setFont(FUENTE_TEXTO);
        estilizarCampo(t);
        return t;
    }

    /**
     * Campo de entrada hundido: fondo más oscuro que la tarjeta, esquinas
     * redondeadas y borde vino al tener el foco. Las áreas de texto (dentro
     * de un JScrollPane) quedan rectas; el borde lo lleva el scroll.
     */
    public static void estilizarCampo(JTextComponent campo) {
        if (campo.getFont() instanceof javax.swing.plaf.UIResource) {
            campo.setFont(FUENTE_TEXTO);
        }
        campo.setBackground(ColoresUI.CAMPO);
        campo.setForeground(ColoresUI.TEXTO_PRIMARIO);
        campo.setCaretColor(ColoresUI.TEXTO_PRIMARIO);
        campo.setSelectionColor(ColoresUI.SELECCION);
        campo.setSelectedTextColor(ColoresUI.BLANCO);
        campo.setDisabledTextColor(ColoresUI.TEXTO_SUAVE);
        if (campo instanceof JTextField) {
            if (campo instanceof javax.swing.JPasswordField) {
                campo.setUI(new javax.swing.plaf.basic.BasicPasswordFieldUI() {
                    @Override
                    protected void paintSafely(Graphics g) {
                        EstiloUI.pintarFondoCampo(g, getComponent());
                        super.paintSafely(g);
                    }
                });
            } else if (campo instanceof javax.swing.JFormattedTextField) {
                campo.setUI(new javax.swing.plaf.basic.BasicFormattedTextFieldUI() {
                    @Override
                    protected void paintSafely(Graphics g) {
                        EstiloUI.pintarFondoCampo(g, getComponent());
                        super.paintSafely(g);
                    }
                });
            } else {
                campo.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
                    @Override
                    protected void paintSafely(Graphics g) {
                        EstiloUI.pintarFondoCampo(g, getComponent());
                        super.paintSafely(g);
                    }
                });
            }
            // setUI restablece colores y bordes que sean UIResource; se fijan de nuevo
            campo.setOpaque(false);
            campo.setBackground(ColoresUI.CAMPO);
            campo.setForeground(ColoresUI.TEXTO_PRIMARIO);
            campo.setCaretColor(ColoresUI.TEXTO_PRIMARIO);
            campo.setBorder(new EstiloUI.BordeCampo(7, 10));
            campo.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    campo.repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    campo.repaint();
                }
            });
        } else {
            campo.setOpaque(true);
            campo.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }
    }

    /** Scroll que envuelve un área de texto: mismo fondo y borde que un campo. */
    public static JScrollPane crearScrollCampo(JTextComponent area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(ColoresUI.BORDE_CAMPO, 1));
        scroll.getViewport().setBackground(ColoresUI.CAMPO);
        estilizarScroll(scroll);
        return scroll;
    }

    /**
     * Barras de desplazamiento delgadas para el tema oscuro: sin flechas, pulgar
     * gris redondeado que se aclara al pasar el cursor.
     */
    public static void estilizarScroll(JScrollPane scroll) {
        // Las barras son transparentes: lo que se ve detrás es el fondo del scroll
        scroll.setBackground(scroll.getViewport().getBackground());
        for (javax.swing.JScrollBar barra : new javax.swing.JScrollBar[]{
                scroll.getVerticalScrollBar(), scroll.getHorizontalScrollBar()}) {
            barra.setUI(new BarraScrollUI());
            barra.setOpaque(false);
            barra.setUnitIncrement(16);
            boolean vertical = barra.getOrientation() == javax.swing.JScrollBar.VERTICAL;
            barra.setPreferredSize(vertical ? new Dimension(10, 0) : new Dimension(0, 10));
        }
        javax.swing.JPanel esquina = new javax.swing.JPanel();
        esquina.setBackground(scroll.getViewport().getBackground());
        scroll.setCorner(JScrollPane.LOWER_RIGHT_CORNER, esquina);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, esquina);
    }

    private static final class BarraScrollUI extends javax.swing.plaf.basic.BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            thumbColor = ColoresUI.BORDE_CLARO;
            trackColor = ColoresUI.SUPERFICIE;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return botonVacio();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return botonVacio();
        }

        private static JButton botonVacio() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected Dimension getMinimumThumbSize() {
            // Con miles de filas el pulgar quedaba de 8 px y era difícil de tomar
            return new Dimension(32, 32);
        }

        @Override
        protected void paintTrack(Graphics g, javax.swing.JComponent c, Rectangle r) {
            // Pista invisible: solo se ve el pulgar
        }

        @Override
        protected void paintThumb(Graphics g, javax.swing.JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() || isDragging ? ColoresUI.GRIS_OSCURO : ColoresUI.BORDE_CLARO);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
            g2.dispose();
        }
    }

    public static void estilizarSpinner(JSpinner spinner) {
    spinner.setOpaque(true);
    spinner.setBackground(ColoresUI.SUPERFICIE);
    spinner.setForeground(ColoresUI.TEXTO_PRIMARIO);
    spinner.setBorder(BorderFactory.createLineBorder(ColoresUI.BORDE, 1));

    if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
        editor.setOpaque(true);
        editor.setBackground(ColoresUI.SUPERFICIE);
        editor.setBorder(BorderFactory.createEmptyBorder());

        JTextField campo = editor.getTextField();
        campo.setOpaque(true);
        campo.setBackground(ColoresUI.SUPERFICIE);
        campo.setForeground(ColoresUI.TEXTO_PRIMARIO);
        campo.setCaretColor(ColoresUI.TEXTO_PRIMARIO);
        campo.setSelectionColor(ColoresUI.VINO_SELECCION);
        campo.setSelectedTextColor(ColoresUI.GRIS_OSCURO);
        campo.setBorder(BorderFactory.createMatteBorder(
                1, 1, 1, 1, ColoresUI.BORDE));
    }

    aplicarEstiloSpinnerRecursivo(spinner);
    spinner.revalidate();
    spinner.repaint();
}

private static void aplicarEstiloSpinnerRecursivo(java.awt.Component componente) {
    if (componente instanceof javax.swing.JComponent jc) {
        jc.setOpaque(true);
        jc.setBackground(ColoresUI.SUPERFICIE);
    }

    if (componente instanceof javax.swing.JButton b) {
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setForeground(ColoresUI.TEXTO_PRIMARIO);
        b.setBackground(ColoresUI.SUPERFICIE);
        b.setBorder(BorderFactory.createEmptyBorder());
    }

    if (componente instanceof javax.swing.plaf.basic.BasicArrowButton arrow) {
        arrow.setOpaque(true);
        arrow.setBackground(ColoresUI.SUPERFICIE);
        arrow.setForeground(ColoresUI.TEXTO_PRIMARIO);
        arrow.setBorder(BorderFactory.createEmptyBorder());
    }

    if (componente instanceof java.awt.Container contenedor) {
        for (java.awt.Component hijo : contenedor.getComponents()) {
            aplicarEstiloSpinnerRecursivo(hijo);
        }
    }
}

    @SuppressWarnings("rawtypes")
public static void estilizarCombo(JComboBox combo) {
    combo.setFont(FUENTE_TEXTO);
    combo.setEditable(false);
    combo.setForeground(ColoresUI.TEXTO_PRIMARIO);
    combo.setFocusable(false);

    combo.setUI(new BasicComboBoxUI() {
        @Override
        public void paint(Graphics g, javax.swing.JComponent c) {
            // Mismo fondo hundido y esquinas que los campos de texto
            EstiloUI.pintarFondoCampo(g, c);
            super.paint(g, c);
        }

        @Override
        protected javax.swing.plaf.basic.ComboPopup createPopup() {
            javax.swing.plaf.basic.BasicComboPopup popup =
                    new javax.swing.plaf.basic.BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createLineBorder(ColoresUI.BORDE_CLARO, 1));
            return popup;
        }

        @Override
        protected javax.swing.JButton createArrowButton() {
            // La flecha se dibuja: Segoe UI no trae el carácter ▾ y salía un cuadro vacío
            javax.swing.JButton arrow = new javax.swing.JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getForeground());
                    g2.setStroke(new java.awt.BasicStroke(1.6f,
                            java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2, cy = getHeight() / 2;
                    g2.drawPolyline(new int[]{cx - 4, cx, cx + 4},
                            new int[]{cy - 2, cy + 2, cy - 2}, 3);
                    g2.dispose();
                }
            };
            arrow.setPreferredSize(new Dimension(28, 28));
            arrow.setFocusable(false);
            arrow.setOpaque(false);
            arrow.setContentAreaFilled(false);
            arrow.setForeground(ColoresUI.TEXTO_SECUNDARIO);
            arrow.setBorder(BorderFactory.createEmptyBorder());
            return arrow;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // El fondo ya lo pinta paint(): aquí se deja transparente
        }
    });
    combo.setOpaque(false);
    combo.setBackground(ColoresUI.CAMPO);
    combo.setBorder(new EstiloUI.BordeCampo(3, 4));

    combo.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            lbl.setFont(FUENTE_TEXTO);
            lbl.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

            if (index == -1) {
                // Valor mostrado en el propio combo: transparente sobre el fondo redondeado
                lbl.setOpaque(false);
                lbl.setForeground(ColoresUI.TEXTO_PRIMARIO);
            } else if (isSelected) {
                lbl.setOpaque(true);
                lbl.setBackground(ColoresUI.HOVER);
                lbl.setForeground(ColoresUI.BLANCO);
            } else {
                lbl.setOpaque(true);
                lbl.setBackground(ColoresUI.SUPERFICIE_ELEVADA);
                lbl.setForeground(ColoresUI.TEXTO_SECUNDARIO);
            }

            return lbl;
        }
    });

    combo.revalidate();
    combo.repaint();
}

    public static void estilizarCheckBox(JCheckBox check) {
        check.setOpaque(true);
        check.setBackground(ColoresUI.SUPERFICIE);
        check.setForeground(ColoresUI.TEXTO_SECUNDARIO);
    }

    /** Tarjeta oscura redondeada con sombra exterior sutil. */
    public static JPanel crearTarjeta(JComponent contenido) {
        return EstiloUI.crearTarjeta(contenido, 14);
    }

    /**
     * Tabla de datos: texto alineado a la izquierda y cifras a la derecha con
     * la cabecera alineada igual, filas de 34 px, hover y selección vino
     * apagada. Las columnas numéricas se indican con {@link #columnasNumericas}.
     */
    public static JScrollPane estilizarTabla(JTable tabla) {
        tabla.setFont(FUENTE_TEXTO.deriveFont(14f));
        tabla.setRowHeight(34);
        tabla.setFillsViewportHeight(true);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setGridColor(ColoresUI.LINEA_TABLA);
        tabla.setBackground(ColoresUI.SUPERFICIE);
        tabla.setForeground(ColoresUI.TEXTO_PRIMARIO);
        tabla.setSelectionBackground(ColoresUI.SELECCION);
        tabla.setSelectionForeground(ColoresUI.BLANCO);
        tabla.setDefaultRenderer(Object.class, new CeldaRenderer());
        tabla.setDefaultRenderer(Number.class, new CeldaRenderer());

        java.awt.event.MouseAdapter hover = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                marcarFilaHover(tabla, tabla.rowAtPoint(e.getPoint()));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                marcarFilaHover(tabla, -1);
            }
        };
        tabla.addMouseMotionListener(hover);
        tabla.addMouseListener(hover);

        JTableHeader header = tabla.getTableHeader();
        header.setReorderingAllowed(false);
        header.setBackground(ColoresUI.SUPERFICIE);
        header.setOpaque(true);
        header.setDefaultRenderer(new CabeceraRenderer());

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(ColoresUI.SUPERFICIE);
        scroll.setBorder(BorderFactory.createLineBorder(ColoresUI.BORDE, 1));
        estilizarScroll(scroll);
        JPanel esquinaCabecera = new JPanel();
        esquinaCabecera.setBackground(ColoresUI.SUPERFICIE);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, esquinaCabecera);
        return scroll;
    }

    /** Marca qué columnas son cifras: se alinean a la derecha (celda y cabecera). */
    public static void columnasNumericas(JTable tabla, int... columnas) {
        java.util.Set<Integer> set = new java.util.HashSet<>();
        for (int c : columnas) {
            set.add(c);
        }
        tabla.putClientProperty("columnasNumericas", set);
        // Los renderers por columna pisan el default: se reponen los del tema
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(null);
        }
        tabla.repaint();
    }

    private static boolean esNumerica(JTable tabla, int columnaVista) {
        Object set = tabla.getClientProperty("columnasNumericas");
        int modelo = tabla.convertColumnIndexToModel(columnaVista);
        return set instanceof java.util.Set<?> s && s.contains(modelo);
    }

    private static void marcarFilaHover(JTable tabla, int fila) {
        Object previa = tabla.getClientProperty("filaHover");
        if (previa instanceof Integer p && p == fila) {
            return;
        }
        tabla.putClientProperty("filaHover", fila);
        tabla.repaint();
    }

    /** Celda con relleno lateral de 14 px, cifras tabulares a la derecha y hover. */
    public static class CeldaRenderer extends javax.swing.table.DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, false, row, column);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            boolean numerica = esNumerica(table, column);
            lbl.setHorizontalAlignment(numerica ? SwingConstants.RIGHT : SwingConstants.LEFT);
            Object hover = table.getClientProperty("filaHover");
            if (isSelected) {
                lbl.setBackground(table.getSelectionBackground());
                lbl.setForeground(table.getSelectionForeground());
            } else {
                lbl.setBackground(hover instanceof Integer h && h == row
                        ? ColoresUI.FILA_HOVER : table.getBackground());
                lbl.setForeground(table.getForeground());
            }
            return lbl;
        }
    }

    /** Cabecera de tabla: mayúsculas pequeñas en gris, alineada como su columna. */
    private static final class CabeceraRenderer extends javax.swing.table.DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value == null ? "" : value.toString().toUpperCase(),
                    false, false, row, column);
            lbl.setOpaque(true);
            lbl.setFont(FUENTE_ETIQUETA);
            lbl.setBackground(ColoresUI.SUPERFICIE);
            lbl.setForeground(ColoresUI.TEXTO_SUAVE);
            lbl.setHorizontalAlignment(esNumerica(table, column)
                    ? SwingConstants.RIGHT : SwingConstants.LEFT);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ColoresUI.BORDE_CLARO),
                    BorderFactory.createEmptyBorder(11, 14, 10, 14)));
            return lbl;
        }
    }

    /** Configura una tabla con un modelo no editable. */
    public static void configurarModeloNoEditable(JTable tabla, Object[] columnas) {
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla.setModel(modelo);
    }

    /** Carga un icono desde la carpeta de recursos. */
    public static ImageIcon cargarIcono(String nombreArchivo, int ancho, int alto) {
        try {
            String[] rutasPosibles = {
                "/icons/" + nombreArchivo,
                "/resources/icons/" + nombreArchivo,
                "src/resources/icons/" + nombreArchivo,
                "resources/icons/" + nombreArchivo
            };

            for (String ruta : rutasPosibles) {
                try {
                    ImageIcon icon = null;
                    if (ruta.startsWith("/")) {
                        java.net.URL url = UIFactory.class.getResource(ruta);
                        if (url != null) {
                            icon = new ImageIcon(url);
                        }
                    } else {
                        java.io.File archivo = new java.io.File(ruta);
                        if (archivo.exists()) {
                            icon = new ImageIcon(ruta);
                        }
                    }

                    if (icon != null && icon.getIconWidth() > 0) {
                        Image img = icon.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                        return new ImageIcon(img);
                    }
                } catch (Exception e) {
                    // Continuar con la siguiente ruta
                }
            }
        } catch (Exception e) {
            // Retornar null si no se puede cargar
        }
        return null;
    }

    /** Carga un icono preservando su proporción dentro de un alto máximo. */
    public static ImageIcon cargarIconoProporcional(String nombreArchivo,
                                                    int altoMaximo, int anchoMaximo) {
        try {
            String[] rutasPosibles = {
                "/icons_H_V_P/" + nombreArchivo,
                "/resources/icons_H_V_P/" + nombreArchivo,
                "src/resources/icons_H_V_P/" + nombreArchivo,
                "resources/icons_H_V_P/" + nombreArchivo
            };

            for (String ruta : rutasPosibles) {
                try {
                    ImageIcon icon = null;
                    if (ruta.startsWith("/")) {
                        java.net.URL url = UIFactory.class.getResource(ruta);
                        if (url != null) {
                            icon = new ImageIcon(url);
                        }
                    } else {
                        java.io.File archivo = new java.io.File(ruta);
                        if (archivo.exists()) {
                            icon = new ImageIcon(ruta);
                        }
                    }

                    if (icon != null && icon.getIconWidth() > 0) {
                        int origW = icon.getIconWidth();
                        int origH = icon.getIconHeight();
                        double escala = Math.min(
                                (double) altoMaximo / origH,
                                (double) anchoMaximo / origW);
                        int ancho = (int) Math.round(origW * escala);
                        int alto = (int) Math.round(origH * escala);
                        Image img = icon.getImage().getScaledInstance(
                                ancho, alto, Image.SCALE_SMOOTH);
                        return new ImageIcon(img);
                    }
                } catch (Exception e) {
                    // Continuar con la siguiente ruta
                }
            }
        } catch (Exception e) {
            // Retornar null si no se puede cargar
        }
        return null;
    }
}