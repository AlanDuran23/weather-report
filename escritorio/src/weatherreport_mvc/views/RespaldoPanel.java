package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import weatherreport_mvc.utils.SelectorArchivos;
import weatherreport_mvc.utils.UIFactory;

/**
 * Módulo 7 - Respaldo y Restauración. Exporta la base de datos a un archivo .sql
 * y la restaura, por la misma conexión de la aplicación (ver RespaldoBD).
 */
public class RespaldoPanel extends JPanel {

    public static final String KEY = "respaldo";

    private final RespaldoController controller;
    private final JTextArea txtLog;
    private boolean hayOperaciones;

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
        JPanel encabezado = EstiloUI.crearEncabezadoModulo("Respaldo y Restauración",
                "Copias de seguridad de la base de datos weather_db");

        JPanel tarjetas = new JPanel(new GridLayout(1, 2, 16, 0));
        tarjetas.setOpaque(false);

        // Respaldar es la operación habitual y segura: lleva el acento
        tarjetas.add(construirTarjeta(
                "Realizar respaldo",
                "Genera una copia completa de la base de datos "
                + "(weather_db) y la guarda como archivo .sql.",
                null,
                UIFactory.crearBotonPrimario("Realizar respaldo"),
                e -> realizarRespaldo()));

        // Restaurar sobrescribe datos: botón secundario y aviso visible antes de pulsar
        tarjetas.add(construirTarjeta(
                "Restaurar respaldo",
                "Ejecuta un archivo .sql (de esta aplicación o de mysqldump) sobre la "
                + "base de datos para recuperar información.",
                "Sobrescribe los datos actuales de la base.",
                UIFactory.crearBotonSecundario("Restaurar respaldo…"),
                e -> restaurarRespaldo()));

        txtLog.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        txtLog.setText("Sin operaciones en esta sesión.\n");
        txtLog.setForeground(ColoresUI.TEXTO_SUAVE);
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createLineBorder(ColoresUI.BORDE, 1));
        UIFactory.estilizarScroll(scrollLog);

        JPanel zonaLog = new JPanel(new BorderLayout(0, 10));
        zonaLog.setOpaque(false);
        zonaLog.add(UIFactory.crearEtiquetaGrupo("Bitácora de operaciones"), BorderLayout.NORTH);
        zonaLog.add(scrollLog, BorderLayout.CENTER);

        JPanel contenedor = new JPanel(new BorderLayout(0, 24));
        contenedor.setBackground(ColoresUI.FONDO_RAIZ);
        contenedor.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contenedor.add(tarjetas, BorderLayout.NORTH);
        contenedor.add(zonaLog, BorderLayout.CENTER);

        add(encabezado, BorderLayout.NORTH);
        add(contenedor, BorderLayout.CENTER);
    }

    private JPanel construirTarjeta(String titulo, String descripcion, String aviso,
                                    JButton btn, java.awt.event.ActionListener accion) {
        JPanel tarjeta = EstiloUI.crearPanelTarjeta();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JLabel lblTitulo = UIFactory.crearLabelTitulo(titulo);
        JLabel lblDesc = UIFactory.crearLabel("<html><body style='width:360px;'>"
                + descripcion + "</body></html>");
        lblDesc.setFont(UIFactory.FUENTE_NORMAL.deriveFont(14f));
        btn.addActionListener(accion);

        tarjeta.add(alinear(lblTitulo));
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(alinear(lblDesc));
        if (aviso != null) {
            JLabel lblAviso = new JLabel(aviso);
            lblAviso.setFont(UIFactory.FUENTE_NORMAL);
            lblAviso.setForeground(ColoresUI.ALERTA_ATENCION_OSC);
            tarjeta.add(Box.createVerticalStrut(8));
            tarjeta.add(alinear(lblAviso));
        }
        tarjeta.add(Box.createVerticalStrut(20));
        tarjeta.add(Box.createVerticalGlue());
        tarjeta.add(alinear(btn));
        return tarjeta;
    }

    private static JComponent alinear(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private void realizarRespaldo() {
        File archivo = SelectorArchivos.elegirDestino(this, "Guardar respaldo SQL",
                ConfiguracionSistema.cargar().getRutaRespaldos(),
                "respaldo_" + DateUtil.selloArchivo() + ".sql", "Archivos SQL (*.sql)", "sql");
        if (archivo != null) {
            controller.realizarRespaldo(archivo);
        }
    }

    private void restaurarRespaldo() {
        // Abre en la carpeta de respaldos configurada (si no existe, JFileChooser usa la del usuario)
        JFileChooser chooser = new JFileChooser(ConfiguracionSistema.cargar().getRutaRespaldos());
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

    /** Quita el texto de estado vacío antes de la primera línea real. */
    private void prepararLog() {
        if (!hayOperaciones) {
            hayOperaciones = true;
            txtLog.setText("");
            txtLog.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        }
    }

    public void mostrarProgreso(String mensaje) {
        prepararLog();
        txtLog.append(mensaje + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    public void mostrarExito(String mensaje, File archivo) {
        prepararLog();
        txtLog.append(mensaje + " -> " + archivo.getAbsolutePath() + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
        new MensajeExitoDialog(this, mensaje, archivo).setVisible(true);
    }

    public void mostrarError(String mensaje) {
        prepararLog();
        txtLog.append("[ERROR] " + mensaje + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
        new MensajeExitoDialog(this, "Error en la operación", mensaje).setVisible(true);
    }
}
