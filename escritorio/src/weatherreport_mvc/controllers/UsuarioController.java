package weatherreport_mvc.controllers;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import weatherreport_mvc.database.ConexionBD;
import weatherreport_mvc.database.UsuarioDAO;
import weatherreport_mvc.models.Usuario;
import weatherreport_mvc.utils.HashUtil;
import weatherreport_mvc.views.UsuariosPanel;

/**
 * Controlador del módulo de Gestión de Usuarios. Realiza el CRUD, el hash de
 * contraseñas y la generación de códigos de recuperación. Impide que el
 * sistema se quede sin ningún Ingeniero activo.
 */
public class UsuarioController {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int LONGITUD_CODIGO = 10;
    private static final Duration VALIDEZ_CODIGO = Duration.ofMinutes(30);
    private static final String CARACTERES_CODIGO =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz0123456789!@#$%&*+=?";

    private final UsuariosPanel view;
    private final Usuario sesion;

    public UsuarioController(UsuariosPanel view, Usuario sesion) {
        this.view = view;
        this.sesion = sesion;
    }

    /** Resultado de la generación del código de recuperación. */
    public static class ResultadoCodigo {
        public final String codigo;
        public final String error;

        public ResultadoCodigo(String codigo, String error) {
            this.codigo = codigo;
            this.error = error;
        }
    }

    public void cargarUsuarios() {
        view.mostrarUsuarios(UsuarioDAO.obtenerTodos());
    }

    /** @return null si fue exitoso, o un mensaje de error. */
    public String registrar(String nombre, String usuario, String contrasena, String rol) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre completo es obligatorio.";
        }
        if (usuario == null || usuario.trim().isEmpty()) {
            return "El nombre de usuario es obligatorio.";
        }
        if (contrasena == null || contrasena.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }
        if (UsuarioDAO.existeUsuario(usuario.trim())) {
            return "El nombre de usuario ya está registrado.";
        }
        Usuario u = new Usuario();
        u.setNombreCompleto(nombre.trim());
        u.setUsuario(usuario.trim());
        u.setContrasenaHash(HashUtil.hashContrasena(contrasena));
        u.setRol(rol);
        u.setActivo(true);
        try {
            UsuarioDAO.insertar(u);
            cargarUsuarios();
            return null;
        } catch (SQLException e) {
            return ConexionBD.mensajeError(e);
        }
    }

    /** @return null si fue exitoso, o un mensaje de error. */
    public String actualizar(Usuario u, String nuevaContrasena) {
        if (u.getNombreCompleto() == null || u.getNombreCompleto().trim().isEmpty()) {
            return "El nombre completo es obligatorio.";
        }
        if (u.getUsuario() == null || u.getUsuario().trim().isEmpty()) {
            return "El nombre de usuario es obligatorio.";
        }
        if (UsuarioDAO.existeUsuarioOtro(u.getUsuario().trim(), u.getId())) {
            return "El nombre de usuario ya está registrado.";
        }
        if (nuevaContrasena != null && !nuevaContrasena.isEmpty()
                && nuevaContrasena.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }
        if (!Usuario.ROL_INGENIERO.equals(u.getRol())) {
            if (u.getId() == sesion.getId()) {
                return "No puede quitarse a sí mismo el rol de Ingeniero.";
            }
            String error = validarNoEsUltimoIngeniero(u.getId(),
                    "No se puede cambiar el rol del último Ingeniero activo.");
            if (error != null) {
                return error;
            }
        }
        if (nuevaContrasena != null && !nuevaContrasena.isEmpty()) {
            u.setContrasenaHash(HashUtil.hashContrasena(nuevaContrasena));
        }
        try {
            UsuarioDAO.actualizar(u);
            cargarUsuarios();
            return null;
        } catch (SQLException e) {
            return ConexionBD.mensajeError(e);
        }
    }

    public String cambiarEstado(int id, boolean activo) {
        if (!activo) {
            if (id == sesion.getId()) {
                return "No puede desactivar su propia cuenta.";
            }
            String error = validarNoEsUltimoIngeniero(id,
                    "No se puede desactivar al último Ingeniero activo.");
            if (error != null) {
                return error;
            }
        }
        try {
            UsuarioDAO.cambiarEstado(id, activo);
            cargarUsuarios();
            return null;
        } catch (SQLException e) {
            return ConexionBD.mensajeError(e);
        }
    }

    public String eliminar(int id) {
        if (id == sesion.getId()) {
            return "No puede eliminar su propia cuenta.";
        }
        String error = validarNoEsUltimoIngeniero(id,
                "No se puede eliminar al último Ingeniero activo.");
        if (error != null) {
            return error;
        }
        try {
            UsuarioDAO.eliminar(id);
            cargarUsuarios();
            return null;
        } catch (SQLException e) {
            return ConexionBD.mensajeError(e);
        }
    }

    /**
     * Devuelve el mensaje indicado si el usuario es un Ingeniero activo y no
     * queda ningún otro; null si la operación no deja al sistema sin Ingeniero.
     */
    private String validarNoEsUltimoIngeniero(int id, String mensaje) {
        Usuario actual = UsuarioDAO.obtenerPorId(id);
        if (actual == null || !actual.esIngeniero() || !actual.isActivo()) {
            return null;
        }
        return UsuarioDAO.contarIngenierosActivos() <= 1 ? mensaje : null;
    }

    /**
     * Genera un código aleatorio de 10 caracteres para la cuenta indicada y lo
     * guarda cifrado en la BD. El Ingeniero se lo entrega al usuario, que lo
     * canjea en "Recuperar contraseña". Generar otro invalida el anterior.
     */
    public ResultadoCodigo generarCodigoRecuperacion(String usuario) {
        StringBuilder sb = new StringBuilder(LONGITUD_CODIGO);
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            sb.append(CARACTERES_CODIGO.charAt(RANDOM.nextInt(CARACTERES_CODIGO.length())));
        }
        String codigo = sb.toString();
        try {
            boolean cuentaValida = UsuarioDAO.guardarCodigoRecuperacion(
                    usuario, HashUtil.hashSHA256(codigo), LocalDateTime.now().plus(VALIDEZ_CODIGO));
            if (!cuentaValida) {
                return new ResultadoCodigo(null, "La cuenta no existe o está inactiva.");
            }
            return new ResultadoCodigo(codigo, null);
        } catch (SQLException e) {
            return new ResultadoCodigo(null, ConexionBD.mensajeError(e));
        }
    }
}
