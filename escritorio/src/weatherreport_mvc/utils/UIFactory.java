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
    public static final Font FUENTE_MENU = new Font("Segoe UI", Font.BOLD, 14);
    /** Título de los slides de módulos: 18. */
    public static final Font FUENTE_TITULO_SLIDE = new Font("Segoe UI", Font.BOLD, 18);
    /** Texto de los slides de módulos: 15. */
    public static final Font FUENTE_TEXTO = new Font("Segoe UI", Font.PLAIN, 15);
    /** Texto destacado (negrita) de los slides: 15. */
    public static final Font FUENTE_TEXTO_NEGRITA = new Font("Segoe UI", Font.BOLD, 15);

    private UIFactory() {
    }

    /** Botón del menú lateral: mate oscuro con hover vino y halo activo. */
    public static BotonGlow crearBotonLateral(String texto) {
        BotonGlow b = new BotonGlow(texto, ColoresUI.LATERAL, null, null, 0);
        b.setFont(FUENTE_MENU);
        b.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setPreferredSize(new Dimension(260, 46));
        b.setLineaInferior(true, ColoresUI.BORDE);
        b.setRelleno(ColoresUI.LATERAL, ColoresUI.VINO_OSCURO);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Botón primario con degradado vino neón y halo rubí. */
    public static JButton crearBotonPrimario(String texto) {
        BotonGlow b = new BotonGlow(texto, ColoresUI.VINO, null,
                ColoresUI.RUBI, 10);
        b.configurarGradiente(ColoresUI.VINO_OSCURO, ColoresUI.VINO_NEON);
        b.setFont(FUENTE_NEGRITA);
        b.setForeground(ColoresUI.BLANCO);
        return b;
    }

    /** Botón secundario: superficie elevada con trazo fino brillante. */
    public static JButton crearBotonSecundario(String texto) {
        BotonGlow b = new BotonGlow(texto, ColoresUI.SUPERFICIE_ELEVADA,
                ColoresUI.BORDE_CLARO, null, 10);
        b.setRelleno(ColoresUI.SUPERFICIE_ELEVADA, new java.awt.Color(0x2E, 0x2E, 0x38));
        b.setFont(FUENTE_NEGRITA);
        b.setForeground(ColoresUI.TEXTO_PRIMARIO);
        return b;
    }

    /** Botón resaltado: relleno vino con borde neón, glow rubí y texto claro. */
    public static JButton crearBotonResaltado(String texto) {
        BotonGlow b = new BotonGlow(texto, ColoresUI.VINO_OSCURO,
                ColoresUI.VINO_NEON, ColoresUI.RUBI, 10);
        b.configurarGradiente(new java.awt.Color(0x33, 0x12, 0x1C),
                new java.awt.Color(0x4A, 0x18, 0x28));
        b.setGlow(ColoresUI.RUBI);
        b.setForeground(ColoresUI.DESTELLO);
        b.setFont(FUENTE_NEGRITA);
        return b;
    }

    public static JButton crearBotonPeligro(String texto) {
        BotonGlow b = new BotonGlow(texto, ColoresUI.ALERTA_CRITICA_OSC, null,
                ColoresUI.ALERTA_CRITICA_OSC, 10);
        b.setRelleno(ColoresUI.ALERTA_CRITICA_OSC,
                new java.awt.Color(0xFF, 0x6B, 0x6B));
        b.setFont(FUENTE_NEGRITA);
        b.setForeground(ColoresUI.BLANCO);
        return b;
    }

    public static JLabel crearLabelTitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FUENTE_TITULO);
        l.setForeground(ColoresUI.TEXTO_VINO);
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

    /** Estilo oscuro para campos de entrada de texto. */
    public static void estilizarCampo(JTextComponent campo) {
        campo.setOpaque(true);
        campo.setBackground(ColoresUI.SUPERFICIE);
        campo.setForeground(ColoresUI.TEXTO_PRIMARIO);
        campo.setCaretColor(ColoresUI.TEXTO_PRIMARIO);
        campo.setSelectionColor(ColoresUI.VINO_SELECCION);
        campo.setSelectedTextColor(ColoresUI.BLANCO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(6, 8, 6, 8),
                BorderFactory.createLineBorder(ColoresUI.BORDE, 1)));
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
    combo.setOpaque(true);
    combo.setBackground(ColoresUI.SUPERFICIE);
    combo.setForeground(ColoresUI.TEXTO_PRIMARIO);
    combo.setFocusable(false);
    combo.setBorder(BorderFactory.createLineBorder(ColoresUI.BORDE, 1));

    combo.setUI(new BasicComboBoxUI() {
        @Override
        protected javax.swing.JButton createArrowButton() {
            javax.swing.JButton arrow = new javax.swing.JButton("▾");
            arrow.setFont(new Font("Segoe UI", Font.BOLD, 12));
            arrow.setFocusable(false);
            arrow.setOpaque(true);
            arrow.setContentAreaFilled(true);
            arrow.setBackground(ColoresUI.SUPERFICIE);
            arrow.setForeground(ColoresUI.TEXTO_PRIMARIO);
            arrow.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColoresUI.BORDE));
            return arrow;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(ColoresUI.SUPERFICIE);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    });

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
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

            if (index == -1) {
                lbl.setBackground(ColoresUI.SUPERFICIE);
                lbl.setForeground(ColoresUI.TEXTO_PRIMARIO);
            } else if (isSelected) {
                lbl.setBackground(ColoresUI.VINO_OSCURO);
                lbl.setForeground(ColoresUI.BLANCO);
            } else {
                lbl.setBackground(ColoresUI.SUPERFICIE);
                lbl.setForeground(ColoresUI.TEXTO_PRIMARIO);
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

    public static JScrollPane estilizarTabla(JTable tabla) {
        tabla.setFont(FUENTE_TEXTO);
        tabla.setRowHeight(28);
        tabla.setFillsViewportHeight(true);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(ColoresUI.BORDE);
        tabla.setBackground(ColoresUI.SUPERFICIE);
        tabla.setForeground(ColoresUI.TEXTO_PRIMARIO);
        tabla.setSelectionBackground(ColoresUI.VINO_SELECCION);
        tabla.setSelectionForeground(ColoresUI.BLANCO);
        JTableHeader header = tabla.getTableHeader();
        header.setFont(FUENTE_NEGRITA);
        header.setBackground(ColoresUI.FONDO_RAIZ);
        header.setForeground(ColoresUI.TEXTO_VINO);
        header.setOpaque(true);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(ColoresUI.SUPERFICIE);
        scroll.setBorder(BorderFactory.createLineBorder(ColoresUI.BORDE, 1));
        return scroll;
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

    /** Hover vino/glow para botones del panel lateral (integrado en BotonGlow). */
    public static void agregarHoverLateral(JButton b) {
        // El hover ya está integrado en BotonGlow. Sin acción adicional.
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