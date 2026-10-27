package weatherreport_mvc.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Modelo de configuración general del sistema. Se persiste en un archivo
 * properties dentro del directorio del usuario (.weatherreport/config.properties).
 */
public class ConfiguracionSistema {

    public static final String DIR_CONFIG = System.getProperty("user.home")
            + File.separator + ".weatherreport";
    public static final String ARCHIVO_CONFIG = DIR_CONFIG + File.separator + "config.properties";

    private String hostBD = "localhost";
    private String puertoBD = "3306";
    private String usuarioBD = "root";
    private String contrasenaBD = "";
    private String nombreBD = "weather_db";
    private String rutaRespaldos = System.getProperty("user.home")
            + File.separator + "WeatherReportBackups";
    private String rutaReportes = System.getProperty("user.home")
            + File.separator + "WeatherReportReportes";
    private String rutaMysqldump = "mysqldump";
    private String rutaMysql = "mysql";
    private String nombreEstacion = "Estación Central";
    private String ubicacion = "Ciudad Principal";
    private double umbralHumedad = 80.0;
    private double umbralViento = 60.0;
    /** Si es true, se generan datos de prueba cuando la tabla de registros está vacía. */
    private boolean modoPrueba = false;

    public static ConfiguracionSistema cargar() {
        ConfiguracionSistema cfg = new ConfiguracionSistema();
        File f = new File(ARCHIVO_CONFIG);
        if (!f.exists()) {
            return cfg;
        }
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(f)) {
            p.load(in);
            cfg.hostBD = p.getProperty("hostBD", cfg.hostBD);
            cfg.puertoBD = p.getProperty("puertoBD", cfg.puertoBD);
            cfg.usuarioBD = p.getProperty("usuarioBD", cfg.usuarioBD);
            cfg.contrasenaBD = p.getProperty("contrasenaBD", cfg.contrasenaBD);
            cfg.nombreBD = p.getProperty("nombreBD", cfg.nombreBD);
            cfg.rutaRespaldos = p.getProperty("rutaRespaldos", cfg.rutaRespaldos);
            cfg.rutaReportes = p.getProperty("rutaReportes", cfg.rutaReportes);
            cfg.rutaMysqldump = p.getProperty("rutaMysqldump", cfg.rutaMysqldump);
            cfg.rutaMysql = p.getProperty("rutaMysql", cfg.rutaMysql);
            cfg.nombreEstacion = p.getProperty("nombreEstacion", cfg.nombreEstacion);
            cfg.ubicacion = p.getProperty("ubicacion", cfg.ubicacion);
            cfg.umbralHumedad = Double.parseDouble(p.getProperty("umbralHumedad",
                    String.valueOf(cfg.umbralHumedad)));
            cfg.umbralViento = Double.parseDouble(p.getProperty("umbralViento",
                    String.valueOf(cfg.umbralViento)));
            cfg.modoPrueba = Boolean.parseBoolean(p.getProperty("datosPrueba",
                    String.valueOf(cfg.modoPrueba)));
        } catch (IOException | NumberFormatException e) {
            // Si el archivo está corrupto se retornan los valores por defecto.
        }
        return cfg;
    }

    public void guardar() throws IOException {
        File dir = new File(DIR_CONFIG);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Properties p = new Properties();
        p.setProperty("hostBD", hostBD);
        p.setProperty("puertoBD", puertoBD);
        p.setProperty("usuarioBD", usuarioBD);
        p.setProperty("contrasenaBD", contrasenaBD);
        p.setProperty("nombreBD", nombreBD);
        p.setProperty("rutaRespaldos", rutaRespaldos);
        p.setProperty("rutaReportes", rutaReportes);
        p.setProperty("rutaMysqldump", rutaMysqldump);
        p.setProperty("rutaMysql", rutaMysql);
        p.setProperty("nombreEstacion", nombreEstacion);
        p.setProperty("ubicacion", ubicacion);
        p.setProperty("umbralHumedad", String.valueOf(umbralHumedad));
        p.setProperty("umbralViento", String.valueOf(umbralViento));
        p.setProperty("datosPrueba", String.valueOf(modoPrueba));
        try (FileOutputStream out = new FileOutputStream(new File(ARCHIVO_CONFIG))) {
            p.store(out, "Configuracion Weather Report");
        }
    }

    public String getHostBD() {
        return hostBD;
    }

    public void setHostBD(String hostBD) {
        this.hostBD = hostBD;
    }

    public String getPuertoBD() {
        return puertoBD;
    }

    public void setPuertoBD(String puertoBD) {
        this.puertoBD = puertoBD;
    }

    public String getUsuarioBD() {
        return usuarioBD;
    }

    public void setUsuarioBD(String usuarioBD) {
        this.usuarioBD = usuarioBD;
    }

    public String getContrasenaBD() {
        return contrasenaBD;
    }

    public void setContrasenaBD(String contrasenaBD) {
        this.contrasenaBD = contrasenaBD;
    }

    public String getNombreBD() {
        return nombreBD;
    }

    public void setNombreBD(String nombreBD) {
        this.nombreBD = nombreBD;
    }

    public String getRutaRespaldos() {
        return rutaRespaldos;
    }

    public void setRutaRespaldos(String rutaRespaldos) {
        this.rutaRespaldos = rutaRespaldos;
    }

    public String getRutaReportes() {
        return rutaReportes;
    }

    public void setRutaReportes(String rutaReportes) {
        this.rutaReportes = rutaReportes;
    }

    public String getRutaMysqldump() {
        return rutaMysqldump;
    }

    public void setRutaMysqldump(String rutaMysqldump) {
        this.rutaMysqldump = rutaMysqldump;
    }

    public String getRutaMysql() {
        return rutaMysql;
    }

    public void setRutaMysql(String rutaMysql) {
        this.rutaMysql = rutaMysql;
    }

    public String getNombreEstacion() {
        return nombreEstacion;
    }

    public void setNombreEstacion(String nombreEstacion) {
        this.nombreEstacion = nombreEstacion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public double getUmbralHumedad() {
        return umbralHumedad;
    }

    public void setUmbralHumedad(double umbralHumedad) {
        this.umbralHumedad = umbralHumedad;
    }

    public double getUmbralViento() {
        return umbralViento;
    }

    public void setUmbralViento(double umbralViento) {
        this.umbralViento = umbralViento;
    }

    public boolean isModoPrueba() {
        return modoPrueba;
    }

    public void setModoPrueba(boolean modoPrueba) {
        this.modoPrueba = modoPrueba;
    }
}
