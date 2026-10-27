package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import weatherreport_mvc.controllers.RespaldoController;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.EstiloUI.PanelRedondeado;
import weatherreport_mvc.utils.UIFactory;

/**
 * Módulo 7 - Respaldo y Restauración. Exporta la base de datos con mysqldump
 * y la restaura con mysql mediante comandos del sistema.
 */
public class RespaldoPanel extends JPanel {

    public static final String KEY = "respaldo";

    private final RespaldoController controller;
    private final JTextArea txtLog;

    public RespaldoPanel() {
        super(new BorderLayout());
        this.controller = new RespaldoController(this);
        this.txtLog = new JTextArea(8, 60);
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtLog.setOpaque(true);
        txtLog.setBackground(ColoresUI.SUPERFICIE);
        txtLog.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        txtLog.setCaretColor(ColoresUI.TEXTO_PRIMARIO);
        setBackground(ColoresUI.FONDO_RAIZ);
        construirInterfaz();
    }

    private void construirInterfaz() {
        PanelRedondeado encabezado = EstiloUI.crearPanelDegradadoVino();
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));
        
        JLabel lblTitulo = new JLabel("Respaldo y Restauración");
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);
        lblTitulo.setForeground(ColoresUI.BLANCO);
        encabezado.add(lblTitulo, BorderLayout.NORTH);
        
        JPanel contenedor = new JPanel(new GridLayout(1, 2, 14, 0));
        contenedor.setBackground(ColoresUI.FONDO_RAIZ);
        contenedor.setBorder(BorderFactory.createEmptyBorder(18, 20, 10, 20));

        contenedor.add(construirTarjeta(
                "Realizar Respaldo",
                "Genera una copia completa de la base de datos "
                + "(weather_db) usando mysqldump y la guarda como archivo .sql.",
                "Realizar Respaldo",
                e -> realizarRespaldo()));

        contenedor.add(construirTarjeta(
                "Restaurar Respaldo",
                "Ejecuta un archivo .sql sobre la base de datos usando el cliente "
                + "mysql para recuperar información.",
                "Restaurar Respaldo",
                e -> restaurarRespaldo()));

        JPanel zonaLog = new JPanel(new BorderLayout());
        zonaLog.setBackground(ColoresUI.FONDO_RAIZ);
        zonaLog.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        zonaLog.add(UIFactory.crearTarjeta(new JScrollPane(txtLog)), BorderLayout.CENTER);

        add(encabezado, BorderLayout.NORTH);
        add(contenedor, BorderLayout.CENTER);
        add(zonaLog, BorderLayout.SOUTH);
    }

    private JPanel construirTarjeta(String titulo, String descripcion,
                                    String textoBoton,
                                    java.awt.event.ActionListener accion) {
        JPanel tarjeta = EstiloUI.crearPanelTarjeta();
        tarjeta.setBorder(BorderFactory.createEmptyBorder(26, 26, 26, 26));

        JLabel lblTitulo = UIFactory.crearLabelTitulo(titulo);
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);

        JLabel lblDesc = UIFactory.crearLabel("<html><body style='width:320px;'>"
                + descripcion + "</body></html>");

        JButton btn = UIFactory.crearBotonPrimario(textoBoton);
        btn.addActionListener(accion);

        JPanel inferior = new JPanel(new BorderLayout());
        inferior.setOpaque(false);
        inferior.add(btn, BorderLayout.WEST);

        tarjeta.add(lblTitulo, BorderLayout.NORTH);
        tarjeta.add(lblDesc, BorderLayout.CENTER);
        tarjeta.add(inferior, BorderLayout.SOUTH);
        return tarjeta;
    }

    private void realizarRespaldo() {
        ConfiguracionSistema cfg = ConfiguracionSistema.cargar();
        File dir = new File(cfg.getRutaRespaldos());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setDialogTitle("Guardar respaldo SQL");
        chooser.setSelectedFile(new File(dir, "respaldo_" + DateUtil.selloArchivo() + ".sql"));
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos SQL (*.sql)", "sql"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith(".sql")) {
                archivo = new File(archivo.getParentFile(), archivo.getName() + ".sql");
            }
            controller.realizarRespaldo(archivo);
        }
    }

    private void restaurarRespaldo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar archivo de respaldo");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos SQL (*.sql)", "sql"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = chooser.getSelectedFile();
        int opcion = JOptionPane.showConfirmDialog(this,
                "Se restaurará el archivo:\n" + archivo.getName()
                + "\n\nEsta operación sobrescribirá los datos existentes. ¿Continuar?",
                "Confirmar restauración", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (opcion == JOptionPane.YES_OPTION) {
            controller.restaurarRespaldo(archivo);
        }
    }

    public void mostrarProgreso(String mensaje) {
        txtLog.append(mensaje + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    public void mostrarExito(String mensaje, File archivo) {
        txtLog.append(mensaje + " -> " + archivo.getAbsolutePath() + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
        new MensajeExitoDialog(this, mensaje, archivo).setVisible(true);
    }

    public void mostrarError(String mensaje) {
        txtLog.append("[ERROR] " + mensaje + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
        new MensajeExitoDialog(this, "Error en la operación", mensaje).setVisible(true);
    }
}
