package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import weatherreport_mvc.models.RegistroMeteorologico;
import weatherreport_mvc.utils.Estadistica;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.UIFactory;

/**
 * JDialog modal que muestra todas las mediciones de un registro meteorológico
 * de la estación en una cuadrícula etiqueta/valor.
 */
public class DetalleRegistroDialog extends JDialog {

    public DetalleRegistroDialog(Component owner, RegistroMeteorologico r) {
        super(owner instanceof java.awt.Window ? (java.awt.Window) owner : null,
                "Detalle del registro " + r.getId(),
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        JLabel lblEncabezado = UIFactory.crearLabelTitulo(
                "Registro #" + r.getId() + "  ·  "
                + DateUtil.formatoFechaSeguro(r.getFecha()) + " "
                + DateUtil.formatoHoraSeguro(r.getHora()));
        lblEncabezado.setFont(UIFactory.FUENTE_SECCION.deriveFont(18f));

        JPanel grid = new JPanel(new GridLayout(0, 2, 24, 8));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 14));

        agregarGrupo(grid, "EXTERIOR");
        agregarFila(grid, "Temperatura exterior", fmt(r.getTempExterior()) + " °C");
        agregarFila(grid, "Temperatura máxima", fmt(r.getHiTemp()) + " °C");
        agregarFila(grid, "Temperatura mínima", fmt(r.getLowTemp()) + " °C");
        agregarFila(grid, "Punto de rocío", fmt(r.getPuntoRocio()) + " °C");
        agregarFila(grid, "Sensación térmica", fmt(r.getSensacionTermica()) + " °C");
        agregarFila(grid, "Índice de calor", fmt(r.getIndiceCalor()) + " °C");
        agregarFila(grid, "Índice THW", fmt(r.getIndiceThw()) + " °C");
        agregarFila(grid, "Humedad relativa", fmt(r.getHumedad()) + " %");

        agregarGrupo(grid, "VIENTO");
        agregarFila(grid, "Velocidad promedio", fmt(r.getVelocidadViento()) + " km/h");
        agregarFila(grid, "Dirección", r.getDireccionViento().isEmpty()
                ? "--" : r.getDireccionViento());
        agregarFila(grid, "Recorrido del viento", fmt(r.getRecorridoViento()) + " km");
        agregarFila(grid, "Ráfaga máxima", fmt(r.getRafagaMaxima()) + " km/h");
        agregarFila(grid, "Dirección de la ráfaga", r.getDireccionRafaga().isEmpty()
                ? "--" : r.getDireccionRafaga());

        agregarGrupo(grid, "PRESIÓN Y LLUVIA");
        agregarFila(grid, "Presión barométrica", fmt(r.getPresionBarometrica()) + " hPa");
        agregarFila(grid, "Lluvia acumulada", fmt(r.getLluvia()) + " mm");
        agregarFila(grid, "Tasa de lluvia", fmt(r.getTasaLluvia()) + " mm/h");
        agregarFila(grid, "Grados día calefacción", fmt(r.getGradosDiaCalefaccion()));
        agregarFila(grid, "Grados día refrigeración", fmt(r.getGradosDiaRefrigeracion()));

        agregarGrupo(grid, "INTERIOR");
        agregarFila(grid, "Temperatura interior", fmt(r.getTempInterior()) + " °C");
        agregarFila(grid, "Humedad interior", fmt(r.getHumedadInterior()) + " %");
        agregarFila(grid, "Punto de rocío interior", fmt(r.getPuntoRocioInterior()) + " °C");
        agregarFila(grid, "Índice de calor interior", fmt(r.getIndiceCalorInterior()) + " °C");
        agregarFila(grid, "EMC interior", fmt(r.getEmcInterior()) + " %");
        agregarFila(grid, "Densidad del aire interior", Estadistica.fmt("%.4f", r.getDensidadAireInterior()) + " kg/m³");

        agregarGrupo(grid, "SENSORES");
        agregarFila(grid, "Muestras de viento", String.valueOf(r.getMuestrasViento()));
        agregarFila(grid, "Estado ISS (iss_tx)", String.valueOf(r.getEstadoTransmision()));
        agregarFila(grid, "Intervalo de archivo", String.valueOf(r.getIntervaloArchivo()) + " min");
        agregarFila(grid, "Calidad de recepción", fmt(r.getCalidadRecepcion()) + " %");

        agregarGrupo(grid, "OBSERVACIONES");
        JLabel obs = new JLabel(r.getObservaciones() == null || r.getObservaciones().isEmpty()
                ? "--" : r.getObservaciones());
        obs.setFont(UIFactory.FUENTE_TEXTO);
        obs.setForeground(ColoresUI.TEXTO_PRIMARIO);
        obs.setBorder(BorderFactory.createLineBorder(ColoresUI.BORDE, 1));
        obs.setOpaque(true);
        obs.setBackground(ColoresUI.SUPERFICIE);
        obs.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresUI.BORDE, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        grid.add(obs);

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setBackground(ColoresUI.SUPERFICIE);
        scroll.getViewport().setBackground(ColoresUI.SUPERFICIE);
        UIFactory.estilizarScroll(scroll);
        scroll.setPreferredSize(new Dimension(560, 560));

        JButton btnCerrar = UIFactory.crearBotonSecundario("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel botonera = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botonera.setOpaque(false);
        botonera.add(btnCerrar);

        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setBackground(ColoresUI.SUPERFICIE);
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));
        contenido.add(lblEncabezado, BorderLayout.NORTH);
        contenido.add(scroll, BorderLayout.CENTER);
        contenido.add(botonera, BorderLayout.SOUTH);

        setContentPane(contenido);
        pack();
        setLocationRelativeTo(owner);
    }

    private void agregarGrupo(JPanel grid, String titulo) {
        JLabel grupo = new JLabel(titulo);
        grupo.setFont(UIFactory.FUENTE_ETIQUETA);
        grupo.setForeground(ColoresUI.TEXTO_SUAVE);
        grupo.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        grid.add(grupo);
        grid.add(new JLabel(""));
    }

    private void agregarFila(JPanel grid, String etiqueta, String valor) {
        JLabel lbl = UIFactory.crearLabel(etiqueta);
        lbl.setFont(UIFactory.FUENTE_NORMAL.deriveFont(14f));
        lbl.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        JLabel lblValor = UIFactory.crearLabel(valor);
        lblValor.setFont(UIFactory.FUENTE_TEXTO);
        lblValor.setHorizontalAlignment(JLabel.RIGHT);
        lblValor.setForeground(ColoresUI.TEXTO_PRIMARIO);
        grid.add(lbl);
        grid.add(lblValor);
    }

    private static String fmt(double valor) {
        if (!Estadistica.hay(valor)) {
            return "--";
        }
        if (valor == 0.0) {
            return "0";
        }
        if (valor == (long) valor) {
            return String.valueOf((long) valor);
        }
        return String.format("%.1f", valor).replace(",", ".");
    }
}
