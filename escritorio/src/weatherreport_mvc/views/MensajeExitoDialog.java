package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.PDFGenerator;
import weatherreport_mvc.utils.UIFactory;

/**
 * JDialog modal que muestra un mensaje de éxito (o error) tras generar un
 * reporte o ejecutar un respaldo, con la ruta del archivo y opción de abrirlo.
 */
public class MensajeExitoDialog extends JDialog {

    public MensajeExitoDialog(Component owner, String titulo, File archivo) {
        this(owner, titulo, null, archivo);
    }

    public MensajeExitoDialog(Component owner, String titulo, String mensaje) {
        this(owner, titulo, mensaje, null);
    }

    private MensajeExitoDialog(Component owner, String titulo, String mensaje, File archivo) {
        super(owner instanceof java.awt.Window ? (java.awt.Window) owner : null,
                titulo, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        // Icono dibujado: Segoe UI no trae \u2713/\u2717 y sal\u00eda un cuadro vac\u00edo
        JLabel lblIcono = new JLabel(new IconoResultado(archivo != null));
        lblIcono.setHorizontalAlignment(JLabel.CENTER);

        String texto;
        if (archivo != null) {
            texto = "<html><body style='width:420px;text-align:center;'>"
                    + (mensaje == null ? "La operación se completó correctamente." : mensaje)
                    + "<br><br><b>Archivo:</b><br>"
                    + archivo.getAbsolutePath() + "</body></html>";
        } else {
            texto = "<html><body style='width:420px;text-align:center;'>"
                    + (mensaje == null ? "La operación no se completó." : mensaje)
                    + "</body></html>";
        }
        JLabel lblTexto = UIFactory.crearLabel(texto);
        lblTexto.setHorizontalAlignment(JLabel.CENTER);

        JButton btnAbrir = null;
        if (archivo != null) {
            // Un respaldo .sql casi nunca tiene programa asociado: se ofrece su carpeta
            boolean esRespaldo = archivo.getName().toLowerCase().endsWith(".sql");
            btnAbrir = UIFactory.crearBotonPrimario(esRespaldo ? "Abrir carpeta" : "Abrir archivo");
            btnAbrir.addActionListener(e -> {
                if (esRespaldo) {
                    PDFGenerator.abrirCarpeta(this, archivo);
                } else {
                    PDFGenerator.abrir(this, archivo);
                }
            });
        }
        JButton btnAceptar = UIFactory.crearBotonSecundario("Aceptar");
        btnAceptar.addActionListener(e -> dispose());

        JPanel botonera = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        botonera.setOpaque(false);
        if (btnAbrir != null) {
            botonera.add(btnAbrir);
        }
        botonera.add(btnAceptar);

        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setBackground(ColoresUI.SUPERFICIE);
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 26, 14, 26));
        contenido.add(lblIcono, BorderLayout.NORTH);
        contenido.add(lblTexto, BorderLayout.CENTER);
        contenido.add(botonera, BorderLayout.SOUTH);

        setContentPane(contenido);
        pack();
        setLocationRelativeTo(owner);
    }

    /** Círculo tenue con palomita (éxito) o tache (error) en el color del estado. */
    private record IconoResultado(boolean exito) implements javax.swing.Icon {

        @Override
        public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.Color color = exito ? ColoresUI.ALERTA_NORMAL_OSC : ColoresUI.ALERTA_CRITICA_OSC;
            g2.setColor(new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), 36));
            g2.fillOval(x, y, 56, 56);
            g2.setColor(color);
            g2.setStroke(new java.awt.BasicStroke(3.5f, java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            if (exito) {
                g2.drawPolyline(new int[]{x + 17, x + 25, x + 39}, new int[]{y + 29, y + 37, y + 21}, 3);
            } else {
                g2.drawLine(x + 20, y + 20, x + 36, y + 36);
                g2.drawLine(x + 36, y + 20, x + 20, y + 36);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 56;
        }

        @Override
        public int getIconHeight() {
            return 56;
        }
    }
}
