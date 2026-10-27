package weatherreport_mvc.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.rendering.PDFRenderer;
import weatherreport_mvc.database.RegistroDAO;
import weatherreport_mvc.models.RegistroMeteorologico;

/**
 * Generador de reportes PDF con Apache PDFBox. Construye el "Reporte Diario"
 * y los "Resumen Semanal"/"Resumen Mensual" a partir de los datos de la base
 * de datos. El contenido se dibuja con paginación automática para que ninguna
 * fila se pierda aunque el reporte supere una hoja.
 */
public final class PDFGenerator {

    private static final float MARGEN = 48f;
    private static final float ANCHO = 502f;
    private static final float MARGEN_INFERIOR = 80f;

    private static final PDFont HELVETICA =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont HELVETICA_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private PDFGenerator() {
    }

    /**
     * Lienzo de dibujo con paginación automática: cuando queda poco margen
     * inferior abre una página nueva y continúa dibujando en ella.
     */
    private static final class Lienzo {
        private final PDDocument doc;
        private PDPage pagina;
        private PDPageContentStream cs;
        private float y;

        private Lienzo(PDDocument doc) throws IOException {
            this.doc = doc;
            nuevaPagina();
        }

        private void nuevaPagina() throws IOException {
            pagina = new PDPage(PDRectangle.A4);
            doc.addPage(pagina);
            cs = new PDPageContentStream(doc, pagina);
            cs.setLeading(16f);
            y = 770f;
        }

        /** Cambia de página si queda poco margen inferior. */
        private void saltoSiNecesario() throws IOException {
            if (y < MARGEN_INFERIOR) {
                cs.close();
                nuevaPagina();
            }
        }

        private void centrado(PDFont font, float tam, String texto, float sep)
                throws IOException {
            saltoSiNecesario();
            float anchoTexto = font.getStringWidth(sanitizar(texto)) / 1000f * tam;
            float x = MARGEN + (ANCHO - anchoTexto) / 2f;
            cs.setFont(font, tam);
            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(sanitizar(texto));
            cs.endText();
            y -= sep;
        }

        private void texto(PDFont font, float tam, String texto, float sep)
                throws IOException {
            saltoSiNecesario();
            cs.setFont(font, tam);
            cs.beginText();
            cs.newLineAtOffset(MARGEN, y);
            cs.showText(sanitizar(texto));
            cs.endText();
            y -= sep;
        }

        private void separador() throws IOException {
            saltoSiNecesario();
            cs.setLineWidth(0.8f);
            cs.moveTo(MARGEN, y);
            cs.lineTo(MARGEN + ANCHO, y);
            cs.stroke();
        }

        private void hueco(float n) {
            y -= n;
        }

        /** Cierra el flujo de contenido de la última página antes de guardar. */
        private void cerrar() throws IOException {
            cs.close();
        }
    }

    public static File generarReporteDiario(String estacion, String ubicacion,
                                            LocalDate dia, File destino) throws IOException {
        List<RegistroMeteorologico> registros = RegistroDAO.obtenerPorDia(dia);
        try (PDDocument doc = new PDDocument()) {
            Lienzo lienzo = new Lienzo(doc);
            escribirEncabezado(lienzo, "Reporte Diario", estacion, ubicacion,
                    "Fecha: " + DateUtil.formatoFecha(dia));

            lienzo.hueco(10);
            lienzo.separador();
            lienzo.hueco(18);
            dibujarNarrativa(lienzo, NarrativaMeteorologica.construir(
                    NarrativaMeteorologica.Periodo.DIARIO, dia, dia, registros));
            lienzo.hueco(10);
            lienzo.separador();
            lienzo.hueco(26);

            lienzo.texto(HELVETICA_BOLD, 12, "Resumen del día", 18);
            for (Map.Entry<String, String> e : resumenDiario(registros).entrySet()) {
                lienzo.texto(HELVETICA, 11, e.getKey() + ": " + e.getValue(), 16);
            }
            lienzo.hueco(6);
            lienzo.separador();
            lienzo.hueco(24);

            lienzo.texto(HELVETICA_BOLD, 12, "Registros del día", 22);
            dibujarTablaDiaria(lienzo, registros);
            lienzo.cerrar();
            doc.save(destino);
        }
        return destino;
    }

