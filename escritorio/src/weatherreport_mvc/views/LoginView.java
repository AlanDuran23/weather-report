package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import weatherreport_mvc.controllers.LoginController;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.UIFactory;

/**
 * Vista de inicio de sesión (JFrame inicial). Solo si el login es exitoso se
 * abre la ventana principal. A la izquierda, la franja institucional en vino;
 * a la derecha, el formulario sobre el mismo fondo oscuro de la aplicación.
 */
public class LoginView extends JFrame {

    private static final java.awt.Color COLOR_ERROR = ColoresUI.ALERTA_CRITICA_OSC;

    private final LoginController controller;
    private final JTextField txtUsuario;
    private final JPasswordField txtContrasena;
    private final JLabel lblError;
    private final JButton btnLogin;

    public LoginView() {
        super("Weather Report - Inicio de Sesión");
        this.controller = new LoginController(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(920, 580);
        setLocationRelativeTo(null);

        txtUsuario = UIFactory.crearTextField(22);
        txtContrasena = new JPasswordField(22);
        UIFactory.estilizarCampo(txtContrasena);
        txtContrasena.setFont(UIFactory.FUENTE_TEXTO);

        lblError = new JLabel(" ");
        lblError.setFont(UIFactory.FUENTE_NORMAL);
        lblError.setForeground(COLOR_ERROR);
        btnLogin = UIFactory.crearBotonPrimario("Iniciar sesión");
        btnLogin.addActionListener(e -> controller.iniciarSesion());

        construirInterfaz();

        getRootPane().setDefaultButton(btnLogin);
        txtContrasena.addActionListener(e -> controller.iniciarSesion());
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(ColoresUI.FONDO_RAIZ);
        raiz.add(construirFranjaMarca(), BorderLayout.WEST);
        raiz.add(construirFormulario(), BorderLayout.CENTER);
        setContentPane(raiz);
    }

    /** Franja de marca: vino institucional plano, producto arriba e institución abajo. */
    private JPanel construirFranjaMarca() {
        JPanel franja = new JPanel(new BorderLayout());
        franja.setBackground(ColoresUI.VINO_OSCURO);
        franja.setPreferredSize(new Dimension(340, 0));
        franja.setBorder(BorderFactory.createEmptyBorder(40, 36, 36, 36));

        JLabel lblLogo = new JLabel(cargarImagen("logo_weather_report.png", 72, 72), JLabel.LEFT);
        JLabel lblMarca = new JLabel("Weather Report");
        lblMarca.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 26));
        lblMarca.setForeground(ColoresUI.BLANCO);
        JLabel lblLema = new JLabel("<html>Sistema de monitoreo<br>meteorológico</html>");
        lblLema.setFont(UIFactory.FUENTE_TEXTO);
        lblLema.setForeground(new java.awt.Color(0xE6, 0xC3, 0xCB));

        JPanel producto = new JPanel();
        producto.setOpaque(false);
        producto.setLayout(new BoxLayout(producto, BoxLayout.Y_AXIS));
        producto.add(alinear(lblLogo));
        producto.add(Box.createVerticalStrut(20));
        producto.add(alinear(lblMarca));
        producto.add(Box.createVerticalStrut(6));
        producto.add(alinear(lblLema));

