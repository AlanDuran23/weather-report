package weatherreport_mvc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Localiza los ejecutables de MySQL (mysqldump y mysql) usados por el módulo
 * de Respaldo y Restauración. Busca primero la ruta configurada, después el
 * PATH del sistema y finalmente las ubicaciones típicas de instalación en
 * Windows (XAMPP, WAMP, Laragon y MySQL Server).
 */
public final class MySqlLocalizador {

    private static final Map<String, String> CACHE = new HashMap<>();

    private MySqlLocalizador() {
    }

    /**
     * Devuelve la ruta completa del ejecutable, o {@code null} si no se
     * encuentra. El parámetro puede ser un nombre simple ("mysqldump") o una
     * ruta configurada.
     */
    public static synchronized String localizar(String ejecutable) {
        if (ejecutable == null || ejecutable.trim().isEmpty()) {
            return null;
        }
        String ruta = ejecutable.trim();
        String nombre = nombreEjecutable(ruta);
        // La clave es la ruta configurada: si cambia en Configuración se vuelve a buscar.
        if (CACHE.containsKey(ruta)) {
            return CACHE.get(ruta);
        }
        String hallado = buscar(ruta, nombre);
        if (hallado != null) {
            CACHE.put(ruta, hallado);
        }
        return hallado;
    }

    private static String buscar(String configurado, String nombre) {
        File config = new File(configurado);
        if (config.isFile()) {
            return config.getAbsolutePath();
        }
        String enPath = buscarEnPath(nombre);
        if (enPath != null) {
            return enPath;
        }
        for (String dir : directoriosExactos()) {
            File exe = new File(dir, nombre + ".exe");
            if (exe.isFile()) {
                return exe.getAbsolutePath();
            }
        }
        for (String base : directoriosConVersion()) {
            File[] subs = new File(base).listFiles(File::isDirectory);
            if (subs == null) {
                continue;
            }
            for (File sub : subs) {
                File exe = new File(sub, "bin" + File.separator + nombre + ".exe");
                if (exe.isFile()) {
                    return exe.getAbsolutePath();
                }
            }
        }
        for (String raiz : raicesRecursivas()) {
            String hallado = buscarRecursivo(new File(raiz), nombre);
            if (hallado != null) {
                return hallado;
            }
        }
        return null;
    }

    private static String nombreEjecutable(String ruta) {
        String nombre = ruta;
        int idx = Math.max(nombre.lastIndexOf('/'), nombre.lastIndexOf('\\'));
        if (idx >= 0) {
            nombre = nombre.substring(idx + 1);
        }
        if (nombre.toLowerCase().endsWith(".exe")) {
            nombre = nombre.substring(0, nombre.length() - 4);
        }
        return nombre;
    }

    private static String buscarEnPath(String nombre) {
        String comando = System.getProperty("os.name", "").toLowerCase().contains("win")
                ? "where.exe" : "which";
        try {
            Process p = new ProcessBuilder(comando, nombre)
                    .redirectErrorStream(true).start();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    linea = linea.trim();
                    if (!linea.isEmpty()
                            && !linea.toLowerCase().contains("no se encontr")
                            && !linea.toLowerCase().contains("could not find")) {
                        File f = new File(linea);
                        if (f.isFile()) {
                            return f.getAbsolutePath();
                        }
                    }
                }
            } finally {
                p.destroy();
            }
        } catch (Exception e) {
            // No se pudo consultar el PATH; se continúa con las ubicaciones típicas.
        }
        return null;
    }

    private static List<String> directoriosExactos() {
        List<String> lista = new ArrayList<>();
        lista.add("C:\\xampp\\mysql\\bin");
        lista.add("C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin");
        lista.add("C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin");
        lista.add("C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin");
        lista.add("C:\\Program Files (x86)\\MySQL\\MySQL Server 5.7\\bin");
        return lista;
    }

    private static List<String> directoriosConVersion() {
        List<String> lista = new ArrayList<>();
        lista.add("C:\\wamp64\\bin\\mysql");
        lista.add("C:\\wamp\\bin\\mysql");
        lista.add("C:\\laragon\\bin\\mysql");
        return lista;
    }

    private static List<String> raicesRecursivas() {
        List<String> lista = new ArrayList<>();
        lista.add("C:\\Program Files\\MySQL");
        lista.add("C:\\Program Files (x86)\\MySQL");
        return lista;
    }

    private static String buscarRecursivo(File raiz, String nombre) {
        File[] hijos = raiz.listFiles();
        if (hijos == null) {
            return null;
        }
        for (File hijo : hijos) {
            if (hijo.isFile()) {
                if (hijo.getName().equalsIgnoreCase(nombre + ".exe")) {
                    return hijo.getAbsolutePath();
                }
            } else if (hijo.isDirectory()) {
                String hallado = buscarRecursivo(hijo, nombre);
                if (hallado != null) {
                    return hallado;
                }
            }
        }
        return null;
    }
}