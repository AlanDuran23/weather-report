package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import weatherreport_mvc.controllers.DashboardController;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.models.RegistroMeteorologico;
import weatherreport_mvc.utils.Estadistica;
import weatherreport_mvc.utils.AlertaUtil;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.EstiloUI.PanelRedondeado;
import weatherreport_mvc.utils.UIFactory;

/**
 * Módulo 1 - Panel Principal (Dashboard). Vista por defecto con
 * indicadores grandes de humedad y viento y alertas dinámicas.
 */
public class DashboardPanel extends JPanel {

    public static final String KEY = "dashboard";

    private final DashboardController controller;
    private final Consumer<String> navegador;
    private final boolean permiteIngreso;

    private final JLabel lblValorHumedad = new JLabel("--");
    private final JLabel lblValorViento = new JLabel("--");
    private final JLabel lblValorTemperatura = new JLabel("--");
    private final JLabel lblPromedioHumedad = new JLabel("");
    private final JLabel lblPromedioViento = new JLabel("");
    private final JLabel lblPromedioTemperatura = new JLabel("");
    private final JLabel lblFechaUltimo = new JLabel("Sin registros");
    private final JLabel lblAlertaHumedad = new JLabel(" ");
    private final JLabel lblAlertaViento = new JLabel(" ");

    private final JLabel[] filasHumedad = new JLabel[6];
    private final JLabel[] filasViento = new JLabel[6];
    private final JLabel[] filasTemperatura = new JLabel[6];

    public DashboardPanel(Consumer<String> navegador, boolean permiteIngreso) {
        super(new BorderLayout());
        this.navegador = navegador;
        this.permiteIngreso = permiteIngreso;
        this.controller = new DashboardController(this);
        setBackground(ColoresUI.FONDO_RAIZ);
        construirInterfaz();
        controller.iniciar();
    }

