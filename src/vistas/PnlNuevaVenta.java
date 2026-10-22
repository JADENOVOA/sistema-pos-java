package vistas;

import dao.ProductoDAO;
import dao.VentaDAO;
import modelo.DetalleVenta;
import modelo.Producto;
import modelo.Usuario;
import modelo.Venta;
import dao.CajaDAO;
import modelo.Caja;

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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PnlNuevaVenta extends JPanel {

    private Usuario usuario;
    private ProductoDAO productoDAO;
    private VentaDAO ventaDAO;
    private CajaDAO cajaDAO;

    // CATÁLOGO IZQUIERDO
    private JTextField txtBuscar;
    private JTable tblCatalogo;
    private DefaultTableModel modCatalogo;
    private TableRowSorter<DefaultTableModel> sorterCatalogo;
    private List<Producto> listaProductosMemoria;

    // CARRITO Y COBRO DERECHO
    private JTextField txtCliente;
    private JTable tblCarrito;
    private DefaultTableModel modCarrito;
    private JLabel lblItemsCount;
    private JLabel lblSubtotal;
    private JLabel lblIgv;
    private JLabel lblTotal;
    private JButton btnEfectivo;
    private JButton btnBilletera;
    private String metodoPagoSeleccionado = "Efectivo";
    private JTextField txtMontoRecibido;
    private JLabel lblVuelto;
    private JButton btnRegistrarVenta;

    // Totales calculados
    private double subtotalCalculado = 0.0;
    private double igvCalculado = 0.0;
    private double totalCalculado = 0.0;

    // Paleta de Colores Corporativos
    private final Color COLOR_VERDE = new Color(21, 128, 61);       // #15803d
    private final Color COLOR_VERDE_OSCURO = new Color(22, 101, 52); // #166534
    private final Color COLOR_SLATE_BG = new Color(241, 245, 249);
    private final Color COLOR_BORDE = new Color(226, 232, 240);

    public PnlNuevaVenta(Usuario usuario) {
        this.usuario = usuario;
        this.productoDAO = new ProductoDAO();
        this.ventaDAO = new VentaDAO();
        this.cajaDAO = new CajaDAO();

        setLayout(new BorderLayout(14, 0));
        setBackground(COLOR_SLATE_BG);
        setBorder(new EmptyBorder(12, 14, 12, 14));

        // 1. COLUMNA IZQUIERDA: Catálogo de Productos con Buscador y Filtros
        add(crearPanelCatalogoIzquierdo(), BorderLayout.CENTER);

        // 2. COLUMNA DERECHA: Carrito en Curso y Caja de Cobro
        add(crearPanelCarritoDerecho(), BorderLayout.EAST);

        // Cargar productos de PostgreSQL a la tabla
        cargarProductosEnTabla();
    }

    // =========================================================================
    // 1. PANEL IZQUIERDO: CATÁLOGO DE PRODUCTOS
    // =========================================================================
    private JPanel crearPanelCatalogoIzquierdo() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Barra Superior: Buscador en tiempo real
        JPanel pnlTop = new JPanel(new BorderLayout(10, 8));
        pnlTop.setBackground(Color.WHITE);

        txtBuscar = new JTextField();
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscar.setPreferredSize(new Dimension(400, 38));
        txtBuscar.setBorder(new CompoundBorder(
                new LineBorder(COLOR_VERDE, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel lblBuscarIcon = new JLabel("  Buscar por nombre o ID: ");
        lblBuscarIcon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBuscarIcon.setForeground(new Color(30, 41, 59));

        JPanel pnlSearchBox = new JPanel(new BorderLayout());
        pnlSearchBox.setBackground(Color.WHITE);
        pnlSearchBox.add(lblBuscarIcon, BorderLayout.WEST);
        pnlSearchBox.add(txtBuscar, BorderLayout.CENTER);

        // Barra de botones de categorías rápidas
        JPanel pnlCategorias = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlCategorias.setBackground(Color.WHITE);

        String[] categorias = {"Todos", "Abarrotes", "Bebidas", "Lácteos", "Golosinas", "Limpieza"};
        for (String cat : categorias) {
            JButton btnCat = new JButton(cat);
            btnCat.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnCat.setFocusPainted(false);
            btnCat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCat.setBackground(new Color(241, 245, 249));
            btnCat.setForeground(new Color(51, 65, 85));
            btnCat.setBorder(new CompoundBorder(
                    new LineBorder(new Color(203, 213, 225), 1, true),
                    new EmptyBorder(5, 12, 5, 12)
            ));

            btnCat.addActionListener(e -> {
                if ("Todos".equals(cat)) {
                    sorterCatalogo.setRowFilter(null);
                } else {
                    sorterCatalogo.setRowFilter(RowFilter.regexFilter("(?i)" + cat, 2));
                }
            });
            pnlCategorias.add(btnCat);
        }

        pnlTop.add(pnlSearchBox, BorderLayout.NORTH);
        pnlTop.add(pnlCategorias, BorderLayout.SOUTH);
        pnl.add(pnlTop, BorderLayout.NORTH);

        // Tabla de Productos Catálogo
        String[] columnas = {"ID", "Descripción del Producto", "Categoría", "Stock", "Precio Unit.", "Acción"};
        modCatalogo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblCatalogo = new JTable(modCatalogo);
        tblCatalogo.setRowHeight(38);
        tblCatalogo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblCatalogo.setFillsViewportHeight(true);
        tblCatalogo.setBackground(Color.WHITE);
        tblCatalogo.setGridColor(new Color(241, 245, 249));
        tblCatalogo.setCursor(new Cursor(Cursor.HAND_CURSOR));

        tblCatalogo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblCatalogo.getTableHeader().setBackground(new Color(248, 250, 252));
        tblCatalogo.getTableHeader().setPreferredSize(new Dimension(0, 32));

        sorterCatalogo = new TableRowSorter<>(modCatalogo);
        tblCatalogo.setRowSorter(sorterCatalogo);

        // Buscador reactivo
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
                String text = txtBuscar.getText().trim();
                if (text.isEmpty()) {
                    sorterCatalogo.setRowFilter(null);
                } else {
                    sorterCatalogo.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        // Alineaciones
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(JLabel.RIGHT);

        tblCatalogo.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblCatalogo.getColumnModel().getColumn(0).setCellRenderer(center);
        tblCatalogo.getColumnModel().getColumn(1).setPreferredWidth(260);
        tblCatalogo.getColumnModel().getColumn(2).setPreferredWidth(90);
        tblCatalogo.getColumnModel().getColumn(2).setCellRenderer(center);
        tblCatalogo.getColumnModel().getColumn(3).setPreferredWidth(70);
        tblCatalogo.getColumnModel().getColumn(3).setCellRenderer(center);
        tblCatalogo.getColumnModel().getColumn(4).setPreferredWidth(85);
        tblCatalogo.getColumnModel().getColumn(4).setCellRenderer(right);
        tblCatalogo.getColumnModel().getColumn(5).setPreferredWidth(95);

        // RENDERER PERSONALIZADO: Dibuja "+ Agregar" como BOTÓN VERDE SÓLIDO
        tblCatalogo.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel("+ AGREGAR", JLabel.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(COLOR_VERDE);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(4, 8, 4, 8),
                        BorderFactory.createLineBorder(COLOR_VERDE_OSCURO, 1, true)
                ));
                return lbl;
            }
        });

        // UN SOLO CLIC EN LA FILA O EN "+ AGREGAR" AGREGA AL CARRITO INMEDIATAMENTE
        tblCatalogo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // <-- UN SOLO CLIC
                    agregarProductoSeleccionado();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblCatalogo);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new LineBorder(COLOR_BORDE, 1));
        pnl.add(scroll, BorderLayout.CENTER);

        // Barra inferior informativa
        JPanel pnlBotonesCatalogo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlBotonesCatalogo.setBackground(Color.WHITE);
        JLabel lblAyuda = new JLabel("Consejo: Haga 1 solo clic sobre cualquier producto o en '+ AGREGAR' para añadirlo al carrito.");
        lblAyuda.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblAyuda.setForeground(new Color(71, 85, 105));
        pnlBotonesCatalogo.add(lblAyuda);
        pnl.add(pnlBotonesCatalogo, BorderLayout.SOUTH);

        return pnl;
    }

    // =========================================================================
    // 2. PANEL DERECHO: CARRITO EN CURSO Y COBRO
    // =========================================================================
    private JPanel crearPanelCarritoDerecho() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setPreferredSize(new Dimension(390, 600));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        // Cabecera del Carrito
        JPanel pnlHeaderCarrito = new JPanel(new BorderLayout());
        pnlHeaderCarrito.setBackground(Color.WHITE);

        JLabel lblTit = new JLabel("Venta en Curso");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTit.setForeground(new Color(15, 23, 42));

        lblItemsCount = new JLabel("0 items");
        lblItemsCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblItemsCount.setForeground(COLOR_VERDE);

        JPanel pnlTitYCont = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlTitYCont.setBackground(Color.WHITE);
        pnlTitYCont.add(lblTit);
        pnlTitYCont.add(lblItemsCount);

        JButton btnVaciar = new JButton("Vaciar");
        btnVaciar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnVaciar.setForeground(new Color(220, 38, 38));
        btnVaciar.setBackground(Color.WHITE);
        btnVaciar.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        btnVaciar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVaciar.addActionListener(e -> limpiarCarrito());

        pnlHeaderCarrito.add(pnlTitYCont, BorderLayout.WEST);
        pnlHeaderCarrito.add(btnVaciar, BorderLayout.EAST);

        // Campo Cliente
        JPanel pnlCliente = new JPanel(new BorderLayout(8, 0));
        pnlCliente.setBackground(Color.WHITE);
        pnlCliente.setBorder(new EmptyBorder(8, 0, 8, 0));
        JLabel lblCli = new JLabel("Cliente:");
        lblCli.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtCliente = new JTextField("Cliente Mostrador");
        txtCliente.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCliente.add(lblCli, BorderLayout.WEST);
        pnlCliente.add(txtCliente, BorderLayout.CENTER);

        JPanel pnlTopCarrito = new JPanel(new BorderLayout());
        pnlTopCarrito.setBackground(Color.WHITE);
        pnlTopCarrito.add(pnlHeaderCarrito, BorderLayout.NORTH);
        pnlTopCarrito.add(pnlCliente, BorderLayout.SOUTH);

        pnl.add(pnlTopCarrito, BorderLayout.NORTH);

        // Tabla del Carrito
        String[] colCarrito = {"Producto", "Cant.", "P.Unit", "Subtotal"};
        modCarrito = new DefaultTableModel(colCarrito, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblCarrito = new JTable(modCarrito);
        tblCarrito.setRowHeight(30);
        tblCarrito.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblCarrito.setFillsViewportHeight(true);
        tblCarrito.setBackground(Color.WHITE);
        tblCarrito.setGridColor(new Color(241, 245, 249));

        tblCarrito.getColumnModel().getColumn(0).setPreferredWidth(160);
        tblCarrito.getColumnModel().getColumn(1).setPreferredWidth(45);
        tblCarrito.getColumnModel().getColumn(2).setPreferredWidth(65);
        tblCarrito.getColumnModel().getColumn(3).setPreferredWidth(70);

        // Doble clic en el carrito para quitar un producto
        tblCarrito.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int f = tblCarrito.getSelectedRow();
                    if (f >= 0) {
                        modCarrito.removeRow(f);
                        actualizarTotales();
                    }
                }
            }
        });

        JScrollPane scrollCarrito = new JScrollPane(tblCarrito);
        scrollCarrito.getViewport().setBackground(Color.WHITE);
        scrollCarrito.setBorder(new LineBorder(COLOR_BORDE, 1));
        pnl.add(scrollCarrito, BorderLayout.CENTER);

        // ZONA INFERIOR DE COBRO
        JPanel pnlInferior = new JPanel();
        pnlInferior.setLayout(new BoxLayout(pnlInferior, BoxLayout.Y_AXIS));
        pnlInferior.setBackground(Color.WHITE);
        pnlInferior.setBorder(new EmptyBorder(10, 0, 0, 0));

        lblSubtotal = new JLabel("Op. Gravada / Subtotal: S/. 0.00");
        lblSubtotal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtotal.setForeground(new Color(71, 85, 105));

        lblIgv = new JLabel("I.G.V. (18%): S/. 0.00");
        lblIgv.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblIgv.setForeground(new Color(71, 85, 105));

        lblTotal = new JLabel("TOTAL A COBRAR: S/. 0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(COLOR_VERDE_OSCURO);

        pnlInferior.add(lblSubtotal);
        pnlInferior.add(Box.createRigidArea(new Dimension(0, 3)));
        pnlInferior.add(lblIgv);
        pnlInferior.add(Box.createRigidArea(new Dimension(0, 5)));
        pnlInferior.add(lblTotal);
        pnlInferior.add(Box.createRigidArea(new Dimension(0, 10)));

        // SELECTOR DE MÉTODO DE PAGO CORREGIDO (NUNCA MÁS TEXTO BLANCO SOBRE BLANCO)
        JLabel lblMetodo = new JLabel("MÉTODO DE PAGO:");
        lblMetodo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pnlInferior.add(lblMetodo);
        pnlInferior.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel pnlMetodos = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlMetodos.setBackground(Color.WHITE);
        pnlMetodos.setMaximumSize(new Dimension(360, 38));

        btnEfectivo = crearBotonCustomToggle("Efectivo", true);
        btnBilletera = crearBotonCustomToggle("Billetera Digital", false);

        btnEfectivo.addActionListener(e -> seleccionarMetodoPago("Efectivo"));
        btnBilletera.addActionListener(e -> seleccionarMetodoPago("Billetera Digital"));

        pnlMetodos.add(btnEfectivo);
        pnlMetodos.add(btnBilletera);
        pnlInferior.add(pnlMetodos);
        pnlInferior.add(Box.createRigidArea(new Dimension(0, 10)));

        // Monto Recibido y Vuelto
        JPanel pnlPagoCalc = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlPagoCalc.setBackground(Color.WHITE);
        pnlPagoCalc.setMaximumSize(new Dimension(360, 48));

        JPanel pnlRec = new JPanel(new BorderLayout(0, 2));
        pnlRec.setBackground(Color.WHITE);
        pnlRec.add(new JLabel("Monto Recibido (S/.)"), BorderLayout.NORTH);
        txtMontoRecibido = new JTextField();
        txtMontoRecibido.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtMontoRecibido.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                calcularVuelto();
            }

            public void removeUpdate(DocumentEvent e) {
                calcularVuelto();
            }

            public void changedUpdate(DocumentEvent e) {
                calcularVuelto();
            }
        });
        pnlRec.add(txtMontoRecibido, BorderLayout.CENTER);

        JPanel pnlVue = new JPanel(new BorderLayout(0, 2));
        pnlVue.setBackground(Color.WHITE);
        pnlVue.add(new JLabel("Vuelto / Cambio"), BorderLayout.NORTH);
        lblVuelto = new JLabel("S/. 0.00", JLabel.RIGHT);
        lblVuelto.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblVuelto.setForeground(COLOR_VERDE_OSCURO);
        pnlVue.add(lblVuelto, BorderLayout.CENTER);

        pnlPagoCalc.add(pnlRec);
        pnlPagoCalc.add(pnlVue);
        pnlInferior.add(pnlPagoCalc);
        pnlInferior.add(Box.createRigidArea(new Dimension(0, 10)));

        // Botón Registrar Venta
        btnRegistrarVenta = crearBotonSolido("REGISTRAR VENTA (F12)", COLOR_VERDE_OSCURO, Color.WHITE);
        btnRegistrarVenta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegistrarVenta.setMaximumSize(new Dimension(360, 44));
        btnRegistrarVenta.setPreferredSize(new Dimension(360, 44));
        btnRegistrarVenta.addActionListener(e -> procesarCobroVenta());
        pnlInferior.add(btnRegistrarVenta);

        pnl.add(pnlInferior, BorderLayout.SOUTH);

        return pnl;
    }

    // =========================================================================
    // LÓGICA DE NEGOCIO
    // =========================================================================
    private void cargarProductosEnTabla() {
        modCatalogo.setRowCount(0);
        listaProductosMemoria = productoDAO.listarTodos();

        for (Producto p : listaProductosMemoria) {
            modCatalogo.addRow(new Object[]{
                p.getIdProducto(),
                p.getNombre(),
                p.getCategoriaNombre(),
                p.getStock() + " " + p.getUnidadMedida(),
                String.format("%.2f", p.getPrecioVenta()),
                "+ AGREGAR"
            });
        }
    }

    private void agregarProductoSeleccionado() {
        int filaVista = tblCatalogo.getSelectedRow();
        if (filaVista < 0) {
            return;
        }

        int filaModelo = tblCatalogo.convertRowIndexToModel(filaVista);
        int idProducto = Integer.parseInt(modCatalogo.getValueAt(filaModelo, 0).toString());
        String nombre = modCatalogo.getValueAt(filaModelo, 1).toString();
        double precio = Double.parseDouble(modCatalogo.getValueAt(filaModelo, 4).toString().replace(",", "."));

        boolean existe = false;
        for (int i = 0; i < modCarrito.getRowCount(); i++) {
            String nomEnCarrito = modCarrito.getValueAt(i, 0).toString();
            if (nomEnCarrito.equals(nombre)) {
                int cantActual = Integer.parseInt(modCarrito.getValueAt(i, 1).toString());
                int nuevaCant = cantActual + 1;
                modCarrito.setValueAt(nuevaCant, i, 1);
                modCarrito.setValueAt(String.format("%.2f", nuevaCant * precio), i, 3);
                existe = true;
                break;
            }
        }

        if (!existe) {
            modCarrito.addRow(new Object[]{
                nombre,
                1,
                String.format("%.2f", precio),
                String.format("%.2f", precio)
            });
        }

        actualizarTotales();
    }

    private void actualizarTotales() {
        double total = 0.0;
        int totalItems = 0;

        for (int i = 0; i < modCarrito.getRowCount(); i++) {
            double sub = Double.parseDouble(modCarrito.getValueAt(i, 3).toString().replace(",", "."));
            int cant = Integer.parseInt(modCarrito.getValueAt(i, 1).toString());
            total += sub;
            totalItems += cant;
        }

        totalCalculado = total;
        subtotalCalculado = totalCalculado / 1.18;
        igvCalculado = totalCalculado - subtotalCalculado;

        lblItemsCount.setText(totalItems + " items");
        lblSubtotal.setText(String.format("Op. Gravada / Subtotal: S/. %.2f", subtotalCalculado));
        lblIgv.setText(String.format("I.G.V. (18%%): S/. %.2f", igvCalculado));
        lblTotal.setText(String.format("TOTAL A COBRAR: S/. %.2f", totalCalculado));

        if ("Billetera Digital".equals(metodoPagoSeleccionado)) {
            txtMontoRecibido.setText(String.format("%.2f", totalCalculado));
        }
        calcularVuelto();
    }

    // BOTÓN PERSONALIZADO QUE NUNCA SE PINTA BLANCO POR WINDOWS
    private JButton crearBotonCustomToggle(String texto, boolean activo) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (activo) {
            btn.setBackground(COLOR_VERDE);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new LineBorder(COLOR_VERDE_OSCURO, 1, true));
        } else {
            btn.setBackground(new Color(241, 245, 249));
            btn.setForeground(new Color(51, 65, 85));
            btn.setBorder(new LineBorder(new Color(203, 213, 225), 1, true));
        }
        return btn;
    }

    private void seleccionarMetodoPago(String metodo) {
        this.metodoPagoSeleccionado = metodo;
        if ("Efectivo".equals(metodo)) {
            btnEfectivo.setBackground(COLOR_VERDE);
            btnEfectivo.setForeground(Color.WHITE);
            btnBilletera.setBackground(new Color(241, 245, 249));
            btnBilletera.setForeground(new Color(51, 65, 85));
            txtMontoRecibido.setEnabled(true);
            txtMontoRecibido.setText("");
            lblVuelto.setText("S/. 0.00");
        } else {
            btnBilletera.setBackground(COLOR_VERDE);
            btnBilletera.setForeground(Color.WHITE);
            btnEfectivo.setBackground(new Color(241, 245, 249));
            btnEfectivo.setForeground(new Color(51, 65, 85));
            txtMontoRecibido.setText(String.format("%.2f", totalCalculado));
            txtMontoRecibido.setEnabled(false);
            lblVuelto.setText("S/. 0.00");
        }
        btnEfectivo.repaint();
        btnBilletera.repaint();
    }

    private void calcularVuelto() {
        if ("Billetera Digital".equals(metodoPagoSeleccionado)) {
            return;
        }

        try {
            String texto = txtMontoRecibido.getText().trim().replace(",", ".");
            if (texto.isEmpty()) {
                lblVuelto.setText("S/. 0.00");
                return;
            }
            double recibido = Double.parseDouble(texto);
            double vuelto = recibido - totalCalculado;

            if (vuelto >= 0) {
                lblVuelto.setText(String.format("S/. %.2f", vuelto));
                lblVuelto.setForeground(COLOR_VERDE_OSCURO);
            } else {
                lblVuelto.setText(String.format("- S/. %.2f", Math.abs(vuelto)));
                lblVuelto.setForeground(new Color(220, 38, 38));
            }
        } catch (NumberFormatException e) {
            lblVuelto.setText("Inválido");
        }
    }

    private void limpiarCarrito() {
        modCarrito.setRowCount(0);
        actualizarTotales();
        txtCliente.setText("Cliente Mostrador");
        txtMontoRecibido.setText("");
        lblVuelto.setText("S/. 0.00");
    }

    private void procesarCobroVenta() {
        if (modCarrito.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito de compras está vacío.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double recibido = 0.0;
        double vuelto = 0.0;

        try {
            recibido = Double.parseDouble(txtMontoRecibido.getText().trim().replace(",", "."));
            if (recibido < totalCalculado) {
                JOptionPane.showMessageDialog(this, "El monto recibido es menor al total a pagar.", "Pago Insuficiente", JOptionPane.WARNING_MESSAGE);
                return;
            }
            vuelto = recibido - totalCalculado;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese el monto recibido en efectivo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cliente = txtCliente.getText().trim();
        if (cliente.isEmpty()) {
            cliente = "Cliente Mostrador";
        }

        Venta v = new Venta();
        String ticket = "TKT-" + (System.currentTimeMillis() % 1000000);
        v.setNumeroTicket(ticket);
        Caja cajaActual = cajaDAO.buscarCajaAbierta(usuario.getIdUsuario());

        if (cajaActual == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No hay una caja abierta.\nAbra una caja antes de registrar ventas.",
                    "Caja cerrada",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        v.setIdCaja(cajaActual.getIdCaja());
        v.setIdUsuario(usuario.getIdUsuario());
        v.setCliente(cliente);
        v.setSubtotal(subtotalCalculado);
        v.setIgv(igvCalculado);
        v.setTotal(totalCalculado);
        v.setMetodoPago(metodoPagoSeleccionado);
        v.setMontoRecibido(recibido);
        v.setVuelto(vuelto);

        List<DetalleVenta> lista = new ArrayList<>();
        for (int i = 0; i < modCarrito.getRowCount(); i++) {
            DetalleVenta d = new DetalleVenta();
            String nombreProd = modCarrito.getValueAt(i, 0).toString();

            for (Producto p : listaProductosMemoria) {
                if (p.getNombre().equals(nombreProd)) {
                    d.setIdProducto(p.getIdProducto());
                    break;
                }
            }
            d.setProductoNombre(nombreProd);
            d.setCantidad(Integer.parseInt(modCarrito.getValueAt(i, 1).toString()));
            d.setPrecioUnitario(Double.parseDouble(modCarrito.getValueAt(i, 2).toString().replace(",", ".")));
            d.setSubtotal(Double.parseDouble(modCarrito.getValueAt(i, 3).toString().replace(",", ".")));
            lista.add(d);
        }
        v.setDetalles(lista);

        System.out.println("ID CAJA: " + v.getIdCaja());
        System.out.println("ID USUARIO: " + v.getIdUsuario());
        System.out.println("CLIENTE: " + v.getCliente());
        System.out.println("METODO PAGO: " + v.getMetodoPago());
        System.out.println("TOTAL: " + v.getTotal());

        boolean exito = ventaDAO.registrarVenta(v);

        if (exito) {
            mostrarTicket(v);
            limpiarCarrito();
            cargarProductosEnTabla();
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar la venta en PostgreSQL.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarTicket(Venta v) {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("         BODEGA LA PERUANITA             \n");
        sb.append("      RUC: 10458796321 - LIMA PERÚ       \n");
        sb.append("=========================================\n");
        sb.append("Ticket:   ").append(v.getNumeroTicket()).append("\n");
        sb.append("Fecha:    ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())).append("\n");
        sb.append("Cajero:   ").append(usuario.getNombreCompleto()).append("\n");
        sb.append("Cliente:  ").append(v.getCliente()).append("\n");
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-18s %4s %8s\n", "PRODUCTO", "CANT", "SUBTOTAL"));
        sb.append("-----------------------------------------\n");

        for (DetalleVenta d : v.getDetalles()) {
            String nom = d.getProductoNombre();
            if (nom.length() > 18) {
                nom = nom.substring(0, 16) + "..";
            }
            sb.append(String.format("%-18s %4d  S/.%6.2f\n", nom, d.getCantidad(), d.getSubtotal()));
        }

        sb.append("-----------------------------------------\n");
        sb.append(String.format("TOTAL A PAGAR:        S/. %8.2f\n", v.getTotal()));
        sb.append(String.format("Método de Pago:       %s\n", v.getMetodoPago()));
        sb.append(String.format("Monto Recibido:       S/. %8.2f\n", v.getMontoRecibido()));
        sb.append(String.format("VUELTO:               S/. %8.2f\n", v.getVuelto()));
        sb.append("=========================================\n");
        sb.append("       ¡GRACIAS POR SU COMPRA!           \n");
        sb.append("=========================================\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(340, 440));

        JOptionPane.showMessageDialog(this, scroll, "Ticket de Venta Emitido", JOptionPane.INFORMATION_MESSAGE);
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(textoColor);
        btn.setBackground(fondo);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(fondo.darker(), 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
