package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JTextField;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import weatherreport_mvc.controllers.UsuarioController;
import weatherreport_mvc.models.Usuario;
import weatherreport_mvc.utils.ColoresUI;
import weatherreport_mvc.utils.DateUtil;
import weatherreport_mvc.utils.EstiloUI;
import weatherreport_mvc.utils.EstiloUI.PanelRedondeado;
import weatherreport_mvc.utils.UIFactory;
import java.awt.Component;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Módulo 6 - Gestión de Usuarios. Directorio de usuarios con CRUD completo y
 * registro mediante JDialog modal.
 */
public class UsuariosPanel extends JPanel {

    public static final String KEY = "usuarios";

    private static final String[] COLUMNAS = {
        "ID", "Nombre Completo", "Usuario", "Rol", "Estado", "Fecha Registro"
    };

    private final UsuarioController controller;

    private final DefaultTableModel modelo;
    private final JTable tabla;

    public UsuariosPanel(Usuario sesion) {
        super(new BorderLayout());
        this.controller = new UsuarioController(this, sesion);
        setBackground(ColoresUI.FONDO_RAIZ);

        modelo = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        configurarTabla();

        construirInterfaz();
        controller.cargarUsuarios();
        
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
    tabla.getColumnModel().getColumn(1).setPreferredWidth(320);  // Nombre Completo
    tabla.getColumnModel().getColumn(2).setPreferredWidth(160);  // Usuario
    tabla.getColumnModel().getColumn(3).setPreferredWidth(200);  // Rol
    tabla.getColumnModel().getColumn(4).setPreferredWidth(160);  // Estado
    tabla.getColumnModel().getColumn(5).setPreferredWidth(280);  // Fecha Registro
}

    private void construirInterfaz() {
        PanelRedondeado encabezado = EstiloUI.crearPanelDegradadoVino();
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));
        
        JLabel lblTitulo = new JLabel("Gestión de Usuarios");
        lblTitulo.setFont(UIFactory.FUENTE_TITULO_SLIDE);
        lblTitulo.setForeground(ColoresUI.BLANCO);
        encabezado.add(lblTitulo, BorderLayout.NORTH);
        
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        barra.setBackground(ColoresUI.FONDO_RAIZ);
        barra.setBorder(BorderFactory.createEmptyBorder(10, 20, 8, 20));

        JButton btnNuevo = agrandar(UIFactory.crearBotonPrimario("Nuevo Usuario"));
        btnNuevo.addActionListener(e -> abrirDialogo(null));
        barra.add(btnNuevo);

        JButton btnEditar = agrandar(UIFactory.crearBotonSecundario("Editar"));
        btnEditar.addActionListener(e -> editarSeleccionado());
        barra.add(btnEditar);

        JButton btnEstado = agrandar(UIFactory.crearBotonSecundario("Activar / Desactivar"));
        btnEstado.addActionListener(e -> alternarEstado());
        barra.add(btnEstado);

        JButton btnCodigo = agrandar(UIFactory.crearBotonSecundario("Código de recuperación"));
        btnCodigo.addActionListener(e -> generarCodigoRecuperacion());
        barra.add(btnCodigo);

        JButton btnEliminar = agrandar(UIFactory.crearBotonPeligro("Eliminar"));
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        barra.add(btnEliminar);

        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(barra, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(UIFactory.estilizarTabla(tabla), BorderLayout.CENTER);
    }

    private JButton agrandar(JButton boton) {
    Dimension d = boton.getPreferredSize();
    boton.setPreferredSize(new Dimension(
            (int) (d.width * 1.30), (int) (d.height * 0.875)));
    return boton;
}

    private void abrirDialogo(Usuario usuario) {
        NuevoUsuarioDialog dialogo = new NuevoUsuarioDialog(this, controller, usuario);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private Usuario seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        Usuario u = new Usuario();
        u.setId((int) modelo.getValueAt(fila, 0));
        u.setNombreCompleto((String) modelo.getValueAt(fila, 1));
        u.setUsuario((String) modelo.getValueAt(fila, 2));
        u.setRol((String) modelo.getValueAt(fila, 3));
        u.setActivo("Activo".equals(modelo.getValueAt(fila, 4)));
        return u;
    }

    private void editarSeleccionado() {
        Usuario u = seleccionarFila();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.",
                    "Editar Usuario", JOptionPane.WARNING_MESSAGE);
            return;
        }
        abrirDialogo(u);
    }

    private void alternarEstado() {
        Usuario u = seleccionarFila();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.",
                    "Estado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String error = controller.cambiarEstado(u.getId(), !u.isActivo());
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarSeleccionado() {
        Usuario u = seleccionarFila();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.",
                    "Eliminar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al usuario '" + u.getUsuario() + "'? Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion == JOptionPane.YES_OPTION) {
            String error = controller.eliminar(u.getId());
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Genera un código de recuperación para el usuario seleccionado y lo muestra al Ingeniero. */
    private void generarCodigoRecuperacion() {
        Usuario u = seleccionarFila();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.",
                    "Código de recuperación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        UsuarioController.ResultadoCodigo resultado =
                controller.generarCodigoRecuperacion(u.getUsuario());
        if (resultado.error != null) {
            JOptionPane.showMessageDialog(this, resultado.error, "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTextField campo = new JTextField(resultado.codigo);
        campo.setEditable(false);
        campo.setFont(new Font("Consolas", Font.BOLD, 20));
        campo.selectAll();
        JOptionPane.showMessageDialog(this, new Object[]{
            "Código de recuperación para '" + u.getUsuario() + "':",
            campo,
            "Entrégueselo al usuario. Es de un solo uso y vence en 30 minutos."
        }, "Código de recuperación", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarUsuarios(List<Usuario> usuarios) {
        modelo.setRowCount(0);
        for (Usuario u : usuarios) {
            modelo.addRow(new Object[]{
                u.getId(),
                u.getNombreCompleto(),
                u.getUsuario(),
                u.getRol(),
                u.getEstadoTexto(),
                u.getFechaRegistro() == null
                        ? ""
                        : DateUtil.formatoFechaHora(u.getFechaRegistro())
            });
        }
    }
}
