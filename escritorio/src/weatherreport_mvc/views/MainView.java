package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import weatherreport_mvc.controllers.MainController;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.models.Usuario;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.EstiloUI.BotonNavegacion;
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
    private final JLabel lblEstacion = new JLabel();

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

    /**
     * Menú lateral sobre el mismo fondo que el contenido (separado por una
     * línea): marca arriba, módulos agrupados y la sesión abajo.
     */
    private JPanel construirLateral() {
        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setBackground(ColoresUI.FONDO_RAIZ);
        lateral.setPreferredSize(new Dimension(256, 0));
        lateral.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColoresUI.BORDE));

        JLabel lblMarca = new JLabel("Weather Report");
        lblMarca.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
        lblMarca.setForeground(ColoresUI.TEXTO_PRIMARIO);
        ImageIcon logo = cargarLogo(28);
        if (logo != null) {
            lblMarca.setIcon(logo);
            lblMarca.setIconTextGap(10);
        }
        JLabel lblLema = new JLabel("Monitoreo meteorológico");
        lblLema.setFont(UIFactory.FUENTE_NORMAL);
        lblLema.setForeground(ColoresUI.TEXTO_SUAVE);
        lblLema.setBorder(BorderFactory.createEmptyBorder(0, logo != null ? 38 : 0, 0, 0));

        JPanel encabezado = new JPanel(new BorderLayout(0, 2));
        encabezado.setOpaque(false);
        encabezado.setBorder(BorderFactory.createEmptyBorder(20, 20, 18, 16));
        encabezado.add(lblMarca, BorderLayout.NORTH);
        encabezado.add(lblLema, BorderLayout.SOUTH);

        JPanel botonera = new JPanel();
        botonera.setOpaque(false);
        botonera.setLayout(new BoxLayout(botonera, BoxLayout.Y_AXIS));
        botonera.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        agregarGrupo(botonera, "Monitoreo");
        agregarBotonNavegacion(botonera, "Panel Principal", DashboardPanel.KEY, "Panel Principal.png");
        if (controller.permiteModulo(IngresoManualPanel.KEY)) {
            agregarBotonNavegacion(botonera, "Ingreso de Datos", IngresoManualPanel.KEY, "Ingreso Manual.png");
        }
        if (controller.permiteModulo(HistoricoPanel.KEY)) {
            agregarBotonNavegacion(botonera, "Histórico", HistoricoPanel.KEY, "Historico.png");
        }
        if (controller.permiteModulo(GraficasPanel.KEY)) {
            agregarBotonNavegacion(botonera, "Gráficas", GraficasPanel.KEY, "Gráficas.png");
        }
        if (controller.permiteModulo(ReportesPanel.KEY)) {
            // Consulta ve la vista "Informe" del mismo módulo
            agregarBotonNavegacion(botonera, controller.getUsuario().esConsulta() ? "Informe" : "Reportes",
                    ReportesPanel.KEY, "Reportes.png");
        }
        boolean hayAdministracion = controller.permiteModulo(UsuariosPanel.KEY)
                || controller.permiteModulo(RespaldoPanel.KEY)
                || controller.permiteModulo(ConfiguracionPanel.KEY);
        if (hayAdministracion) {
            agregarGrupo(botonera, "Administración");
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

        lateral.add(encabezado, BorderLayout.NORTH);
        lateral.add(botonera, BorderLayout.CENTER);
        lateral.add(construirSesion(), BorderLayout.SOUTH);
        return lateral;
    }

    /** Bloque de sesión al pie del menú: quién está de guardia y salida. */
    private JPanel construirSesion() {
        JLabel lblNombre = new JLabel(usuario.getNombreCompleto());
        lblNombre.setFont(UIFactory.FUENTE_BOTON);
        lblNombre.setForeground(ColoresUI.TEXTO_PRIMARIO);
        JLabel lblRol = new JLabel(usuario.getRol() + " · " + usuario.getUsuario());
        lblRol.setFont(UIFactory.FUENTE_NORMAL.deriveFont(12f));
        lblRol.setForeground(ColoresUI.TEXTO_SUAVE);

        JPanel datos = new JPanel(new BorderLayout(0, 1));
        datos.setOpaque(false);
        datos.add(lblNombre, BorderLayout.NORTH);
        datos.add(lblRol, BorderLayout.SOUTH);

        JButton btnSalir = UIFactory.crearBotonSecundario("Cerrar sesión");
        btnSalir.addActionListener(e -> cerrarSesion());

        JPanel sesion = new JPanel(new BorderLayout(0, 12));
        sesion.setOpaque(false);
        sesion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ColoresUI.BORDE),
                BorderFactory.createEmptyBorder(14, 20, 16, 20)));
        sesion.add(datos, BorderLayout.NORTH);
        sesion.add(btnSalir, BorderLayout.SOUTH);
        return sesion;
    }

    private static ImageIcon cargarLogo(int lado) {
        java.net.URL url = MainView.class.getResource("/logo_weather_report.png");
        if (url == null) {
            url = MainView.class.getResource("/resources/logo_weather_report.png");
        }
        if (url == null) {
            return null;
        }
        ImageIcon icon = new ImageIcon(url);
        return new ImageIcon(icon.getImage().getScaledInstance(lado, lado, java.awt.Image.SCALE_SMOOTH));
    }

    private static void agregarGrupo(JPanel botonera, String titulo) {
        JLabel lbl = UIFactory.crearEtiquetaGrupo(titulo);
        lbl.setBorder(BorderFactory.createEmptyBorder(
                botonera.getComponentCount() == 0 ? 4 : 18, 14, 8, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, lbl.getPreferredSize().height));
        botonera.add(lbl);
    }

    private void agregarBotonNavegacion(JPanel botonera, String texto, String clave, String archivoIcono) {
        BotonNavegacion boton = new BotonNavegacion(texto);
        ImageIcon icono = UIFactory.cargarIcono(archivoIcono, 18, 18);
        if (icono != null) {
            boton.setIcon(icono);
        }
        boton.setAlignmentX(LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boton.setPreferredSize(new Dimension(220, 40));
        boton.addActionListener(e -> controller.navegar(clave));
        botonera.add(boton);
        botonera.add(Box.createVerticalStrut(2));
        botonesNavegacion.put(clave, boton);
    }

    /**
     * Refleja el estado vigente en el menú (nivel de alerta o falta de conexión):
     * el punto junto a "Panel Principal" se ve desde cualquier módulo.
     */
    public void mostrarEstadoMonitoreo(Color color, String descripcion) {
        if (botonesNavegacion.get(DashboardPanel.KEY) instanceof BotonNavegacion b) {
            b.setIndicador(color, descripcion);
        }
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
                    controller.permiteModulo(IngresoManualPanel.KEY),
                    controller.permiteModulo(HistoricoPanel.KEY),
                    this::mostrarEstadoMonitoreo);
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
            return new ReportesPanel(controller.getUsuario().esConsulta());
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

    /** Barra inferior: estación activa a la izquierda, reloj a la derecha. */
    private JPanel construirBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(ColoresUI.FONDO_RAIZ);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ColoresUI.BORDE),
                BorderFactory.createEmptyBorder(5, 20, 5, 20)));

        lblEstacion.setFont(UIFactory.FUENTE_NORMAL.deriveFont(12f));
        lblEstacion.setForeground(ColoresUI.TEXTO_SUAVE);
        actualizarEstacion();

        lblReloj.setFont(UIFactory.FUENTE_NORMAL.deriveFont(12f));
        lblReloj.setForeground(ColoresUI.TEXTO_SECUNDARIO);

        // CENTER y no WEST: toma todo el ancho libre y el texto no se corta
        barra.add(lblEstacion, BorderLayout.CENTER);
        barra.add(lblReloj, BorderLayout.EAST);
        return barra;
    }

    /** La estación puede renombrarse en Información: se relee al navegar. */
    private void actualizarEstacion() {
        ConfiguracionSistema cfg = ConfiguracionSistema.cargar();
        String ubicacion = cfg.getUbicacion();
        lblEstacion.setText(cfg.getNombreEstacion()
                + (ubicacion == null || ubicacion.isBlank() ? "" : " · " + ubicacion));
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
        actualizarEstacion();
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
            if (e.getValue() instanceof BotonNavegacion b) {
                b.setActivo(e.getKey().equals(clave));
            }
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
