package vistas;

import dao.InventarioDAO;
import modelo.MovimientoInventario;
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
import java.text.SimpleDateFormat;
import java.util.List;

public class PnlInventario extends JPanel {

    private Usuario usuario;
    private InventarioDAO inventarioDAO;

    private JLabel lblStockTotal;
    private JLabel lblStockBajo;
    private JLabel lblValorizado;

    private JTable tblStock;
    private JTable tblMovimientos;

    private DefaultTableModel modStock;
    private DefaultTableModel modMovimientos;

    private JTextField txtBuscar;
    private JCheckBox chkSoloStockBajo;

    private TableRowSorter<DefaultTableModel> sorterStock;

    private final Color COLOR_VERDE = new Color(21, 128, 61);
    private final Color COLOR_VERDE_OSCURO = new Color(22, 101, 52);
    private final Color COLOR_FONDO = new Color(241, 245, 249);
    private final Color COLOR_BORDE = new Color(226, 232, 240);

    public PnlInventario(Usuario usuario) {

        this.usuario = usuario;
        this.inventarioDAO = new InventarioDAO();

        setLayout(new BorderLayout(0, 14));
        setBackground(COLOR_FONDO);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        crearInterfaz();
        refrescarDatos();
    }

    private void crearInterfaz() {

        // ===========================
        // CABECERA
        // ===========================

        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setBackground(Color.WHITE);
        pnlCabecera.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel pnlTitulos = new JPanel();
        pnlTitulos.setLayout(new BoxLayout(pnlTitulos, BoxLayout.Y_AXIS));
        pnlTitulos.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Gestión de Inventario y Almacén");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lblSubtitulo = new JLabel(
                "Supervisión de existencias, stock mínimo e historial de movimientos."
        );
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        pnlTitulos.add(lblTitulo);
        pnlTitulos.add(Box.createRigidArea(new Dimension(0, 4)));
        pnlTitulos.add(lblSubtitulo);

      JButton btnIngreso = crearBoton(
        "+ Aumentar Stock",
        COLOR_VERDE
);

btnIngreso.addActionListener(
        e -> ingresarMercaderiaSeleccionada()
);

        btnIngreso.addActionListener(e -> ingresarMercaderiaSeleccionada());

        pnlCabecera.add(pnlTitulos, BorderLayout.WEST);
        pnlCabecera.add(btnIngreso, BorderLayout.EAST);

        add(pnlCabecera, BorderLayout.NORTH);

        // ===========================
        // CENTRO GENERAL
        // ===========================

        JPanel pnlCentro = new JPanel(new BorderLayout(0, 14));
        pnlCentro.setBackground(COLOR_FONDO);

        // TARJETAS
        JPanel pnlTarjetas = new JPanel(new GridLayout(1, 3, 12, 0));
        pnlTarjetas.setBackground(COLOR_FONDO);

        lblStockTotal = new JLabel("0 unidades");
        lblStockBajo = new JLabel("0");
        lblValorizado = new JLabel("S/. 0.00");

        pnlTarjetas.add(
                crearTarjeta(
                        "STOCK TOTAL EN TIENDA",
                        lblStockTotal,
                        "Suma de existencias actuales"
                )
        );

        pnlTarjetas.add(
                crearTarjeta(
                        "PRODUCTOS STOCK BAJO",
                        lblStockBajo,
                        "Requieren reposición"
                )
        );

        pnlTarjetas.add(
                crearTarjeta(
                        "VALORIZADO ESTIMADO",
                        lblValorizado,
                        "Stock × precio de venta"
                )
        );

        pnlCentro.add(pnlTarjetas, BorderLayout.NORTH);

        // ===========================
        // PESTAÑAS
        // ===========================

        JTabbedPane tabs = new JTabbedPane();

        tabs.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        tabs.addTab(
                "Control de Stock Actual",
                crearPanelStock()
        );

        tabs.addTab(
                "Historial de Movimientos",
                crearPanelMovimientos()
        );

        pnlCentro.add(tabs, BorderLayout.CENTER);