    public static File generarResumenSemanal(String estacion, String ubicacion,
                                             LocalDate desde, LocalDate hasta,
                                             File destino) throws IOException {
        return generarResumenPorPeriodo(estacion, ubicacion, desde, hasta,
                NarrativaMeteorologica.Periodo.SEMANAL, destino);
    }

    public static File generarResumenMensual(String estacion, String ubicacion,
                                             LocalDate desde, LocalDate hasta,
                                             File destino) throws IOException {
        return generarResumenPorPeriodo(estacion, ubicacion, desde, hasta,
                NarrativaMeteorologica.Periodo.MENSUAL, destino);
    }

    private static File generarResumenPorPeriodo(String estacion, String ubicacion,
                                                 LocalDate desde, LocalDate hasta,
                                                 NarrativaMeteorologica.Periodo periodo,
                                                 File destino) throws IOException {
        String titulo = switch (periodo) {
            case SEMANAL -> "Resumen Semanal";
            case MENSUAL -> "Resumen Mensual";
            default -> "Resumen";
        };
        List<RegistroMeteorologico> registros = RegistroDAO.obtenerEntre(
                desde.atStartOfDay(), hasta.atTime(23, 59, 59));
        try (PDDocument doc = new PDDocument()) {
            Lienzo lienzo = new Lienzo(doc);
            escribirEncabezado(lienzo, titulo, estacion, ubicacion,
                    "Periodo: " + DateUtil.formatoFecha(desde) + " - "
                    + DateUtil.formatoFecha(hasta));

            lienzo.hueco(10);
            lienzo.separador();
            lienzo.hueco(18);
            dibujarNarrativa(lienzo, NarrativaMeteorologica.construir(
                    periodo, desde, hasta, registros));
            lienzo.hueco(10);
            lienzo.separador();
            lienzo.hueco(26);

            lienzo.texto(HELVETICA_BOLD, 12, "Totales del periodo", 18);
            for (Map.Entry<String, String> e : resumenDiario(registros).entrySet()) {
                lienzo.texto(HELVETICA, 11, e.getKey() + ": " + e.getValue(), 16);
            }
            lienzo.hueco(6);
            lienzo.separador();
            lienzo.hueco(24);

            lienzo.texto(HELVETICA_BOLD, 12, "Detalle por día", 22);
            dibujarTablaSemanal(lienzo, registros);
            lienzo.cerrar();
            doc.save(destino);
        }
        return destino;
    }

    private static void escribirEncabezado(Lienzo lienzo, String subtitulo,
                                           String estacion, String ubicacion,
                                           String lineaExtra) throws IOException {
        lienzo.centrado(HELVETICA_BOLD, 20, "WEATHER REPORT", 18);
        lienzo.centrado(HELVETICA_BOLD, 14, subtitulo, 24);
        lienzo.centrado(HELVETICA, 11,
                "Estación: " + estacion + "  |  " + ubicacion, 22);
        lienzo.centrado(HELVETICA, 11, lineaExtra, 20);
    }

    private static Map<String, String> resumenDiario(List<RegistroMeteorologico> registros) {
        Map<String, String> resumen = new LinkedHashMap<>();
        if (registros.isEmpty()) {
            resumen.put("Registros", "0");
            resumen.put("Temperatura promedio", "--");
            resumen.put("Humedad promedio", "--");
            resumen.put("Viento promedio", "--");
            resumen.put("Presión promedio", "--");
            resumen.put("Lluvia acumulada", "--");
            return resumen;
        }
        Estadistica hum = new Estadistica();
        Estadistica viento = new Estadistica();
        Estadistica lluvia = new Estadistica();
        Estadistica temp = new Estadistica();
        Estadistica presion = new Estadistica();
        Estadistica rocio = new Estadistica();
        Estadistica tempMax = new Estadistica();
        Estadistica tempMin = new Estadistica();
        Estadistica rafaga = new Estadistica();
        Map<String, Integer> direcciones = new LinkedHashMap<>();
        for (RegistroMeteorologico r : registros) {
            hum.agregar(r.getHumedad());
            viento.agregar(r.getVelocidadViento());
            lluvia.agregar(r.getLluvia());
            temp.agregar(r.getTempExterior());
            presion.agregar(r.getPresionBarometrica());
            rocio.agregar(r.getPuntoRocio());
            tempMax.agregar(r.getHiTemp());
            tempMin.agregar(r.getLowTemp());
            rafaga.agregar(r.getRafagaMaxima());
            String dir = r.getDireccionViento();
            if (dir != null && !dir.isEmpty()) {
                direcciones.merge(dir, 1, Integer::sum);
            }
        }
        int n = registros.size();
        resumen.put("Registros", String.valueOf(n));
        resumen.put("Temperatura promedio", Estadistica.fmtRoot("%.1f °C", temp.promedio()));
        resumen.put("Temperatura máx/mín", Estadistica.fmtRoot("%.1f", tempMax.max())
                + " / " + Estadistica.fmtRoot("%.1f °C", tempMin.min()));
        resumen.put("Punto de rocío promedio", Estadistica.fmtRoot("%.1f °C", rocio.promedio()));
        resumen.put("Humedad promedio", Estadistica.fmtRoot("%.1f %%", hum.promedio()));
        resumen.put("Viento promedio", Estadistica.fmtRoot("%.1f km/h", viento.promedio()));
        resumen.put("Ráfaga máxima", Estadistica.fmtRoot("%.1f km/h", rafaga.max()));
        resumen.put("Dirección predominante", direccionPredominante(direcciones));
        resumen.put("Presión promedio", Estadistica.fmtRoot("%.1f hPa", presion.promedio()));
        resumen.put("Lluvia acumulada", Estadistica.fmtRoot("%.1f mm", lluvia.suma()));
        return resumen;
    }

