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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import weatherreport_mvc.controllers.LoginController;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.UIFactory;

/**
 * Diálogo modal para crear una cuenta de consulta (solo lectura) desde la
 * vista de inicio de sesión. El rol queda fijo en "Consulta".
 */
public class RegistroCuentaDialog extends JDialog {

    private final LoginController controller;

    private final JTextField txtNombre;
    private final JTextField txtUsuario;
    private final JPasswordField txtContrasena;
    private final JPasswordField txtConfirmacion;
    private final JLabel lblMensaje;

    public RegistroCuentaDialog(LoginView owner, LoginController controller) {
        super(owner, "Crear cuenta de consulta", ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        txtNombre = UIFactory.crearTextField(20);
        txtUsuario = UIFactory.crearTextField(20);
        txtContrasena = new JPasswordField(20);
        UIFactory.estilizarCampo(txtContrasena);
        txtConfirmacion = new JPasswordField(20);
        UIFactory.estilizarCampo(txtConfirmacion);
        lblMensaje = new JLabel(" ");

        construirInterfaz();
        pack();
        setMinimumSize(new Dimension(430, 360));
        setSize(430, 370);
        setLocationRelativeTo(owner);
    }

    private void construirInterfaz() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(ColoresUI.FONDO_RAIZ);
        formulario.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int fila = 0;

        JLabel lblInfo = UIFactory.crearLabel(
                "<html>Las cuentas creadas aquí son de <b>consulta</b>: "
                + "solo podrán ver la información, sin poder modificar nada.</html>");
        lblInfo.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        formulario.add(lblInfo, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;
        formulario.add(UIFactory.crearLabel("Nombre completo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formulario.add(txtNombre, gbc);

        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;
        formulario.add(UIFactory.crearLabel("Usuario:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formulario.add(txtUsuario, gbc);

        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;
        formulario.add(UIFactory.crearLabel("Contraseña:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formulario.add(txtContrasena, gbc);

        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;
        formulario.add(UIFactory.crearLabel("Confirmar contraseña:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formulario.add(txtConfirmacion, gbc);

        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        lblMensaje.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formulario.add(lblMensaje, gbc);
        gbc.gridwidth = 1;

        JButton btnGuardar = UIFactory.crearBotonPrimario("Crear cuenta");
        btnGuardar.addActionListener(e -> guardar());
        JButton btnCancelar = UIFactory.crearBotonSecundario("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        JPanel botonera = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        botonera.setBackground(ColoresUI.FONDO_RAIZ);
        botonera.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 20));
        botonera.add(btnCancelar);
        botonera.add(btnGuardar);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(ColoresUI.FONDO_RAIZ);
        contenido.add(formulario, BorderLayout.CENTER);
        contenido.add(botonera, BorderLayout.SOUTH);
        setContentPane(contenido);

        getRootPane().setDefaultButton(btnGuardar);
    }

    private void guardar() {
        String error = controller.crearCuentaConsulta(
                txtNombre.getText(),
                txtUsuario.getText(),
                new String(txtContrasena.getPassword()),
                new String(txtConfirmacion.getPassword()));

        if (error == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Cuenta de consulta creada correctamente. Ya puede iniciar sesión.",
                    "Cuenta creada", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            lblMensaje.setText(error);
            lblMensaje.setForeground(ColoresUI.ALERTA_CRITICA_OSC);
        }
    }
}