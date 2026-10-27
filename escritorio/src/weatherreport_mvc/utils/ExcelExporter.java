package weatherreport_mvc.utils;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import weatherreport_mvc.models.RegistroMeteorologico;

/**
 * Utilidad para exportar registros meteorológicos a un archivo Excel (.xlsx).
 */
public final class ExcelExporter {

    private static final String[] COLUMNAS = {
        "ID", "Fecha", "Hora",
        "Temp ext (°C)", "Temp máxima (°C)", "Temp mínima (°C)", "Punto de rocío (°C)",
        "Sensación térmica (°C)", "Índice de calor (°C)", "Índice THW (°C)",
        "Humedad relativa (%)",
        "Velocidad promedio (km/h)", "Dirección", "Recorrido del viento (km)",
        "Ráfaga máxima (km/h)", "Dirección de la ráfaga",
        "Presión barométrica (hPa)", "Lluvia acumulada (mm)", "Tasa de lluvia (mm/h)",
        "Grados día calefacción", "Grados día refrigeración",
        "Temp interior (°C)", "Humedad interior (%)", "Punto de rocío interior (°C)",
        "Índice de calor interior (°C)", "EMC interior (%)", "Densidad del aire interior (kg/m³)",
        "Muestras de viento", "Estado ISS", "Intervalo de archivo (min)",
        "Calidad de recepción (%)", "Observaciones"
    };

    private ExcelExporter() {
    }

    public static void exportarExcel(List<RegistroMeteorologico> registros, File destino)
            throws IOException {
        try (Workbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("Histórico");

            CellStyle estiloEncabezado = libro.createCellStyle();
            estiloEncabezado.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
            estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font fuenteEncabezado = libro.createFont();
            fuenteEncabezado.setBold(true);
            fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
            estiloEncabezado.setFont(fuenteEncabezado);

            Row filaEncabezado = hoja.createRow(0);
            for (int i = 0; i < COLUMNAS.length; i++) {
                Cell celda = filaEncabezado.createCell(i);
                celda.setCellValue(COLUMNAS[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            int filaActual = 1;
            for (RegistroMeteorologico r : registros) {
                Row fila = hoja.createRow(filaActual++);
                fila.createCell(0).setCellValue(r.getId());
                fila.createCell(1).setCellValue(DateUtil.formatoFechaEstacion(r.getFecha()));
                fila.createCell(2).setCellValue(DateUtil.formatoHoraEstacion(r.getHora()));
                numero(fila, 3, r.getTempExterior());
                numero(fila, 4, r.getHiTemp());
                numero(fila, 5, r.getLowTemp());
                numero(fila, 6, r.getPuntoRocio());
                numero(fila, 7, r.getSensacionTermica());
                numero(fila, 8, r.getIndiceCalor());
                numero(fila, 9, r.getIndiceThw());
                numero(fila, 10, r.getHumedad());
                numero(fila, 11, r.getVelocidadViento());
                fila.createCell(12).setCellValue(r.getDireccionViento());
                numero(fila, 13, r.getRecorridoViento());
                numero(fila, 14, r.getRafagaMaxima());
                fila.createCell(15).setCellValue(r.getDireccionRafaga());
                numero(fila, 16, r.getPresionBarometrica());
                numero(fila, 17, r.getLluvia());
                numero(fila, 18, r.getTasaLluvia());
                numero(fila, 19, r.getGradosDiaCalefaccion());
                numero(fila, 20, r.getGradosDiaRefrigeracion());
                numero(fila, 21, r.getTempInterior());
                numero(fila, 22, r.getHumedadInterior());
                numero(fila, 23, r.getPuntoRocioInterior());
                numero(fila, 24, r.getIndiceCalorInterior());
                numero(fila, 25, r.getEmcInterior());
                numero(fila, 26, r.getDensidadAireInterior());
                fila.createCell(27).setCellValue(r.getMuestrasViento());
                fila.createCell(28).setCellValue(r.getEstadoTransmision());
                fila.createCell(29).setCellValue(r.getIntervaloArchivo());
                numero(fila, 30, r.getCalidadRecepcion());
                fila.createCell(31).setCellValue(
                        r.getObservaciones() == null ? "" : r.getObservaciones());
            }

            for (int i = 0; i < COLUMNAS.length; i++) {
                hoja.autoSizeColumn(i);
            }

            try (FileOutputStream salida = new FileOutputStream(destino)) {
                libro.write(salida);
            }
        }
    }

    /** Escribe el número en la celda; si el dato falta la celda queda vacía. */
    private static void numero(Row fila, int columna, double valor) {
        Cell celda = fila.createCell(columna);
        if (Estadistica.hay(valor)) {
            celda.setCellValue(valor);
        }
    }

    public static File elegirDestino(Component padre) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Exportar Histórico a Excel");
        selector.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
        selector.setSelectedFile(new File("historico.xlsx"));
        if (selector.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File archivo = selector.getSelectedFile();
        String nombre = archivo.getName();
        if (!nombre.toLowerCase().endsWith(".xlsx")) {
            archivo = new File(archivo.getParentFile(), nombre + ".xlsx");
        }
        return archivo;
    }

    public static void mostrarError(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error al exportar",
                JOptionPane.ERROR_MESSAGE);
    }
}