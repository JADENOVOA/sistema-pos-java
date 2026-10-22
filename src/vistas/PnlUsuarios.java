package vistas;

import dao.UsuarioDAO;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.List;

public class PnlUsuarios extends JPanel {

    private Usuario usuarioLogueado;
    private UsuarioDAO usuarioDAO;

    private JTable tblUsuarios;
    private DefaultTableModel modeloTabla;

    private final Color COLOR_VERDE = new Color(21, 128, 61);
    private final Color COLOR_VERDE_OSCURO = new Color(22, 101, 52);
    private final Color COLOR_FONDO = new Color(241, 245, 249);
    private final Color COLOR_BORDE = new Color(226, 232, 240);

    public PnlUsuarios(Usuario usuarioLogueado) {

        this.usuarioLogueado = usuarioLogueado;
        this.usuarioDAO = new UsuarioDAO();

        setLayout(new BorderLayout(0, 14));
        setBackground(COLOR_FONDO);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        crearInterfaz();
        refrescarUsuarios();
    }

    private void crearInterfaz() {

        // CABECERA
        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setBackground(Color.WHITE);
        pnlCabecera.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel pnlTitulos = new JPanel();
        pnlTitulos.setLayout(new BoxLayout(pnlTitulos, BoxLayout.Y_AXIS));
        pnlTitulos.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Administración de Usuarios y Accesos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lblSubtitulo = new JLabel(
                "Gestión de credenciales, roles y estado del personal."
        );
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        pnlTitulos.add(lblTitulo);
        pnlTitulos.add(Box.createRigidArea(new Dimension(0, 4)));
        pnlTitulos.add(lblSubtitulo);

        
JButton btnNuevo = new JButton("+  Nuevo usuario") {

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(getBackground());

        g2.fillRoundRect(
                0, 0,
                getWidth(), getHeight(),
                16, 16
        );

        g2.dispose();

        super.paintComponent(g);
    }
};

btnNuevo.setFont(
        new Font("Segoe UI", Font.BOLD, 13)
);

btnNuevo.setForeground(Color.WHITE);
btnNuevo.setBackground(new Color(21, 128, 61));

btnNuevo.setFocusPainted(false);
btnNuevo.setBorderPainted(false);
btnNuevo.setContentAreaFilled(false);
btnNuevo.setOpaque(false);

btnNuevo.setCursor(
        new Cursor(Cursor.HAND_CURSOR)
);

btnNuevo.setMargin(
        new Insets(12, 20, 12, 20)
);

btnNuevo.setPreferredSize(
        new Dimension(165, 44)
);

// Efecto al pasar el mouse
btnNuevo.addMouseListener(new java.awt.event.MouseAdapter() {

    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {
        btnNuevo.setBackground(new Color(22, 101, 52));
    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {
        btnNuevo.setBackground(new Color(21, 128, 61));
    }
});

btnNuevo.addActionListener(
        e -> mostrarFormularioNuevo()
);


        pnlCabecera.add(pnlTitulos, BorderLayout.WEST);
        pnlCabecera.add(btnNuevo, BorderLayout.EAST);

        add(pnlCabecera, BorderLayout.NORTH);

        // CENTRO
        JPanel pnlCentro = new JPanel(new BorderLayout(0, 12));
        pnlCentro.setBackground(COLOR_FONDO);

        // INFO ROLES
        JPanel pnlRoles = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlRoles.setBackground(COLOR_FONDO);

        pnlRoles.add(crearTarjetaRol(
                "Rol Administrador",
                "Acceso completo: productos, inventario, caja, reportes y usuarios."
        ));

        pnlRoles.add(crearTarjetaRol(
                "Rol Cajero",
                "Acceso a ventas, caja y consulta operativa."
        ));

        pnlCentro.add(pnlRoles, BorderLayout.NORTH);

        // TABLA
        String[] columnas = {
            "N°",
            "ID_REAL",
            "Nombre Completo",
            "Usuario",
            "Rol",
            "Estado"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblUsuarios = new JTable(modeloTabla);
        tblUsuarios.setRowHeight(32);
        tblUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblUsuarios.setFillsViewportHeight(true);

        // ocultar ID_REAL
        tblUsuarios.removeColumn(
                tblUsuarios.getColumnModel().getColumn(1)
        );

        JScrollPane scroll = new JScrollPane(tblUsuarios);

        JPanel pnlTabla = new JPanel(new BorderLayout());
        pnlTabla.setBackground(Color.WHITE);
        pnlTabla.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblTabla = new JLabel("Usuarios Registrados en el Sistema");
        lblTabla.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlAcciones.setBackground(Color.WHITE);

        JButton btnEditar = new JButton("Editar");
        JButton btnEstado = new JButton("Activar / Desactivar");

        btnEditar.addActionListener(e -> editarUsuarioSeleccionado());
        btnEstado.addActionListener(e -> cambiarEstadoSeleccionado());

        pnlAcciones.add(btnEditar);
        pnlAcciones.add(btnEstado);

        pnlTabla.add(lblTabla, BorderLayout.NORTH);
        pnlTabla.add(scroll, BorderLayout.CENTER);
        pnlTabla.add(pnlAcciones, BorderLayout.SOUTH);

        pnlCentro.add(pnlTabla, BorderLayout.CENTER);

        add(pnlCentro, BorderLayout.CENTER);
    }

    private JPanel crearTarjetaRol(String titulo, String descripcion) {

        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(Color.WHITE);

        pnl.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(COLOR_VERDE_OSCURO);

        JLabel lblDescripcion = new JLabel(
                "<html>" + descripcion + "</html>"
        );
        lblDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        pnl.add(lblTitulo);
        pnl.add(Box.createRigidArea(new Dimension(0, 6)));
        pnl.add(lblDescripcion);

        return pnl;
    }

    public void refrescarUsuarios() {

        modeloTabla.setRowCount(0);

        List<Usuario> lista = usuarioDAO.listarTodos();

        int numero = 1;

        for (Usuario u : lista) {

            modeloTabla.addRow(new Object[]{
                numero,
                u.getIdUsuario(),
                u.getNombreCompleto(),
                u.getUsuario(),
                u.getRol(),
                u.getEstado()
            });

            numero++;
        }
    }

    private void mostrarFormularioNuevo() {

        JTextField txtNombre = new JTextField();
        JTextField txtUsuario = new JTextField();
        JPasswordField txtPassword = new JPasswordField();

        JComboBox<String> cboRol = new JComboBox<>(
                new String[]{"Administrador", "Cajero"}
        );

        JComboBox<String> cboEstado = new JComboBox<>(
                new String[]{"Activo", "Inactivo"}
        );

        Object[] campos = {
            "Nombre completo:", txtNombre,
            "Usuario:", txtUsuario,
            "Contraseña:", txtPassword,
            "Rol:", cboRol,
            "Estado:", cboEstado
        };

        int opcion = JOptionPane.showConfirmDialog(
                this,
                campos,
                "Nuevo Usuario",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (nombre.isEmpty() || usuario.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Complete todos los campos obligatorios."
            );

            return;
        }

        if (usuarioDAO.existeUsuario(usuario)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ese nombre de usuario ya existe."
            );

            return;
        }

        Usuario nuevo = new Usuario();

        nuevo.setNombreCompleto(nombre);
        nuevo.setUsuario(usuario);
        nuevo.setPassword(password);
        nuevo.setRol(cboRol.getSelectedItem().toString());
        nuevo.setEstado(cboEstado.getSelectedItem().toString());

        if (usuarioDAO.insertar(nuevo)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Usuario registrado correctamente."
            );

            refrescarUsuarios();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo registrar el usuario."
            );
        }
    }

    private void editarUsuarioSeleccionado() {

        int filaVista = tblUsuarios.getSelectedRow();

        if (filaVista < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un usuario."
            );
            return;
        }

        int fila = tblUsuarios.convertRowIndexToModel(filaVista);

        int idUsuario = Integer.parseInt(
                modeloTabla.getValueAt(fila, 1).toString()
        );

        String nombreActual =
                modeloTabla.getValueAt(fila, 2).toString();

        String usuarioActual =
                modeloTabla.getValueAt(fila, 3).toString();

        String rolActual =
                modeloTabla.getValueAt(fila, 4).toString();

        String estadoActual =
                modeloTabla.getValueAt(fila, 5).toString();

        JTextField txtNombre =
                new JTextField(nombreActual);

        JTextField txtUsuario =
                new JTextField(usuarioActual);

        JPasswordField txtPassword =
                new JPasswordField();

        JComboBox<String> cboRol =
                new JComboBox<>(
                        new String[]{"Administrador", "Cajero"}
                );

        cboRol.setSelectedItem(rolActual);

        JComboBox<String> cboEstado =
                new JComboBox<>(
                        new String[]{"Activo", "Inactivo"}
                );

        cboEstado.setSelectedItem(estadoActual);

        Object[] campos = {
            "Nombre completo:", txtNombre,
            "Usuario:", txtUsuario,
            "Nueva contraseña:", txtPassword,
            "Rol:", cboRol,
            "Estado:", cboEstado
        };

        int opcion = JOptionPane.showConfirmDialog(
                this,
                campos,
                "Editar Usuario",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        Usuario u = new Usuario();

        u.setIdUsuario(idUsuario);
        u.setNombreCompleto(
                txtNombre.getText().trim()
        );

        u.setUsuario(
                txtUsuario.getText().trim()
        );

        // temporalmente obliga a poner nueva contraseña
        u.setPassword(
                new String(
                        txtPassword.getPassword()
                ).trim()
        );

        u.setRol(
                cboRol.getSelectedItem().toString()
        );

        u.setEstado(
                cboEstado.getSelectedItem().toString()
        );

        if (u.getPassword().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ingrese la contraseña."
            );

            return;
        }

        if (usuarioDAO.actualizar(u)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Usuario actualizado correctamente."
            );

            refrescarUsuarios();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo actualizar el usuario."
            );
        }
    }

    private void cambiarEstadoSeleccionado() {

        int filaVista = tblUsuarios.getSelectedRow();

        if (filaVista < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un usuario."
            );
            return;
        }

        int fila =
                tblUsuarios.convertRowIndexToModel(
                        filaVista
                );

        int idUsuario =
                Integer.parseInt(
                        modeloTabla.getValueAt(
                                fila,
                                1
                        ).toString()
                );

        String estadoActual =
                modeloTabla.getValueAt(
                        fila,
                        5
                ).toString();

        String nuevoEstado =
                "Activo".equalsIgnoreCase(
                        estadoActual
                )
                ? "Inactivo"
                : "Activo";

        if (usuarioDAO.cambiarEstado(
                idUsuario,
                nuevoEstado
        )) {

            JOptionPane.showMessageDialog(
                    this,
                    "Estado actualizado a "
                            + nuevoEstado
            );

            refrescarUsuarios();
        }
    }
}