    private static String direccionPredominante(Map<String, Integer> direcciones) {
        return direcciones.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("--");
    }

    private static void dibujarTablaDiaria(Lienzo lienzo,
                                           List<RegistroMeteorologico> registros)
            throws IOException {
        float[] cols = {64f, 82f, 78f, 78f, 78f, 78f, 44f};
        String[] headers = {"Fecha/Hora", "Temp °C", "Hum %", "Viento", "Presión", "Lluvia", "Obs"};
        float inicioX = MARGEN;

        lienzo.saltoSiNecesario();
        PDPageContentStream cs = lienzo.cs;
        cs.setFont(HELVETICA_BOLD, 9);
        float x = inicioX;
        for (int i = 0; i < headers.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(x, lienzo.y);
            cs.showText(sanitizar(headers[i]));
            cs.endText();
            x += cols[i];
        }
        lienzo.y -= 14;

        for (RegistroMeteorologico r : registros) {
            lienzo.saltoSiNecesario();
            cs = lienzo.cs;
            x = inicioX;
            String[] fila = {
                DateUtil.formatoFechaSeguro(r.getFecha()) + " "
                        + DateUtil.formatoHoraSeguro(r.getHora()),
                Estadistica.fmtRoot("%.1f", r.getTempExterior()),
                Estadistica.fmtRoot("%.1f", r.getHumedad()),
                Estadistica.fmtRoot("%.1f", r.getVelocidadViento()),
                Estadistica.fmtRoot("%.1f", r.getPresionBarometrica()),
                Estadistica.fmtRoot("%.1f", r.getLluvia()),
                truncar(r.getObservaciones(), 8)
            };
            cs.setFont(HELVETICA, 8);
            for (int i = 0; i < fila.length; i++) {
                cs.beginText();
                cs.newLineAtOffset(x, lienzo.y);
                cs.showText(sanitizar(fila[i]));
                cs.endText();
                x += cols[i];
            }
            lienzo.y -= 12;
        }
    }

