package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
 * abre la ventana principal.
 */
public class LoginView extends JFrame {

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
        setSize(920, 560);
        setLocationRelativeTo(null);

        txtUsuario = new JTextField(25);
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.setPreferredSize(new Dimension(250, 35));
        
        txtContrasena = new JPasswordField(25);
        txtContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtContrasena.setPreferredSize(new Dimension(250, 35));
        lblError = new JLabel(" ");
        lblError.setForeground(ColoresUI.ALERTA_CRITICA);
        btnLogin = UIFactory.crearBotonPrimario("Iniciar Sesión");
        btnLogin.addActionListener(e -> controller.iniciarSesion());

        construirInterfaz();

        getRootPane().setDefaultButton(btnLogin);
        txtContrasena.addActionListener(e -> controller.iniciarSesion());
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(ColoresUI.BLANCO);

        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setBackground(ColoresUI.NEGRO);
        lateral.setPreferredSize(new Dimension(360, 0));

        JLabel lblMarca = new JLabel("WEATHER REPORT");
        lblMarca.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblMarca.setForeground(ColoresUI.BLANCO);
        lblMarca.setHorizontalAlignment(JLabel.CENTER);
        lblMarca.setBorder(BorderFactory.createEmptyBorder(30, 20, 10, 20));

        // Logo de Weather Report
        JLabel lblLogoWeather = new JLabel();
        lblLogoWeather.setHorizontalAlignment(JLabel.CENTER);
        lblLogoWeather.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        lblLogoWeather.setPreferredSize(new Dimension(120, 120));
        try {
            // Intentar cargar desde diferentes ubicaciones
            ImageIcon icon = null;
            String[] rutasPosibles = {
                "/logo_weather_report.png",
                "/resources/logo_weather_report.png",
                "src/resources/logo_weather_report.png",
                "resources/logo_weather_report.png"
            };

            for (String ruta : rutasPosibles) {
                try {
                    if (ruta.startsWith("/")) {
                        icon = new ImageIcon(getClass().getResource(ruta));
                    } else {
                        icon = new ImageIcon(ruta);
                    }
                    if (icon.getIconWidth() > 0) break;
                } catch (Exception e) {
                    // Continuar con la siguiente ruta
                }
            }

            if (icon != null && icon.getIconWidth() > 0) {
                // Escalar la imagen preservando la proporción (caja 120x120)
                java.awt.Image img = icon.getImage();
                int w = img.getWidth(null);
                int h = img.getHeight(null);
                int maxW = 130;
                int maxH = 110;
                double escala = Math.min(1.0, Math.min((double) maxW / w, (double) maxH / h));
                int nuevoW = Math.max(1, (int) Math.round(w * escala));
                int nuevoH = Math.max(1, (int) Math.round(h * escala));
                lblLogoWeather.setIcon(new ImageIcon(
                        img.getScaledInstance(nuevoW, nuevoH, java.awt.Image.SCALE_SMOOTH)));
            } else {
                lblLogoWeather.setText("(Logo no encontrado)");
                lblLogoWeather.setForeground(ColoresUI.GRIS_CLARO);
            }
        } catch (Exception e) {
            lblLogoWeather.setText("(Logo no encontrado)");
            lblLogoWeather.setForeground(ColoresUI.GRIS_CLARO);
        }

        // Logo de la aplicación
        JLabel lblLogo = new JLabel();
        try {
            // Intentar cargar desde diferentes ubicaciones
            ImageIcon icon = null;
            String[] rutasPosibles = {
                "/logo_weather_report.png",
                "/resources/logo_weather_report.png",
                "src/resources/logo_weather_report.png",
                "resources/logo_weather_report.png"
            };
            
            for (String ruta : rutasPosibles) {
                try {
                    if (ruta.startsWith("/")) {
                        icon = new ImageIcon(getClass().getResource(ruta));
                    } else {
                        icon = new ImageIcon(ruta);
                    }
                    if (icon.getIconWidth() > 0) break;
                } catch (Exception e) {
                    // Continuar con la siguiente ruta
                }
            }
            
            if (icon != null && icon.getIconWidth() > 0) {
                // Escalar la imagen a un tamaño apropiado
                java.awt.Image img = icon.getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                lblLogo.setIcon(new ImageIcon(img));
            } else {
                lblLogo.setText("(Logo no encontrado)");
                lblLogo.setForeground(ColoresUI.GRIS_CLARO);
            }
        } catch (Exception e) {
            lblLogo.setText("(Logo no encontrado)");
            lblLogo.setForeground(ColoresUI.GRIS_CLARO);
        }
        lblLogo.setHorizontalAlignment(JLabel.CENTER);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblProteccion = new JLabel("Weather Report");
        lblProteccion.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblProteccion.setForeground(ColoresUI.BLANCO);
        lblProteccion.setHorizontalAlignment(JLabel.CENTER);
        lblProteccion.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblSub = new JLabel("<html><center>Sistema de monitoreo<br>meteorológico</center></html>");
        lblSub.setFont(UIFactory.FUENTE_SUBTITULO);
        lblSub.setForeground(ColoresUI.GRIS_CLARO);
        lblSub.setHorizontalAlignment(JLabel.CENTER);

