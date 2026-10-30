package weatherreport_mvc.database;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Respaldo y restauración de la base por JDBC, sin mysqldump ni mysql.exe: los clientes de
 * XAMPP (MariaDB 10.4) no saben autenticarse con MySQL 8.4 (Aiven). El archivo generado es
 * SQL estándar, compatible con el cliente mysql, y la restauración acepta también los
 * respaldos antiguos hechos con mysqldump.
 */
public final class RespaldoBD {

    /** Filas por cada INSERT del respaldo. */
    private static final int FILAS_POR_INSERT = 200;

    /** DEFINER=`root`@`localhost` de triggers/rutinas: en un servidor administrado no se puede usar. */
    private static final Pattern DEFINER = Pattern.compile(
            "(/\\*!\\d+\\s+)?DEFINER\\s*=\\s*(`[^`]*`|'[^']*'|\\S+)@(`[^`]*`|'[^']*'|\\S+)(\\s*\\*/)?",
            Pattern.CASE_INSENSITIVE);

    /** El respaldo siempre se restaura en la base configurada, no en la del archivo. */
    private static final Pattern BASE_DEL_ARCHIVO = Pattern.compile(
            "^(CREATE\\s+DATABASE|USE)\\s", Pattern.CASE_INSENSITIVE);

    private RespaldoBD() {
    }