        franja.add(producto, BorderLayout.NORTH);
        return franja;
    }

    /** Formulario centrado de ancho fijo: etiquetas sobre los campos y una sola acción principal. */
    private JPanel construirFormulario() {
        JLabel lblTitulo = new JLabel("Iniciar sesión");
        lblTitulo.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 26));
        lblTitulo.setForeground(ColoresUI.TEXTO_PRIMARIO);
        JLabel lblSub = new JLabel("Ingrese su usuario y contraseña para continuar.");
        lblSub.setFont(UIFactory.FUENTE_NORMAL.deriveFont(14f));
        lblSub.setForeground(ColoresUI.TEXTO_SUAVE);

        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setPreferredSize(new Dimension(340, 42));

        JButton btnRegistrarse = UIFactory.crearBotonTexto("Crear cuenta");
        btnRegistrarse.addActionListener(e -> controller.abrirRegistro());
        JButton btnRecuperar = UIFactory.crearBotonTexto("Recuperar contraseña");
        btnRecuperar.addActionListener(e -> controller.abrirRecuperacion());
        JPanel enlaces = new JPanel(new BorderLayout());
        enlaces.setOpaque(false);
        enlaces.add(btnRegistrarse, BorderLayout.WEST);
        enlaces.add(btnRecuperar, BorderLayout.EAST);

        JButton btnCancelar = UIFactory.crearBotonTexto("Salir");
        btnCancelar.addActionListener(e -> controller.salir());
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pie.setOpaque(false);
        pie.add(btnCancelar);

        JPanel columna = new JPanel();
        columna.setOpaque(false);
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setPreferredSize(new Dimension(340, 440));
        columna.add(alinear(lblTitulo));
        columna.add(Box.createVerticalStrut(4));
        columna.add(alinear(lblSub));
        columna.add(Box.createVerticalStrut(28));
        columna.add(alinear(campo("Usuario", txtUsuario)));
        columna.add(Box.createVerticalStrut(16));
        columna.add(alinear(campo("Contraseña", txtContrasena)));
        columna.add(Box.createVerticalStrut(10));
        columna.add(alinear(lblError));
        columna.add(Box.createVerticalStrut(10));
        columna.add(alinear(btnLogin));
        columna.add(Box.createVerticalStrut(20));
        columna.add(alinear(separador()));
        columna.add(Box.createVerticalStrut(10));
        columna.add(alinear(enlaces));
        columna.add(Box.createVerticalGlue());
        columna.add(alinear(pie));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(ColoresUI.FONDO_RAIZ);
        formulario.setBorder(BorderFactory.createEmptyBorder(32, 40, 24, 40));
        formulario.add(columna);
        return formulario;
    }

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

    private static JComponent separador() {
        JPanel linea = new JPanel();
        linea.setBackground(ColoresUI.BORDE);
        linea.setPreferredSize(new Dimension(340, 1));
        return linea;
    }

    /** Ancho completo de la columna y alto natural (BoxLayout no estira en vertical). */
    private static JComponent alinear(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    /** Carga una imagen de recursos escalada dentro de una caja, conservando su proporción. */
    private ImageIcon cargarImagen(String nombre, int maxAncho, int maxAlto) {
        String[] rutasPosibles = {
            "/" + nombre,
            "/resources/" + nombre,
            "src/resources/" + nombre,
            "resources/" + nombre
        };
        for (String ruta : rutasPosibles) {
            try {
                ImageIcon icon;
                if (ruta.startsWith("/")) {
                    java.net.URL url = getClass().getResource(ruta);
                    if (url == null) {
                        continue;
                    }
                    icon = new ImageIcon(url);
                } else {
                    icon = new ImageIcon(ruta);
                }
                if (icon.getIconWidth() > 0) {
                    java.awt.Image img = icon.getImage();
                    double escala = Math.min((double) maxAncho / img.getWidth(null),
                            (double) maxAlto / img.getHeight(null));
                    int w = Math.max(1, (int) Math.round(img.getWidth(null) * escala));
                    int h = Math.max(1, (int) Math.round(img.getHeight(null) * escala));
                    return new ImageIcon(img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH));
                }
            } catch (Exception e) {
                // Continuar con la siguiente ruta
            }
        }
        return null;
    }

    public String getUsuario() {
        return txtUsuario.getText();
    }

    public String getContrasena() {
        return new String(txtContrasena.getPassword());
    }

    public void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setForeground(COLOR_ERROR);
    }

    public void mostrarExito(String mensaje) {
        lblError.setText(mensaje);
        lblError.setForeground(ColoresUI.ALERTA_NORMAL_OSC);
    }
}
