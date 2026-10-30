package weatherreport_mvc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import weatherreport_mvc.models.RegistroMeteorologico;

/**
 * Importador de registros meteorológicos desde archivos de texto (.txt/.csv)
 * y Excel (.xls/.xlsx).
 *
 * <p>Formato de texto: una fila por registro con campos separados por coma,
 * punto y coma o tabulador: {@code dd/MM/yyyy HH:mm, humedad, viento, lluvia, observaciones}.
 * Las líneas en blanco y las que comienzan con '#' se ignoran.</p>
 *
 * <p>Formato estación (exportación nativa .txt): dos filas de cabecera y datos
 * tabulados de 30 columnas con fecha {@code dd/MM/yy}, hora de 12 h con sufijo
 * a/p y todas las mediciones exteriores, de viento, presión/lluvia, interiores
 * y de sensores de la estación.</p>
 *
 * <p>Formato Excel: si la primera fila contiene encabezados con "Fecha", "Humedad",
 * "Viento", "Lluvia" y "Observaciones" se reconocen en cualquier orden. En caso
 * contrario se asume el orden {@code Fecha, Humedad, Viento, Lluvia, Observaciones}.</p>
 */
public final class ImportadorArchivos {

    public record ResultadoImportacion(List<RegistroMeteorologico> registros, List<String> errores) {
    }

    private static final DataFormatter FORMATO_CELDA = new DataFormatter();

    private ImportadorArchivos() {
    }

    public static ResultadoImportacion importar(File archivo) throws IOException {
        if (!archivo.isFile()) {
            // Sin esto el mensaje era solo la ruta, sin decir qué pasó
            throw new IOException("el archivo no existe o no se puede abrir.");
        }
        String nombre = archivo.getName().toLowerCase();
        if (nombre.endsWith(".txt") || nombre.endsWith(".csv")) {
            return importarTexto(archivo);
        }
        if (nombre.endsWith(".xlsx") || nombre.endsWith(".xls")) {
            return importarExcel(archivo);
        }
        throw new IOException("Formato no soportado. Use .txt, .csv, .xls o .xlsx.");
    }

    private static ResultadoImportacion importarTexto(File archivo) throws IOException {
        List<String> lineas = leerLineas(archivo);
        if (esArchivoEstacion(lineas)) {
            return importarTextoEstacion(lineas);
        }
        return importarTextoSencillo(lineas);
    }

    /** Indica si el archivo usa el formato nativo de la estación (cabecera Date/Time). */
    private static boolean esArchivoEstacion(List<String> lineas) {
        int revisadas = 0;
        for (String l : lineas) {
            String limpia = l.trim();
            if (limpia.isEmpty() || limpia.startsWith("#")) {
                continue;
            }
            if (esLineaCabeceraEstacion(limpia)) {
                return true;
            }
            if (++revisadas >= 5) {
                return false;
            }
        }
        return false;
    }

