package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import weatherreport_mvc.controllers.HistoricoController;
import weatherreport_mvc.models.RegistroMeteorologico;
import weatherreport_mvc.utils.Estadistica;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.EstiloUI.PanelRedondeado;
import weatherreport_mvc.utils.UIFactory;
import java.awt.Component;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Módulo 3 - Histórico. Tabla de registros con búsqueda por texto y filtrado
 * por mes conectados a la base de datos.
 */
public class HistoricoPanel extends JPanel {

    public static final String KEY = "historico";

    private static final String[] COLUMNAS = {
        "ID", "Fecha", "Hora", "Temp ext °C", "Humedad %",
        "Viento km/h", "Presión hPa", "Observaciones"
    };

    private final HistoricoController controller;
    private final boolean soloLectura;

    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JTextField txtBusqueda;
    private final JComboBox<String> cmbCampo;
    private final JComboBox<String> cmbMes;
    private final JLabel lblContador;

    private List<RegistroMeteorologico> registrosActuales = List.of();

    public HistoricoPanel(boolean soloLectura) {
        super(new BorderLayout());
        this.soloLectura = soloLectura;
        this.controller = new HistoricoController(this);
        setBackground(ColoresUI.FONDO_RAIZ);

        modelo = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        configurarTabla();
        txtBusqueda = UIFactory.crearTextField(9);
        cmbCampo = new JComboBox<>(new String[]{
            "Todo", "Fecha", "Hora", "Temperatura", "Humedad", "Viento",
            "Presión", "Dirección", "Observaciones"
        });
        cmbMes = new JComboBox<>(new String[]{
            "Todos los meses", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        });
        UIFactory.estilizarCombo(cmbCampo);
        UIFactory.estilizarCombo(cmbMes);
        lblContador = new JLabel("0 registros");

        construirInterfaz();
        controller.cargarTodos();
    }

