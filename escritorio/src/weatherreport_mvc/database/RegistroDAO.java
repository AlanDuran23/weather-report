package weatherreport_mvc.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import weatherreport_mvc.models.RegistroMeteorologico;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.Estadistica;

/**
 * DAO de acceso a la tabla 'report' (datos de la estación meteorológica).
 * La fecha y la hora se guardan por separado en formato de texto propio de
 * la estación (fecha dd/MM/aa y hora de 12 h con sufijo a/p).
 */
public class RegistroDAO {

    public static final int TOTAL_MESES = 12;

    /** Un punto de la serie semanal: promedios por día. */
    public record PuntoSemanal(LocalDate dia, double humedad, double viento,
                               double lluvia, double temperatura, double presion,
                               double rocio) {
    }

    /** Resumen mensual: lluvia acumulada y promedios. */
    public record AcumuladoMes(int mes, double lluvia, double humedad, double viento,
                               double temperatura, double presion, double rocio) {
    }

    /** Resumen anual: lluvia acumulada y promedios. */
    public record AcumuladoAnio(int anio, double lluvia, double humedad, double viento,
                                double temperatura, double presion, double rocio) {
    }

    private static final String[] COLUMNAS_SELECCION = {
        "id", "fecha", "hora", "temp_out", "hi_temp", "low_temp", "dew_pt",
        "wind_chill", "heat_index", "thw_index", "out_hum", "wind_speed",
        "wind_dir", "wind_run", "hi_speed", "hi_dir", "bar", "rain",
        "rain_rate", "heat_dd", "cool_dd", "in_temp", "in_hum", "in_dew",
        "in_heat", "in_emc", "in_air_density", "wind_samp", "iss_tx",
        "arc_int", "recept", "Observaciones"
    };

    private static final String COLUMNAS_SELECT = String.join(", ", COLUMNAS_SELECCION);
    private static final String COLUMNAS_INSERT = COLUMNAS_SELECT.substring(
            COLUMNAS_SELECT.indexOf("fecha"));
    private static final String REPETIDOR = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
            + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
    private static final String COLUMNAS_UPDATE = "fecha = ?, hora = ?, temp_out = ?, "
            + "hi_temp = ?, low_temp = ?, dew_pt = ?, wind_chill = ?, heat_index = ?, "
            + "thw_index = ?, out_hum = ?, wind_speed = ?, wind_dir = ?, wind_run = ?, "
            + "hi_speed = ?, hi_dir = ?, bar = ?, rain = ?, rain_rate = ?, heat_dd = ?, "
            + "cool_dd = ?, in_temp = ?, in_hum = ?, in_dew = ?, in_heat = ?, in_emc = ?, "
            + "in_air_density = ?, wind_samp = ?, iss_tx = ?, arc_int = ?, recept = ?, "
            + "Observaciones = ?";

