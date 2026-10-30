package weatherreport_mvc.utils;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Diálogo "Guardar como" común a reportes PDF, respaldos SQL y exportación a
 * Excel. Resuelve dos fallos de las versiones anteriores: si la carpeta
 * configurada no existía el selector abría en otra sin avisar, y un archivo
 * existente se sobrescribía sin preguntar.
 */
public final class SelectorArchivos {

    private SelectorArchivos() {
    }

    /**
     * Pide al usuario dónde guardar un archivo.
     *
     * @param carpetaPreferida carpeta inicial (la de Configuración); puede ser null
     * @param extension extensión sin punto ("pdf", "sql", "xlsx"); se agrega si falta
     * @return el archivo elegido, o null si el usuario canceló
     */
    public static File elegirDestino(Component padre, String titulo, String carpetaPreferida,
                                     String nombreSugerido, String descripcionFiltro,
                                     String extension) {
        File carpeta = prepararCarpeta(padre, carpetaPreferida);
        JFileChooser selector = new JFileChooser(carpeta);
        selector.setDialogTitle(titulo);
        selector.setFileFilter(new FileNameExtensionFilter(descripcionFiltro, extension));
        selector.setSelectedFile(new File(carpeta, nombreSugerido));
        while (selector.showSaveDialog(padre) == JFileChooser.APPROVE_OPTION) {
            File archivo = conExtension(selector.getSelectedFile(), extension);
            if (!archivo.exists() || confirmarSobrescritura(padre, archivo)) {
                return archivo;
            }
            // Dijo que no: se vuelve a mostrar el selector para elegir otro nombre
            selector.setSelectedFile(archivo);
        }
        return null;
    }

    /**
     * Carpeta inicial del selector: la configurada si existe o se puede crear;
     * si no, la carpeta del usuario, avisando del cambio.
     */
    private static File prepararCarpeta(Component padre, String ruta) {
        File inicio = new File(System.getProperty("user.home"));
        if (ruta == null || ruta.isBlank()) {
            return inicio;
        }
        File carpeta = new File(ruta.trim());
        if (carpeta.isDirectory() || carpeta.mkdirs()) {
            return carpeta;
        }
        Bitacora.aviso("SelectorArchivos", "carpeta configurada no disponible: " + carpeta);
        JOptionPane.showMessageDialog(padre, "La carpeta configurada no está disponible:\n"
                + carpeta.getAbsolutePath()
                + "\n\nElija dónde guardar el archivo. Puede cambiar la carpeta en el módulo Información.",
                "Carpeta no disponible", JOptionPane.WARNING_MESSAGE);
        return inicio;
    }

    private static File conExtension(File archivo, String extension) {
        String sufijo = "." + extension.toLowerCase();
        if (archivo.getName().toLowerCase().endsWith(sufijo)) {
            return archivo;
        }
        return new File(archivo.getParentFile(), archivo.getName() + sufijo);
    }

    private static boolean confirmarSobrescritura(Component padre, File archivo) {
        return JOptionPane.showConfirmDialog(padre,
                "Ya existe un archivo con ese nombre:\n" + archivo.getAbsolutePath()
                + "\n\n¿Desea reemplazarlo?",
                "Reemplazar archivo", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
