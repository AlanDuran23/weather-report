package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import weatherreport_mvc.controllers.MainController;
import weatherreport_mvc.models.Usuario;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.EstiloUI.BotonGlow;
import weatherreport_mvc.utils.EstiloUI.PanelRedondeado;
import weatherreport_mvc.utils.UIFactory;

/**
 * Ventana principal de la aplicación. Se abre maximizada y no redimensionable,
 * con un panel lateral de navegación y un panel central blanco que usa
 * estrictamente CardLayout para alternar entre módulos.
 */
public class MainView extends JFrame {

    private final MainController controller;
    private final Usuario usuario;

    private final CardLayout layout;
    private final JPanel contenedorCards;
    private final Map<String, JButton> botonesNavegacion = new LinkedHashMap<>();
    private final JLabel lblReloj = new JLabel();

    private DashboardPanel dashboard;
    private HistoricoPanel historico;
    private GraficasPanel graficas;
    private IngresoManualPanel ingreso;
    private final Map<String, JPanel> paneles = new HashMap<>();
    private final Timer reloj;

    public MainView(Usuario usuario) {
        super("Weather Report - Monitoreo Meteorológico");
        this.usuario = usuario;
        this.controller = new MainController(this, usuario);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        layout = new CardLayout();
        contenedorCards = new JPanel(layout);

        construirInterfaz();
        configurarReloj();
        reloj = new Timer(1000, e -> actualizarReloj());
        reloj.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                detenerModulos();
            }
        });

        controller.navegar(DashboardPanel.KEY);
        controller.iniciarDatosPrueba();
    }

    /** Impide minimizar la ventana principal (el botón queda visible pero inerte). */
    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_ICONIFIED) {
            return;
        }
        super.processWindowEvent(e);
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(ColoresUI.FONDO_RAIZ);

        raiz.add(construirLateral(), BorderLayout.WEST);
        raiz.add(construirCentral(), BorderLayout.CENTER);
        raiz.add(construirBarraEstado(), BorderLayout.SOUTH);

        setContentPane(raiz);
    }

    private JPanel construirLateral() {
        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setBackground(ColoresUI.LATERAL);
        lateral.setPreferredSize(new Dimension(280, 0));

        PanelRedondeado encabezado = EstiloUI.crearPanelDegradadoVino();
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));

        JLabel lblMarca = new JLabel("WEATHER REPORT");
        lblMarca.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblMarca.setForeground(ColoresUI.BLANCO);

        JLabel lblBienvenido = new JLabel("Usuario: " + usuario.getUsuario()
                + "  |  " + usuario.getRol());
        lblBienvenido.setFont(UIFactory.FUENTE_NORMAL);
        lblBienvenido.setForeground(ColoresUI.TEXTO_PRIMARIO);

        encabezado.add(lblMarca, BorderLayout.NORTH);
        encabezado.add(lblBienvenido, BorderLayout.SOUTH);

        JPanel botonera = new JPanel(new java.awt.GridLayout(0, 1, 0, 3));
        botonera.setBackground(ColoresUI.LATERAL);

        agregarBotonNavegacion(botonera, "Panel Principal", DashboardPanel.KEY, "Panel Principal.png");
        if (controller.permiteModulo(IngresoManualPanel.KEY)) {
            agregarBotonNavegacion(botonera, "Ingreso de Datos", IngresoManualPanel.KEY, "Ingreso Manual.png");
        }
        agregarBotonNavegacion(botonera, "Histórico", HistoricoPanel.KEY, "Historico.png");
        agregarBotonNavegacion(botonera, "Gráficas", GraficasPanel.KEY, "Gráficas.png");
        if (controller.permiteModulo(ReportesPanel.KEY)) {
            agregarBotonNavegacion(botonera, "Reportes", ReportesPanel.KEY, "Reportes.png");
        }
        if (controller.permiteModulo(UsuariosPanel.KEY)) {
            agregarBotonNavegacion(botonera, "Gestión de Usuarios", UsuariosPanel.KEY, "Gestion de usuarios.png");
        }
        if (controller.permiteModulo(RespaldoPanel.KEY)) {
            agregarBotonNavegacion(botonera, "Respaldo y Restauración", RespaldoPanel.KEY, "Respaldo y restauracion.png");
        }
        if (controller.permiteModulo(ConfiguracionPanel.KEY)) {
            agregarBotonNavegacion(botonera, "Información", ConfiguracionPanel.KEY, "Configuracion.png");
        }

        BotonGlow btnSalir = UIFactory.crearBotonLateral("Cerrar Sesión");
        btnSalir.setRelleno(ColoresUI.VINO_OSCURO, ColoresUI.ALERTA_CRITICA_OSC);
        btnSalir.setForeground(ColoresUI.TEXTO_PRIMARIO);
        btnSalir.setGlow(ColoresUI.RUBI);
        btnSalir.addActionListener(e -> cerrarSesion());

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(ColoresUI.LATERAL);
        pie.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        pie.add(btnSalir, BorderLayout.NORTH);

        lateral.add(encabezado, BorderLayout.NORTH);
        lateral.add(botonera, BorderLayout.CENTER);
        lateral.add(pie, BorderLayout.SOUTH);
        return lateral;
    }

    private void agregarBotonNavegacion(JPanel botonera, String texto, String clave) {
        agregarBotonNavegacion(botonera, texto, clave, "");
    }

    private void agregarBotonNavegacion(JPanel botonera, String texto, String clave, String archivoIcono) {
        BotonGlow boton = UIFactory.crearBotonLateral(texto);

        // Cargar icono si se proporciona
        if (!archivoIcono.isEmpty()) {
            boton.configurarIcono(archivoIcono, 24, 24);
        }

        boton.addActionListener(e -> controller.navegar(clave));
        botonera.add(boton);
        botonesNavegacion.put(clave, boton);
    }

    /** Construye solo el contenedor central; los módulos se crean al navegar. */
    private JPanel construirCentral() {
        contenedorCards.setBackground(ColoresUI.FONDO_RAIZ);
        return contenedorCards;
    }

    /** Devuelve el panel del módulo creándolo la primera vez que se navega. */
    private JPanel obtenerPanel(String clave) {
        JPanel existente = paneles.get(clave);
        if (existente != null) {
            return existente;
        }
        JPanel nuevo = crearPanel(clave);
        if (nuevo != null) {
            paneles.put(clave, nuevo);
            contenedorCards.add(nuevo, clave);
        }
        return nuevo;
    }

    private JPanel crearPanel(String clave) {
        if (DashboardPanel.KEY.equals(clave)) {
            dashboard = new DashboardPanel(c -> controller.navegar(c),
                    controller.permiteModulo(IngresoManualPanel.KEY));
            return dashboard;
        }
        if (IngresoManualPanel.KEY.equals(clave)) {
            ingreso = new IngresoManualPanel(c -> controller.navegar(c));
            return ingreso;
        }
        if (HistoricoPanel.KEY.equals(clave)) {
            historico = new HistoricoPanel(controller.getUsuario().esConsulta());
            return historico;
        }
        if (GraficasPanel.KEY.equals(clave)) {
            graficas = new GraficasPanel();
            return graficas;
        }
        if (ReportesPanel.KEY.equals(clave)) {
            return new ReportesPanel();
        }
        if (UsuariosPanel.KEY.equals(clave)) {
            return new UsuariosPanel(controller.getUsuario());
        }
        if (RespaldoPanel.KEY.equals(clave)) {
            return new RespaldoPanel();
        }
        if (ConfiguracionPanel.KEY.equals(clave)) {
            return new ConfiguracionPanel();
        }
        return null;
    }

    private JPanel construirBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(ColoresUI.SUPERFICIE);
        barra.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ColoresUI.BORDE));

        JLabel lblSesion = new JLabel("Sesión iniciada: " + usuario.getNombreCompleto()
                + " (" + usuario.getRol() + ")");
        lblSesion.setFont(UIFactory.FUENTE_NORMAL);
        lblSesion.setForeground(ColoresUI.TEXTO_SECUNDARIO);

        lblReloj.setFont(UIFactory.FUENTE_NORMAL);
        lblReloj.setForeground(ColoresUI.TEXTO_PRIMARIO);

        barra.add(lblSesion, BorderLayout.WEST);
        barra.add(lblReloj, BorderLayout.EAST);
        return barra;
    }

    private void configurarReloj() {
        actualizarReloj();
    }

    private void actualizarReloj() {
        lblReloj.setText(controller.getHoraActual());
    }

    private void detenerModulos() {
        if (dashboard != null) {
            dashboard.detener();
        }
        if (reloj != null) {
            reloj.stop();
        }
    }

    public void mostrarCard(String clave) {
        if (obtenerPanel(clave) == null) {
            return;
        }
        layout.show(contenedorCards, clave);
        resaltarBoton(clave);
        refrescarAlNavegar(clave);
    }

    /** Recarga los datos del módulo que se está mostrando. */
    private void refrescarAlNavegar(String clave) {
        switch (clave) {
            case HistoricoPanel.KEY -> historico.cargarTodos();
            case GraficasPanel.KEY -> graficas.cargar();
            case DashboardPanel.KEY -> dashboard.refrescar();
            case IngresoManualPanel.KEY -> ingreso.recargarRecientes();
            default -> {
            }
        }
    }

    private void resaltarBoton(String clave) {
        for (Map.Entry<String, JButton> e : botonesNavegacion.entrySet()) {
            JButton b = e.getValue();
            if (b instanceof BotonGlow glow) {
                glow.setRelleno(ColoresUI.LATERAL, ColoresUI.VINO_OSCURO);
                glow.setGlow(null);
                glow.setForeground(ColoresUI.TEXTO_SECUNDARIO);
                glow.repaint();
            }
        }
        JButton activo = botonesNavegacion.get(clave);
        if (activo instanceof BotonGlow glow) {
            glow.configurarGradiente(ColoresUI.VINO_OSCURO, ColoresUI.VINO_NEON);
            glow.setGlow(ColoresUI.RUBI);
            glow.setForeground(ColoresUI.BLANCO);
            glow.repaint();
        }
    }

    public void refrescarModulos() {
        if (dashboard != null) {
            dashboard.refrescar();
        }
        if (historico != null) {
            historico.cargarTodos();
        }
        if (graficas != null) {
            graficas.cargar();
        }
    }

    private void cerrarSesion() {
        detenerModulos();
        dispose();
        new LoginView().setVisible(true);
    }

    public void abrirLogin() {
        cerrarSesion();
    }
}
