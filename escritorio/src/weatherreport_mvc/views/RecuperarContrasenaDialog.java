package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import weatherreport_mvc.controllers.LoginController;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.UIFactory;

/**
 * Diálogo modal para recuperar la contraseña con un código de un solo uso.
 * El código lo genera un Ingeniero desde Gestión de Usuarios y se lo entrega
 * al usuario; nunca se muestra aquí, para que nadie pueda restablecer la
 * contraseña de otra cuenta solo con conocer su nombre de usuario.
 */
public class RecuperarContrasenaDialog extends JDialog {

    private final LoginController controller;

    private final JTextField txtUsuario;
    private final JTextField txtCodigo;
    private final JPasswordField txtNuevaContrasena;
    private final JPasswordField txtConfirmacion;
    private final JLabel lblMensaje;
    private final JButton btnCambiar;

    public RecuperarContrasenaDialog(LoginView owner, LoginController controller) {
        super(owner, "Recuperar contraseña", ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        txtUsuario = UIFactory.crearTextField(20);
        txtCodigo = UIFactory.crearTextField(20);
        txtNuevaContrasena = new JPasswordField(20);
        UIFactory.estilizarCampo(txtNuevaContrasena);
        txtConfirmacion = new JPasswordField(20);
        UIFactory.estilizarCampo(txtConfirmacion);
        lblMensaje = new JLabel(" ");

        btnCambiar = UIFactory.crearBotonPrimario("Cambiar contraseña");

        construirInterfaz();
        pack();
        setMinimumSize(new Dimension(520, 380));
        setSize(520, 390);
        setLocationRelativeTo(owner);
    }

    private void construirInterfaz() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(ColoresUI.FONDO_RAIZ);
        formulario.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int fila = 0;

        JLabel lblInfo = UIFactory.crearLabel(
                "<html>Solicite a un <b>Ingeniero</b> un código de recuperación para su "
                + "cuenta (se genera en <b>Gestión de Usuarios</b>). El código es de "
                + "<b>un solo uso</b> y vence a los <b>30 minutos</b>.</html>");
        lblInfo.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        formulario.add(lblInfo, gbc);
        gbc.gridwidth = 1;

        fila = agregarCampo(formulario, gbc, fila, "Usuario:", txtUsuario);
        fila = agregarCampo(formulario, gbc, fila, "Código de recuperación:", txtCodigo);
        fila = agregarCampo(formulario, gbc, fila, "Nueva contraseña:", txtNuevaContrasena);
        fila = agregarCampo(formulario, gbc, fila, "Confirmar contraseña:", txtConfirmacion);

        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        lblMensaje.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formulario.add(lblMensaje, gbc);
        gbc.gridwidth = 1;

        btnCambiar.addActionListener(e -> cambiarContrasena());
        JButton btnCancelar = UIFactory.crearBotonSecundario("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        JPanel botonera = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        botonera.setBackground(ColoresUI.FONDO_RAIZ);
        botonera.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 20));
        botonera.add(btnCancelar);
        botonera.add(btnCambiar);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(ColoresUI.FONDO_RAIZ);
        contenido.add(formulario, BorderLayout.CENTER);
        contenido.add(botonera, BorderLayout.SOUTH);
        setContentPane(contenido);
    }

    private int agregarCampo(JPanel formulario, GridBagConstraints gbc, int fila,
                             String etiqueta, JTextField campo) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;
        formulario.add(UIFactory.crearLabel(etiqueta), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formulario.add(campo, gbc);
        return fila + 1;
    }

    private void cambiarContrasena() {
        String error = controller.cambiarContrasenaConCodigo(
                txtUsuario.getText(),
                txtCodigo.getText(),
                new String(txtNuevaContrasena.getPassword()),
                new String(txtConfirmacion.getPassword()));

        if (error == null) {
            JOptionPane.showMessageDialog(this,
                    "Contraseña actualizada correctamente. Ya puede iniciar sesión.",
                    "Contraseña actualizada", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            mostrarError(error);
        }
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setText("<html>" + mensaje + "</html>");
        lblMensaje.setForeground(ColoresUI.ALERTA_CRITICA_OSC);
    }
}