    /**
     * Escribe todas las tablas de la base (estructura y datos) como SQL. Igual que mysqldump,
     * las columnas TIMESTAMP se escriben en UTC y el archivo fija TIME_ZONE='+00:00', así se
     * restauran al mismo instante en cualquier servidor. Cambia la zona horaria de
     * {@code conn}: usar una conexión solo para el respaldo.
     */
    public static void exportar(Connection conn, Writer out) throws SQLException, IOException {
        List<String> tablas = new ArrayList<>();
        try (Statement st = conn.createStatement()) {
            st.execute("SET time_zone = '+00:00'");
            try (ResultSet rs = st.executeQuery("SHOW FULL TABLES WHERE Table_type = 'BASE TABLE'")) {
                while (rs.next()) {
                    tablas.add(rs.getString(1));
                }
            }
        }
        out.write("-- Respaldo de " + conn.getCatalog() + " generado por Weather Report el "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
        out.write("SET NAMES utf8mb4;\nSET @OLD_TIME_ZONE = @@TIME_ZONE;\nSET TIME_ZONE = '+00:00';\n"
                + "SET FOREIGN_KEY_CHECKS = 0;\nSET UNIQUE_CHECKS = 0;\n\n");
        for (String tabla : tablas) {
            exportarTabla(conn, tabla, out);
        }
        out.write("SET FOREIGN_KEY_CHECKS = 1;\nSET UNIQUE_CHECKS = 1;\nSET TIME_ZONE = @OLD_TIME_ZONE;\n");
        out.flush();
    }

    private static void exportarTabla(Connection conn, String tabla, Writer out)
            throws SQLException, IOException {
        String nombre = "`" + tabla.replace("`", "``") + "`";
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SHOW CREATE TABLE " + nombre)) {
            rs.next();
            out.write("DROP TABLE IF EXISTS " + nombre + ";\n" + rs.getString(2) + ";\n");
        }
        // El nombre viene de SHOW FULL TABLES (no del usuario) y va entre `` con escape;
        // un PreparedStatement no admite nombres de tabla como parámetro
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM " + nombre)) { // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
            ResultSetMetaData md = rs.getMetaData();
            int columnas = md.getColumnCount();
            int enLote = 0;
            while (rs.next()) {
                out.write(enLote == 0 ? "INSERT INTO " + nombre + " VALUES\n(" : ",\n(");
                for (int i = 1; i <= columnas; i++) {
                    if (i > 1) {
                        out.write(',');
                    }
                    out.write(valorSql(rs, i, md.getColumnType(i)));
                }
                out.write(')');
                if (++enLote == FILAS_POR_INSERT) {
                    out.write(";\n");
                    enLote = 0;
                }
            }
            if (enLote > 0) {
                out.write(";\n");
            }
        }
        out.write('\n');
    }

    private static String valorSql(ResultSet rs, int col, int tipo) throws SQLException {
        switch (tipo) {
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> {
                return hex(rs.getBytes(col));
            }
            case Types.BIT, Types.BOOLEAN -> {
                // BIT(1) y TINYINT(1) llegan como Boolean; BIT(n>1) como byte[]
                Object v = rs.getObject(col);
                if (v instanceof Boolean b) {
                    return b ? "1" : "0";
                }
                return v instanceof byte[] b ? hex(b) : v == null ? "NULL" : v.toString();
            }
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT, Types.REAL,
                    Types.FLOAT, Types.DOUBLE, Types.DECIMAL, Types.NUMERIC -> {
                String v = rs.getString(col);
                return v == null ? "NULL" : v;
            }
            default -> {
                String v = rs.getString(col);
                return v == null ? "NULL" : texto(v);
            }
        }
    }

    private static String hex(byte[] b) {
        if (b == null) {
            return "NULL";
        }
        if (b.length == 0) {
            return "''";
        }
        StringBuilder sb = new StringBuilder(b.length * 2 + 2).append("0x");
        for (byte x : b) {
            sb.append(String.format("%02X", x));
        }
        return sb.toString();
    }

    /** Literal de texto con los mismos escapes que mysqldump. */
    private static String texto(String v) {
        StringBuilder sb = new StringBuilder(v.length() + 8).append('\'');
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '\'' -> sb.append("\\'");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\0' -> sb.append("\\0");
                case '\u001A' -> sb.append("\\Z");
                default -> sb.append(c);
            }
        }
        return sb.append('\'').toString();
    }

    /**
     * Ejecuta un archivo SQL (de {@link #exportar} o de mysqldump) sentencia por sentencia.
     * Se detiene en el primer error indicando el número de sentencia.
     *
     * @return número de sentencias ejecutadas
     */
    public static int importar(Connection conn, String sql) throws SQLException {
        int n = 0;
        try (Statement st = conn.createStatement()) {
            for (String sentencia : dividir(sql)) {
                String limpia = DEFINER.matcher(sentencia).replaceAll("").trim();
                if (sinContenido(limpia) || BASE_DEL_ARCHIVO.matcher(limpia).find()) {
                    continue;
                }
                n++;
                try {
                    // Restaurar = ejecutar el script del archivo (como mysql < respaldo.sql); el
                    // módulo de Respaldo solo lo abre el rol Ingeniero (MainController.permiteModulo)
                    st.execute(limpia); // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
                } catch (SQLException e) {
                    String inicio = limpia.length() > 120 ? limpia.substring(0, 120) + "..." : limpia;
                    throw new SQLException("Sentencia " + n + " (" + inicio + "): " + e.getMessage(),
                            e.getSQLState(), e.getErrorCode(), e);
                }
            }
        }
        return n;
    }

    /**
     * Divide el script en sentencias respetando textos entre comillas, comentarios y las
     * líneas DELIMITER que usa mysqldump para triggers. Los comentarios de línea se quitan;
     * los de bloque se conservan porque los versionados (/*!40101 ... *&#47;) sí se ejecutan.
     */
    static List<String> dividir(String sql) {
        List<String> sentencias = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        String delimitador = ";";
        int i = 0;
        int largo = sql.length();
        while (i < largo) {
            if (actual.toString().isBlank() && empiezaLinea(sql, i)) {
                int finLinea = finDeLinea(sql, i);
                String linea = sql.substring(i, finLinea).trim();
                if (linea.regionMatches(true, 0, "DELIMITER ", 0, 10)) {
                    delimitador = linea.substring(10).trim();
                    actual.setLength(0);
                    i = finLinea;
                    continue;
                }
            }
            char c = sql.charAt(i);
            if (c == '\'' || c == '"' || c == '`') {
                int fin = finDeCita(sql, i, c);
                actual.append(sql, i, fin);
                i = fin;
            } else if (c == '#' || (c == '-' && sql.startsWith("-- ", i))
                    || (c == '-' && sql.startsWith("--", i) && (i + 2 == largo || sql.charAt(i + 2) == '\n'
                    || sql.charAt(i + 2) == '\r'))) {
                i = finDeLinea(sql, i);
            } else if (c == '/' && sql.startsWith("/*", i)) {
                int fin = sql.indexOf("*/", i + 2);
                fin = fin < 0 ? largo : fin + 2;
                actual.append(sql, i, fin);
                i = fin;
            } else if (sql.startsWith(delimitador, i)) {
                sentencias.add(actual.toString());
                actual.setLength(0);
                i += delimitador.length();
            } else {
                actual.append(c);
                i++;
            }
        }
        if (!actual.toString().isBlank()) {
            sentencias.add(actual.toString());
        }
        return sentencias;
    }

    private static boolean empiezaLinea(String sql, int i) {
        int j = i - 1;
        while (j >= 0 && (sql.charAt(j) == ' ' || sql.charAt(j) == '\t')) {
            j--;
        }
        return j < 0 || sql.charAt(j) == '\n' || sql.charAt(j) == '\r';
    }

    private static int finDeLinea(String sql, int i) {
        int fin = sql.indexOf('\n', i);
        return fin < 0 ? sql.length() : fin + 1;
    }

    /** Posición después de la comilla que cierra; con \ y comilla doble como escapes. */
    private static int finDeCita(String sql, int inicio, char comilla) {
        int i = inicio + 1;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (c == '\\' && comilla != '`') {
                i += 2;
            } else if (c == comilla) {
                if (i + 1 < sql.length() && sql.charAt(i + 1) == comilla) {
                    i += 2;
                } else {
                    return i + 1;
                }
            } else {
                i++;
            }
        }
        return sql.length();
    }

    /** true si solo quedan espacios o comentarios normales (no versionados /*! ... *&#47;). */
    private static boolean sinContenido(String s) {
        String sinComentarios = s.replaceAll("(?s)/\\*(?!!).*?\\*/", "");
        return sinComentarios.isBlank();
    }
}
