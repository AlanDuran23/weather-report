package weatherreport_mvc.views;

import java.awt.BorderLayout;
import java.awt.Color;
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
import weatherreport_mvc.utils.UIFactory;
import java.awt.Component;

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
        tabla.setAutoCreateRowSorter(false);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        UIFactory.columnasNumericas(tabla, 0);
        tabla.getTableHeader().setResizingAllowed(false);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);    // ID
        tabla.getColumnModel().getColumn(1).setPreferredWidth(320);   // Nombre Completo
        tabla.getColumnModel().getColumn(2).setPreferredWidth(180);   // Usuario
        tabla.getColumnModel().getColumn(3).setPreferredWidth(180);   // Rol
        tabla.getColumnModel().getColumn(4).setPreferredWidth(160);   // Estado
        tabla.getColumnModel().getColumn(5).setPreferredWidth(220);   // Fecha Registro
        tabla.getColumnModel().getColumn(4).setCellRenderer(new EstadoRenderer());
    }

    /** Estado con punto de color: verde si está activo, gris si no. */
    private static final class EstadoRenderer extends UIFactory.CeldaRenderer {

        @Override
        public Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            boolean activo = "Activo".equals(value);
            lbl.setIcon(new PuntoEstado(activo ? ColoresUI.ALERTA_NORMAL_OSC : ColoresUI.TEXTO_SUAVE));
            lbl.setIconTextGap(8);
            if (!isSelected && !activo) {
                lbl.setForeground(ColoresUI.TEXTO_SUAVE);
            }
            return lbl;
        }
    }

    private record PuntoEstado(Color color) implements javax.swing.Icon {

        @Override
        public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, 7, 7);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 7;
        }

        @Override
        public int getIconHeight() {
            return 7;
        }
    }

    private void construirInterfaz() {
        JPanel encabezado = EstiloUI.crearEncabezadoModulo("Gestión de Usuarios",
                "Cuentas con acceso al sistema. Seleccione una fila para editarla.");

        // Acciones sobre la cuenta seleccionada a la izquierda; eliminar, aparte a la derecha
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);

        JButton btnNuevo = UIFactory.crearBotonPrimario("Nuevo usuario");
        btnNuevo.addActionListener(e -> abrirDialogo(null));
        acciones.add(btnNuevo);

        JButton btnEditar = UIFactory.crearBotonSecundario("Editar");
        btnEditar.addActionListener(e -> editarSeleccionado());
        acciones.add(btnEditar);

        JButton btnEstado = UIFactory.crearBotonSecundario("Activar / Desactivar");
        btnEstado.addActionListener(e -> alternarEstado());
        acciones.add(btnEstado);

        JButton btnCodigo = UIFactory.crearBotonSecundario("Código de recuperación");
        btnCodigo.addActionListener(e -> generarCodigoRecuperacion());
        acciones.add(btnCodigo);

        JButton btnEliminar = UIFactory.crearBotonPeligro("Eliminar");
        btnEliminar.addActionListener(e -> eliminarSeleccionado());

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(ColoresUI.FONDO_RAIZ);
        barra.setBorder(BorderFactory.createEmptyBorder(16, 12, 12, 20));
        barra.add(acciones, BorderLayout.WEST);
        barra.add(btnEliminar, BorderLayout.EAST);

        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(barra, BorderLayout.SOUTH);

        JPanel zonaTabla = new JPanel(new BorderLayout());
        zonaTabla.setBackground(ColoresUI.FONDO_RAIZ);
        zonaTabla.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        zonaTabla.add(UIFactory.estilizarTabla(tabla), BorderLayout.CENTER);

        add(superior, BorderLayout.NORTH);
        add(zonaTabla, BorderLayout.CENTER);
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

    /** No se pudo leer la lista: se avisa en lugar de mostrar una tabla vacía sin explicación. */
    public void mostrarErrorCarga(String mensaje) {
        modelo.setRowCount(0);
        JOptionPane.showMessageDialog(this, "No se pudo cargar la lista de usuarios.\n" + mensaje,
                "Gestión de Usuarios", JOptionPane.ERROR_MESSAGE);
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