    public static void insertar(RegistroMeteorologico r) throws SQLException {
        String sql = "INSERT INTO report (" + COLUMNAS_INSERT + ") VALUES ("
                + REPETIDOR + ")";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            fijarParametros(ps, r);
            ps.executeUpdate();
        }
    }

    /**
     * Inserta varios registros en una sola transacción, omitiendo los que ya
     * existen en la BD (misma fecha y hora) o que se repiten dentro de la lista,
     * igual que el importador del panel web.
     *
     * @return cantidad de registros insertados.
     */
    public static int insertarMasivo(List<RegistroMeteorologico> registros) throws SQLException {
        String sql = "INSERT INTO report (" + COLUMNAS_INSERT + ") VALUES ("
                + REPETIDOR + ")";
        Set<LocalDateTime> existentes = new HashSet<>();
        for (RegistroMeteorologico r : obtenerTodos()) {
            existentes.add(LocalDateTime.of(r.getFecha(), r.getHora()));
        }
        int insertados = 0;
        try (Connection conn = ConexionBD.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (RegistroMeteorologico r : registros) {
                    if (r.getFecha() != null && r.getHora() != null
                            && !existentes.add(LocalDateTime.of(r.getFecha(), r.getHora()))) {
                        continue;
                    }
                    fijarParametros(ps, r);
                    ps.addBatch();
                    insertados++;
                }
                ps.executeBatch();
                conn.commit();
                return insertados;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static RegistroMeteorologico obtenerPorId(int id) {
        String sql = "SELECT " + COLUMNAS_SELECT + " FROM report WHERE id = ? LIMIT 1";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("RegistroDAO.obtenerPorId: " + e.getMessage());
        }
        return null;
    }

    /** Actualiza todos los campos de un registro de la estación. */
    public static void actualizar(RegistroMeteorologico r) throws SQLException {
        String sql = "UPDATE report SET " + COLUMNAS_UPDATE + " WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            fijarParametros(ps, r);
            ps.setInt(32, r.getId());
            ps.executeUpdate();
        }
    }

    /** Indica si ya hay un registro con esa fecha y hora (distinto de idExcluido). */
    public static boolean existeFechaHora(LocalDate fecha, LocalTime hora, int idExcluido) {
        for (RegistroMeteorologico r : obtenerTodos()) {
            if (r.getId() != idExcluido && r.getFecha().equals(fecha)
                    && r.getHora().equals(hora)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Todos los registros con fecha y hora válidas, del más reciente al más
     * antiguo. La lectura completa se reutiliza mientras la tabla no cambie
     * (se compara su CHECKSUM, que se calcula en el servidor), para que el
     * refresco periódico del Dashboard y las gráficas no transfieran y
     * conviertan toda la tabla cada vez.
     */
    public static synchronized List<RegistroMeteorologico> obtenerTodos() {
        try (Connection conn = ConexionBD.getConnection()) {
            Long firma = firmaTabla(conn);
            if (firma == null || !firma.equals(firmaCache) || cache == null) {
                cache = leerTodos(conn);
                firmaCache = firma;
            }
        } catch (SQLException e) {
            System.err.println("RegistroDAO.obtenerTodos: " + e.getMessage());
            return new ArrayList<>();
        }
        return new ArrayList<>(cache);
    }

    private static List<RegistroMeteorologico> cache;
    private static Long firmaCache;

    private static Long firmaTabla(Connection conn) {
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("CHECKSUM TABLE report")) {
            if (rs.next()) {
                long valor = rs.getLong("Checksum");
                return rs.wasNull() ? null : valor;
            }
        } catch (SQLException e) {
            System.err.println("RegistroDAO.firmaTabla: " + e.getMessage());
        }
        return null;
    }

    private static List<RegistroMeteorologico> leerTodos(Connection conn) throws SQLException {
        List<RegistroMeteorologico> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS_SELECT + " FROM report";
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        lista.removeIf(r -> r.getFecha() == null || r.getHora() == null);
        lista.sort(Comparator.comparing(RegistroMeteorologico::getFecha)
                .thenComparing(RegistroMeteorologico::getHora).reversed());
        return lista;
    }

    public static List<RegistroMeteorologico> buscar(String texto, Integer mes, String campo) {
        List<RegistroMeteorologico> lista = obtenerTodos();
        if (texto != null && !texto.trim().isEmpty()) {
            String patron = texto.trim().toLowerCase();
            String campoLow = campo == null ? "Todo" : campo;
            lista.removeIf(r -> !coincide(r, patron, campoLow));
        }
        if (mes != null) {
            lista.removeIf(r -> r.getFecha() == null || r.getFecha().getMonthValue() != mes);
        }
        return lista;
    }

    private static boolean coincide(RegistroMeteorologico r, String patron, String campo) {
        String fecha = r.getFecha() == null ? "" : DateUtil.formatoFechaEstacion(r.getFecha());
        String hora = r.getHora() == null ? "" : DateUtil.formatoHoraEstacion(r.getHora());
        String obs = r.getObservaciones() == null ? "" : r.getObservaciones();
        String dir = r.getDireccionViento() == null ? "" : r.getDireccionViento();
        switch (campo.toLowerCase()) {
            case "fecha" -> {
                return fecha.contains(patron);
            }
            case "hora" -> {
                return hora.toLowerCase().contains(patron);
            }
            case "temperatura" -> {
                return Estadistica.fmtRoot("%.1f", r.getTempExterior()).contains(patron);
            }
            case "humedad" -> {
                return Estadistica.fmtRoot("%.1f", r.getHumedad()).contains(patron);
            }
            case "viento" -> {
                return Estadistica.fmtRoot("%.1f", r.getVelocidadViento()).contains(patron);
            }
            case "presión" -> {
                return Estadistica.fmtRoot("%.1f", r.getPresionBarometrica()).contains(patron);
            }
            case "dirección" -> {
                String raf = r.getDireccionRafaga() == null ? "" : r.getDireccionRafaga();
                return (dir + " " + raf).toLowerCase().contains(patron);
            }
            case "observaciones" -> {
                return obs.toLowerCase().contains(patron);
            }
            default -> {
                return obs.toLowerCase().contains(patron)
                        || fecha.contains(patron)
                        || hora.toLowerCase().contains(patron)
                        || Estadistica.fmtRoot("%.1f", r.getTempExterior()).contains(patron)
                        || Estadistica.fmtRoot("%.1f", r.getHumedad()).contains(patron)
                        || Estadistica.fmtRoot("%.1f", r.getVelocidadViento()).contains(patron)
                        || Estadistica.fmtRoot("%.1f", r.getPresionBarometrica()).contains(patron)
                        || Estadistica.fmtRoot("%.1f", r.getLluvia()).contains(patron)
                        || dir.toLowerCase().contains(patron);
            }
        }
    }

    public static List<RegistroMeteorologico> obtenerPorDia(LocalDate dia) {
        List<RegistroMeteorologico> lista = obtenerTodos();
        lista.removeIf(r -> r.getFecha() == null || r.getHora() == null
                || !r.getFecha().equals(dia));
        lista.sort(Comparator.comparing(RegistroMeteorologico::getHora));
        return lista;
    }

    public static List<RegistroMeteorologico> obtenerEntre(LocalDateTime inicio, LocalDateTime fin) {
        List<RegistroMeteorologico> lista = obtenerTodos();
        lista.removeIf(r -> r.getFecha() == null || r.getHora() == null);
        lista.removeIf(r -> {
            LocalDateTime fechaHora = LocalDateTime.of(r.getFecha(), r.getHora());
            return fechaHora.isBefore(inicio) || fechaHora.isAfter(fin);
        });
        lista.sort(Comparator.comparing(RegistroMeteorologico::getFecha)
                .thenComparing(RegistroMeteorologico::getHora));
        return lista;
    }

    public static RegistroMeteorologico obtenerUltimo() {
        List<RegistroMeteorologico> lista = obtenerTodos();
        return lista.isEmpty() ? null : lista.get(0);
    }

    public static long contar() {
        String sql = "SELECT COUNT(*) FROM report";
        try (Connection conn = ConexionBD.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("RegistroDAO.contar: " + e.getMessage());
        }
        return 0;
    }

    /** Promedios por día de los últimos 7 días (la lluvia es el total del día). */
    public static List<PuntoSemanal> obtenerSerieSemanal() {
        LocalDate hoy = LocalDate.now();
        return obtenerSerieEntre(hoy.minusDays(6), hoy);
    }

    private interface ValorExtractor {
        double extraer(RegistroMeteorologico r);
    }

    /** Promedio ignorando valores faltantes; NaN si no hay ninguno. */
    private static double promedio(List<RegistroMeteorologico> lista, ValorExtractor ex) {
        Estadistica e = new Estadistica();
        for (RegistroMeteorologico r : lista) {
            e.agregar(ex.extraer(r));
        }
        return e.promedio();
    }

    /** Suma ignorando valores faltantes; NaN si no hay ninguno. */
    private static double suma(List<RegistroMeteorologico> lista, ValorExtractor ex) {
        Estadistica e = new Estadistica();
        for (RegistroMeteorologico r : lista) {
            e.agregar(ex.extraer(r));
        }
        return e.suma();
    }

    /** Resumen mensual del año indicado: lluvia acumulada y promedios de humedad y viento. */
    public static List<AcumuladoMes> obtenerAcumuladoMensual(int anio) {
        Map<Integer, List<RegistroMeteorologico>> porMes = new LinkedHashMap<>();
        for (RegistroMeteorologico r : obtenerTodos()) {
            LocalDate d = r.getFecha();
            if (d != null && d.getYear() == anio) {
                porMes.computeIfAbsent(d.getMonthValue(), k -> new ArrayList<>()).add(r);
            }
        }
        List<AcumuladoMes> lista = new ArrayList<>();
        for (Map.Entry<Integer, List<RegistroMeteorologico>> e : porMes.entrySet()) {
            List<RegistroMeteorologico> delMes = e.getValue();
            double lluvia = suma(delMes, r -> r.getLluvia());
            double hum = promedio(delMes, r -> r.getHumedad());
            double viento = promedio(delMes, r -> r.getVelocidadViento());
            double temp = promedio(delMes, r -> r.getTempExterior());
            double presion = promedio(delMes, r -> r.getPresionBarometrica());
            double rocio = promedio(delMes, r -> r.getPuntoRocio());
            lista.add(new AcumuladoMes(e.getKey(), lluvia, hum, viento, temp, presion, rocio));
        }
        lista.sort(Comparator.comparingInt(AcumuladoMes::mes));
        return lista;
    }

    /** Resumen anual: lluvia acumulada y promedios de humedad y viento por año. */
    public static List<AcumuladoAnio> obtenerAcumuladoAnual() {
        Map<Integer, List<RegistroMeteorologico>> porAnio = new LinkedHashMap<>();
        for (RegistroMeteorologico r : obtenerTodos()) {
            LocalDate d = r.getFecha();
            if (d != null) {
                porAnio.computeIfAbsent(d.getYear(), k -> new ArrayList<>()).add(r);
            }
        }
        List<AcumuladoAnio> lista = new ArrayList<>();
        for (Map.Entry<Integer, List<RegistroMeteorologico>> e : porAnio.entrySet()) {
            List<RegistroMeteorologico> delAnio = e.getValue();
            double lluvia = suma(delAnio, r -> r.getLluvia());
            double hum = promedio(delAnio, r -> r.getHumedad());
            double viento = promedio(delAnio, r -> r.getVelocidadViento());
            double temp = promedio(delAnio, r -> r.getTempExterior());
            double presion = promedio(delAnio, r -> r.getPresionBarometrica());
            double rocio = promedio(delAnio, r -> r.getPuntoRocio());
            lista.add(new AcumuladoAnio(e.getKey(), lluvia, hum, viento, temp, presion, rocio));
        }
        lista.sort(Comparator.comparingInt(AcumuladoAnio::anio));
        return lista;
    }

    /** Años distintos con registros, ordenados de forma descendente. */
    public static List<Integer> obtenerAnios() {
        Map<Integer, Boolean> porAnio = new LinkedHashMap<>();
        for (RegistroMeteorologico r : obtenerTodos()) {
            LocalDate d = r.getFecha();
            if (d != null) {
                porAnio.putIfAbsent(d.getYear(), Boolean.TRUE);
            }
        }
        List<Integer> anios = new ArrayList<>(porAnio.keySet());
        anios.sort(Comparator.reverseOrder());
        return anios;
    }

    /** Última fecha con registros; {@code null} si no hay datos. */
    public static LocalDate obtenerUltimaFecha() {
        List<RegistroMeteorologico> todos = obtenerTodos();
        return todos.isEmpty() ? null : todos.get(0).getFecha();
    }

    /** Valores por día en el rango [inicio, fin]: promedios y lluvia total del día. */
    public static List<PuntoSemanal> obtenerSerieEntre(LocalDate inicio, LocalDate fin) {
        List<PuntoSemanal> lista = new ArrayList<>();
        Map<LocalDate, List<RegistroMeteorologico>> porDia = new LinkedHashMap<>();
        for (RegistroMeteorologico r : obtenerTodos()) {
            LocalDate d = r.getFecha();
            if (d != null && !d.isBefore(inicio) && !d.isAfter(fin)) {
                porDia.computeIfAbsent(d, k -> new ArrayList<>()).add(r);
            }
        }
        porDia.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> lista.add(agruparDia(e.getKey(), e.getValue())));
        return lista;
    }

    /** Valores por día del mes indicado: promedios y lluvia total del día. */
    public static List<PuntoSemanal> obtenerSerieMensual(int anio, int mes) {
        LocalDate inicio = LocalDate.of(anio, mes, 1);
        return obtenerSerieEntre(inicio, inicio.withDayOfMonth(inicio.lengthOfMonth()));
    }

    /**
     * Agrupa las mediciones de un día. La lluvia de cada registro es la caída
     * en su intervalo, por lo que el valor diario es la suma, no el promedio.
     */
    private static PuntoSemanal agruparDia(LocalDate dia, List<RegistroMeteorologico> delDia) {
        double hum = promedio(delDia, r -> r.getHumedad());
        double viento = promedio(delDia, r -> r.getVelocidadViento());
        double lluvia = suma(delDia, r -> r.getLluvia());
        double temp = promedio(delDia, r -> r.getTempExterior());
        double presion = promedio(delDia, r -> r.getPresionBarometrica());
        double rocio = promedio(delDia, r -> r.getPuntoRocio());
        return new PuntoSemanal(dia, hum, viento, lluvia, temp, presion, rocio);
    }

    private static RegistroMeteorologico mapear(ResultSet rs) throws SQLException {
        RegistroMeteorologico r = new RegistroMeteorologico();
        r.setId(rs.getInt("id"));
        r.setFecha(DateUtil.parsearFechaEstacion(rs.getString("fecha")));
        r.setHora(DateUtil.parsearHoraEstacion(rs.getString("hora")));
        r.setTempExterior(parsearNumero(rs.getString("temp_out")));
        r.setHiTemp(parsearNumero(rs.getString("hi_temp")));
        r.setLowTemp(parsearNumero(rs.getString("low_temp")));
        r.setPuntoRocio(parsearNumero(rs.getString("dew_pt")));
        r.setSensacionTermica(parsearNumero(rs.getString("wind_chill")));
        r.setIndiceCalor(parsearNumero(rs.getString("heat_index")));
        r.setIndiceThw(parsearNumero(rs.getString("thw_index")));
        r.setHumedad(parsearNumero(rs.getString("out_hum")));
        r.setVelocidadViento(parsearNumero(rs.getString("wind_speed")));
        r.setDireccionViento(nulo(rs.getString("wind_dir")));
        r.setRecorridoViento(parsearNumero(rs.getString("wind_run")));
        r.setRafagaMaxima(parsearNumero(rs.getString("hi_speed")));
        r.setDireccionRafaga(nulo(rs.getString("hi_dir")));
        r.setPresionBarometrica(parsearNumero(rs.getString("bar")));
        r.setLluvia(parsearNumero(rs.getString("rain")));
        r.setTasaLluvia(parsearNumero(rs.getString("rain_rate")));
        r.setGradosDiaCalefaccion(parsearNumero(rs.getString("heat_dd")));
        r.setGradosDiaRefrigeracion(parsearNumero(rs.getString("cool_dd")));
        r.setTempInterior(parsearNumero(rs.getString("in_temp")));
        r.setHumedadInterior(parsearNumero(rs.getString("in_hum")));
        r.setPuntoRocioInterior(parsearNumero(rs.getString("in_dew")));
        r.setIndiceCalorInterior(parsearNumero(rs.getString("in_heat")));
        r.setEmcInterior(parsearNumero(rs.getString("in_emc")));
        r.setDensidadAireInterior(parsearNumero(rs.getString("in_air_density")));
        r.setMuestrasViento((int) parsearNumero(rs.getString("wind_samp")));
        r.setEstadoTransmision((int) parsearNumero(rs.getString("iss_tx")));
        r.setIntervaloArchivo((int) parsearNumero(rs.getString("arc_int")));
        r.setCalidadRecepcion(parsearNumero(rs.getString("recept")));
        r.setObservaciones(nulo(rs.getString("Observaciones")));
        return r;
    }

    private static String nulo(String valor) {
        return valor == null ? "" : valor;
    }

    private static void fijarParametros(PreparedStatement ps, RegistroMeteorologico r)
            throws SQLException {
        int i = 1;
        ps.setString(i++, r.getFecha() == null ? null : DateUtil.formatoFechaEstacion(r.getFecha()));
        ps.setString(i++, r.getHora() == null ? null : DateUtil.formatoHoraEstacion(r.getHora()));
        ps.setString(i++, texto(r.getTempExterior()));
        ps.setString(i++, texto(r.getHiTemp()));
        ps.setString(i++, texto(r.getLowTemp()));
        ps.setString(i++, texto(r.getPuntoRocio()));
        ps.setString(i++, texto(r.getSensacionTermica()));
        ps.setString(i++, texto(r.getIndiceCalor()));
        ps.setString(i++, texto(r.getIndiceThw()));
        ps.setString(i++, texto(r.getHumedad()));
        ps.setString(i++, texto(r.getVelocidadViento()));
        ps.setString(i++, r.getDireccionViento());
        ps.setString(i++, texto(r.getRecorridoViento()));
        ps.setString(i++, texto(r.getRafagaMaxima()));
        ps.setString(i++, r.getDireccionRafaga());
        ps.setString(i++, texto(r.getPresionBarometrica()));
        ps.setString(i++, texto(r.getLluvia()));
        ps.setString(i++, texto(r.getTasaLluvia()));
        ps.setString(i++, texto(r.getGradosDiaCalefaccion()));
        ps.setString(i++, texto(r.getGradosDiaRefrigeracion()));
        ps.setString(i++, texto(r.getTempInterior()));
        ps.setString(i++, texto(r.getHumedadInterior()));
        ps.setString(i++, texto(r.getPuntoRocioInterior()));
        ps.setString(i++, texto(r.getIndiceCalorInterior()));
        ps.setString(i++, texto(r.getEmcInterior()));
        ps.setString(i++, texto(r.getDensidadAireInterior()));
        ps.setString(i++, String.valueOf(r.getMuestrasViento()));
        ps.setString(i++, String.valueOf(r.getEstadoTransmision()));
        ps.setString(i++, String.valueOf(r.getIntervaloArchivo()));
        ps.setString(i++, texto(r.getCalidadRecepcion()));
        ps.setString(i, r.getObservaciones() == null ? "" : r.getObservaciones());
    }

    /** Convierte un número a texto evitando notación científica; NULL si falta. */
    private static String texto(double valor) {
        if (!Estadistica.hay(valor)) {
            return null;
        }
        if (valor == 0.0) {
            return "0";
        }
        if (valor == (long) valor) {
            return String.valueOf((long) valor);
        }
        return String.valueOf(valor);
    }

    /** NULL o texto no numérico en la BD = medición faltante (NaN), no 0. */
    private static double parsearNumero(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return Estadistica.SIN_DATO;
        }
        try {
            return Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            return Estadistica.SIN_DATO;
        }
    }
}
