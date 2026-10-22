
package vistas;

import dao.ReporteDAO;
import util.ExportarExcel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class PnlReportes extends JPanel {

    private ReporteDAO reporteDAO;

    private JLabel lblIngresos;
    private JLabel lblCantidadVentas;
    private JLabel lblTicketPromedio;

    private JTable tblProductos;
    private JTable tblMetodos;

    private DefaultTableModel modProductos;
    private DefaultTableModel modMetodos;

    private JButton btnHoy;
    private JButton btnSemana;
    private JButton btnMes;
    private JButton btnExportarExcel;

    private String periodoActual = "HOY";

    private final Color VERDE = new Color(21, 128, 61);
    private final Color VERDE_OSCURO = new Color(22, 101, 52);
    private final Color VERDE_CLARO = new Color(220, 252, 231);
    private final Color FONDO = new Color(241, 245, 249);
    private final Color BORDE = new Color(226, 232, 240);
    private final Color TEXTO = new Color(51, 65, 85);

    public PnlReportes() {

        reporteDAO = new ReporteDAO();

        setLayout(new BorderLayout(0, 14));
        setBackground(FONDO);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        crearInterfaz();
        refrescarDatos();
    }

    private void crearInterfaz() {


        JPanel pnlCabecera = new JPanel(new BorderLayout(20, 10));
        pnlCabecera.setBackground(Color.WHITE);

        pnlCabecera.setBorder(new CompoundBorder(
                new LineBorder(BORDE, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel pnlTitulos = new JPanel();
        pnlTitulos.setLayout(
                new BoxLayout(pnlTitulos, BoxLayout.Y_AXIS)
        );
        pnlTitulos.setBackground(Color.WHITE);

        JLabel lblTitulo =
                new JLabel("Reportes de Ventas e Ingresos");

        lblTitulo.setFont(
                new Font("Segoe UI", Font.BOLD, 22)
        );
        lblTitulo.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitulo = new JLabel(
                "Resumen de ventas, productos y métodos de pago."
        );

        lblSubtitulo.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        lblSubtitulo.setForeground(new Color(100, 116, 139));

        pnlTitulos.add(lblTitulo);
        pnlTitulos.add(Box.createRigidArea(new Dimension(0, 5)));
        pnlTitulos.add(lblSubtitulo);

        // Panel de botones
        JPanel pnlFiltros = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        pnlFiltros.setBackground(Color.WHITE);

        btnHoy = crearBotonFiltro("Ventas de hoy");
        btnSemana = crearBotonFiltro("Esta semana");
        btnMes = crearBotonFiltro("Este mes");

        btnExportarExcel = crearBotonExportar("Exportar Excel");

        pnlFiltros.add(btnHoy);
        pnlFiltros.add(btnSemana);
        pnlFiltros.add(btnMes);
        pnlFiltros.add(Box.createHorizontalStrut(8));
        pnlFiltros.add(btnExportarExcel);

        pnlCabecera.add(pnlTitulos, BorderLayout.CENTER);
        pnlCabecera.add(pnlFiltros, BorderLayout.EAST);

        add(pnlCabecera, BorderLayout.NORTH);

  
        JPanel pnlCentro = new JPanel(
                new BorderLayout(0, 14)
        );
        pnlCentro.setBackground(FONDO);

        // TARJETAS DE RESUMEN
        JPanel pnlTarjetas = new JPanel(
                new GridLayout(1, 3, 12, 0)
        );
        pnlTarjetas.setBackground(FONDO);

        lblIngresos = new JLabel("S/. 0.00");
        lblCantidadVentas = new JLabel("0 transacciones");
        lblTicketPromedio = new JLabel("S/. 0.00");

        pnlTarjetas.add(
                crearTarjeta(
                        "INGRESOS TOTALES",
                        lblIngresos,
                        "Total registrado"
                )
        );

        pnlTarjetas.add(
                crearTarjeta(
                        "TOTAL DE VENTAS REALIZADAS",
                        lblCantidadVentas,
                        "Tickets generados"
                )
        );

        pnlTarjetas.add(
                crearTarjeta(
                        "TICKET PROMEDIO",
                        lblTicketPromedio,
                        "Promedio por venta"
                )
        );

        pnlCentro.add(pnlTarjetas, BorderLayout.NORTH);

        // TABLAS
        JPanel pnlTablas = new JPanel(
                new GridLayout(1, 2, 14, 0)
        );
        pnlTablas.setBackground(FONDO);

        pnlTablas.add(crearPanelProductos());
        pnlTablas.add(crearPanelMetodos());

        pnlCentro.add(pnlTablas, BorderLayout.CENTER);

        add(pnlCentro, BorderLayout.CENTER);

        // ===============================
        // EVENTOS DE LOS BOTONES
        // ===============================

        btnHoy.addActionListener(e -> {
            periodoActual = "HOY";
            actualizarEstiloFiltros();
            refrescarDatos();
        });

        btnSemana.addActionListener(e -> {
            periodoActual = "SEMANA";
            actualizarEstiloFiltros();
            refrescarDatos();
        });

        btnMes.addActionListener(e -> {
            periodoActual = "MES";
            actualizarEstiloFiltros();
            refrescarDatos();
        });

        btnExportarExcel.addActionListener(e -> exportarExcel());

        actualizarEstiloFiltros();
    }

   
    private JButton crearBotonFiltro(String texto) {

        JButton btn = new JButton(texto);

        btn.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        btn.setForeground(TEXTO);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(9, 12, 9, 12)
        ));

        btn.setPreferredSize(new Dimension(122, 40));
        btn.setMinimumSize(new Dimension(122, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Filtrar reportes por periodo");

        btn.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {

                if (!esFiltroSeleccionado(btn)) {
                    btn.setBackground(new Color(240, 253, 244));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {

                if (!esFiltroSeleccionado(btn)) {
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(TEXTO);
                }
            }
        });

        return btn;
    }

    // ===============================
    // BOTÓN EXPORTAR EXCEL
    // ===============================

    private JButton crearBotonExportar(String texto) {

        JButton btn = new JButton(texto);

        btn.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        btn.setForeground(Color.WHITE);
        btn.setBackground(VERDE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        btn.setBorder(
                new EmptyBorder(10, 16, 10, 16)
        );

        btn.setPreferredSize(new Dimension(145, 40));
        btn.setMinimumSize(new Dimension(145, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setToolTipText(
                "Exportar los reportes a un archivo Excel"
        );

        btn.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(VERDE_OSCURO);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(VERDE);
            }
        });

        return btn;
    }

    // ===============================
    // RESALTAR FILTRO SELECCIONADO
    // ===============================

    private boolean esFiltroSeleccionado(JButton boton) {

        if ("HOY".equals(periodoActual)) {
            return boton == btnHoy;
        }

        if ("SEMANA".equals(periodoActual)) {
            return boton == btnSemana;
        }

        return boton == btnMes;
    }

    private void actualizarEstiloFiltros() {

        actualizarBotonFiltro(
                btnHoy,
                "HOY".equals(periodoActual)
        );

        actualizarBotonFiltro(
                btnSemana,
                "SEMANA".equals(periodoActual)
        );

        actualizarBotonFiltro(
                btnMes,
                "MES".equals(periodoActual)
        );
    }

    private void actualizarBotonFiltro(
            JButton boton,
            boolean seleccionado
    ) {

        if (boton == null) {
            return;
        }

        if (seleccionado) {

            boton.setBackground(VERDE_CLARO);
            boton.setForeground(VERDE_OSCURO);

            boton.setBorder(new CompoundBorder(
                    new LineBorder(VERDE, 1, true),
                    new EmptyBorder(9, 12, 9, 12)
            ));

        } else {

            boton.setBackground(Color.WHITE);
            boton.setForeground(TEXTO);

            boton.setBorder(new CompoundBorder(
                    new LineBorder(
                            new Color(203, 213, 225),
                            1,
                            true
                    ),
                    new EmptyBorder(9, 12, 9, 12)
            ));
        }

        boton.setOpaque(true);
        boton.repaint();
    }

    // ===============================
    // PANEL DE PRODUCTOS
    // ===============================

    private JPanel crearPanelProductos() {

        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(Color.WHITE);

        pnl.setBorder(new CompoundBorder(
                new LineBorder(BORDE, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblTitulo = new JLabel(
                "Ranking: productos más vendidos"
        );

        lblTitulo.setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        String[] columnas = {
            "N°",
            "Producto",
            "Categoría",
            "Cant.",
            "Recaudado"
        };

        modProductos = new DefaultTableModel(columnas, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblProductos = new JTable(modProductos);
        configurarTabla(tblProductos);

        JScrollPane scroll = new JScrollPane(tblProductos);

        pnl.add(lblTitulo, BorderLayout.NORTH);
        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    // ===============================
    // PANEL DE MÉTODOS DE PAGO
    // ===============================

    private JPanel crearPanelMetodos() {

        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(Color.WHITE);

        pnl.setBorder(new CompoundBorder(
                new LineBorder(BORDE, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblTitulo = new JLabel(
                "Métodos de pago utilizados"
        );

        lblTitulo.setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        String[] columnas = {
            "Método",
            "Ventas",
            "Total"
        };

        modMetodos = new DefaultTableModel(columnas, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblMetodos = new JTable(modMetodos);
        configurarTabla(tblMetodos);

        JScrollPane scroll = new JScrollPane(tblMetodos);

        pnl.add(lblTitulo, BorderLayout.NORTH);
        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    // ===============================
    // ESTILO DE LAS TABLAS
    // ===============================

    private void configurarTabla(JTable tabla) {

        tabla.setRowHeight(30);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setForeground(new Color(30, 41, 59));
        tabla.setGridColor(new Color(226, 232, 240));
        tabla.setSelectionBackground(new Color(220, 252, 231));
        tabla.setSelectionForeground(new Color(22, 101, 52));
        tabla.setShowVerticalLines(true);
        tabla.setShowHorizontalLines(true);
        tabla.setFillsViewportHeight(true);

        tabla.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        tabla.getTableHeader().setBackground(
                new Color(248, 250, 252)
        );

        tabla.getTableHeader().setForeground(
                new Color(51, 65, 85)
        );

        tabla.getTableHeader().setPreferredSize(
                new Dimension(0, 32)
        );
    }

    // ===============================
    // TARJETAS DE RESUMEN
    // ===============================

    private JPanel crearTarjeta(
            String titulo,
            JLabel valor,
            String descripcion
    ) {

        JPanel pnl = new JPanel();

        pnl.setLayout(
                new BoxLayout(pnl, BoxLayout.Y_AXIS)
        );

        pnl.setBackground(Color.WHITE);

        pnl.setBorder(new CompoundBorder(
                new LineBorder(BORDE, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitulo = new JLabel(titulo);

        lblTitulo.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );

        lblTitulo.setForeground(new Color(51, 65, 85));

        valor.setFont(
                new Font("Segoe UI", Font.BOLD, 22)
        );

        valor.setForeground(VERDE_OSCURO);

        JLabel lblDescripcion = new JLabel(descripcion);

        lblDescripcion.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );

        lblDescripcion.setForeground(new Color(100, 116, 139));

        pnl.add(lblTitulo);
        pnl.add(Box.createRigidArea(new Dimension(0, 7)));
        pnl.add(valor);
        pnl.add(Box.createRigidArea(new Dimension(0, 4)));
        pnl.add(lblDescripcion);

        return pnl;
    }

    // ===============================
    // ACTUALIZAR DATOS
    // ===============================

    public void refrescarDatos() {

        double ingresos =
                reporteDAO.obtenerIngresos(periodoActual);

        int cantidad =
                reporteDAO.obtenerCantidadVentas(periodoActual);

        double promedio =
                reporteDAO.obtenerTicketPromedio(periodoActual);

        lblIngresos.setText(
                String.format("S/. %.2f", ingresos)
        );

        lblCantidadVentas.setText(
                cantidad + " transacciones"
        );

        lblTicketPromedio.setText(
                String.format("S/. %.2f", promedio)
        );

        cargarProductos();
        cargarMetodos();
    }

    // ===============================
    // CARGAR PRODUCTOS
    // ===============================

    private void cargarProductos() {

        modProductos.setRowCount(0);

        List<Object[]> lista =
                reporteDAO.listarProductosMasVendidos(
                        periodoActual
                );

        int numero = 1;

        for (Object[] fila : lista) {

            double recaudado =
                    ((Number) fila[3]).doubleValue();

            modProductos.addRow(
                    new Object[]{
                        numero,
                        fila[0],
                        fila[1],
                        fila[2],
                        String.format("S/. %.2f", recaudado)
                    }
            );

            numero++;
        }
    }

    // ===============================
    // CARGAR MÉTODOS DE PAGO
    // ===============================

    private void cargarMetodos() {

        modMetodos.setRowCount(0);

        List<Object[]> lista =
                reporteDAO.listarMetodosPago(
                        periodoActual
                );

        for (Object[] fila : lista) {

            double total =
                    ((Number) fila[2]).doubleValue();

            modMetodos.addRow(
                    new Object[]{
                        fila[0],
                        fila[1],
                        String.format("S/. %.2f", total)
                    }
            );
        }
    }

    // ===============================
    // EXPORTAR A EXCEL
    // ===============================

    private void exportarExcel() {

        String[] opciones = {
            "Productos más vendidos",
            "Métodos de pago"
        };

        int seleccion = JOptionPane.showOptionDialog(
                this,
                "¿Qué reporte deseas exportar a Excel?",
                "Seleccionar reporte",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (seleccion == 0) {

            List<Object[]> datos =
                    reporteDAO.listarProductosMasVendidos(
                            periodoActual
                    );

            if (datos.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "No hay productos registrados para el periodo: "
                        + periodoActual,
                        "Sin datos",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            JFileChooser fileChooser = new JFileChooser();

            fileChooser.setDialogTitle(
                    "Guardar reporte de productos en Excel"
            );

            fileChooser.setSelectedFile(
                    new File(
                            "Reporte_Productos_Vendidos_"
                            + periodoActual + ".xlsx"
                    )
            );

            int userSelection =
                    fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {

                File archivo = fileChooser.getSelectedFile();
                String ruta = archivo.getAbsolutePath();

                if (!ruta.toLowerCase().endsWith(".xlsx")) {
                    ruta += ".xlsx";
                }

                boolean exito =
                        ExportarExcel.exportarProductosMasVendidos(
                                ruta,
                                datos,
                                periodoActual
                        );

                mostrarResultadoExportacion(exito, ruta);
            }

        } else if (seleccion == 1) {

            List<Object[]> datos =
                    reporteDAO.listarMetodosPago(
                            periodoActual
                    );

            if (datos.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "No hay transacciones registradas para el periodo: "
                        + periodoActual,
                        "Sin datos",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            JFileChooser fileChooser = new JFileChooser();

            fileChooser.setDialogTitle(
                    "Guardar reporte de métodos de pago en Excel"
            );

            fileChooser.setSelectedFile(
                    new File(
                            "Reporte_Metodos_Pago_"
                            + periodoActual + ".xlsx"
                    )
            );

            int userSelection =
                    fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {

                File archivo = fileChooser.getSelectedFile();
                String ruta = archivo.getAbsolutePath();

                if (!ruta.toLowerCase().endsWith(".xlsx")) {
                    ruta += ".xlsx";
                }

                boolean exito =
                        ExportarExcel.exportarMetodosPago(
                                ruta,
                                datos,
                                periodoActual
                        );

                mostrarResultadoExportacion(exito, ruta);
            }
        }
    }

    // ===============================
    // MENSAJE DE EXPORTACIÓN
    // ===============================

    private void mostrarResultadoExportacion(
            boolean exito,
            String ruta
    ) {

        if (exito) {

            JOptionPane.showMessageDialog(
                    this,
                    "¡Reporte exportado exitosamente!\n"
                    + "Guardado en: " + ruta,
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Ocurrió un error al crear el archivo Excel.\n"
                    + "Verifica las librerías de Apache POI y la ruta.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
