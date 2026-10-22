package vistas;

import dao.ProductoDAO;
import modelo.Producto;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PnlProductos extends JPanel {

    private Usuario usuario;
    private ProductoDAO productoDAO;

    // Componentes del Formulario
    private JTextField txtId;
    private JTextField txtNombre;
    private JComboBox<String> cboCategoria;
    private JTextField txtPrecioCosto;
    private JTextField txtPrecioVenta;
    private JTextField txtStock;
    private JTextField txtStockMinimo;
    private JComboBox<String> cboUnidadMedida;

    // Botones del CRUD
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnModificar;
    private JButton btnEliminar;

    // Tabla y Buscador
    private JTextField txtBuscar;
    private JTable tblProductos;
    private DefaultTableModel modTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblTotalRegistros;

    // Colores corporativos
    private final Color COLOR_VERDE = new Color(21, 128, 61);
    private final Color COLOR_VERDE_OSCURO = new Color(22, 101, 52);
    private final Color COLOR_BORDE = new Color(226, 232, 240);

    // Constructor sin parámetros (por si lo abres directamente)
    public PnlProductos() {
        this(null);
    }

    // Constructor principal con el usuario logueado
    public PnlProductos(Usuario usuario) {
        this.usuario = usuario;
        this.productoDAO = new ProductoDAO();

        setLayout(new BorderLayout(14, 0));
        setBackground(new Color(241, 245, 249));
        setBorder(new EmptyBorder(12, 14, 12, 14));

        // 1. PANEL IZQUIERDO: Formulario de Registro y Edición
        add(crearPanelFormulario(), BorderLayout.WEST);

        // 2. PANEL DERECHO: Tabla de Consulta con Buscador
        add(crearPanelTabla(), BorderLayout.CENTER);

        // Cargar datos iniciales desde PostgreSQL
        listarProductosEnTabla();
        limpiarFormulario();
    }

    // =========================================================================
    // 1. FORMULARIO CRUD (Izquierda)
    // =========================================================================
    private JPanel crearPanelFormulario() {
        JPanel pnl = new JPanel();
        pnl.setPreferredSize(new Dimension(360, 600));
        pnl.setBackground(Color.WHITE);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lblTit = new JLabel("DATOS DEL PRODUCTO");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTit.setForeground(new Color(15, 23, 42));
        lblTit.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.add(lblTit);
        pnl.add(Box.createRigidArea(new Dimension(0, 12)));

        // Campo ID (Autogenerado)
        pnl.add(crearLabel("ID Producto (Autogenerado):"));
        txtId = new JTextField();
        txtId.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtId.setEnabled(false);
        txtId.setMaximumSize(new Dimension(330, 30));
        pnl.add(txtId);
        pnl.add(Box.createRigidArea(new Dimension(0, 8)));

        // Campo Nombre
        pnl.add(crearLabel("Nombre Comercial / Descripción: *"));
        txtNombre = new JTextField();
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNombre.setMaximumSize(new Dimension(330, 32));
        pnl.add(txtNombre);
        pnl.add(Box.createRigidArea(new Dimension(0, 8)));

        // Campo Categoría (las 8 de tu PostgreSQL)
        pnl.add(crearLabel("Categoría: *"));
        String[] categorias = {
            "Abarrotes",
            "Bebidas",
            "Lácteos",
            "Golosinas",
            "Limpieza"
        };
        cboCategoria = new JComboBox<>(categorias);
        cboCategoria.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboCategoria.setMaximumSize(new Dimension(330, 32));
        pnl.add(cboCategoria);
        pnl.add(Box.createRigidArea(new Dimension(0, 8)));

        // Precios en 2 Columnas
        JPanel pnlPrecios = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlPrecios.setBackground(Color.WHITE);
        pnlPrecios.setMaximumSize(new Dimension(330, 52));

        JPanel pnlCosto = new JPanel(new BorderLayout(0, 2));
        pnlCosto.setBackground(Color.WHITE);
        pnlCosto.add(crearLabel("Precio Costo (S/.):"), BorderLayout.NORTH);
        txtPrecioCosto = new JTextField("0.00");
        txtPrecioCosto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCosto.add(txtPrecioCosto, BorderLayout.CENTER);

        JPanel pnlVenta = new JPanel(new BorderLayout(0, 2));
        pnlVenta.setBackground(Color.WHITE);
        pnlVenta.add(crearLabel("Precio Venta (S/.): *"), BorderLayout.NORTH);
        txtPrecioVenta = new JTextField();
        txtPrecioVenta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlVenta.add(txtPrecioVenta, BorderLayout.CENTER);

        pnlPrecios.add(pnlCosto);
        pnlPrecios.add(pnlVenta);
        pnl.add(pnlPrecios);
        pnl.add(Box.createRigidArea(new Dimension(0, 8)));

        // Stocks y Unidad en 2 Columnas
        JPanel pnlStocks = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlStocks.setBackground(Color.WHITE);
        pnlStocks.setMaximumSize(new Dimension(330, 52));

        JPanel pnlStk = new JPanel(new BorderLayout(0, 2));
        pnlStk.setBackground(Color.WHITE);
        pnlStk.add(crearLabel("Stock Inicial: *"), BorderLayout.NORTH);
        txtStock = new JTextField("0");
        txtStock.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlStk.add(txtStock, BorderLayout.CENTER);

        JPanel pnlStkMin = new JPanel(new BorderLayout(0, 2));
        pnlStkMin.setBackground(Color.WHITE);
        pnlStkMin.add(crearLabel("Stock Mínimo Alerta:"), BorderLayout.NORTH);
        txtStockMinimo = new JTextField("5");
        txtStockMinimo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlStkMin.add(txtStockMinimo, BorderLayout.CENTER);

        pnlStocks.add(pnlStk);
        pnlStocks.add(pnlStkMin);
        pnl.add(pnlStocks);
        pnl.add(Box.createRigidArea(new Dimension(0, 8)));

        // Unidad de Medida
        pnl.add(crearLabel("Unidad de Medida:"));
        String[] unidades = {
            "und",
            "kg",
            "lt"
        };
        cboUnidadMedida = new JComboBox<>(unidades);
        cboUnidadMedida.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboUnidadMedida.setMaximumSize(new Dimension(330, 32));
        pnl.add(cboUnidadMedida);

        pnl.add(Box.createRigidArea(new Dimension(0, 16)));

        // BOTONES DE ACCIÓN (CRUD)
        JPanel pnlBotonesGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        pnlBotonesGrid.setBackground(Color.WHITE);
        pnlBotonesGrid.setMaximumSize(new Dimension(330, 80));

        btnNuevo = crearBotonSolido("Limpiar / Nuevo", new Color(100, 116, 139), Color.WHITE);
        btnGuardar = crearBotonSolido("Registrar (Crear)", COLOR_VERDE_OSCURO, Color.WHITE);
        btnModificar = crearBotonSolido("Guardar Cambios", new Color(30, 41, 59), Color.WHITE);
        btnEliminar = crearBotonSolido("Eliminar Producto", new Color(220, 38, 38), Color.WHITE);

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> registrarProducto());
        btnModificar.addActionListener(e -> modificarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());

        pnlBotonesGrid.add(btnNuevo);
        pnlBotonesGrid.add(btnGuardar);
        pnlBotonesGrid.add(btnModificar);
        pnlBotonesGrid.add(btnEliminar);

        pnl.add(pnlBotonesGrid);
        pnl.add(Box.createVerticalGlue());

        return pnl;
    }

    // =========================================================================
    // 2. PANEL TABLA Y BUSCADOR (Derecha)
    // =========================================================================
    private JPanel crearPanelTabla() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        // Buscador
        JPanel pnlTop = new JPanel(new BorderLayout(10, 0));
        pnlTop.setBackground(Color.WHITE);

        JLabel lblBuscar = new JLabel("Buscar Producto: ");
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBuscar.setForeground(new Color(15, 23, 42));

        txtBuscar = new JTextField();
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscar.setPreferredSize(new Dimension(300, 34));
        txtBuscar.setBorder(new CompoundBorder(
                new LineBorder(COLOR_VERDE, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        // Filtro dinámico en tiempo real mientras escribes
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }

            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }

            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }

            private void filtrar() {
                String t = txtBuscar.getText().trim();
                if (t.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + t));
                }
            }
        });

        lblTotalRegistros = new JLabel("Cargando productos...");
        lblTotalRegistros.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalRegistros.setForeground(COLOR_VERDE_OSCURO);

        pnlTop.add(lblBuscar, BorderLayout.WEST);
        pnlTop.add(txtBuscar, BorderLayout.CENTER);
        pnlTop.add(lblTotalRegistros, BorderLayout.EAST);
        pnl.add(pnlTop, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {
            "N°",
            "ID_REAL",
            "Descripción del Producto",
            "Categoría",
            "P. Costo",
            "P. Venta",
            "Stock",
            "Alerta Min.",
            "Unidad"
        };
        modTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblProductos = new JTable(modTabla);

        tblProductos.removeColumn(
                tblProductos.getColumnModel().getColumn(1)
        );
        tblProductos.setRowHeight(32);
        tblProductos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblProductos.setFillsViewportHeight(true);
        tblProductos.setBackground(Color.WHITE);
        tblProductos.setGridColor(new Color(241, 245, 249));

        tblProductos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblProductos.getTableHeader().setBackground(new Color(248, 250, 252));
        tblProductos.getTableHeader().setPreferredSize(new Dimension(0, 32));

        sorter = new TableRowSorter<>(modTabla);
        tblProductos.setRowSorter(sorter);

        // Alineaciones
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(JLabel.RIGHT);

        tblProductos.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblProductos.getColumnModel().getColumn(0).setCellRenderer(center);
        tblProductos.getColumnModel().getColumn(1).setPreferredWidth(230);
        tblProductos.getColumnModel().getColumn(2).setPreferredWidth(95);
        tblProductos.getColumnModel().getColumn(2).setCellRenderer(center);
        tblProductos.getColumnModel().getColumn(3).setPreferredWidth(65);
        tblProductos.getColumnModel().getColumn(3).setCellRenderer(right);
        tblProductos.getColumnModel().getColumn(4).setPreferredWidth(70);
        tblProductos.getColumnModel().getColumn(4).setCellRenderer(right);
        tblProductos.getColumnModel().getColumn(5).setPreferredWidth(55);
        tblProductos.getColumnModel().getColumn(5).setCellRenderer(center);
        tblProductos.getColumnModel().getColumn(6).setPreferredWidth(65);
        tblProductos.getColumnModel().getColumn(6).setCellRenderer(center);
        tblProductos.getColumnModel().getColumn(7).setPreferredWidth(60);
        tblProductos.getColumnModel().getColumn(7).setCellRenderer(center);

        // Al hacer clic en una fila, cargar datos al formulario
        tblProductos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarFormularioDesdeFila();
            }
        });

        JScrollPane scroll = new JScrollPane(tblProductos);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new LineBorder(COLOR_BORDE, 1));
        pnl.add(scroll, BorderLayout.CENTER);

        JLabel lblTip = new JLabel("Haga clic en un producto para cargarlo en el formulario y modificarlo o eliminarlo.");
        lblTip.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblTip.setForeground(new Color(100, 116, 139));
        pnl.add(lblTip, BorderLayout.SOUTH);

        return pnl;
    }

    // =========================================================================
    // LÓGICA DE NEGOCIO DEL CRUD (Carga desde ProductoDAO)
    // =========================================================================
    public void listarProductosEnTabla() {

        modTabla.setRowCount(0);

        List<Producto> lista = productoDAO.listarTodos();

        int numero = 1;

        for (Producto p : lista) {

            modTabla.addRow(new Object[]{
                numero, // Número visible
                p.getIdProducto(), // ID real PostgreSQL
                p.getNombre(),
                p.getCategoriaNombre(),
                String.format("%.2f", p.getPrecioCosto()),
                String.format("%.2f", p.getPrecioVenta()),
                p.getStock(),
                p.getStockMinimo(),
                p.getUnidadMedida()

            });

            numero++;
        }

        lblTotalRegistros.setText(
                "Total: " + lista.size() + " productos  "
        );
    }

    private void cargarFormularioDesdeFila() {
        int filaVista = tblProductos.getSelectedRow();
        if (filaVista < 0) {
            return;
        }

        int fila = tblProductos.convertRowIndexToModel(filaVista);

        txtId.setText(
                modTabla.getValueAt(fila, 1).toString()
        );

        txtNombre.setText(
                modTabla.getValueAt(fila, 2).toString()
        );

        cboCategoria.setSelectedItem(
                modTabla.getValueAt(fila, 3).toString()
        );

        txtPrecioCosto.setText(
                modTabla.getValueAt(fila, 4)
                        .toString()
                        .replace(",", ".")
        );

        txtPrecioVenta.setText(
                modTabla.getValueAt(fila, 5)
                        .toString()
                        .replace(",", ".")
        );

        txtStock.setText(
                modTabla.getValueAt(fila, 6).toString()
        );

        txtStockMinimo.setText(
                modTabla.getValueAt(fila, 7).toString()
        );

        cboUnidadMedida.setSelectedItem(
                modTabla.getValueAt(fila, 8).toString()
        );

        // Activamos botones de edición
        btnGuardar.setEnabled(false);
        btnModificar.setEnabled(true);
        btnEliminar.setEnabled(true);
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        cboCategoria.setSelectedIndex(0);
        txtPrecioCosto.setText("0.00");
        txtPrecioVenta.setText("");
        txtStock.setText("0");
        txtStockMinimo.setText("5");
        cboUnidadMedida.setSelectedIndex(0);

        if (tblProductos != null) {
            tblProductos.clearSelection();
        }
        btnGuardar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
        txtNombre.requestFocus();
    }

    private void registrarProducto() {
        if (!validarCampos()) {
            return;
        }

        Producto p = new Producto();
        p.setNombre(txtNombre.getText().trim());

        p.setIdCategoria(obtenerIdCategoriaSeleccionada());
        p.setPrecioCosto(Double.parseDouble(txtPrecioCosto.getText().trim().replace(",", ".")));
        p.setPrecioVenta(Double.parseDouble(txtPrecioVenta.getText().trim().replace(",", ".")));
        p.setStock(Integer.parseInt(txtStock.getText().trim()));
        p.setStockMinimo(Integer.parseInt(txtStockMinimo.getText().trim()));
        p.setUnidadMedida(cboUnidadMedida.getSelectedItem().toString());

        boolean ok = productoDAO.insertar(p);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Producto registrado correctamente en PostgreSQL.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            listarProductosEnTabla();
            limpiarFormulario();
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar producto.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarProducto() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarCampos()) {
            return;
        }

        Producto p = new Producto();
        p.setIdProducto(Integer.parseInt(txtId.getText().trim()));
        p.setNombre(txtNombre.getText().trim());
        p.setIdCategoria(cboCategoria.getSelectedIndex() + 1);
        p.setPrecioCosto(Double.parseDouble(txtPrecioCosto.getText().trim().replace(",", ".")));
        p.setPrecioVenta(Double.parseDouble(txtPrecioVenta.getText().trim().replace(",", ".")));
        p.setStock(Integer.parseInt(txtStock.getText().trim()));
        p.setStockMinimo(Integer.parseInt(txtStockMinimo.getText().trim()));
        p.setUnidadMedida(cboUnidadMedida.getSelectedItem().toString());

        boolean ok = productoDAO.actualizar(p);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.", "Actualizado", JOptionPane.INFORMATION_MESSAGE);
            listarProductosEnTabla();
            limpiarFormulario();
        } else {
            JOptionPane.showMessageDialog(this, "Error al actualizar producto.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarProducto() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el producto: " + txtNombre.getText() + "?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (resp == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(txtId.getText().trim());
            boolean ok = productoDAO.eliminar(id);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Producto eliminado correctamente.", "Eliminado", JOptionPane.INFORMATION_MESSAGE);
                listarProductosEnTabla();
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar producto.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el nombre del producto.", "Campo Obligatorio", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return false;
        }
        try {
            double pv = Double.parseDouble(txtPrecioVenta.getText().trim().replace(",", "."));
            if (pv <= 0) {
                JOptionPane.showMessageDialog(this, "El precio de venta debe ser mayor a 0.", "Precio Inválido", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio de venta debe ser un número válido.", "Formato Inválido", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            int stk = Integer.parseInt(txtStock.getText().trim());
            if (stk < 0) {
                JOptionPane.showMessageDialog(this, "El stock no puede ser negativo.", "Stock Inválido", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El stock debe ser un número entero.", "Formato Inválido", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(15, 23, 42));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton crearBotonSolido(String texto, Color fondo, Color textoColor) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(textoColor);
        btn.setBackground(fondo);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(fondo.darker(), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private int obtenerIdCategoriaSeleccionada() {

        String categoria = cboCategoria.getSelectedItem().toString();

        switch (categoria) {
            case "Abarrotes":
                return 1;
            case "Bebidas":
                return 2;
            case "Lácteos":
                return 3;
            case "Golosinas":
                return 4;
            case "Limpieza":
                return 5;
            default:
                return 1;
        }
    }
}
