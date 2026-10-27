package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
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

        JLabel lblIcono = new JLabel(archivo != null ? "\u2713" : "\u2717");
        lblIcono.setFont(new Font("Segoe UI", Font.BOLD, 56));
        lblIcono.setForeground(archivo != null
                ? ColoresUI.ALERTA_NORMAL_OSC : ColoresUI.ALERTA_CRITICA_OSC);
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
            btnAbrir = UIFactory.crearBotonPrimario("Abrir archivo");
            btnAbrir.addActionListener(e -> PDFGenerator.abrir(archivo));
        }
        JButton btnAceptar = UIFactory.crearBotonSecundario("Aceptar");
        btnAceptar.addActionListener(e -> dispose());

        JPanel botonera = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        botonera.setBackground(ColoresUI.FONDO_RAIZ);
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
}