    private static void dibujarTablaSemanal(Lienzo lienzo,
                                            List<RegistroMeteorologico> registros)
            throws IOException {
        Map<LocalDate, List<RegistroMeteorologico>> porDia = new LinkedHashMap<>();
        for (RegistroMeteorologico r : registros) {
            porDia.computeIfAbsent(r.getFecha(), k -> new ArrayList<>()).add(r);
        }
        float[] cols = {70f, 88f, 86f, 88f, 88f, 82f};
        String[] headers = {"Fecha", "Temp °C", "Humedad %", "Viento km/h", "Lluvia mm", "Registros"};
        float inicioX = MARGEN;

        lienzo.saltoSiNecesario();
        PDPageContentStream cs = lienzo.cs;
        cs.setFont(HELVETICA_BOLD, 10);
        float x = inicioX;
        for (int i = 0; i < headers.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(x, lienzo.y);
            cs.showText(sanitizar(headers[i]));
            cs.endText();
            x += cols[i];
        }
        lienzo.y -= 14;

        for (Map.Entry<LocalDate, List<RegistroMeteorologico>> e : porDia.entrySet()) {
            lienzo.saltoSiNecesario();
            cs = lienzo.cs;
            List<RegistroMeteorologico> delDia = e.getValue();
            Estadistica temp = new Estadistica();
            Estadistica hum = new Estadistica();
            Estadistica viento = new Estadistica();
            Estadistica lluvia = new Estadistica();
            for (RegistroMeteorologico r : delDia) {
                temp.agregar(r.getTempExterior());
                hum.agregar(r.getHumedad());
                viento.agregar(r.getVelocidadViento());
                lluvia.agregar(r.getLluvia());
            }
            int n = delDia.size();
            String[] fila = {
                DateUtil.formatoFecha(e.getKey()),
                Estadistica.fmtRoot("%.1f", temp.promedio()),
                Estadistica.fmtRoot("%.1f", hum.promedio()),
                Estadistica.fmtRoot("%.1f", viento.promedio()),
                Estadistica.fmtRoot("%.1f", lluvia.suma()),
                String.valueOf(n)
            };
            x = inicioX;
            cs.setFont(HELVETICA, 9);
            for (int i = 0; i < fila.length; i++) {
                cs.beginText();
                cs.newLineAtOffset(x, lienzo.y);
                cs.showText(sanitizar(fila[i]));
                cs.endText();
                x += cols[i];
            }
            lienzo.y -= 13;
        }
    }

    /** Escribe la narrativa en párrafos ajustados al ancho. */
    private static void dibujarNarrativa(Lienzo lienzo, String narrativa) throws IOException {
        for (String parrafo : narrativa.split("\n")) {
            if (parrafo.trim().isEmpty()) {
                lienzo.hueco(10);
                continue;
            }
            for (String linea : envolver(parrafo, HELVETICA, 10, ANCHO)) {
                lienzo.texto(HELVETICA, 10, linea, 14);
            }
        }
    }

    /** Divide un párrafo en líneas que caben dentro del ancho máximo. */
    private static List<String> envolver(String texto, PDFont font, float tam,
                                         float anchoMaximo) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (String palabra : texto.trim().split("\\s+")) {
            String prueba = lineas.isEmpty()
                    ? palabra
                    : lineas.get(lineas.size() - 1) + " " + palabra;
            float ancho = font.getStringWidth(sanitizar(prueba)) / 1000f * tam;
            if (lineas.isEmpty() || ancho <= anchoMaximo) {
                if (lineas.isEmpty()) {
                    lineas.add(palabra);
                } else {
                    lineas.set(lineas.size() - 1, prueba);
                }
            } else {
                lineas.add(palabra);
            }
        }
        return lineas;
    }

    private static String truncar(String texto, int max) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        String limpio = texto.replace('\n', ' ').replace('\r', ' ');
        return limpio.length() > max ? limpio.substring(0, max - 3) + "..." : limpio;
    }

    /** Elimina caracteres no representables en la codificación WinAnsi del PDF. */
    private static String sanitizar(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(texto.length());
        for (char c : texto.toCharArray()) {
            if (c == '\u00B0' || (c >= 32 && c <= 255)) {
                sb.append(c);
            } else if (c != '\n' && c != '\r') {
                sb.append('?');
            }
        }
        return sb.toString();
    }

    public static void abrir(File archivo) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivo);
            }
        } catch (IOException e) {
            System.err.println("No se pudo abrir el PDF: " + e.getMessage());
        }
    }

    /** Renderiza las páginas del PDF como imágenes para usarlas como vista previa. */
    public static JPanel crearVistaPrevia(File pdf) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            int paginas = doc.getNumberOfPages();
            JPanel paginasPanel = new JPanel();
            paginasPanel.setLayout(new BoxLayout(paginasPanel, BoxLayout.Y_AXIS));
            paginasPanel.setBackground(Color.WHITE);
            for (int i = 0; i < paginas; i++) {
                BufferedImage imagen = renderer.renderImage(i, 1.5f);
                JLabel lblPagina = new JLabel(new ImageIcon(imagen));
                lblPagina.setAlignmentX(Component.CENTER_ALIGNMENT);
                paginasPanel.add(lblPagina);
                if (i < paginas - 1) {
                    paginasPanel.add(Box.createVerticalStrut(12));
                }
            }
            return paginasPanel;
        }
    }
}