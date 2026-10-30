package weatherreport_mvc.views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.Path2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import weatherreport_mvc.controllers.DashboardController;
import weatherreport_mvc.controllers.DashboardController.Resumen24h;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.models.RegistroMeteorologico;
import weatherreport_mvc.utils.AlertaUtil;
import weatherreport_mvc.utils.AlertaUtil.Nivel;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.Estadistica;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.UIFactory;

/**
 * Módulo 1 - Panel Principal (Dashboard). Responde de un vistazo: si hay
 * alerta, qué tan reciente es el dato y cómo se compara cada lectura con su
 * umbral y con las últimas 24 h.
 */
public class DashboardPanel extends JPanel {

    public static final String KEY = "dashboard";

    /** A partir de estas horas sin datos nuevos, la antigüedad se marca en ámbar. */
    private static final long HORAS_DATO_ANTIGUO = 24;

    // Escala tipográfica (Segoe UI): etiqueta / meta / dato / estado / lectura
    private static final Font FUENTE_ETIQUETA = conTracking(new Font("Segoe UI", Font.BOLD, 12), 0.06f);
    private static final Font FUENTE_META = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FUENTE_DATO = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FUENTE_ESTADO = new Font("Segoe UI", Font.BOLD, 17);
    private static final Font FUENTE_LECTURA = new Font("Segoe UI", Font.BOLD, 56);

    private final DashboardController controller;
    private final Consumer<String> navegador;
    private final boolean permiteIngreso;
    private final boolean permiteHistorico;
    /** Avisa a la ventana principal del estado vigente (color y texto del punto del menú). */
    private final BiConsumer<Color, String> oyenteEstado;

    private final FranjaEstado franja = new FranjaEstado();
    private final JLabel lblEstado = new JLabel("Cargando…");
    private final JLabel lblEstadoDetalle = new JLabel(" ");
    private final JLabel lblEstacion = new JLabel(" ");
    private final JLabel lblFrescura = new JLabel(" ");

    private final Tarjeta humedad = new Tarjeta("HUMEDAD RELATIVA", "%");
    private final Tarjeta viento = new Tarjeta("VELOCIDAD DEL VIENTO", "km/h");
    private final Tarjeta temperatura = new Tarjeta("TEMPERATURA EXTERIOR", "°C");

    public DashboardPanel(Consumer<String> navegador, boolean permiteIngreso, boolean permiteHistorico,
                          BiConsumer<Color, String> oyenteEstado) {
        super(new BorderLayout());
        this.navegador = navegador;
        this.permiteIngreso = permiteIngreso;
        this.permiteHistorico = permiteHistorico;
        this.oyenteEstado = oyenteEstado;
        this.controller = new DashboardController(this);
        setBackground(ColoresUI.FONDO_RAIZ);
        construirInterfaz();
        controller.iniciar();
    }

    private void construirInterfaz() {
        JPanel encabezado = EstiloUI.crearEncabezadoModulo("Panel Principal",
                "Última lectura de la estación comparada con sus umbrales y las últimas 24 h");

        JPanel tarjetas = new JPanel(new GridLayout(1, 3, 16, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(humedad.construir());
        tarjetas.add(viento.construir());
        tarjetas.add(temperatura.construir());

        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setBackground(ColoresUI.FONDO_RAIZ);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(20, 20, 4, 20));
        cuerpo.add(construirFranja(), BorderLayout.NORTH);
        cuerpo.add(tarjetas, BorderLayout.CENTER);

        // Un solo botón con el acento vino: la acción principal es capturar datos
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        acciones.setBackground(ColoresUI.FONDO_RAIZ);
        acciones.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JButton btnActualizar = UIFactory.crearBotonSecundario("Actualizar vista");
        btnActualizar.addActionListener(e -> controller.refrescar());
        acciones.add(btnActualizar);
        if (permiteIngreso) {
            JButton btnIngreso = UIFactory.crearBotonPrimario("Ingresar datos");
            btnIngreso.addActionListener(e -> navegador.accept(IngresoManualPanel.KEY));
            acciones.add(btnIngreso);
        }

        add(encabezado, BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }

    /** Franja superior: estado general a la izquierda, estación y antigüedad a la derecha. */
    private JPanel construirFranja() {
        lblEstado.setFont(FUENTE_ESTADO);
        lblEstado.setForeground(ColoresUI.TEXTO_PRIMARIO);
        lblEstadoDetalle.setFont(FUENTE_META);
        lblEstadoDetalle.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        lblEstacion.setFont(FUENTE_ETIQUETA);
        lblEstacion.setForeground(ColoresUI.TEXTO_SUAVE);
        lblFrescura.setFont(FUENTE_META);
        lblFrescura.setForeground(ColoresUI.TEXTO_SECUNDARIO);

        franja.setLayout(new BorderLayout(24, 0));
        franja.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 20));
        franja.add(columna(lblEstado, lblEstadoDetalle, LEFT_ALIGNMENT), BorderLayout.CENTER);
        franja.add(columna(lblEstacion, lblFrescura, RIGHT_ALIGNMENT), BorderLayout.EAST);
        return franja;
    }

