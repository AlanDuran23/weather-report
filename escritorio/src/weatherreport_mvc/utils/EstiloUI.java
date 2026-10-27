package weatherreport_mvc.utils;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Componentes a medida para el tema oscuro premium: paneles redondeados con
 * sombra exterior difuminada y sutil, degradados vino/rubí, efecto neón (glow)
 * y botones con halo luminoso.
 */
public final class EstiloUI {

    private EstiloUI() {
    }

    /**
     * Panel con esquinas redondeadas, fondo mate o degradado, sombra exterior
     * amplia y difuminada, halo vino (glow) y trazo fino brillante.
     */
    public static class PanelRedondeado extends JPanel {

        private final int radio;
        private Color relleno;
        private Color borde;
        private Color glow;
        private int margenSombra;
        private Color[] coloresGrad;
        private float[] fracGrad;
        private boolean soloBordeInferior;

        public PanelRedondeado(int radio) {
            this(radio, null, null);
        }

        public PanelRedondeado(int radio, Color relleno) {
            this(radio, relleno, null);
        }

        public PanelRedondeado(int radio, Color relleno, Color borde) {
            this.radio = radio;
            this.relleno = relleno;
            this.borde = borde;
            setOpaque(false);
        }

        /** Activa un degradado diagonal superior-izquierda a inferior-derecha. */
        public void configurarDegradado(Color c1, Color c2) {
            this.coloresGrad = new Color[]{c1, c2};
            this.fracGrad = new float[]{0f, 1f};
            this.relleno = null;
        }

        /** Activa un degradado con varias paradas (permitido el destello rubí). */
        public void configurarDegradado(Color[] colores, float[] fracciones) {
            this.coloresGrad = colores;
            this.fracGrad = fracciones;
            this.relleno = null;
        }

        /** Halo exterior en el borde (efecto neón). requiere margenSombra > 0. */
        public void configurarGlow(Color glow) {
            this.glow = glow;
        }

        /** Espacio reservado alrededor para sombra/glow exterior. */
        public void configurarSombra(int margen) {
            this.margenSombra = margen;
        }

        public void setRelleno(Color relleno) {
            this.relleno = relleno;
        }

        /** Dibuja solo la línea inferior (cabeceras de módulo). */
        public void setSoloBordeInferior(boolean solo) {
            this.soloBordeInferior = solo;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int m = margenSombra;
            int x = m, y = m, w = getWidth() - 2 * m, h = getHeight() - 2 * m;
            if (w <= 0 || h <= 0) {
                g2.dispose();
                return;
            }

            // Sombra exterior amplia, difuminada y muy sutil
            if (m > 0) {
                for (int i = 1; i <= m; i++) {
                    int alpha = (int) (24.0 * (1.0 - ((double) (i - 1)) / m));
                    g2.setColor(new Color(0, 0, 0, alpha));
                    g2.setStroke(new BasicStroke(1.6f));
                    g2.drawRoundRect(x - i, y - i, w + 2 * i, h + 2 * i,
                            radio + i, radio + i);
                }
            }

            // Relleno mate o degradado vino/rubí
            if (coloresGrad != null) {
                Point2D p1 = new Point2D.Float(x, y);
                Point2D p2 = new Point2D.Float(x + w, y + h);
                g2.setPaint(new LinearGradientPaint(p1, p2, fracGrad, coloresGrad));
            } else if (relleno != null) {
                g2.setColor(relleno);
            }
            g2.fillRoundRect(x, y, w, h, radio, radio);

            // Halo vino (glow) por el borde exterior
            if (glow != null && m > 0) {
                for (int i = 1; i <= 2; i++) {
                    int alpha = 90 - (i - 1) * 40;
                    g2.setColor(new Color(glow.getRed(), glow.getGreen(),
                            glow.getBlue(), alpha));
                    g2.setStroke(new BasicStroke((3 - i) * 2f));
                    g2.drawRoundRect(x - i * 4, y - i * 4,
                            w + i * 8, h + i * 8, radio + i * 4, radio + i * 4);
                }
            }

            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int m = margenSombra;
            int x = m, y = m, w = getWidth() - 2 * m, h = getHeight() - 2 * m;
            if (w <= 0 || h <= 0) {
                g2.dispose();
                return;
            }

            if (soloBordeInferior && borde != null) {
                // Línea de separación fina y brillante en la base (cabeceras)
                int baseY = Math.max(0, y + h - 1);
                g2.setColor(new Color(borde.getRed(), borde.getGreen(),
                        borde.getBlue(), 160));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(x, baseY, x + w, baseY);
                if (glow != null) {
                    g2.setColor(new Color(glow.getRed(), glow.getGreen(),
                            glow.getBlue(), 70));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawLine(x - 1, baseY + 2, x + w + 1, baseY + 2);
                }
            } else if (borde != null) {
                g2.setColor(borde);
                g2.setStroke(new BasicStroke(1f));
                if (radio > 0) {
                    g2.drawRoundRect(x, y, w, h, radio, radio);
                } else {
                    g2.drawRect(x, y, w, h);
                }
            }
            g2.dispose();
        }
    }

    /**
     * Botón con relleno redondeado (mate o degradado), trazo fino brillante y
     * halo neón vino/rubí. Hover incluido.
     */
    public static class BotonGlow extends JButton {