    private static List<String> leerLineas(File archivo) throws IOException {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(
                new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                lineas.add(linea);
            }
        }
        return lineas;
    }

    /** Detecta la cabecera del formato nativo de la estación ("Date" + "Time"). */
    private static boolean esLineaCabeceraEstacion(String linea) {
        String[] p = linea.split("\t", -1);
        return p.length >= 2
                && p[0].trim().equalsIgnoreCase("Date")
                && p[1].trim().equalsIgnoreCase("Time");
    }

    private static ResultadoImportacion importarTextoSencillo(List<String> lineas) {
        List<RegistroMeteorologico> registros = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        String separador = null;
        int numeroLinea = 0;
        for (String linea : lineas) {
            numeroLinea++;
            String limpia = linea.trim();
            if (limpia.isEmpty() || limpia.startsWith("#")) {
                continue;
            }
            if (separador == null) {
                separador = detectarSeparador(limpia);
            }
            try {
                registros.add(parsearLinea(limpia, separador));
            } catch (ImportacionInvalida ex) {
                errores.add("Línea " + numeroLinea + ": " + ex.getMessage());
            }
        }
        return new ResultadoImportacion(registros, errores);
    }

    private static String detectarSeparador(String linea) {
        if (linea.contains(";")) {
            return ";";
        }
        if (linea.contains("\t")) {
            return "\t";
        }
        return ",";
    }

    private static RegistroMeteorologico parsearLinea(String linea, String separador) {
        String[] partes = linea.split(Pattern.quote(separador), -1);
        if (partes.length < 4) {
            throw new ImportacionInvalida("se requieren al menos 4 campos "
                    + "(fecha y hora, humedad, viento, lluvia).");
        }
        return crearRegistro(
                partes[0].trim(),
                partes[1].trim(),
                partes[2].trim(),
                partes[3].trim(),
                unir(partes, 4),
                "campo");
    }

    private static String unir(String[] partes, int desde) {
        if (desde >= partes.length) {
            return "";
        }
        StringBuilder sb = new StringBuilder(partes[desde]);
        for (int i = desde + 1; i < partes.length; i++) {
            sb.append(",").append(partes[i]);
        }
        return sb.toString().trim();
    }

    /** Importa el formato nativo de la estación (30 columnas tabuladas). */
    private static ResultadoImportacion importarTextoEstacion(List<String> lineas) {
        List<RegistroMeteorologico> registros = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        int inicio = 0;
        while (inicio < lineas.size()) {
            String limpia = lineas.get(inicio).trim();
            if (!limpia.isEmpty() && !limpia.startsWith("#")
                    && esLineaCabeceraEstacion(limpia)) {
                break;
            }
            inicio++;
        }
        if (inicio >= lineas.size()) {
            return new ResultadoImportacion(registros,
                    List.of("No se encontró la cabecera de la estación (Date/Time)."));
        }
        for (int i = inicio + 1; i < lineas.size(); i++) {
            String limpia = lineas.get(i).trim();
            if (limpia.isEmpty() || limpia.startsWith("#")) {
                continue;
            }
            try {
                registros.add(parsearLineaEstacion(limpia));
            } catch (ImportacionInvalida ex) {
                errores.add("Línea " + (i + 1) + ": " + ex.getMessage());
            }
        }
        return new ResultadoImportacion(registros, errores);
    }

    /** Mapea una fila tabulada de la estación a un registro meteorológico completo. */
    private static RegistroMeteorologico parsearLineaEstacion(String linea) {
        String[] p = linea.split(Pattern.quote("\t"), -1);
        if (p.length < 2) {
            throw new ImportacionInvalida("fila sin fecha y hora.");
        }
        LocalDate fecha = DateUtil.parsearFechaEstacion(p[0].trim());
        LocalTime hora = DateUtil.parsearHoraEstacion(p[1].trim());
        if (fecha == null) {
            throw new ImportacionInvalida("fecha inválida '" + p[0] + "'.");
        }
        if (hora == null) {
            throw new ImportacionInvalida("hora inválida '" + p[1] + "'.");
        }

        RegistroMeteorologico r = new RegistroMeteorologico();
        r.setFecha(fecha);
        r.setHora(hora);
        r.setTempExterior(estacion(p, 2));
        r.setHiTemp(estacion(p, 3));
        r.setLowTemp(estacion(p, 4));
        r.setHumedad(estacion(p, 5));
        r.setPuntoRocio(estacion(p, 6));
        r.setVelocidadViento(estacion(p, 7));
        r.setDireccionViento(estacionTexto(p, 8));
        r.setRecorridoViento(estacion(p, 9));
        r.setRafagaMaxima(estacion(p, 10));
        r.setDireccionRafaga(estacionTexto(p, 11));
        r.setSensacionTermica(estacion(p, 12));
        r.setIndiceCalor(estacion(p, 13));
        r.setIndiceThw(estacion(p, 14));
        r.setPresionBarometrica(estacion(p, 15));
        r.setLluvia(estacion(p, 16));
        r.setTasaLluvia(estacion(p, 17));
        r.setGradosDiaCalefaccion(estacion(p, 18));
        r.setGradosDiaRefrigeracion(estacion(p, 19));
        r.setTempInterior(estacion(p, 20));
        r.setHumedadInterior(estacion(p, 21));
        r.setPuntoRocioInterior(estacion(p, 22));
        r.setIndiceCalorInterior(estacion(p, 23));
        r.setEmcInterior(estacion(p, 24));
        r.setDensidadAireInterior(estacion(p, 25));
        r.setMuestrasViento((int) estacion(p, 26));
        r.setEstadoTransmision((int) estacion(p, 27));
        r.setCalidadRecepcion(estacion(p, 28));
        r.setIntervaloArchivo((int) estacion(p, 29));
        return r;
    }

    /** Valor numérico de la columna; las celdas vacías o "---" son datos faltantes (NaN). */
    private static double estacion(String[] p, int idx) {
        if (idx >= p.length) {
            return Estadistica.SIN_DATO;
        }
        Double valor = parsearNumero(p[idx]);
        return valor == null ? Estadistica.SIN_DATO : valor;
    }

    private static String estacionTexto(String[] p, int idx) {
        if (idx >= p.length) {
            return "";
        }
        return p[idx] == null ? "" : p[idx].trim();
    }

    private static ResultadoImportacion importarExcel(File archivo) throws IOException {
        List<RegistroMeteorologico> registros = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        try (Workbook libro = abrirLibro(archivo)) {
            Sheet hoja = libro.getSheetAt(0);

            int filaDatos = 0;
            IndicesColumnas indices = detectarEncabezados(hoja);
            if (indices != null) {
                filaDatos = 1;
            }

            for (int i = filaDatos; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (fila == null || celdaVacia(fila, indices)) {
                    continue;
                }
                String origen = "Fila " + (i + 1);
                try {
                    registros.add(leerFilaExcel(fila, indices, origen));
                } catch (ImportacionInvalida ex) {
                    errores.add(origen + ": " + ex.getMessage());
                }
            }
        }
        return new ResultadoImportacion(registros, errores);
    }

    /**
     * Abre el libro de Excel. POI señala los archivos dañados, cifrados o que
     * no son Excel con excepciones no verificadas (NotOfficeXmlFileException,
     * EncryptedDocumentException...) que nadie capturaba y la importación
     * fallaba sin mensaje; aquí se convierten en IOException con un texto claro.
     */
    private static Workbook abrirLibro(File archivo) throws IOException {
        try {
            return WorkbookFactory.create(archivo);
        } catch (org.apache.poi.EncryptedDocumentException e) {
            throw new IOException("el libro de Excel está protegido con contraseña.", e);
        } catch (RuntimeException e) {
            throw new IOException("no es un archivo de Excel válido o está dañado.", e);
        } catch (IOException e) {
            // POI responde en inglés ("unsupported file type..."): se antepone el motivo
            throw new IOException("no se pudo abrir como libro de Excel (" + e.getMessage() + ").", e);
        }
    }

    private static RegistroMeteorologico leerFilaExcel(Row fila, IndicesColumnas idx, String origen) {
        int colFecha = idx == null ? 0 : idx.fecha;
        int colHumedad = idx == null ? 1 : idx.humedad;
        int colViento = idx == null ? 2 : idx.viento;
        int colLluvia = idx == null ? 3 : idx.lluvia;
        int colObs = idx == null ? 4 : idx.observaciones;

        return crearRegistro(
                valorCelda(fila.getCell(colFecha)),
                valorCelda(fila.getCell(colHumedad)),
                valorCelda(fila.getCell(colViento)),
                valorCelda(fila.getCell(colLluvia)),
                colObs >= 0 ? valorCelda(fila.getCell(colObs)) : "",
                origen);
    }

    private record IndicesColumnas(int fecha, int humedad, int viento, int lluvia, int observaciones) {
    }

    private static IndicesColumnas detectarEncabezados(Sheet hoja) {
        Row primera = hoja.getRow(0);
        if (primera == null) {
            return null;
        }
        int fecha = -1;
        int humedad = -1;
        int viento = -1;
        int lluvia = -1;
        int obs = -1;
        for (Cell celda : primera) {
            String texto = valorCelda(celda).toLowerCase();
            if (texto.isEmpty()) {
                continue;
            }
            if (texto.contains("fecha")) {
                fecha = celda.getColumnIndex();
            } else if (texto.contains("humedad")) {
                humedad = celda.getColumnIndex();
            } else if (texto.contains("viento")) {
                viento = celda.getColumnIndex();
            } else if (texto.contains("lluvia")) {
                lluvia = celda.getColumnIndex();
            } else if (texto.contains("observ")) {
                obs = celda.getColumnIndex();
            }
        }
        if (fecha < 0 || humedad < 0 || viento < 0 || lluvia < 0) {
            return null;
        }
        return new IndicesColumnas(fecha, humedad, viento, lluvia, obs);
    }

    private static boolean celdaVacia(Row fila, IndicesColumnas idx) {
        int inicio = idx == null ? 0 : Math.min(idx.fecha, idx.humedad);
        int fin = idx == null ? 4 : Math.max(Math.max(idx.fecha, idx.humedad),
                Math.max(idx.viento, idx.lluvia));
        for (int c = inicio; c <= fin; c++) {
            if (!valorCelda(fila.getCell(c)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String valorCelda(Cell celda) {
        if (celda == null) {
            return "";
        }
        return switch (celda.getCellType()) {
            case STRING -> celda.getStringCellValue().trim();
            case BOOLEAN -> String.valueOf(celda.getBooleanCellValue());
            case NUMERIC -> {
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(celda)) {
                    yield celda.getDateCellValue().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate()
                            + " " + celda.getDateCellValue().toInstant()
                                    .atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalTime()
                                    .format(DateTimeFormatter.ofPattern("HH:mm"));
                }
                yield FORMATO_CELDA.formatCellValue(celda).trim();
            }
            default -> "";
        };
    }

    private static RegistroMeteorologico crearRegistro(String fechaTexto, String humedadTexto,
            String vientoTexto, String lluviaTexto, String observaciones, String origen) {
        LocalDateTime fecha = parsearFechaHora(fechaTexto);
        Double humedad = parsearNumero(humedadTexto);
        Double viento = parsearNumero(vientoTexto);
        Double lluvia = parsearNumero(lluviaTexto);

        if (fecha == null) {
            throw new ImportacionInvalida("el campo fecha y hora es inválido. Use dd/MM/yyyy HH:mm.");
        }
        if (humedad == null || humedad < 0 || humedad > 100) {
            throw new ImportacionInvalida("la humedad debe estar entre 0 y 100.");
        }
        if (viento == null || viento < 0) {
            throw new ImportacionInvalida("la velocidad del viento debe ser mayor o igual a 0.");
        }
        if (lluvia == null || lluvia < 0) {
            throw new ImportacionInvalida("la lluvia debe ser mayor o igual a 0.");
        }

        RegistroMeteorologico r = new RegistroMeteorologico();
        r.setFecha(fecha.toLocalDate());
        r.setHora(fecha.toLocalTime());
        r.setHumedad(humedad);
        r.setVelocidadViento(viento);
        r.setLluvia(lluvia);
        r.setObservaciones(observaciones == null ? "" : observaciones);
        return r;
    }

    private static Double parsearNumero(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        String t = texto.trim().toLowerCase();
        t = t.replace("%", "").replace("km/h", "").replace("mm/h", "").replace("mm", "").trim();
        if (t.contains(",") && !t.contains(".")) {
            t = t.replace(',', '.');
        }
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDateTime parsearFechaHora(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        String t = texto.trim();
        DateTimeFormatter f1 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter f2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        DateTimeFormatter f3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter f4 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter f5 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter f6 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            return LocalDateTime.parse(t, f1);
        } catch (DateTimeParseException e) {
            // seguir
        }
        try {
            return LocalDateTime.parse(t, f2);
        } catch (DateTimeParseException e) {
            // seguir
        }
        try {
            return LocalDateTime.parse(t, f3);
        } catch (DateTimeParseException e) {
            // seguir
        }
        try {
            return LocalDateTime.parse(t, f4);
        } catch (DateTimeParseException e) {
            // seguir
        }
        try {
            return LocalDate.parse(t, f5).atStartOfDay();
        } catch (DateTimeParseException e) {
            // seguir
        }
        try {
            return LocalDate.parse(t, f6).atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static class ImportacionInvalida extends RuntimeException {
        ImportacionInvalida(String mensaje) {
            super(mensaje);
        }
    }
}