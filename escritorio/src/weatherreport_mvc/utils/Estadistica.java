package weatherreport_mvc.utils;

import java.util.Locale;

/**
 * Acumulador de mediciones que ignora los valores faltantes. En el modelo un
 * dato que la estación no registró se representa con {@link Double#NaN}, de
 * modo que no se mezcle con un 0 real en promedios, máximos o sumas.
 */
public final class Estadistica {

    /** Valor que representa una medición faltante. */
    public static final double SIN_DATO = Double.NaN;

    private double suma;
    private double max = Double.NEGATIVE_INFINITY;
    private double min = Double.POSITIVE_INFINITY;
    private int cantidad;

    public Estadistica agregar(double valor) {
        if (!hay(valor)) {
            return this;
        }
        suma += valor;
        max = Math.max(max, valor);
        min = Math.min(min, valor);
        cantidad++;
        return this;
    }

    public boolean hayDatos() {
        return cantidad > 0;
    }

    public double promedio() {
        return cantidad == 0 ? SIN_DATO : suma / cantidad;
    }

    public double suma() {
        return cantidad == 0 ? SIN_DATO : suma;
    }

    public double max() {
        return cantidad == 0 ? SIN_DATO : max;
    }

    public double min() {
        return cantidad == 0 ? SIN_DATO : min;
    }

    /** Indica si el valor es una medición real (no faltante). */
    public static boolean hay(double valor) {
        return !Double.isNaN(valor);
    }

    /** Formatea el valor o devuelve "--" si falta. */
    public static String fmt(String formato, double valor) {
        return hay(valor) ? String.format(formato, valor) : "--";
    }

    /** Igual que {@link #fmt(String, double)} pero con punto decimal fijo. */
    public static String fmtRoot(String formato, double valor) {
        return hay(valor) ? String.format(Locale.ROOT, formato, valor) : "--";
    }

    /** Valor para series de JFreeChart: {@code null} deja un hueco en la gráfica. */
    public static Double serie(double valor) {
        return hay(valor) ? valor : null;
    }
}