        private Color relleno;
        private Color rellenoHover;
        private Color borde;
        private Color glow;
        private final int radio;
        private Color[] coloresGrad;
        private float[] fracGrad;
        private boolean hover;
        private boolean lineaInferior;
        private Color lineaColor;

        public BotonGlow(String texto) {
            this(texto, ColoresUI.SUPERFICIE_ELEVADA, ColoresUI.BORDE_CLARO,
                    null, 10);
        }

        public BotonGlow(String texto, Color relleno, Color borde, Color glow,
                int radio) {
            super(texto);
            this.relleno = relleno;
            this.borde = borde;
            this.glow = glow;
            this.radio = radio;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        public void setRelleno(Color relleno, Color rellenoHover) {
            this.relleno = relleno;
            this.rellenoHover = rellenoHover;
            this.coloresGrad = null;
        }

        public void configurarGradiente(Color c1, Color c2) {
            this.coloresGrad = new Color[]{c1, c2};
            this.fracGrad = new float[]{0f, 1f};
            this.relleno = null;
            this.rellenoHover = null;
        }

        public void setGlow(Color glow) {
            this.glow = glow;
            repaint();
        }

        public void setBorde(Color borde) {
            this.borde = borde;
        }

        public void setLineaInferior(boolean activa, Color color) {
            this.lineaInferior = activa;
            this.lineaColor = color;
        }

        public void configurarIcono(String nombre, int ancho, int alto) {
            ImageIcon icono = UIFactory.cargarIcono(nombre, ancho, alto);
            if (icono != null) {
                setIcon(icono);
                setHorizontalTextPosition(JButton.RIGHT);
                setIconTextGap(14);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (coloresGrad != null) {
                Point2D p1 = new Point2D.Float(0, 0);
                Point2D p2 = new Point2D.Float(w, h);
                g2.setPaint(new LinearGradientPaint(p1, p2, fracGrad, coloresGrad));
            } else {
                Color c = hover && rellenoHover != null ? rellenoHover : relleno;
                if (c != null) {
                    g2.setColor(c);
                }
            }
            if (radio > 0) {
                g2.fillRoundRect(0, 0, w, h, radio, radio);
            } else {
                g2.fillRect(0, 0, w, h);
            }
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 1;

            if (lineaInferior && lineaColor != null) {
                g2.setColor(lineaColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, h, w, h);
            }

            if (glow != null) {
                int base = hover ? 120 : 80;
                g2.setColor(new Color(glow.getRed(), glow.getGreen(),
                        glow.getBlue(), base));
                g2.setStroke(new BasicStroke(2f));
                if (radio > 0) {
                    g2.drawRoundRect(1, 1, w - 2, h - 2, radio, radio);
                } else {
                    g2.drawRect(1, 1, w - 2, h - 2);
                }
                g2.setColor(new Color(glow.getRed(), glow.getGreen(),
                        glow.getBlue(), hover ? 60 : 40));
                g2.setStroke(new BasicStroke(4f));
                if (radio > 0) {
                    g2.drawRoundRect(3, 3, w - 6, h - 6, Math.max(0, radio - 2),
                            Math.max(0, radio - 2));
                } else {
                    g2.drawRect(3, 3, w - 6, h - 6);
                }
            }

            if (borde != null && glow == null) {
                g2.setColor(borde);
                g2.setStroke(new BasicStroke(1f));
                if (radio > 0) {
                    g2.drawRoundRect(0, 0, w, h, radio, radio);
                } else {
                    g2.drawRect(0, 0, w, h);
                }
            }
            g2.dispose();
        }
    }

    /** Cabecera de módulo con degradado vino intenso -> rubí y línea inferior. */
    public static PanelRedondeado crearPanelDegradadoVino() {
        PanelRedondeado p = new PanelRedondeado(0, null, ColoresUI.DESTELLO);
        p.configurarDegradado(new Color[]{
            ColoresUI.VINO_OSCURO, ColoresUI.VINO, ColoresUI.RUBI_OSCURO
        }, new float[]{0f, 0.55f, 1f});
        p.setSoloBordeInferior(true);
        p.setLayout(new BorderLayout());
        return p;
    }

    /** Tarjeta oscura redondeada con sombra exterior sutil y listo para contenido. */
    public static PanelRedondeado crearPanelTarjeta() {
        PanelRedondeado p = new PanelRedondeado(14, ColoresUI.SUPERFICIE_ELEVADA,
                ColoresUI.BORDE);
        p.configurarSombra(6);
        p.setLayout(new BorderLayout());
        return p;
    }

    /** Tarjeta oscura que envuelve un contenido, con padding interior. */
    public static PanelRedondeado crearTarjeta(JComponent contenido, int padding) {
        PanelRedondeado p = crearPanelTarjeta();
        p.setBorder(BorderFactory.createEmptyBorder(padding, padding,
                padding, padding));
        p.add(contenido, BorderLayout.CENTER);
        return p;
    }

    /** Superficie oscura simple (fondo de módulo). */
    public static JPanel crearSuperficieFondo() {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(ColoresUI.FONDO_RAIZ);
        return p;
    }
}