package weatherreport_mvc.utils;

import java.awt.Color;

/**
 * Utilidad para evaluar los niveles de alerta de humedad y viento.
 * Un solo registro con estos datos, permitiendo que la vista decida cómo
 * presentar las alertas dinámicas.
 */
public final class AlertaUtil {

    public enum Nivel {
        NORMAL("Normal", ColoresUI.ALERTA_NORMAL_OSC),
        ATENCION("Atención", ColoresUI.ALERTA_ATENCION_OSC),
        ALERTA("Alerta", ColoresUI.ALERTA_CRITICA_OSC);

        private final String etiqueta;
        private final Color color;

        Nivel(String etiqueta, Color color) {
            this.etiqueta = etiqueta;
            this.color = color;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        public Color getColor() {
            return color;
        }
    }

    private AlertaUtil() {
    }

    public static Nivel evaluarHumedad(double humedad, double umbral) {
        if (Double.isNaN(humedad) || humedad < 0) {
            return Nivel.NORMAL;
        }
        if (humedad > umbral + 5) {
            return Nivel.ALERTA;
        }
        if (humedad > umbral) {
            return Nivel.ATENCION;
        }
        return Nivel.NORMAL;
    }

    public static Nivel evaluarViento(double viento, double umbral) {
        if (Double.isNaN(viento) || viento < 0) {
            return Nivel.NORMAL;
        }
        if (viento > umbral + 15) {
            return Nivel.ALERTA;
        }
        if (viento > umbral) {
            return Nivel.ATENCION;
        }
        return Nivel.NORMAL;
    }

    public static String mensajeHumedad(double humedad, double umbral) {
        if (Double.isNaN(humedad)) {
            return "Sin dato de humedad en el último registro.";
        }
        Nivel n = evaluarHumedad(humedad, umbral);
        return switch (n) {
            case NORMAL ->
                "Humedad dentro de rangos normales (" + String.format("%.1f", humedad) + "%).";
            case ATENCION ->
                "Atención: humedad elevada (" + String.format("%.1f", humedad)
                + "%) superando el umbral (" + String.format("%.0f", umbral) + "%).";
            case ALERTA ->
                "ALERTA CRÍTICA: humedad muy alta (" + String.format("%.1f", humedad)
                + "%). Riesgo de saturación.";
        };
    }

    public static String mensajeViento(double viento, double umbral) {
        if (Double.isNaN(viento)) {
            return "Sin dato de viento en el último registro.";
        }
        Nivel n = evaluarViento(viento, umbral);
        return switch (n) {
            case NORMAL ->
                "Viento estable (" + String.format("%.1f", viento) + " km/h).";
            case ATENCION ->
                "Atención: viento fuerte (" + String.format("%.1f", viento)
                + " km/h) superando el umbral (" + String.format("%.0f", umbral) + " km/h).";
            case ALERTA ->
                "ALERTA CRÍTICA: vientos peligrosos (" + String.format("%.1f", viento)
                + " km/h). Tome precauciones.";
        };
    }
}
