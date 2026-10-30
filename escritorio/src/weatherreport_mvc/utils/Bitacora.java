package weatherreport_mvc.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

/**
 * Bitácora de errores de la aplicación en
 * {@code ~/.weatherreport/weatherreport.log}. La versión instalada no tiene
 * consola, así que todo lo que antes iba a System.err se perdía; aquí queda
 * con fecha, contexto y la traza completa. Al pasar de 1 MB el archivo se
 * renombra a {@code weatherreport.log.1} (se conserva solo uno anterior).
 */
public final class Bitacora {

    private static final Path ARCHIVO = Paths.get(System.getProperty("user.home"),
            ".weatherreport", "weatherreport.log");
    private static final long TAMANO_MAXIMO = 1024 * 1024;
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Bitacora() {
    }

    public static Path getArchivo() {
        return ARCHIVO;
    }

    /** Registra un error con su contexto (clase.método) y la traza de la excepción. */
    public static void error(String contexto, Throwable e) {
        StringWriter traza = new StringWriter();
        if (e != null) {
            e.printStackTrace(new PrintWriter(traza));
        }
        escribir("ERROR", contexto, traza.toString().trim());
    }

    /** Registra una situación anómala que no llega a ser error (p. ej. config dañada). */
    public static void aviso(String contexto, String mensaje) {
        escribir("AVISO", contexto, mensaje);
    }

    /**
     * Mensaje legible de una excepción para mostrar al usuario: quita el
     * envoltorio de SwingWorker (ExecutionException) y, si la causa no trae
     * mensaje, usa el nombre de su clase.
     */
    public static String mensaje(Throwable e) {
        Throwable causa = e;
        while ((causa instanceof ExecutionException
                || causa instanceof java.lang.reflect.InvocationTargetException)
                && causa.getCause() != null) {
            causa = causa.getCause();
        }
        if (causa == null) {
            return "error desconocido";
        }
        String texto = causa.getMessage();
        return texto == null || texto.isBlank() ? causa.getClass().getSimpleName() : texto;
    }

    private static synchronized void escribir(String nivel, String contexto, String detalle) {
        String linea = LocalDateTime.now().format(FORMATO) + " [" + nivel + "] "
                + contexto + (detalle.isEmpty() ? "" : ": " + detalle)
                + System.lineSeparator();
        try {
            Files.createDirectories(ARCHIVO.getParent());
            if (Files.exists(ARCHIVO) && Files.size(ARCHIVO) > TAMANO_MAXIMO) {
                Files.move(ARCHIVO, ARCHIVO.resolveSibling("weatherreport.log.1"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            Files.writeString(ARCHIVO, linea, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException ex) {
            // Último recurso: si ni la bitácora se puede escribir, al menos a la consola
            System.err.print(linea);
        }
    }
}
