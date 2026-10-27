package weatherreport_mvc.controllers;

import java.sql.SQLException;
import weatherreport_mvc.database.ConexionBD;
import weatherreport_mvc.database.UsuarioDAO;
import weatherreport_mvc.models.Usuario;
import weatherreport_mvc.utils.HashUtil;
import weatherreport_mvc.views.LoginView;
import weatherreport_mvc.views.MainView;
import weatherreport_mvc.views.RecuperarContrasenaDialog;
import weatherreport_mvc.views.RegistroCuentaDialog;

/**
 * Controlador del módulo de inicio de sesión. Valida las credenciales contra
 * la base de datos y, en caso de éxito, abre la ventana principal. También
 * permite crear cuentas de solo consulta y canjear un código de recuperación
 * generado por un Ingeniero desde Gestión de Usuarios.
 */
public class LoginController {

    private final LoginView view;

    public LoginController(LoginView view) {
        this.view = view;
    }

    public void iniciarSesion() {
        String usuario = view.getUsuario().trim();
        String contrasena = view.getContrasena();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            view.mostrarError("Ingrese usuario y contraseña.");
            return;
        }

        Usuario usuarioBD;
        try {
            usuarioBD = UsuarioDAO.buscarActivoPorUsuario(usuario);
            if (usuarioBD == null
                    || !HashUtil.verificarContrasena(contrasena, usuarioBD.getContrasenaHash())) {
                view.mostrarError("Credenciales inválidas o usuario inactivo.");
                return;
            }
            // Las cuentas con el hash SHA-256 antiguo se migran al formato con sal.
            if (HashUtil.esHashAntiguo(usuarioBD.getContrasenaHash())) {
                UsuarioDAO.cambiarContrasena(usuario, HashUtil.hashContrasena(contrasena));
            }
        } catch (SQLException e) {
            view.mostrarError("No se pudo conectar a la base de datos. "
                    + ConexionBD.mensajeError(e)
                    + ". Verifique que el servicio de MySQL esté activo.");
            return;
        }

        view.mostrarExito("Bienvenido, " + usuarioBD.getNombreCompleto());
        MainView principal = new MainView(usuarioBD);
        principal.setVisible(true);
        view.dispose();
    }

    public void abrirRegistro() {
        new RegistroCuentaDialog(view, this).setVisible(true);
    }

    public void abrirRecuperacion() {
        new RecuperarContrasenaDialog(view, this).setVisible(true);
    }

    /**
     * Valida el código y, si es correcto y no ha expirado, cambia la
     * contraseña y elimina el código (uso único).
     *
     * @return null si fue exitoso, o un mensaje de error.
     */
    public String cambiarContrasenaConCodigo(String usuario, String codigo,
                                             String nueva, String confirmacion) {
        String u = usuario == null ? "" : usuario.trim();
        if (u.isEmpty()) {
            return "Ingrese su nombre de usuario.";
        }
        if (codigo == null || codigo.trim().isEmpty()) {
            return "Ingrese el código de recuperación.";
        }
        if (nueva == null || nueva.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }
        if (!nueva.equals(confirmacion)) {
            return "Las contraseñas no coinciden.";
        }
        try {
            if (!UsuarioDAO.validarCodigoRecuperacion(u, HashUtil.hashSHA256(codigo.trim()))) {
                return "El código es inválido o ya expiró. Solicite uno nuevo a un Ingeniero.";
            }
            UsuarioDAO.cambiarContrasena(u, HashUtil.hashContrasena(nueva));
            UsuarioDAO.limpiarCodigoRecuperacion(u);
            return null;
        } catch (SQLException e) {
            return "No se pudo cambiar la contraseña: " + ConexionBD.mensajeError(e);
        }
    }

    /** @return null si fue exitoso, o un mensaje de error. */
    public String crearCuentaConsulta(String nombre, String usuario, String contrasena,
                                      String confirmacion) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre completo es obligatorio.";
        }
        if (usuario == null || usuario.trim().isEmpty()) {
            return "El nombre de usuario es obligatorio.";
        }
        if (contrasena == null || contrasena.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }
        if (!contrasena.equals(confirmacion)) {
            return "Las contraseñas no coinciden.";
        }
        if (UsuarioDAO.existeUsuario(usuario.trim())) {
            return "El nombre de usuario ya está registrado.";
        }
        Usuario u = new Usuario();
        u.setNombreCompleto(nombre.trim());
        u.setUsuario(usuario.trim());
        u.setContrasenaHash(HashUtil.hashContrasena(contrasena));
        u.setRol(Usuario.ROL_CONSULTA);
        u.setActivo(true);
        try {
            UsuarioDAO.insertar(u);
            return null;
        } catch (SQLException e) {
            return "No se pudo crear la cuenta: " + ConexionBD.mensajeError(e);
        }
    }

    public void salir() {
        System.exit(0);
    }
}