    private void construirInterfaz() {
        PanelRedondeado encabezado = EstiloUI.crearPanelDegradadoVino();
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));
        
        JLabel lblTitulo = new JLabel("Panel Principal");
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);
        lblTitulo.setForeground(ColoresUI.BLANCO);
        
        encabezado.add(lblTitulo, BorderLayout.NORTH);

        JPanel indicadores = new JPanel(new GridLayout(1, 3, 14, 0));
        indicadores.setBackground(ColoresUI.FONDO_RAIZ);
        indicadores.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));

        indicadores.add(construirTarjetaIndicador(
                "Humedad Relativa", lblValorHumedad, " %", lblPromedioHumedad,
                lblAlertaHumedad, filasHumedad, "Humedad_Relativa.png"));
        indicadores.add(construirTarjetaIndicador(
                "Velocidad del Viento", lblValorViento, " km/h", lblPromedioViento,
                lblAlertaViento, filasViento, "Velocidad_del_Viento.png"));
        indicadores.add(construirTarjetaTemperatura());

        JPanel panelAlerta = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelAlerta.setBackground(ColoresUI.FONDO_RAIZ);
        JButton btnActualizar = UIFactory.crearBotonPrimario("Actualizar vista");
        btnActualizar.addActionListener(e -> controller.refrescar());
        panelAlerta.add(btnActualizar);

        if (permiteIngreso) {
            JButton btnActualizarDatos = UIFactory.crearBotonSecundario("Actualizar datos");
            btnActualizarDatos.addActionListener(e -> navegador.accept(IngresoManualPanel.KEY));
            panelAlerta.add(btnActualizarDatos);
        }

        add(encabezado, BorderLayout.NORTH);
        add(indicadores, BorderLayout.CENTER);
        add(panelAlerta, BorderLayout.SOUTH);
    }

    private JPanel construirTarjetaIndicador(String titulo, JLabel valor,
                                             String unidad, JLabel promedio,
                                             JLabel alerta, JLabel[] filas,
                                             String icono) {
        JPanel tarjeta = EstiloUI.crearPanelTarjeta();
        tarjeta.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JLabel lblTitulo = UIFactory.crearLabelTitulo(titulo);
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);

        valor.setFont(UIFactory.FUENTE_INDICADOR);
        valor.setForeground(ColoresUI.TEXTO_PRIMARIO);
        valor.setHorizontalAlignment(JLabel.CENTER);

        JLabel lblUnidad = UIFactory.crearLabel(unidad);
        lblUnidad.setFont(UIFactory.FUENTE_TEXTO);
        lblUnidad.setForeground(ColoresUI.TEXTO_SUAVE);
        lblUnidad.setHorizontalAlignment(JLabel.CENTER);

        JPanel valorYUnidad = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        valorYUnidad.setOpaque(false);
        valorYUnidad.add(valor);
        valorYUnidad.add(lblUnidad);

        JLabel lblIcono = crearEtiquetaIcono(icono);

        promedio.setFont(UIFactory.FUENTE_TEXTO);
        promedio.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        promedio.setHorizontalAlignment(JLabel.CENTER);

        alerta.setFont(UIFactory.FUENTE_TEXTO_NEGRITA);
        alerta.setHorizontalAlignment(JLabel.CENTER);
        alerta.setForeground(ColoresUI.ALERTA_NORMAL);

        JPanel centro = new JPanel(new BorderLayout(0, 6));
        centro.setOpaque(false);
        centro.add(valorYUnidad, BorderLayout.NORTH);
        centro.add(lblIcono, BorderLayout.CENTER);

        tarjeta.add(construirEncabezadoTarjeta(lblTitulo), BorderLayout.NORTH);
        tarjeta.add(centro, BorderLayout.CENTER);
        JPanel inferior = new JPanel(new GridLayout(2, 1, 0, 4));
        inferior.setOpaque(false);
        inferior.add(promedio);
        inferior.add(alerta);
        JPanel sur = new JPanel(new BorderLayout(0, 10));
        sur.setOpaque(false);
        sur.add(crearEtiquetaUltimos(), BorderLayout.NORTH);
        sur.add(construirListaReciente(filas), BorderLayout.CENTER);
        sur.add(inferior, BorderLayout.SOUTH);
        tarjeta.add(sur, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JLabel crearEtiquetaIcono(String nombreArchivo) {
        JLabel icono = new JLabel(" ");
        icono.setHorizontalAlignment(JLabel.CENTER);
        ImageIcon img = UIFactory.cargarIconoProporcional(nombreArchivo, 154, 315);
        if (img != null) {
            icono.setIcon(img);
            icono.setPreferredSize(new Dimension(img.getIconWidth(), img.getIconHeight()));
        } else {
            icono.setPreferredSize(new Dimension(64, 28));
        }
        return icono;
    }

    private JPanel construirEncabezadoTarjeta(JLabel lblTitulo) {
        lblTitulo.setHorizontalAlignment(JLabel.CENTER);
        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        JLabel espacio = new JLabel(" ");
        espacio.setPreferredSize(new Dimension(1, 16));
        superior.add(espacio, BorderLayout.NORTH);
        superior.add(lblTitulo, BorderLayout.CENTER);
        return superior;
    }

    private JPanel construirTarjetaTemperatura() {
        JPanel tarjeta = EstiloUI.crearPanelTarjeta();
        tarjeta.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JLabel lblTitulo = UIFactory.crearLabelTitulo("Temperatura Exterior");
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);

        lblValorTemperatura.setFont(UIFactory.FUENTE_INDICADOR);
        lblValorTemperatura.setForeground(ColoresUI.ALERTA_ATENCION);
        lblValorTemperatura.setHorizontalAlignment(JLabel.CENTER);

        JLabel lblUnidad = UIFactory.crearLabel(" °C");
        lblUnidad.setFont(UIFactory.FUENTE_TEXTO);
        lblUnidad.setForeground(ColoresUI.TEXTO_SUAVE);
        lblUnidad.setHorizontalAlignment(JLabel.CENTER);

        JPanel valorYUnidad = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        valorYUnidad.setOpaque(false);
        valorYUnidad.add(lblValorTemperatura);
        valorYUnidad.add(lblUnidad);

        lblPromedioTemperatura.setFont(UIFactory.FUENTE_TEXTO);
        lblPromedioTemperatura.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        lblPromedioTemperatura.setHorizontalAlignment(JLabel.CENTER);

        lblFechaUltimo.setFont(UIFactory.FUENTE_TEXTO);
        lblFechaUltimo.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        lblFechaUltimo.setHorizontalAlignment(JLabel.CENTER);

        JPanel centro = new JPanel(new BorderLayout(0, 6));
        centro.setOpaque(false);
        centro.add(valorYUnidad, BorderLayout.NORTH);
        centro.add(crearEtiquetaIcono("Temperatura_Exterior.png"), BorderLayout.CENTER);

        tarjeta.add(construirEncabezadoTarjeta(lblTitulo), BorderLayout.NORTH);
        tarjeta.add(centro, BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout(0, 10));
        sur.setOpaque(false);
        sur.add(crearEtiquetaUltimos(), BorderLayout.NORTH);
        sur.add(construirListaReciente(filasTemperatura), BorderLayout.CENTER);
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 4));
        info.setOpaque(false);
        info.add(lblPromedioTemperatura);
        info.add(lblFechaUltimo);
        sur.add(info, BorderLayout.SOUTH);
        tarjeta.add(sur, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JLabel crearEtiquetaUltimos() {
        JLabel lbl = new JLabel("Últimos registros");
        lbl.setFont(UIFactory.FUENTE_TEXTO_NEGRITA);
        lbl.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        lbl.setHorizontalAlignment(JLabel.CENTER);
        return lbl;
    }

    private JPanel construirListaReciente(JLabel[] filas) {
        JPanel lista = new JPanel(new GridLayout(6, 1, 0, 4));
        lista.setOpaque(false);
        for (int i = 0; i < 6; i++) {
            filas[i] = new JLabel("• Sin datos");
            filas[i].setFont(UIFactory.FUENTE_TEXTO.deriveFont(15f));
            filas[i].setForeground(ColoresUI.TEXTO_SUAVE);
            filas[i].setHorizontalAlignment(JLabel.CENTER);
            filas[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            filas[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    navegador.accept(HistoricoPanel.KEY);
                }
            });
            lista.add(filas[i]);
        }
        return lista;
    }

    public void mostrarDatos(RegistroMeteorologico ultimo, double promedioHumedad,
                             double promedioViento, double promedioTemperatura,
                             boolean hayDatos24h, ConfiguracionSistema cfg) {
        if (ultimo == null) {
            lblValorHumedad.setText("--");
            lblValorViento.setText("--");
            lblValorTemperatura.setText("--");
            lblPromedioHumedad.setText("Promedio 24h: --");
            lblPromedioViento.setText("Promedio 24h: --");
            lblPromedioTemperatura.setText("Promedio 24h: --");
            lblFechaUltimo.setText("Sin registros");
            lblAlertaHumedad.setText("Esperando datos del sensor...");
            lblAlertaHumedad.setForeground(ColoresUI.TEXTO_SUAVE);
            lblAlertaViento.setText("");
            return;
        }

        lblValorHumedad.setText(Estadistica.fmt("%.1f", ultimo.getHumedad()));
        lblValorViento.setText(Estadistica.fmt("%.1f", ultimo.getVelocidadViento()));
        lblValorTemperatura.setText(Estadistica.fmt("%.1f", ultimo.getTempExterior()));

        lblPromedioHumedad.setText("Promedio 24h: "
                + (hayDatos24h ? Estadistica.fmt("%.1f %%", promedioHumedad) : "--"));
        lblPromedioViento.setText("Promedio 24h: "
                + (hayDatos24h ? Estadistica.fmt("%.1f km/h", promedioViento) : "--"));
        lblPromedioTemperatura.setText("Promedio 24h: "
                + (hayDatos24h ? Estadistica.fmt("%.1f °C", promedioTemperatura) : "--"));
        lblFechaUltimo.setText("Último registro: "
                + DateUtil.formatoFechaSeguro(ultimo.getFecha())
                + " " + DateUtil.formatoHoraSeguro(ultimo.getHora()));

        AlertaUtil.Nivel nivelHum = AlertaUtil.evaluarHumedad(
                ultimo.getHumedad(), cfg.getUmbralHumedad());
        AlertaUtil.Nivel nivelViento = AlertaUtil.evaluarViento(
                ultimo.getVelocidadViento(), cfg.getUmbralViento());

        lblAlertaHumedad.setText(AlertaUtil.mensajeHumedad(
                ultimo.getHumedad(), cfg.getUmbralHumedad()));
        lblAlertaHumedad.setForeground(nivelHum.getColor());
        lblAlertaViento.setText(AlertaUtil.mensajeViento(
                ultimo.getVelocidadViento(), cfg.getUmbralViento()));
        lblAlertaViento.setForeground(nivelViento.getColor());
    }

    public void mostrarRecientes(List<RegistroMeteorologico> recientes) {
        for (int i = 0; i < 6; i++) {
            if (i < recientes.size()) {
                RegistroMeteorologico r = recientes.get(i);
                filasHumedad[i].setText("• " + Estadistica.fmt("%.1f %%", r.getHumedad()));
                filasHumedad[i].setForeground(ColoresUI.TEXTO_PRIMARIO);
                filasViento[i].setText("• "
                        + Estadistica.fmt("%.1f km/h", r.getVelocidadViento()));
                filasViento[i].setForeground(ColoresUI.TEXTO_PRIMARIO);
                filasTemperatura[i].setText("• "
                        + Estadistica.fmt("%.1f °C", r.getTempExterior()));
                filasTemperatura[i].setForeground(ColoresUI.TEXTO_PRIMARIO);
            } else {
                filasHumedad[i].setText("• Sin datos");
                filasHumedad[i].setForeground(ColoresUI.TEXTO_SUAVE);
                filasViento[i].setText("• Sin datos");
                filasViento[i].setForeground(ColoresUI.TEXTO_SUAVE);
                filasTemperatura[i].setText("• Sin datos");
                filasTemperatura[i].setForeground(ColoresUI.TEXTO_SUAVE);
            }
        }
    }

    public void refrescar() {
        controller.refrescar();
    }

    public void detener() {
        controller.detener();
    }
}