        add(pnlCentro, BorderLayout.CENTER);
    }

    private JPanel crearPanelStock() {

        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(12, 12, 12, 12));

        // ===========================
        // BUSCADOR
        // ===========================

        JPanel pnlTop = new JPanel(new BorderLayout(10, 0));
        pnlTop.setBackground(Color.WHITE);

        chkSoloStockBajo = new JCheckBox("Solo stock bajo");
        chkSoloStockBajo.setBackground(Color.WHITE);

        txtBuscar = new JTextField();
        txtBuscar.setPreferredSize(new Dimension(280, 32));

        pnlTop.add(chkSoloStockBajo, BorderLayout.WEST);
        pnlTop.add(txtBuscar, BorderLayout.EAST);

        pnl.add(pnlTop, BorderLayout.NORTH);

        // ===========================
        // TABLA STOCK
        // ===========================

        String[] columnas = {
            "N°",
            "ID_REAL",
            "Producto",
            "Categoría",
            "Stock Actual",
            "Stock Mínimo",
            "Unidad",
            "Estado"
        };

        modStock = new DefaultTableModel(columnas, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblStock = new JTable(modStock);

        tblStock.setRowHeight(31);
        tblStock.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblStock.setFillsViewportHeight(true);
        tblStock.setBackground(Color.WHITE);

        sorterStock = new TableRowSorter<>(modStock);
        tblStock.setRowSorter(sorterStock);

        // Ocultamos ID_REAL
        tblStock.removeColumn(
                tblStock.getColumnModel().getColumn(1)
        );

        DefaultTableCellRenderer center =
                new DefaultTableCellRenderer();

        center.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < tblStock.getColumnCount(); i++) {
            if (i != 1) {
                tblStock.getColumnModel()
                        .getColumn(i)
                        .setCellRenderer(center);
            }
        }

        JScrollPane scroll = new JScrollPane(tblStock);

        pnl.add(scroll, BorderLayout.CENTER);

        // FILTROS
        txtBuscar.getDocument().addDocumentListener(
                new DocumentListener() {

                    public void insertUpdate(DocumentEvent e) {
                        filtrarStock();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        filtrarStock();
                    }

                    public void changedUpdate(DocumentEvent e) {
                        filtrarStock();
                    }
                }
        );

        chkSoloStockBajo.addActionListener(
                e -> filtrarStock()
        );

        return pnl;
    }

    private JPanel crearPanelMovimientos() {

        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] columnas = {
            "Fecha",
            "Producto",
            "Tipo",
            "Cantidad",
            "Stock Anterior",
            "Stock Posterior",
            "Usuario",
            "Motivo"
        };

        modMovimientos =
                new DefaultTableModel(columnas, 0) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        tblMovimientos =
                new JTable(modMovimientos);

        tblMovimientos.setRowHeight(30);
        tblMovimientos.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        JScrollPane scroll =
                new JScrollPane(tblMovimientos);

        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel crearTarjeta(
            String titulo,
            JLabel valor,
            String descripcion
    ) {

        JPanel pnl = new JPanel();

        pnl.setLayout(
                new BoxLayout(
                        pnl,
                        BoxLayout.Y_AXIS
                )
        );

        pnl.setBackground(Color.WHITE);

        pnl.setBorder(
                new CompoundBorder(
                        new LineBorder(
                                COLOR_BORDE,
                                1,
                                true
                        ),
                        new EmptyBorder(
                                14,
                                16,
                                14,
                                16
                        )
                )
        );

        JLabel lblTitulo = new JLabel(titulo);

        lblTitulo.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        valor.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        22
                )
        );

        valor.setForeground(COLOR_VERDE_OSCURO);

        JLabel lblDescripcion =
                new JLabel(descripcion);

        lblDescripcion.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        pnl.add(lblTitulo);
        pnl.add(Box.createRigidArea(new Dimension(0, 7)));
        pnl.add(valor);
        pnl.add(Box.createRigidArea(new Dimension(0, 4)));
        pnl.add(lblDescripcion);

        return pnl;
    }

    public void refrescarDatos() {

        cargarTarjetas();
        cargarTablaStock();
        cargarMovimientos();
    }

    private void cargarTarjetas() {

        int stockTotal =
                inventarioDAO.obtenerStockTotal();

        int stockBajo =
                inventarioDAO.contarStockBajo();

        double valorizado =
                inventarioDAO.obtenerValorInventario();

        lblStockTotal.setText(
                stockTotal + " unidades"
        );

        lblStockBajo.setText(
                String.valueOf(stockBajo)
        );

        lblValorizado.setText(
                String.format(
                        "S/. %.2f",
                        valorizado
                )
        );
    }

    private void cargarTablaStock() {

        modStock.setRowCount(0);

        List<Producto> lista =
                inventarioDAO.listarStock();

        int numero = 1;

        for (Producto p : lista) {

            String estado;

            if (p.getStock() == 0) {

                estado = "SIN STOCK";

            } else if (
                    p.getStock()
                    <= p.getStockMinimo()
            ) {

                estado = "STOCK BAJO";

            } else {

                estado = "NORMAL";
            }

            modStock.addRow(
                    new Object[]{
                        numero,
                        p.getIdProducto(),
                        p.getNombre(),
                        p.getCategoriaNombre(),
                        p.getStock(),
                        p.getStockMinimo(),
                        p.getUnidadMedida(),
                        estado
                    }
            );

            numero++;
        }
    }

    private void cargarMovimientos() {

        modMovimientos.setRowCount(0);

        List<MovimientoInventario> lista =
                inventarioDAO.listarMovimientos();

        SimpleDateFormat formato =
                new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm"
                );

        for (MovimientoInventario m : lista) {

            modMovimientos.addRow(
                    new Object[]{
                        formato.format(
                                m.getFechaHora()
                        ),
                        m.getProductoNombre(),
                        m.getTipo(),
                        m.getCantidad(),
                        m.getStockAnterior(),
                        m.getStockPosterior(),
                        m.getUsuarioNombre(),
                        m.getMotivo()
                    }
            );
        }
    }

    private void filtrarStock() {

        String texto =
                txtBuscar.getText().trim();

        boolean soloBajo =
                chkSoloStockBajo.isSelected();

        RowFilter<DefaultTableModel, Object> filtro;

        if (texto.isEmpty() && !soloBajo) {

            sorterStock.setRowFilter(null);
            return;
        }

        if (!texto.isEmpty() && !soloBajo) {

            filtro =
                    RowFilter.regexFilter(
                            "(?i)" + texto
                    );

        } else if (
                texto.isEmpty()
                && soloBajo
        ) {

            filtro =
                    RowFilter.regexFilter(
                            "STOCK BAJO|SIN STOCK",
                            7
                    );

        } else {

            RowFilter<DefaultTableModel, Object> filtroTexto =
                    RowFilter.regexFilter(
                            "(?i)" + texto
                    );

            RowFilter<DefaultTableModel, Object> filtroBajo =
                    RowFilter.regexFilter(
                            "STOCK BAJO|SIN STOCK",
                            7
                    );

            filtro =
                    RowFilter.andFilter(
                            java.util.Arrays.asList(
                                    filtroTexto,
                                    filtroBajo
                            )
                    );
        }

        sorterStock.setRowFilter(filtro);
    }

    private void ingresarMercaderiaSeleccionada() {

        int filaVista =
                tblStock.getSelectedRow();

        if (filaVista < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un producto de la tabla.",
                    "Inventario",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int filaModelo =
                tblStock.convertRowIndexToModel(
                        filaVista
                );

        int idProducto =
                Integer.parseInt(
                        modStock.getValueAt(
                                filaModelo,
                                1
                        ).toString()
                );

        String producto =
                modStock.getValueAt(
                        filaModelo,
                        2
                ).toString();

        String cantidadTexto =
                JOptionPane.showInputDialog(
                        this,
                        "Producto: "
                                + producto
                                + "\n\nCantidad a ingresar:",
                        "Ingreso de Mercadería",
                        JOptionPane.QUESTION_MESSAGE
                );

        if (cantidadTexto == null) {
            return;
        }

        try {

            int cantidad =
                    Integer.parseInt(
                            cantidadTexto.trim()
                    );

            if (cantidad <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "La cantidad debe ser mayor a 0."
                );

                return;
            }

            String motivo =
                    JOptionPane.showInputDialog(
                            this,
                            "Motivo del ingreso:",
                            "Reposición de mercadería"
                    );

            if (motivo == null ||
                    motivo.trim().isEmpty()) {

                motivo =
                        "Reposición de mercadería";
            }

            boolean ok =
                    inventarioDAO.ingresarStock(
                            idProducto,
                            usuario.getIdUsuario(),
                            cantidad,
                            motivo
                    );

            if (ok) {

                JOptionPane.showMessageDialog(
                        this,
                        "Stock actualizado correctamente."
                );

                refrescarDatos();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo actualizar el inventario."
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ingrese una cantidad válida."
            );
        }
    }

private JButton crearBoton(String texto, Color fondo) {

    JButton btn = new JButton(texto);

    btn.setFont(
            new Font("Segoe UI", Font.BOLD, 13)
    );

    btn.setForeground(Color.WHITE);
    btn.setBackground(fondo);

    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setContentAreaFilled(true);
    btn.setOpaque(true);

    btn.setBorder(
            new EmptyBorder(12, 20, 12, 20)
    );

    btn.setPreferredSize(
            new Dimension(175, 44)
    );

    btn.setMinimumSize(
            new Dimension(175, 44)
    );

    btn.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
    );

    btn.setToolTipText(
            "Selecciona un producto para aumentar sus existencias"
    );

    // Efecto visual al pasar el cursor
    btn.addMouseListener(
            new java.awt.event.MouseAdapter() {

        @Override
        public void mouseEntered(
                java.awt.event.MouseEvent e
        ) {
            btn.setBackground(COLOR_VERDE_OSCURO);
        }

        @Override
        public void mouseExited(
                java.awt.event.MouseEvent e
        ) {
            btn.setBackground(fondo);
        }
    });

    return btn;
}

}