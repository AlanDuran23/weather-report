package weatherreport_mvc.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import weatherreport_mvc.models.ConfiguracionSistema;

/**
 * Administra la conexión a la base de datos MySQL.
 * La configuración se obtiene de {@link ConfiguracionSistema} y puede ser
 * recargada en tiempo de ejecución mediante {@link #aplicarConfiguracion}.
 */
public final class ConexionBD {

    private static String host = "localhost";
    private static String puerto = "3306";
    private static String usuario = "root";
    private static String contrasena = "";
    private static String nombreBD = "weather_db";

    private static final ConfiguracionSistema CONFIG = ConfiguracionSistema.cargar();

    /** DDL de la tabla de registros meteorológicos (una fila por medición). */
    private static final String CREAR_TABLA_REPORT = "CREATE TABLE IF NOT EXISTS report ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "fecha VARCHAR(10) NULL, hora VARCHAR(8) NULL, "
            + "temp_out DOUBLE NULL, hi_temp DOUBLE NULL, low_temp DOUBLE NULL, "
            + "dew_pt DOUBLE NULL, wind_chill DOUBLE NULL, heat_index DOUBLE NULL, "
            + "thw_index DOUBLE NULL, out_hum DOUBLE NULL, wind_speed DOUBLE NULL, "
            + "wind_dir VARCHAR(8) NULL, wind_run DOUBLE NULL, hi_speed DOUBLE NULL, "
            + "hi_dir VARCHAR(8) NULL, bar DOUBLE NULL, rain DOUBLE NULL, "
            + "rain_rate DOUBLE NULL, heat_dd DOUBLE NULL, cool_dd DOUBLE NULL, "
            + "in_temp DOUBLE NULL, in_hum DOUBLE NULL, in_dew DOUBLE NULL, "
            + "in_heat DOUBLE NULL, in_emc DOUBLE NULL, in_air_density DOUBLE NULL, "
            + "wind_samp INT NULL, iss_tx INT NULL, arc_int INT NULL, "
            + "recept DOUBLE NULL, Observaciones TEXT NULL) ENGINE=InnoDB";

    static {
        aplicarConfiguracion(CONFIG);
    }

    private ConexionBD() {
    }

    public static void aplicarConfiguracion(ConfiguracionSistema cfg) {
        if (cfg == null) {
            return;
        }
        host = cfg.getHostBD();
        puerto = cfg.getPuertoBD();
        usuario = cfg.getUsuarioBD();
        contrasena = cfg.getContrasenaBD();
        nombreBD = cfg.getNombreBD();
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver MySQL JDBC.", e);
        }
        String url = "jdbc:mysql://" + host + ":" + puerto + "/" + nombreBD
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
                + "&characterEncoding=utf8&useUnicode=true"
                + "&connectTimeout=5000";
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    public static boolean probarConexion() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Ajusta el esquema de la base de datos: convierte el rol a VARCHAR,
     * garantiza las columnas usadas por la recuperación de contraseña y crea
     * la tabla 'report' si no existe. Es idempotente y tolerante a errores.
     */
    public static void asegurarEsquema() {
        try (Connection conn = getConnection()) {
            migrarRol(conn);
            migrarColumnaHash(conn);
            migrarColumnasRecuperacion(conn);
            asegurarTablaReport(conn);
        } catch (SQLException e) {
            System.err.println("ConexionBD.asegurarEsquema: " + e.getMessage());
        }
    }

    /** Crea la tabla 'report' si aún no existe (para instalaciones limpias). */
    private static void asegurarTablaReport(Connection conn) {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, "report", null)) {
            if (rs.next()) {
                return;
            }
        } catch (SQLException e) {
            System.err.println("ConexionBD.asegurarTablaReport: " + e.getMessage());
            return;
        }
        try (java.sql.Statement st = conn.createStatement()) {
            st.executeUpdate(CREAR_TABLA_REPORT);
        } catch (SQLException e) {
            System.err.println("ConexionBD.asegurarTablaReport: " + e.getMessage());
        }
    }

    private static void migrarRol(Connection conn) {
        String sql = "ALTER TABLE usuarios MODIFY rol VARCHAR(20) NOT NULL DEFAULT 'Consulta'";
        try (java.sql.Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
            st.executeUpdate("UPDATE usuarios SET rol = 'Ingeniero' WHERE rol = 'Administrador'");
        } catch (SQLException e) {
            System.err.println("ConexionBD.migrarRol: " + e.getMessage());
        }
    }

    /** El hash PBKDF2 con sal no cabe en CHAR(64); ensancha la columna (idempotente). */
    private static void migrarColumnaHash(Connection conn) {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "usuarios", "contrasena_hash")) {
            if (rs.next() && rs.getInt("COLUMN_SIZE") >= 255) {
                return;
            }
        } catch (SQLException e) {
            System.err.println("ConexionBD.migrarColumnaHash: " + e.getMessage());
            return;
        }
        try (java.sql.Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE usuarios MODIFY contrasena_hash VARCHAR(255) NOT NULL");
        } catch (SQLException e) {
            System.err.println("ConexionBD.migrarColumnaHash: " + e.getMessage());
        }
    }

    private static void migrarColumnasRecuperacion(Connection conn) {
        boolean tieneCodigo = false;
        boolean tieneExpiracion = false;
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "usuarios", null)) {
            while (rs.next()) {
                String nombre = rs.getString("COLUMN_NAME");
                if ("codigo_recuperacion".equals(nombre)) {
                    tieneCodigo = true;
                } else if ("codigo_expiracion".equals(nombre)) {
                    tieneExpiracion = true;
                }
            }
        } catch (SQLException e) {
            System.err.println("ConexionBD.migrarColumnasRecuperacion: " + e.getMessage());
            return;
        }
        try (java.sql.Statement st = conn.createStatement()) {
            if (!tieneCodigo) {
                st.executeUpdate("ALTER TABLE usuarios ADD COLUMN codigo_recuperacion CHAR(64) NULL");
            }
            if (!tieneExpiracion) {
                st.executeUpdate("ALTER TABLE usuarios ADD COLUMN codigo_expiracion DATETIME NULL");
            }
        } catch (SQLException e) {
            System.err.println("ConexionBD.migrarColumnasRecuperacion: " + e.getMessage());
        }
    }

    public static String mensajeError(SQLException e) {
        return "Error de base de datos: " + e.getMessage();
    }
}
