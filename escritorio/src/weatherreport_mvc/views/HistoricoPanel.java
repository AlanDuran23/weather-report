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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import weatherreport_mvc.controllers.HistoricoController;
import weatherreport_mvc.models.RegistroMeteorologico;
import weatherreport_mvc.utils.Estadistica;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.UIFactory;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        JPanel encabezado = EstiloUI.crearEncabezadoModulo("Histórico de Registros",
                "Consulte, filtre y exporte las mediciones guardadas. Doble clic en una fila para ver el detalle.");

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setBackground(ColoresUI.FONDO_RAIZ);
        barra.setBorder(BorderFactory.createEmptyBorder(16, 12, 12, 20));

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
        pie.setBorder(BorderFactory.createEmptyBorder(12, 20, 14, 20));
        lblContador.setFont(UIFactory.FUENTE_NORMAL);
        lblContador.setForeground(ColoresUI.TEXTO_SUAVE);
        pie.add(lblContador, BorderLayout.WEST);
        pie.add(acciones, BorderLayout.EAST);

        // La tabla respeta el mismo margen de 20 px que la cabecera y los filtros
        JPanel zonaTabla = new JPanel(new BorderLayout());
        zonaTabla.setBackground(ColoresUI.FONDO_RAIZ);
        zonaTabla.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        zonaTabla.add(UIFactory.estilizarTabla(tabla), BorderLayout.CENTER);

        add(superior, BorderLayout.NORTH);
        add(zonaTabla, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
    }

    private void configurarTabla() {
        tabla.setAutoCreateRowSorter(false);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        // ID y lecturas son cifras: a la derecha para comparar por magnitud
        UIFactory.columnasNumericas(tabla, 0, 3, 4, 5, 6);

        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    mostrarDetalleSeleccionado();
                }
            }
        });

        JTableHeader header = tabla.getTableHeader();
        header.setResizingAllowed(false);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(90);    // ID
        tabla.getColumnModel().getColumn(1).setPreferredWidth(120);   // Fecha
        tabla.getColumnModel().getColumn(2).setPreferredWidth(110);   // Hora
        tabla.getColumnModel().getColumn(3).setPreferredWidth(140);   // Temp ext
        tabla.getColumnModel().getColumn(4).setPreferredWidth(130);   // Humedad
        tabla.getColumnModel().getColumn(5).setPreferredWidth(130);   // Viento
        tabla.getColumnModel().getColumn(6).setPreferredWidth(140);   // Presión
        tabla.getColumnModel().getColumn(7).setPreferredWidth(360);   // Observaciones
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
        lblContador.setForeground(ColoresUI.TEXTO_SUAVE);
        lblContador.setToolTipText(null);
    }

    /** La consulta falló: tabla vacía y el motivo en el pie, no un engañoso "0 registros". */
    public void mostrarErrorCarga(String detalle) {
        registrosActuales = List.of();
        modelo.setRowCount(0);
        lblContador.setText("No se pudieron cargar los registros. " + detalle);
        lblContador.setForeground(ColoresUI.ALERTA_ATENCION_OSC);
        lblContador.setToolTipText(null);
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
