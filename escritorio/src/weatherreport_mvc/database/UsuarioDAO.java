package weatherreport_mvc.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import weatherreport_mvc.models.Usuario;

/**
 * DAO de acceso a la tabla 'usuarios'.
 */
public class UsuarioDAO {

    /** Cuenta activa con ese nombre de usuario (la contraseña se verifica aparte). */
    public static Usuario buscarActivoPorUsuario(String usuario) throws SQLException {
        String sql = "SELECT id, nombre_completo, usuario, contrasena_hash, rol, "
                + "fecha_registro, activo FROM usuarios "
                + "WHERE usuario = ? AND activo = 1";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public static boolean existeUsuario(String usuario) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE usuario = ?";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.existeUsuario: " + e.getMessage());
        }
        return false;
    }

    /** Indica si otro usuario (distinto de idExcluido) ya usa ese nombre. */
    public static boolean existeUsuarioOtro(String usuario, int idExcluido) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE usuario = ? AND id <> ?";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, idExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.existeUsuarioOtro: " + e.getMessage());
        }
        return false;
    }

    public static List<Usuario> obtenerTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre_completo, usuario, contrasena_hash, rol, "
                + "fecha_registro, activo FROM usuarios ORDER BY id ASC";
        try (Connection conn = ConexionBD.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.obtenerTodos: " + e.getMessage());
        }
        return lista;
    }

    public static Usuario obtenerPorId(int id) {
        String sql = "SELECT id, nombre_completo, usuario, contrasena_hash, rol, "
                + "fecha_registro, activo FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.obtenerPorId: " + e.getMessage());
        }
        return null;
    }

    public static int insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre_completo, usuario, contrasena_hash, rol, activo) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombreCompleto());
            ps.setString(2, u.getUsuario());
            ps.setString(3, u.getContrasenaHash());
            ps.setString(4, u.getRol());
            ps.setInt(5, u.isActivo() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public static void actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET nombre_completo = ?, usuario = ?, rol = ?, activo = ?";
        if (u.getContrasenaHash() != null && !u.getContrasenaHash().isEmpty()) {
            sql += ", contrasena_hash = ?";
        }
        sql += " WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, u.getNombreCompleto());
            ps.setString(i++, u.getUsuario());
            ps.setString(i++, u.getRol());
            ps.setInt(i++, u.isActivo() ? 1 : 0);
            if (u.getContrasenaHash() != null && !u.getContrasenaHash().isEmpty()) {
                ps.setString(i++, u.getContrasenaHash());
            }
            ps.setInt(i, u.getId());
            ps.executeUpdate();
        }
    }

    /** Cantidad de cuentas activas con rol Ingeniero. */
    public static int contarIngenierosActivos() {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE rol = ? AND activo = 1";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Usuario.ROL_INGENIERO);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.contarIngenierosActivos: " + e.getMessage());
        }
        return 0;
    }

    public static void cambiarEstado(int id, boolean activo) throws SQLException {
        String sql = "UPDATE usuarios SET activo = ? WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, activo ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Guarda el hash del código de recuperación y su expiración para la cuenta
     * indicada. Generar un nuevo código sobrescribe el anterior.
     *
     * @return true si la cuenta existe y está activa.
     */
    public static boolean guardarCodigoRecuperacion(String usuario, String hashCodigo,
                                                    LocalDateTime expiracion)
            throws SQLException {
        String sql = "UPDATE usuarios SET codigo_recuperacion = ?, codigo_expiracion = ? "
                + "WHERE usuario = ? AND activo = 1";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashCodigo);
            ps.setTimestamp(2, Timestamp.valueOf(expiracion));
            ps.setString(3, usuario);
            return ps.executeUpdate() > 0;
        }
    }

    /** @return true si el código coincide con el guardado y aún no expiró. */
    public static boolean validarCodigoRecuperacion(String usuario, String hashCodigo) {
        String sql = "SELECT codigo_expiracion FROM usuarios "
                + "WHERE usuario = ? AND activo = 1 AND codigo_recuperacion = ?";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, hashCodigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp exp = rs.getTimestamp("codigo_expiracion");
                    return exp != null && exp.toLocalDateTime().isAfter(LocalDateTime.now());
                }
            }
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.validarCodigoRecuperacion: " + e.getMessage());
        }
        return false;
    }

    /** Invalida el código de recuperación de la cuenta (uso único). */
    public static void limpiarCodigoRecuperacion(String usuario) throws SQLException {
        String sql = "UPDATE usuarios SET codigo_recuperacion = NULL, codigo_expiracion = NULL "
                + "WHERE usuario = ?";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.executeUpdate();
        }
    }

    public static void cambiarContrasena(String usuario, String nuevoHash) throws SQLException {
        String sql = "UPDATE usuarios SET contrasena_hash = ? WHERE usuario = ?";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoHash);
            ps.setString(2, usuario);
            ps.executeUpdate();
        }
    }

    private static Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setUsuario(rs.getString("usuario"));
        u.setContrasenaHash(rs.getString("contrasena_hash"));
        u.setRol(rs.getString("rol"));
        u.setActivo(rs.getInt("activo") == 1);
        if (rs.getTimestamp("fecha_registro") != null) {
            u.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        }
        return u;
    }
}