    private void construirInterfaz() {
        PanelRedondeado encabezado = EstiloUI.crearPanelDegradadoVino();
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));
        
        JLabel lblTitulo = new JLabel("Histórico de Registros");
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);
        lblTitulo.setForeground(ColoresUI.BLANCO);
        encabezado.add(lblTitulo, BorderLayout.NORTH);
        
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        barra.setBackground(ColoresUI.FONDO_RAIZ);
        barra.setBorder(BorderFactory.createEmptyBorder(10, 20, 8, 20));

        barra.add(UIFactory.crearLabel("Campo:"));
        barra.add(cmbCampo);
        barra.add(UIFactory.crearLabel("Buscar:"));
        barra.add(txtBusqueda);
        barra.add(UIFactory.crearLabel("Mes:"));
        barra.add(cmbMes);

        JButton btnBuscar = UIFactory.crearBotonPrimario("Buscar");
        btnBuscar.addActionListener(e -> ejecutarBusqueda());
        barra.add(btnBuscar);

        JButton btnTodos = UIFactory.crearBotonSecundario("Mostrar Todos");
        btnTodos.addActionListener(e -> {
            txtBusqueda.setText("");
            cmbCampo.setSelectedIndex(0);
            cmbMes.setSelectedIndex(0);
            controller.cargarTodos();
        });
        barra.add(btnTodos);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        if (!soloLectura) {
            JButton btnExportar = UIFactory.crearBotonSecundario("Exportar Excel");
            btnExportar.addActionListener(e -> controller.exportarExcel(registrosActuales));
            acciones.add(btnExportar);

            JButton btnImprimir = UIFactory.crearBotonSecundario("Imprimir");
            btnImprimir.addActionListener(e -> imprimirTabla());
            acciones.add(btnImprimir);
        }
        JButton btnDetalle = UIFactory.crearBotonSecundario("Ver detalle");
        btnDetalle.addActionListener(e -> mostrarDetalleSeleccionado());
        acciones.add(btnDetalle);

        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(barra, BorderLayout.SOUTH);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(ColoresUI.FONDO_RAIZ);
        pie.setBorder(BorderFactory.createEmptyBorder(6, 20, 12, 20));
        lblContador.setFont(UIFactory.FUENTE_TEXTO_NEGRITA);
        lblContador.setForeground(ColoresUI.TEXTO_SECUNDARIO);
        pie.add(lblContador, BorderLayout.WEST);
        pie.add(acciones, BorderLayout.EAST);

        add(superior, BorderLayout.NORTH);
        add(UIFactory.estilizarTabla(tabla), BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
    }
    
    private void configurarTabla() {
    tabla.setRowHeight(30);
    tabla.setAutoCreateRowSorter(false);
    tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
    centro.setHorizontalAlignment(SwingConstants.CENTER);

    for (int i = 0; i < tabla.getColumnCount(); i++) {
        tabla.getColumnModel().getColumn(i).setCellRenderer(centro);
    }

    tabla.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getClickCount() == 2) {
                mostrarDetalleSeleccionado();
            }
        }
    });

    JTableHeader header = tabla.getTableHeader();
    header.setReorderingAllowed(false);
    header.setResizingAllowed(false);
    header.setDefaultRenderer(new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(
                javax.swing.JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(ColoresUI.FONDO_RAIZ);
            lbl.setForeground(ColoresUI.TEXTO_VINO);
            lbl.setFont(UIFactory.FUENTE_NEGRITA);
            lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, ColoresUI.BORDE));
            return lbl;
        }
    });

    tabla.getColumnModel().getColumn(0).setPreferredWidth(130);   // ID
    tabla.getColumnModel().getColumn(1).setPreferredWidth(110);   // Fecha
    tabla.getColumnModel().getColumn(2).setPreferredWidth(110);   // Hora
    tabla.getColumnModel().getColumn(3).setPreferredWidth(145);   // Temp ext
    tabla.getColumnModel().getColumn(4).setPreferredWidth(145);   // Humedad
    tabla.getColumnModel().getColumn(5).setPreferredWidth(145);   // Viento
    tabla.getColumnModel().getColumn(6).setPreferredWidth(145);   // Presión
    tabla.getColumnModel().getColumn(7).setPreferredWidth(300);   // Observaciones
}

    private void mostrarDetalleSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un registro para ver su detalle.",
                    "Detalle del registro", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (fila >= registrosActuales.size()) {
            return;
        }
        new DetalleRegistroDialog(this, registrosActuales.get(fila)).setVisible(true);
    }

    private void ejecutarBusqueda() {
        controller.buscar(txtBusqueda.getText().trim(), getMesSeleccionado(), getCampoSeleccionado());
    }

    public String getTextoBusqueda() {
        return txtBusqueda.getText().trim();
    }

    public String getCampoSeleccionado() {
        return (String) cmbCampo.getSelectedItem();
    }

    public Integer getMesSeleccionado() {
        int idx = cmbMes.getSelectedIndex();
        return idx <= 0 ? null : idx;
    }

    public void mostrarRegistros(List<RegistroMeteorologico> registros) {
        registrosActuales = List.copyOf(registros);
        modelo.setRowCount(0);
        for (RegistroMeteorologico r : registros) {
            modelo.addRow(new Object[]{
                r.getId(),
                DateUtil.formatoFechaSeguro(r.getFecha()),
                DateUtil.formatoHoraSeguro(r.getHora()),
                Estadistica.fmt("%.1f", r.getTempExterior()),
                Estadistica.fmt("%.1f", r.getHumedad()),
                Estadistica.fmt("%.1f", r.getVelocidadViento()),
                Estadistica.fmt("%.1f", r.getPresionBarometrica()),
                r.getObservaciones() == null ? "" : r.getObservaciones()
            });
        }
        lblContador.setText(registros.size() + " registros");
    }

    public void cargarTodos() {
        controller.cargarTodos();
    }

    private void imprimirTabla() {
        MessageFormat cabecera = new MessageFormat("Histórico de Registros Meteorológicos");
        MessageFormat pie = new MessageFormat(
                "Página {0} · " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        try {
            tabla.print(JTable.PrintMode.FIT_WIDTH, cabecera, pie);
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo imprimir la tabla:\n" + ex.getMessage(),
                    "Error al imprimir", JOptionPane.ERROR_MESSAGE);
        }
    }
}
