package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import weatherreport_mvc.controllers.ConfiguracionController;
import weatherreport_mvc.models.ConfiguracionSistema;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.EstiloUI.PanelRedondeado;
import weatherreport_mvc.utils.UIFactory;

/**
 * Módulo 8 - Información. Ajustes de la estación meteorológica y rutas de
 * salida, con el botón "Aplicar Cambios".
 */
public class ConfiguracionPanel extends JPanel {

    public static final String KEY = "configuracion";

    private final ConfiguracionController controller;

    private final JTextField txtRutaReportes;
    private final JTextField txtRutaRespaldos;
    private final JTextField txtNombreEstacion;
    private final JTextField txtUbicacion;

    public ConfiguracionPanel() {
        super(new BorderLayout());
        this.controller = new ConfiguracionController(this);
        setBackground(ColoresUI.FONDO_RAIZ);

        txtRutaReportes = UIFactory.crearTextField(22);
        txtRutaRespaldos = UIFactory.crearTextField(22);
        txtNombreEstacion = UIFactory.crearTextField(20);
        txtUbicacion = UIFactory.crearTextField(20);

        construirInterfaz();
        controller.cargar();
    }

    private void construirInterfaz() {
        PanelRedondeado encabezado = EstiloUI.crearPanelDegradadoVino();
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));

        JLabel lblTitulo = new JLabel("Información del Sistema");
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);
        lblTitulo.setForeground(ColoresUI.BLANCO);
        encabezado.add(lblTitulo, BorderLayout.NORTH);

        JPanel contenedor = new JPanel(new GridLayout(1, 2, 14, 0));
        contenedor.setBackground(ColoresUI.FONDO_RAIZ);
        contenedor.setBorder(BorderFactory.createEmptyBorder(18, 20, 10, 20));

        JPanel cardEstacion = tarjetaCampos("Estación meteorológica",
                filaCampo("Nombre de la estación:", txtNombreEstacion, null),
                filaCampo("Ubicación:", txtUbicacion, null));

        JPanel cardRutas = tarjetaCampos("Rutas de salida",
                filaCampo("Carpeta de reportes:", txtRutaReportes,
                        e -> seleccionarCarpeta(txtRutaReportes, "Carpeta de reportes")),
                filaCampo("Carpeta de respaldos:", txtRutaRespaldos,
                        e -> seleccionarCarpeta(txtRutaRespaldos, "Carpeta de respaldos")));

        contenedor.add(cardEstacion);
        contenedor.add(cardRutas);

        JScrollPane scroll = new JScrollPane(contenedor);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ColoresUI.FONDO_RAIZ);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pie.setBackground(ColoresUI.FONDO_RAIZ);
        pie.setBorder(BorderFactory.createEmptyBorder(0, 20, 12, 20));

        JButton btnAplicar = UIFactory.crearBotonPrimario("Aplicar Cambios");
        btnAplicar.addActionListener(e -> aplicar());
        pie.add(btnAplicar);

        add(encabezado, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
    }

    /** Tarjeta con título al inicio y varias filas de campos agrupadas. */
    private JPanel tarjetaCampos(String titulo, JPanel... filas) {
        JPanel tarjeta = EstiloUI.crearPanelTarjeta();
        tarjeta.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel lblTitulo = UIFactory.crearLabelTitulo(titulo);
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);

        JPanel cuerpo = new JPanel();
        cuerpo.setOpaque(false);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.add(Box.createVerticalStrut(6));
        for (JPanel fila : filas) {
            cuerpo.add(fila);
            cuerpo.add(Box.createVerticalStrut(16));
        }

        tarjeta.add(lblTitulo, BorderLayout.NORTH);
        tarjeta.add(cuerpo, BorderLayout.CENTER);
        return tarjeta;
    }

    /** Fila de un campo: etiqueta sobre el campo y un botón de búsqueda opcional. */
    private JPanel filaCampo(String etiqueta, JTextField campo,
                             java.awt.event.ActionListener accionBuscar) {
        JLabel lbl = UIFactory.crearLabel(etiqueta);
        lbl.setForeground(ColoresUI.TEXTO_SECUNDARIO);

        JPanel linea = new JPanel(new BorderLayout(8, 0));
        linea.setOpaque(false);
        linea.add(campo, BorderLayout.CENTER);
        if (accionBuscar != null) {
            JButton btnBuscar = UIFactory.crearBotonSecundario("...");
            btnBuscar.addActionListener(accionBuscar);
            linea.add(btnBuscar, BorderLayout.EAST);
        }

        JPanel fila = new JPanel();
        fila.setOpaque(false);
        fila.setLayout(new BoxLayout(fila, BoxLayout.Y_AXIS));
        fila.add(lbl);
        fila.add(Box.createVerticalStrut(6));
        fila.add(linea);
        return fila;
    }

    private void seleccionarCarpeta(JTextField campo, String titulo) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(titulo);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String actual = campo.getText().trim();
        if (!actual.isEmpty()) {
            File dir = new File(actual);
            if (dir.isDirectory()) {
                chooser.setCurrentDirectory(dir);
            }
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            campo.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void aplicar() {
        ConfiguracionSistema cfg = ConfiguracionSistema.cargar();
        cfg.setRutaReportes(txtRutaReportes.getText().trim());
        cfg.setRutaRespaldos(txtRutaRespaldos.getText().trim());
        cfg.setNombreEstacion(txtNombreEstacion.getText().trim());
        cfg.setUbicacion(txtUbicacion.getText().trim());

        String error = controller.aplicar(cfg);
        if (error == null) {
            JOptionPane.showMessageDialog(this, "Cambios aplicados correctamente.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void mostrarConfiguracion(ConfiguracionSistema cfg) {
        txtRutaReportes.setText(cfg.getRutaReportes());
        txtRutaRespaldos.setText(cfg.getRutaRespaldos());
        txtNombreEstacion.setText(cfg.getNombreEstacion());
        txtUbicacion.setText(cfg.getUbicacion());
    }
}