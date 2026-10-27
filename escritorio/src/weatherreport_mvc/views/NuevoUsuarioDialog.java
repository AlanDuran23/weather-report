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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import weatherreport_mvc.controllers.UsuarioController;
import weatherreport_mvc.models.Usuario;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.UIFactory;

/**
 * Diálogo modal para registrar o editar usuarios. La contraseña se cifra con
 * SHA-256 antes de guardarla en la base de datos.
 */
public class NuevoUsuarioDialog extends JDialog {

    private static final String[] ROLES = {
        Usuario.ROL_INGENIERO, Usuario.ROL_CONSULTA
    };

    private final UsuarioController controller;
    private final Usuario editando;

    private final JTextField txtNombre;
    private final JTextField txtUsuario;
    private final JPasswordField txtContrasena;
    private final JComboBox<String> cmbRol;
    private final JLabel lblMensaje;

    public NuevoUsuarioDialog(UsuariosPanel owner, UsuarioController controller,
                              Usuario editando) {
        super(SwingUtilities.getWindowAncestor(owner),
                editando == null ? "Nuevo Usuario" : "Editar Usuario",
                ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.editando = editando;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        txtNombre = UIFactory.crearTextField(20);
        txtUsuario = UIFactory.crearTextField(20);
        txtContrasena = new JPasswordField(20);
        UIFactory.estilizarCampo(txtContrasena);
        cmbRol = new JComboBox<>(ROLES);
        UIFactory.estilizarCombo(cmbRol);
        lblMensaje = new JLabel(" ");

        construirInterfaz();
        if (editando != null) {
            precargar(editando);
        }
        pack();
        setMinimumSize(new Dimension(430, 320));
        setSize(430, 330);
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

    gbc.gridx = 0;
    gbc.gridy = fila;
    gbc.weightx = 0;
    formulario.add(UIFactory.crearLabel("Nombre completo:"), gbc);

    gbc.gridx = 1;
    gbc.gridy = fila;
    gbc.weightx = 1.0;
    formulario.add(txtNombre, gbc);

    fila++;
    gbc.gridx = 0;
    gbc.gridy = fila;
    gbc.weightx = 0;
    formulario.add(UIFactory.crearLabel("Usuario:"), gbc);

    gbc.gridx = 1;
    gbc.gridy = fila;
    gbc.weightx = 1.0;
    formulario.add(txtUsuario, gbc);

    fila++;
    gbc.gridx = 0;
    gbc.gridy = fila;
    gbc.weightx = 0;
    formulario.add(UIFactory.crearLabel("Contraseña:"), gbc);

    gbc.gridx = 1;
    gbc.gridy = fila;
    gbc.weightx = 1.0;
    formulario.add(txtContrasena, gbc);

    fila++;
    gbc.gridx = 0;
    gbc.gridy = fila;
    gbc.weightx = 0;
    formulario.add(UIFactory.crearLabel("Rol:"), gbc);

    gbc.gridx = 1;
    gbc.gridy = fila;
    gbc.weightx = 1.0;
    formulario.add(cmbRol, gbc);

    fila++;
    gbc.gridx = 0;
    gbc.gridy = fila;
    gbc.gridwidth = 2;
    gbc.weightx = 1.0;
    lblMensaje.setFont(new Font("Segoe UI", Font.BOLD, 12));
    formulario.add(lblMensaje, gbc);
    gbc.gridwidth = 1;

    JButton btnGuardar = UIFactory.crearBotonPrimario("Guardar");
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
}

    private void precargar(Usuario u) {
        txtNombre.setText(u.getNombreCompleto());
        txtUsuario.setText(u.getUsuario());
        txtContrasena.setText("");
        if (u.getRol() != null) {
            boolean existe = false;
            for (int i = 0; i < cmbRol.getItemCount(); i++) {
                if (u.getRol().equals(cmbRol.getItemAt(i))) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                cmbRol.addItem(u.getRol());
            }
        }
        cmbRol.setSelectedItem(u.getRol());
        txtUsuario.setToolTipText("Nombre de usuario");
        txtContrasena.setToolTipText("Dejar vacío para conservar la contraseña actual");
    }

    private void guardar() {
        String nombre = txtNombre.getText();
        String usuario = txtUsuario.getText();
        String contrasena = new String(txtContrasena.getPassword());
        String rol = (String) cmbRol.getSelectedItem();
        if (rol == null || rol.trim().isEmpty()) {
            lblMensaje.setText("Seleccione un rol.");
            lblMensaje.setForeground(ColoresUI.ALERTA_CRITICA_OSC);
            return;
        }

        String error;
        if (editando == null) {
            error = controller.registrar(nombre, usuario, contrasena, rol);
        } else {
            editando.setNombreCompleto(nombre);
            editando.setUsuario(usuario);
            editando.setRol(rol);
            error = controller.actualizar(editando, contrasena);
        }

        if (error == null) {
            dispose();
        } else {
            lblMensaje.setText(error);
            lblMensaje.setForeground(ColoresUI.ALERTA_CRITICA_OSC);
        }
    }
}