    private static JPanel columna(JLabel arriba, JLabel abajo, float alineacion) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        arriba.setAlignmentX(alineacion);
        abajo.setAlignmentX(alineacion);
        p.add(arriba);
        p.add(Box.createVerticalStrut(4));
        p.add(abajo);
        return p;
    }

    /**
     * La base de datos no respondió. Se dice explícitamente (antes se mostraba
     * "Esperando datos" y el aviso de alerta se apagaba) y las tarjetas
     * conservan la última lectura obtenida, marcada como posiblemente vieja.
     */
    public void mostrarErrorLectura(String titulo, String detalle) {
        Color ambar = ColoresUI.ALERTA_ATENCION_OSC;
        lblEstado.setText(titulo);
        lblEstado.setForeground(ambar);
        lblEstadoDetalle.setText("Las lecturas mostradas pueden no estar al día. "
                + "Se reintenta cada 5 segundos.");
        lblEstadoDetalle.setToolTipText(detalle);
        lblFrescura.setForeground(ambar);
        franja.setAcento(ambar);
        oyenteEstado.accept(ambar, titulo);
    }

    public void mostrarDatos(RegistroMeteorologico ultimo, Resumen24h resHumedad,
                             Resumen24h resViento, Resumen24h resTemperatura,
                             ConfiguracionSistema cfg) {
        lblEstacion.setText(cfg.getNombreEstacion().toUpperCase());
        lblEstadoDetalle.setToolTipText(null);
        if (ultimo == null) {
            lblEstado.setText("Esperando datos de la estación");
            lblEstado.setForeground(ColoresUI.TEXTO_SECUNDARIO);
            lblEstadoDetalle.setText("Importe un archivo o capture un registro en Ingreso de Datos.");
            lblFrescura.setText("Sin registros");
            lblFrescura.setForeground(ColoresUI.TEXTO_SUAVE);
            franja.setAcento(ColoresUI.BORDE_CLARO);
            oyenteEstado.accept(null, null);
            humedad.vaciar();
            viento.vaciar();
            temperatura.vaciar();
            return;
        }

        double umbralHum = cfg.getUmbralHumedad();
        double umbralViento = cfg.getUmbralViento();
        double hum = ultimo.getHumedad();
        double vel = ultimo.getVelocidadViento();
        double temp = ultimo.getTempExterior();
        Nivel nivelHum = Estadistica.hay(hum) ? AlertaUtil.evaluarHumedad(hum, umbralHum) : null;
        Nivel nivelViento = Estadistica.hay(vel) ? AlertaUtil.evaluarViento(vel, umbralViento) : null;

        humedad.mostrar(hum, nivelHum, resHumedad, umbralHum, 0, 100,
                "Umbral de alerta: " + Estadistica.fmt("%.0f %%", umbralHum));
        double mayorViento = Math.max(Estadistica.hay(resViento.max()) ? resViento.max() : 0,
                Estadistica.hay(vel) ? vel : 0);
        viento.mostrar(vel, nivelViento, resViento, umbralViento, 0,
                Math.max(umbralViento * 1.6, mayorViento * 1.15),
                "Umbral de alerta: " + Estadistica.fmt("%.0f km/h", umbralViento));
        temperatura.mostrarRango(temp, resTemperatura);

        // Estado general: manda el peor nivel entre humedad y viento
        Nivel peor = peor(nivelHum, nivelViento);
        List<String> avisos = new ArrayList<>();
        if (nivelHum != null && nivelHum != Nivel.NORMAL) {
            avisos.add(aviso("Humedad", hum, " %", umbralHum, nivelHum, "riesgo de saturación"));
        }
        if (nivelViento != null && nivelViento != Nivel.NORMAL) {
            avisos.add(aviso("Viento", vel, " km/h", umbralViento, nivelViento, "tome precauciones"));
        }
        if (peor == Nivel.ALERTA) {
            lblEstado.setText("Alerta crítica");
        } else if (peor == Nivel.ATENCION) {
            lblEstado.setText("Atención");
        } else if (peor == Nivel.NORMAL) {
            lblEstado.setText("Condiciones normales");
        } else {
            lblEstado.setText("Sin datos de humedad ni viento");
        }
        Color colorEstado = peor == null ? ColoresUI.TEXTO_SECUNDARIO : peor.getColor();
        lblEstado.setForeground(colorEstado);
        franja.setAcento(peor == null ? ColoresUI.BORDE_CLARO : colorEstado);
        oyenteEstado.accept(peor == null ? null : colorEstado,
                peor == null ? null : "Estado actual: " + peor.getEtiqueta());
        lblEstadoDetalle.setText(avisos.isEmpty()
                ? "Humedad y viento por debajo de sus umbrales."
                : String.join("   ·   ", avisos));

        // Antigüedad del dato: las lecturas grandes pueden ser de hace días
        LocalDateTime fechaUltimo = LocalDateTime.of(ultimo.getFecha(), ultimo.getHora());
        long horas = Math.max(0, Duration.between(fechaUltimo, LocalDateTime.now()).toHours());
        String hace = horas < 1 ? "hace menos de 1 h"
                : horas < 48 ? "hace " + horas + " h" : "hace " + (horas / 24) + " días";
        boolean antiguo = horas >= HORAS_DATO_ANTIGUO;
        lblFrescura.setText((antiguo ? "Sin datos nuevos · último registro " : "Último registro ")
                + DateUtil.formatoFechaSeguro(ultimo.getFecha()) + " "
                + DateUtil.formatoHoraSeguro(ultimo.getHora()) + " · " + hace);
        lblFrescura.setForeground(antiguo ? ColoresUI.ALERTA_ATENCION_OSC : ColoresUI.TEXTO_SECUNDARIO);
    }

    private static String aviso(String variable, double valor, String unidad, double umbral,
                                Nivel nivel, String consecuencia) {
        String texto = variable + " " + Estadistica.fmt("%.1f", valor) + unidad
                + " sobre el umbral de " + Estadistica.fmt("%.0f", umbral) + unidad;
        return nivel == Nivel.ALERTA ? texto + ": " + consecuencia : texto;
    }

    private static Nivel peor(Nivel a, Nivel b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.ordinal() >= b.ordinal() ? a : b;
    }

    private static Font conTracking(Font f, float tracking) {
        return f.deriveFont(Map.of(TextAttribute.TRACKING, tracking));
    }

    public void refrescar() {
        controller.refrescar();
    }

    public void detener() {
        controller.detener();
    }

    /** Tarjeta de una variable: lectura, nivel, medidor contra el umbral, tendencia y resumen. */
    private final class Tarjeta {

        private final String nombre;
        private final String unidad;
        private final JLabel lblValor = new JLabel("--");
        private final Chip chip = new Chip();
        private final Medidor medidor = new Medidor();
        private final JLabel lblEscala = new JLabel(" ");
        private final Tendencia tendencia = new Tendencia();
        private final JLabel lblPromedio = new JLabel("--");
        private final JLabel lblMinimo = new JLabel("--");
        private final JLabel lblMaximo = new JLabel("--");

        Tarjeta(String nombre, String unidad) {
            this.nombre = nombre;
            this.unidad = unidad;
        }

        JPanel construir() {
            JLabel lblNombre = new JLabel(nombre);
            lblNombre.setFont(FUENTE_ETIQUETA);
            lblNombre.setForeground(ColoresUI.TEXTO_SECUNDARIO);
            JPanel cabecera = new JPanel(new BorderLayout());
            cabecera.setOpaque(false);
            cabecera.add(lblNombre, BorderLayout.WEST);
            cabecera.add(chip, BorderLayout.EAST);

            lblValor.setFont(FUENTE_LECTURA);
            lblValor.setForeground(ColoresUI.TEXTO_PRIMARIO);
            JLabel lblUnidad = new JLabel(unidad);
            lblUnidad.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            lblUnidad.setForeground(ColoresUI.TEXTO_SUAVE);
            JPanel lectura = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            lectura.setOpaque(false);
            lectura.add(lblValor);
            lectura.add(lblUnidad);

            lblEscala.setFont(FUENTE_META);
            lblEscala.setForeground(ColoresUI.TEXTO_SUAVE);

            JLabel lblTendencia = new JLabel("ÚLTIMOS " + DashboardController.REGISTROS_TENDENCIA + " REGISTROS");
            lblTendencia.setFont(FUENTE_ETIQUETA);
            lblTendencia.setForeground(ColoresUI.TEXTO_SUAVE);
            if (permiteHistorico) {
                // El rol Básico no entra al Histórico: sin cursor de enlace ni "Ver en Histórico"
                tendencia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                tendencia.setToolTipText("Ver en Histórico");
                tendencia.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        navegador.accept(HistoricoPanel.KEY);
                    }
                });
            }

            JPanel resumen = new JPanel(new GridLayout(1, 3, 12, 0));
            resumen.setOpaque(false);
            resumen.add(dato("Promedio 24 h", lblPromedio));
            resumen.add(dato("Mínimo 24 h", lblMinimo));
            resumen.add(dato("Máximo 24 h", lblMaximo));

            JPanel contenido = new JPanel(new GridBagLayout());
            contenido.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridy = 0;
            contenido.add(cabecera, c);
            c.gridy = 1;
            c.insets = new Insets(10, -6, 0, 0); // compensa el margen izquierdo del FlowLayout
            contenido.add(lectura, c);
            c.gridy = 2;
            c.insets = new Insets(18, 0, 0, 0);
            contenido.add(medidor, c);
            c.gridy = 3;
            c.insets = new Insets(8, 0, 0, 0);
            contenido.add(lblEscala, c);
            c.gridy = 4;
            c.insets = new Insets(28, 0, 0, 0);
            contenido.add(lblTendencia, c);
            c.gridy = 5;
            c.insets = new Insets(8, 0, 0, 0);
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1; // la tendencia ocupa el alto que sobre
            contenido.add(tendencia, c);
            c.gridy = 6;
            c.weighty = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(20, 0, 0, 0);
            contenido.add(resumen, c);

            return EstiloUI.crearTarjeta(contenido, 24);
        }

        private JPanel dato(String etiqueta, JLabel valor) {
            JLabel lbl = new JLabel(etiqueta);
            lbl.setFont(FUENTE_META);
            lbl.setForeground(ColoresUI.TEXTO_SUAVE);
            valor.setFont(FUENTE_DATO);
            valor.setForeground(ColoresUI.TEXTO_PRIMARIO);
            JPanel p = new JPanel(new BorderLayout(0, 2));
            p.setOpaque(false);
            p.add(lbl, BorderLayout.NORTH);
            p.add(valor, BorderLayout.CENTER);
            return p;
        }

        /** Variable con umbral (humedad, viento): la lectura toma el color de su nivel. */
        void mostrar(double valor, Nivel nivel, Resumen24h res, double umbral,
                     double escalaMin, double escalaMax, String textoEscala) {
            lblValor.setText(Estadistica.fmt("%.1f", valor));
            // En normal la lectura queda neutra: el color se reserva para lo que pide atención
            boolean resaltar = nivel != null && nivel != Nivel.NORMAL;
            Color color = resaltar ? nivel.getColor() : ColoresUI.TEXTO_SECUNDARIO;
            lblValor.setForeground(resaltar ? color
                    : nivel == null ? ColoresUI.TEXTO_SUAVE : ColoresUI.TEXTO_PRIMARIO);
            if (nivel == null) {
                chip.mostrar("Sin dato", ColoresUI.TEXTO_SUAVE);
            } else {
                chip.mostrar(nivel.getEtiqueta(), nivel.getColor());
            }
            medidor.configurar(escalaMin, escalaMax, valor, umbral, Double.NaN, Double.NaN, color);
            lblEscala.setText(textoEscala);
            tendencia.configurar(res.serie(), umbral, resaltar ? color : ColoresUI.TEXTO_PRIMARIO);
            mostrarResumen(res);
        }

        /** Variable sin umbral (temperatura): el medidor muestra dónde cae la lectura en su rango de 24 h. */
        void mostrarRango(double valor, Resumen24h res) {
            lblValor.setText(Estadistica.fmt("%.1f", valor));
            lblValor.setForeground(Estadistica.hay(valor) ? ColoresUI.TEXTO_PRIMARIO : ColoresUI.TEXTO_SUAVE);
            chip.ocultar();
            double min = res.min(), max = res.max();
            double bajo = Estadistica.hay(min) ? min : Estadistica.hay(valor) ? valor : 0;
            double alto = Estadistica.hay(max) ? max : Estadistica.hay(valor) ? valor : 40;
            if (Estadistica.hay(valor)) {
                bajo = Math.min(bajo, valor);
                alto = Math.max(alto, valor);
            }
            medidor.configurar(bajo - 4, alto + 4, valor, Double.NaN, min, max,
                    ColoresUI.TEXTO_PRIMARIO);
            lblEscala.setText(res.hayDatos()
                    ? "Rango 24 h: " + Estadistica.fmt("%.1f", min) + " – "
                    + Estadistica.fmt("%.1f", max) + " " + unidad
                    : "Sin datos en las últimas 24 h");
            tendencia.configurar(res.serie(), Double.NaN, ColoresUI.TEXTO_PRIMARIO);
            mostrarResumen(res);
        }

        private void mostrarResumen(Resumen24h res) {
            lblPromedio.setText(Estadistica.fmt("%.1f", res.promedio()) + " " + unidad);
            lblMinimo.setText(Estadistica.fmt("%.1f", res.min()) + " " + unidad);
            lblMaximo.setText(Estadistica.fmt("%.1f", res.max()) + " " + unidad);
        }

        void vaciar() {
            lblValor.setText("--");
            lblValor.setForeground(ColoresUI.TEXTO_SUAVE);
            chip.mostrar("Sin dato", ColoresUI.TEXTO_SUAVE);
            medidor.configurar(0, 1, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                    ColoresUI.TEXTO_SUAVE);
            lblEscala.setText(" ");
            tendencia.configurar(new double[0], Double.NaN, ColoresUI.TEXTO_SUAVE);
            lblPromedio.setText("--");
            lblMinimo.setText("--");
            lblMaximo.setText("--");
        }
    }

    /** Franja con fondo de tarjeta y una barra de acento a la izquierda con el color del estado. */
    private static final class FranjaEstado extends JPanel {

        private Color acento = ColoresUI.BORDE_CLARO;

        FranjaEstado() {
            setOpaque(false);
        }

        void setAcento(Color acento) {
            this.acento = acento;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(ColoresUI.SUPERFICIE_ELEVADA);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
            g2.setColor(ColoresUI.BORDE);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
            g2.setColor(acento);
            g2.fillRoundRect(0, 0, 6, h, 6, 6);
            g2.fillRect(3, 0, 3, h);
            g2.dispose();
        }
    }

    /** Etiqueta de nivel: punto de color y texto sobre un fondo tenue del mismo tono. */
    private static final class Chip extends JComponent {

        private String texto = "";
        private Color color = ColoresUI.TEXTO_SUAVE;

        Chip() {
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        void mostrar(String texto, Color color) {
            this.texto = texto;
            this.color = color;
            setVisible(true);
            revalidate();
            repaint();
        }

        /** Sin etiqueta pero conservando su alto, para que las lecturas de las tres tarjetas queden alineadas. */
        void ocultar() {
            mostrar("", ColoresUI.TEXTO_SUAVE);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(texto.isEmpty() ? 0 : fm.stringWidth(texto) + 34, 24);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (texto.isEmpty()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 34));
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(color);
            g2.fillOval(11, h / 2 - 4, 8, 8);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, 25, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /**
     * Medidor horizontal. Con umbral: relleno hasta la lectura y la zona por
     * encima del umbral sombreada. Con rango: el tramo mín–máx de 24 h y la
     * lectura marcada encima.
     */
    private static final class Medidor extends JComponent {

        private double min = 0, max = 1, valor = Double.NaN, umbral = Double.NaN;
        private double rangoMin = Double.NaN, rangoMax = Double.NaN;
        private Color color = ColoresUI.TEXTO_SECUNDARIO;

        void configurar(double min, double max, double valor, double umbral,
                        double rangoMin, double rangoMax, Color color) {
            this.min = min;
            this.max = max > min ? max : min + 1;
            this.valor = valor;
            this.umbral = umbral;
            this.rangoMin = rangoMin;
            this.rangoMax = rangoMax;
            this.color = color;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(120, 16);
        }

        private int x(double v, int ancho) {
            double t = (v - min) / (max - min);
            return 7 + (int) Math.round(Math.max(0, Math.min(1, t)) * (ancho - 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), cy = getHeight() / 2, alto = 6, y = cy - alto / 2;
            int inicio = 7, fin = w - 7;

            g2.setColor(ColoresUI.BORDE);
            g2.fillRoundRect(inicio, y, fin - inicio, alto, alto, alto);

            if (Estadistica.hay(umbral)) {
                // Zona por encima del umbral, tenue: dice "más allá de aquí es alerta"
                int xu = x(umbral, w);
                Color zona = ColoresUI.ALERTA_CRITICA_OSC;
                g2.setColor(new Color(zona.getRed(), zona.getGreen(), zona.getBlue(), 60));
                g2.fillRoundRect(xu, y, fin - xu, alto, alto, alto);
            }
            boolean conRango = Estadistica.hay(rangoMin) && Estadistica.hay(rangoMax);
            if (conRango) {
                int a = x(rangoMin, w), b = x(rangoMax, w);
                g2.setColor(ColoresUI.TEXTO_SUAVE);
                g2.fillRoundRect(a, y, Math.max(alto, b - a), alto, alto, alto);
            } else if (Estadistica.hay(valor)) {
                g2.setColor(color);
                g2.fillRoundRect(inicio, y, Math.max(alto, x(valor, w) - inicio), alto, alto, alto);
            }
            if (Estadistica.hay(umbral)) {
                int xu = x(umbral, w);
                g2.setColor(ColoresUI.TEXTO_SECUNDARIO);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(xu, cy - 7, xu, cy + 7);
            }
            if (Estadistica.hay(valor)) {
                int xv = x(valor, w);
                g2.setColor(ColoresUI.SUPERFICIE_ELEVADA);
                g2.fillOval(xv - 7, cy - 7, 14, 14);
                g2.setColor(conRango ? ColoresUI.TEXTO_PRIMARIO : color);
                g2.fillOval(xv - 5, cy - 5, 10, 10);
            }
            g2.dispose();
        }
    }

    /** Línea de tendencia de 24 h, con el umbral punteado y el último punto resaltado. */
    private static final class Tendencia extends JComponent {

        private double[] serie = new double[0];
        private double umbral = Double.NaN;
        private Color colorUltimo = ColoresUI.TEXTO_PRIMARIO;

        void configurar(double[] serie, double umbral, Color colorUltimo) {
            this.serie = serie;
            this.umbral = umbral;
            this.colorUltimo = colorUltimo;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(120, 64);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(60, 40);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), pad = 7;

            Estadistica e = new Estadistica();
            int puntos = 0;
            for (double v : serie) {
                e.agregar(v);
                if (Estadistica.hay(v)) {
                    puntos++;
                }
            }
            if (puntos < 2) {
                g2.setColor(ColoresUI.TEXTO_SUAVE);
                g2.setFont(FUENTE_META);
                FontMetrics fm = g2.getFontMetrics();
                String msg = "Sin datos suficientes";
                g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2 + fm.getAscent() / 2);
                g2.dispose();
                return;
            }
            double lo = e.min(), hi = e.max();
            // El umbral entra en la escala solo si está cerca; si no, aplanaría la línea
            boolean conUmbral = Estadistica.hay(umbral) && umbral <= hi * 1.25 && umbral >= lo * 0.75;
            if (conUmbral) {
                lo = Math.min(lo, umbral);
                hi = Math.max(hi, umbral);
            }
            if (hi - lo < 1e-9) {
                lo -= 1;
                hi += 1;
            }
            double margen = (hi - lo) * 0.1;
            lo -= margen;
            hi += margen;

            int n = serie.length;
            double paso = (double) (w - 2 * pad) / (n - 1);
            Path2D linea = new Path2D.Double();
            Path2D area = new Path2D.Double();
            boolean dentro = false;
            double primeroX = pad, ultimoX = pad, ultimoY = h / 2.0;
            for (int i = 0; i < n; i++) {
                double v = serie[i];
                if (!Estadistica.hay(v)) {
                    dentro = false; // el dato faltante corta la línea
                    continue;
                }
                double px = pad + i * paso;
                double py = pad + (hi - v) / (hi - lo) * (h - 2 * pad);
                if (!dentro) {
                    linea.moveTo(px, py);
                    if (area.getCurrentPoint() == null) {
                        area.moveTo(px, h);
                        primeroX = px;
                    }
                    dentro = true;
                } else {
                    linea.lineTo(px, py);
                }
                area.lineTo(px, py);
                ultimoX = px;
                ultimoY = py;
            }
            area.lineTo(ultimoX, h);
            area.lineTo(primeroX, h);
            area.closePath();

            Color base = ColoresUI.TEXTO_SECUNDARIO;
            g2.setPaint(new GradientPaint(0, 0, new Color(base.getRed(), base.getGreen(), base.getBlue(), 40),
                    0, h, new Color(base.getRed(), base.getGreen(), base.getBlue(), 0)));
            g2.fill(area);

            if (conUmbral) {
                int yu = (int) Math.round(pad + (hi - umbral) / (hi - lo) * (h - 2 * pad));
                g2.setColor(ColoresUI.TEXTO_SUAVE);
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10f, new float[]{4f, 4f}, 0f));
                g2.drawLine(pad, yu, w - pad, yu);
            }

            g2.setColor(base);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(linea);

            // Con pocos puntos se marca cada lectura para que se vea dónde hay dato real
            if (n <= 30) {
                for (int i = 0; i < n; i++) {
                    if (Estadistica.hay(serie[i])) {
                        int px = (int) Math.round(pad + i * paso);
                        int py = (int) Math.round(pad + (hi - serie[i]) / (hi - lo) * (h - 2 * pad));
                        g2.fillOval(px - 2, py - 2, 5, 5);
                    }
                }
            }

            g2.setColor(ColoresUI.SUPERFICIE_ELEVADA);
            g2.fillOval((int) ultimoX - 6, (int) ultimoY - 6, 12, 12);
            g2.setColor(colorUltimo);
            g2.fillOval((int) ultimoX - 4, (int) ultimoY - 4, 8, 8);
            g2.dispose();
        }
    }
}
