package weatherreport_mvc.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidades de formato de fechas y horas.
 */
public final class DateUtil {

    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
    public static final DateTimeFormatter FORMATO_SQL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] NOMBRES_MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private DateUtil() {
    }

    public static String formatoFecha(LocalDate fecha) {
        return fecha.format(FORMATO_FECHA);
    }

    public static String formatoFechaHora(LocalDateTime fechaHora) {
        return fechaHora.format(FORMATO_FECHA_HORA);
    }

    public static String formatoHora(LocalTime hora) {
        return hora.format(FORMATO_HORA);
    }

    /** Convierte "16/01/26" (dd/MM/aa) a LocalDate. */
    public static LocalDate parsearFechaEstacion(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(),
                    DateTimeFormatter.ofPattern("dd/MM/yy"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convierte una hora de estación a LocalTime. Acepta "6:00 a", "6:00 p",
     * "6:00 am/pm", "7:00a" (sin espacio) y formato de 24 h ("18:00").
     */
    public static LocalTime parsearHoraEstacion(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        String t = texto.trim().toLowerCase();
        boolean pm = false;
        boolean am = false;
        if (t.endsWith("pm")) {
            pm = true;
            t = t.substring(0, t.length() - 2);
        } else if (t.endsWith("am")) {
            am = true;
            t = t.substring(0, t.length() - 2);
        } else if (t.endsWith("p")) {
            pm = true;
            t = t.substring(0, t.length() - 1);
        } else if (t.endsWith("a")) {
            am = true;
            t = t.substring(0, t.length() - 1);
        }
        t = t.trim();
        try {
            String[] partes = t.split(":");
            if (partes.length != 2) {
                return null;
            }
            int hora = Integer.parseInt(partes[0]);
            int minuto = Integer.parseInt(partes[1]);
            if ((pm || am) && (hora < 1 || hora > 12)) {
                return null;
            }
            if (pm && hora < 12) {
                hora += 12;
            } else if (am && hora == 12) {
                // 12:xx a = medianoche; sin sufijo se interpreta como 24 h.
                hora = 0;
            }
            return LocalTime.of(hora, minuto);
        } catch (Exception e) {
            return null;
        }
    }

    /** Convierte LocalDate a "16/01/26" (dd/MM/aa), igual que la estación. */
    public static String formatoFechaEstacion(LocalDate fecha) {
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yy"));
    }

    /** Formatea la fecha o "--" si es null (evita NPE con datos corruptos). */
    public static String formatoFechaSeguro(LocalDate fecha) {
        return fecha == null ? "--" : formatoFechaEstacion(fecha);
    }

    /** Convierte LocalTime a "7:00 a" / "6:00 p" (12 h con a/p minúsculas). */
    public static String formatoHoraEstacion(LocalTime hora) {
        int h12 = hora.getHour() % 12;
        if (h12 == 0) {
            h12 = 12;
        }
        String sufijo = hora.getHour() < 12 ? "a" : "p";
        return h12 + ":" + String.format("%02d", hora.getMinute()) + " " + sufijo;
    }

    /** Formatea la hora o "--" si es null (evita NPE con datos corruptos). */
    public static String formatoHoraSeguro(LocalTime hora) {
        return hora == null ? "--" : formatoHoraEstacion(hora);
    }

    public static String nombreMes(int mes) {
        if (mes < 1 || mes > 12) {
            return "";
        }
        return NOMBRES_MESES[mes - 1];
    }

    public static String nombreMesCorto(int mes) {
        String nombre = nombreMes(mes);
        return nombre.isEmpty() ? nombre : nombre.substring(0, 3);
    }

    public static String selloArchivo() {
        return LocalDateTime.now().format(FORMATO_ARCHIVO);
    }
}