        // Panel superior: Weather Report + Logo
        JPanel panelWeather = new JPanel(new BorderLayout());
        panelWeather.setOpaque(false);
        panelWeather.add(lblMarca, BorderLayout.NORTH);
        panelWeather.add(lblLogoWeather, BorderLayout.CENTER);

        // Panel inferior: Protección Civil + Logo
        JPanel panelProteccion = new JPanel(new BorderLayout());
        panelProteccion.setOpaque(false);
        panelProteccion.add(lblProteccion, BorderLayout.NORTH);
        panelProteccion.add(lblLogo, BorderLayout.CENTER);

        // Panel central que contiene ambos
        JPanel panelCentral = new JPanel(new BorderLayout(0, 10));
        panelCentral.setOpaque(false);
        panelCentral.add(panelWeather, BorderLayout.NORTH);
        panelCentral.add(panelProteccion, BorderLayout.CENTER);

        JPanel lateralCentro = new JPanel(new BorderLayout());
        lateralCentro.setOpaque(false);
        lateralCentro.add(panelCentral, BorderLayout.NORTH);
        
        JPanel panelSub = new JPanel(new BorderLayout());
        panelSub.setOpaque(false);
        panelSub.add(lblSub, BorderLayout.CENTER);
        lateralCentro.add(panelSub, BorderLayout.SOUTH);

        lateral.add(lateralCentro, BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(ColoresUI.VINO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblTitulo = UIFactory.crearLabelTitulo("Iniciar Sesión");
        lblTitulo.setForeground(ColoresUI.BLANCO);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formulario.add(lblTitulo, gbc);

        JLabel lblUsuario = UIFactory.crearLabel("Usuario:");
        lblUsuario.setForeground(ColoresUI.BLANCO);
        lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(15, 10, 15, 10);
        formulario.add(lblUsuario, gbc);
        gbc.gridx = 1;
        formulario.add(txtUsuario, gbc);

        JLabel lblPass = UIFactory.crearLabel("Contraseña:");
        lblPass.setForeground(ColoresUI.BLANCO);
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridy = 2;
        gbc.gridx = 0;
        formulario.add(lblPass, gbc);
        gbc.gridx = 1;
        formulario.add(txtContrasena, gbc);

        JButton btnLoginLocal = btnLogin;
        btnLoginLocal.setPreferredSize(new Dimension(250, 45));
        btnLoginLocal.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton btnCancelar = UIFactory.crearBotonSecundario("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(110, 40));
        btnCancelar.addActionListener(e -> controller.salir());

        JPanel botonera = new JPanel(new BorderLayout(10, 0));
        botonera.setBackground(ColoresUI.VINO);
        botonera.add(btnLoginLocal, BorderLayout.CENTER);
        botonera.add(btnCancelar, BorderLayout.EAST);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(botonera, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        lblError.setForeground(ColoresUI.ALERTA_CRITICA);
        formulario.add(lblError, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 10, 6, 10);
        JLabel lblCrear = UIFactory.crearLabel("¿No tienes cuenta?");
        lblCrear.setForeground(ColoresUI.BLANCO);
        lblCrear.setHorizontalAlignment(JLabel.CENTER);
        formulario.add(lblCrear, gbc);

        gbc.gridy = 6;
        JButton btnRegistrarse = UIFactory.crearBotonSecundario("Crear cuenta de consulta");
        btnRegistrarse.setPreferredSize(new Dimension(250, 40));
        btnRegistrarse.addActionListener(e -> controller.abrirRegistro());
        formulario.add(btnRegistrarse, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(4, 10, 4, 10);
        JButton btnRecuperar = UIFactory.crearBotonSecundario("Recuperar contraseña");
        btnRecuperar.setPreferredSize(new Dimension(250, 40));
        btnRecuperar.addActionListener(e -> controller.abrirRecuperacion());
        formulario.add(btnRecuperar, gbc);

        raiz.add(lateral, BorderLayout.WEST);
        raiz.add(formulario, BorderLayout.CENTER);

        setContentPane(raiz);
    }

    public String getUsuario() {
        return txtUsuario.getText();
    }

    public String getContrasena() {
        return new String(txtContrasena.getPassword());
    }

    public void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setForeground(ColoresUI.ALERTA_CRITICA);
    }

    public void mostrarExito(String mensaje) {
        lblError.setText(mensaje);
        lblError.setForeground(ColoresUI.ALERTA_NORMAL);
    }
}
