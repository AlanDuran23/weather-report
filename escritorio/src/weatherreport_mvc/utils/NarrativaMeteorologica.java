package weatherreport_mvc.utils;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import weatherreport_mvc.models.RegistroMeteorologico;

/**
 * Construye la narrativa descriptiva del resumen meteorológico para un periodo
 * (diario, semanal o mensual). Se usa tanto en la vista previa del panel de
 * Reportes como dentro del PDF generado.
 */
public final class NarrativaMeteorologica {

    public enum Periodo {
        DIARIO("Durante este día",
                "recorrido acumulado de",
                "La presión barométrica durante el día",
                "acumulado total de lluvia de",
                "promedio de"),
        SEMANAL("Durante esta semana",
                "recorrido acumulado semanal de",
                "La presión barométrica en el transcurso de la semana",
                "acumulado semanal total de lluvia de",
                "promedio semanal de"),
        MENSUAL("Durante este mes",
                "recorrido acumulado mensual de",
                "La presión barométrica en el transcurso del mes",
                "acumulado mensual total de lluvia de",
                "promedio mensual de");

        private final String durante;
        private final String recorrido;
        private final String presion;
        private final String lluvia;
        private final String humedad;

        Periodo(String durante, String recorrido, String presion, String lluvia, String humedad) {
            this.durante = durante;
            this.recorrido = recorrido;
            this.presion = presion;
            this.lluvia = lluvia;
            this.humedad = humedad;
        }
    }

    private NarrativaMeteorologica() {
    }

    /** Construye la narrativa del periodo con los registros obtenidos. */
    public static String construir(Periodo periodo, LocalDate desde,
                                   LocalDate hasta,
                                   List<RegistroMeteorologico> registros) {
        if (registros == null || registros.isEmpty()) {
            return "No hay registros meteorológicos para el periodo seleccionado.";
        }

        Estadistica rocio = new Estadistica();
        Estadistica sensacion = new Estadistica();
        Estadistica viento = new Estadistica();
        Estadistica presion = new Estadistica();
        Estadistica lluvia = new Estadistica();
        Estadistica humedad = new Estadistica();
        Estadistica humedadInt = new Estadistica();
        Estadistica recorrido = new Estadistica();
        Estadistica maxTemp = new Estadistica();
        Estadistica minTemp = new Estadistica();
        Estadistica calor = new Estadistica();
        Estadistica rafaga = new Estadistica();
        Estadistica tasa = new Estadistica();
        Map<String, Integer> direcciones = new LinkedHashMap<>();

        for (RegistroMeteorologico r : registros) {
            rocio.agregar(r.getPuntoRocio());
            sensacion.agregar(r.getSensacionTermica());
            viento.agregar(r.getVelocidadViento());
            presion.agregar(r.getPresionBarometrica());
            lluvia.agregar(r.getLluvia());
            humedad.agregar(r.getHumedad());
            humedadInt.agregar(r.getHumedadInterior());
            recorrido.agregar(r.getRecorridoViento());
            maxTemp.agregar(r.getHiTemp());
            minTemp.agregar(r.getLowTemp());
            calor.agregar(r.getIndiceCalor());
            rafaga.agregar(r.getRafagaMaxima());
            tasa.agregar(r.getTasaLluvia());
            String dir = r.getDireccionViento();
            if (dir != null && !dir.isEmpty()) {
                direcciones.merge(dir, 1, Integer::sum);
            }
        }

        String direccion = direcciones.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("--");

        String f = "%.1f";
        StringBuilder sb = new StringBuilder();
        // "a el" se contrae a "al" (al día, al mes); "a la semana" queda igual
        String apertura = apertura(periodo, desde, hasta);
        sb.append("A continuación, el resumen meteorológico correspondiente ")
                .append(apertura.startsWith("el ") ? "al " + apertura.substring(3) : "a " + apertura)
                .append(".\n\n");

        sb.append(periodo.durante)
                .append(", las temperaturas registradas oscilaron entre máximas absolutas de ")
                .append(Estadistica.fmt(f, maxTemp.max())).append(" grados y mínimas de ")
                .append(Estadistica.fmt(f, minTemp.min())).append(" grados, con un índice de calor que alcanzó un pico de ")
                .append(Estadistica.fmt(f, calor.max())).append(" grados, un punto de rocío de ")
                .append(Estadistica.fmt(f, rocio.promedio())).append(" grados y una sensación térmica por viento de ")
                .append(Estadistica.fmt(f, sensacion.promedio())).append(" grados.\n\n");

        sb.append("En cuanto al comportamiento del viento, se registraron velocidades promedio de ")
                .append(Estadistica.fmt(f, viento.promedio()))
                .append(" kilómetros por hora con dirección predominante del ")
                .append(direccion)
                .append(", alcanzando ráfagas máximas de ")
                .append(Estadistica.fmt(f, rafaga.max()))
                .append(" kilómetros por hora y un ")
                .append(periodo.recorrido).append(" ")
                .append(Estadistica.fmt(f, recorrido.suma())).append(" kilómetros.\n\n");

        sb.append(periodo.presion).append(" se mantuvo en un promedio de ")
                .append(Estadistica.fmt(f, presion.promedio()))
                .append(" hectopascales. En el apartado de precipitaciones, se tuvo un ")
                .append(periodo.lluvia).append(" ")
                .append(Estadistica.fmt(f, lluvia.suma()))
                .append(" milímetros, registrando una tasa máxima de intensidad de ")
                .append(Estadistica.fmt(f, tasa.max()))
                .append(" milímetros por hora durante las jornadas de mayor actividad.\n\n");

        sb.append("Por último, la humedad relativa en el exterior se situó en un ")
                .append(periodo.humedad).append(" ")
                .append(Estadistica.fmt(f, humedad.promedio()))
                .append(" por ciento, mientras que la humedad en ambientes interiores se mantuvo en un ")
                .append(Estadistica.fmt(f, humedadInt.promedio()))
                .append(" por ciento.");

        return sb.toString();
    }

    private static String apertura(Periodo periodo, LocalDate desde, LocalDate hasta) {
        return switch (periodo) {
            case DIARIO -> "el día " + DateUtil.formatoFecha(desde);
            case SEMANAL -> "la semana del " + DateUtil.formatoFecha(desde)
                    + " al " + DateUtil.formatoFecha(hasta);
            case MENSUAL -> "el mes de " + DateUtil.nombreMes(desde.getMonthValue())
                    + " del año " + desde.getYear();
        };
    }
}