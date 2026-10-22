package vistas;

import modelo.Usuario;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FrmPrincipal extends javax.swing.JFrame {

    private Usuario usuarioLogueado;
    private JPanel pnlContenido;
    private CardLayout cardLayout;
    private PnlControlCaja pnlControlCaja;

    // Botones del menú
    private JButton btnInicio;
    private JButton btnNuevaVenta;
    private JButton btnProductos;
    private JButton btnInventario;
    private JButton btnCaja;
    private JButton btnReportes;
    private JButton btnUsuarios;
    private JButton btnCerrarSesion;

    // Paleta de colores
    private final Color COLOR_PRIMARIO = new Color(21, 128, 61);   // Verde Esmeralda (#15803d)
    private final Color COLOR_NAV_BG = new Color(15, 23, 42);      // Slate 900 (#0f172a)
    private final Color COLOR_BTN_DEFAULT = new Color(30, 41, 59); // Slate 800 (#1e293b)
    private final Color COLOR_BTN_HOVER = new Color(51, 65, 85);   // Slate 700 (#334155)

    public FrmPrincipal(Usuario usuario) {
        this.usuarioLogueado = usuario;

        setTitle("Bodega La Peruanita - Sistema POS");
        setSize(1280, 720);
        setMinimumSize(new Dimension(1024, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // =======================================================
        // 1. CABECERA SUPERIOR
        // =======================================================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PRIMARIO);
        pnlHeader.setPreferredSize(new Dimension(1280, 56));
        pnlHeader.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel lblLogo = new JLabel("BODEGA LA PERUANITA - POS");
        lblLogo.setIcon(crearIcono("tienda", Color.WHITE));
        lblLogo.setIconTextGap(10);
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        pnlHeader.add(lblLogo, BorderLayout.WEST);

        String textoUsuario = (usuario != null)
                ? "Operador: " + usuario.getNombreCompleto() + "  |  Rol: " + usuario.getRol()
                : "Operador: Administrador  |  Rol: Administrador";

        JLabel lblUsuarioInfo = new JLabel(textoUsuario);
        lblUsuarioInfo.setIcon(crearIcono("usuario", new Color(220, 252, 231)));
        lblUsuarioInfo.setIconTextGap(8);
        lblUsuarioInfo.setForeground(new Color(220, 252, 231));
        lblUsuarioInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlHeader.add(lblUsuarioInfo, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        // =======================================================
        // 2. MENÚ LATERAL IZQUIERDO
        // =======================================================
        JPanel pnlMenu = new JPanel();
        pnlMenu.setBackground(COLOR_NAV_BG);
        pnlMenu.setPreferredSize(new Dimension(230, 664));
        pnlMenu.setLayout(new BoxLayout(pnlMenu, BoxLayout.Y_AXIS));
        pnlMenu.setBorder(new EmptyBorder(15, 12, 15, 12));

        JLabel lblSeccion = new JLabel("MENÚ PRINCIPAL");
        lblSeccion.setForeground(new Color(148, 163, 184));
        lblSeccion.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSeccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlMenu.add(lblSeccion);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 12)));

        btnInicio = crearBotonMenu("1. Dashboard", "dashboard");
        btnNuevaVenta = crearBotonMenu("2. Nueva Venta (POS)", "carrito");
        btnProductos = crearBotonMenu("3. Catálogo Productos", "paquete");
        btnInventario = crearBotonMenu("4. Inventario / Stock", "kardex");
        btnCaja = crearBotonMenu("5. Control de Caja", "caja");
        btnReportes = crearBotonMenu("6. Reportes y PDF", "reporte");
        btnUsuarios = crearBotonMenu("7. Usuarios", "usuarios");

        btnCerrarSesion = crearBotonMenu("Cerrar Sesión", "salir");
        btnCerrarSesion.setBackground(new Color(153, 27, 27));

        if (usuario != null && "Cajero".equalsIgnoreCase(usuario.getRol())) {
            btnReportes.setEnabled(false);
            btnReportes.setBackground(new Color(23, 33, 48));
            btnReportes.setForeground(new Color(100, 116, 139));
            btnUsuarios.setEnabled(false);
            btnUsuarios.setBackground(new Color(23, 33, 48));
            btnUsuarios.setForeground(new Color(100, 116, 139));
        }

        pnlMenu.add(btnInicio);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 8)));
        pnlMenu.add(btnNuevaVenta);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 8)));
        pnlMenu.add(btnProductos);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 8)));
        pnlMenu.add(btnInventario);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 8)));
        pnlMenu.add(btnCaja);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 8)));
        pnlMenu.add(btnReportes);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 8)));
        pnlMenu.add(btnUsuarios);

        pnlMenu.add(Box.createVerticalGlue());
        pnlMenu.add(btnCerrarSesion);

        add(pnlMenu, BorderLayout.WEST);

        // =======================================================
        // 3. PANEL CENTRAL (CardLayout)
        // =======================================================
        cardLayout = new CardLayout();
        pnlContenido = new JPanel(cardLayout);
        pnlContenido.setBackground(new Color(248, 250, 252));

        pnlContenido.add(crearPanelPlaceholder("DASHBOARD GENERAL", "Métricas del día, ingresos en Soles y productos bajo stock."), "inicio");
        pnlContenido.add(new PnlProductos(usuarioLogueado), "productos");
        pnlContenido.add(new PnlNuevaVenta(usuarioLogueado), "ventas");
        PnlInventario pnlInventario
                = new PnlInventario(usuarioLogueado);

        pnlContenido.add(
                pnlInventario,
                "inventario"
        );

        pnlControlCaja = new PnlControlCaja(usuarioLogueado);
        pnlContenido.add(pnlControlCaja, "caja");
        PnlReportes pnlReportes = new PnlReportes();

        pnlContenido.add(
                pnlReportes,
                "reportes"
        );
        PnlUsuarios pnlUsuarios
                = new PnlUsuarios(usuarioLogueado);

        pnlContenido.add(
                pnlUsuarios,
                "usuarios"
        );

        add(pnlContenido, BorderLayout.CENTER);

        // =======================================================
        // 4. BARRA DE ESTADO INFERIOR
        // =======================================================
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(new Color(241, 245, 249));
        pnlFooter.setPreferredSize(new Dimension(1280, 28));
        pnlFooter.setBorder(new EmptyBorder(4, 15, 4, 15));

        JLabel lblFooterLeft = new JLabel("Sistema POS Bodega La Peruanita v1.0  |  Integrador I (UTP)");
        lblFooterLeft.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooterLeft.setForeground(new Color(100, 116, 139));

        JLabel lblFooterRight = new JLabel("● Base de Datos: PostgreSQL Conectado ");
        lblFooterRight.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFooterRight.setForeground(new Color(22, 163, 74));

        pnlFooter.add(lblFooterLeft, BorderLayout.WEST);
        pnlFooter.add(lblFooterRight, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);

        // =======================================================
        // 5. ACCIONES DE BOTONES
        // =======================================================
        btnInicio.addActionListener((ActionEvent e) -> seleccionarModulo("inicio", btnInicio));
        btnNuevaVenta.addActionListener((ActionEvent e) -> seleccionarModulo("ventas", btnNuevaVenta));
        btnProductos.addActionListener((ActionEvent e) -> seleccionarModulo("productos", btnProductos));
        btnInventario.addActionListener(e -> {

            pnlInventario.refrescarDatos();

            seleccionarModulo(
                    "inventario",
                    btnInventario
            );
        });

        btnCaja.addActionListener((ActionEvent e) -> {

            pnlControlCaja.refrescarDatos();

            seleccionarModulo(
                    "caja",
                    btnCaja
            );
        });

        btnReportes.addActionListener(e -> {

    pnlReportes.refrescarDatos();

    seleccionarModulo(
            "reportes",
            btnReportes
    );
});
        btnUsuarios.addActionListener(e -> {

            pnlUsuarios.refrescarUsuarios();

            seleccionarModulo(
                    "usuarios",
                    btnUsuarios
            );
        });
        btnCerrarSesion.addActionListener((ActionEvent e) -> {
            int opc = JOptionPane.showConfirmDialog(this, "¿Está seguro de cerrar sesión?", "Salir", JOptionPane.YES_NO_OPTION);
            if (opc == JOptionPane.YES_OPTION) {
                new FrmLogin().setVisible(true);
                this.dispose();
            }
        });

        // Módulo inicial (Abre en Nueva Venta)
        seleccionarModulo("ventas", btnNuevaVenta);
    }

    private void seleccionarModulo(String claveCard, JButton botonActivo) {
        cardLayout.show(pnlContenido, claveCard);

        JButton[] botones = {btnInicio, btnNuevaVenta, btnProductos, btnInventario, btnCaja, btnReportes, btnUsuarios};
        for (JButton b : botones) {
            if (b.isEnabled()) {
                b.setBackground(COLOR_BTN_DEFAULT);
                b.setForeground(Color.WHITE);
            }
        }
        if (botonActivo.isEnabled() && botonActivo != btnCerrarSesion) {
            botonActivo.setBackground(COLOR_PRIMARIO);
        }
    }

    private JButton crearBotonMenu(String texto, String tipoIcono) {
        JButton btn = new JButton(texto);
        btn.setIcon(crearIcono(tipoIcono, Color.WHITE));
        btn.setIconTextGap(12);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_BTN_DEFAULT);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(206, 42));
        btn.setPreferredSize(new Dimension(206, 42));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled() && btn.getBackground() != COLOR_PRIMARIO && btn != btnCerrarSesion) {
                    btn.setBackground(COLOR_BTN_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled() && btn.getBackground() != COLOR_PRIMARIO && btn != btnCerrarSesion) {
                    btn.setBackground(COLOR_BTN_DEFAULT);
                }
            }
        });

        return btn;
    }

    private Icon crearIcono(String tipo, Color color) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f));

                switch (tipo) {
                    case "dashboard":
                        g2.drawRect(x + 2, y + 2, 5, 5);
                        g2.drawRect(x + 10, y + 2, 5, 5);
                        g2.drawRect(x + 2, y + 10, 5, 5);
                        g2.drawRect(x + 10, y + 10, 5, 5);
                        break;
                    case "carrito":
                        g2.drawLine(x + 1, y + 3, x + 4, y + 3);
                        g2.drawLine(x + 4, y + 3, x + 6, y + 11);
                        g2.drawLine(x + 6, y + 11, x + 14, y + 11);
                        g2.drawLine(x + 14, y + 11, x + 16, y + 5);
                        g2.drawLine(x + 5, y + 5, x + 16, y + 5);
                        g2.fillOval(x + 6, y + 13, 3, 3);
                        g2.fillOval(x + 13, y + 13, 3, 3);
                        break;
                    case "paquete":
                        g2.drawRect(x + 2, y + 4, 13, 10);
                        g2.drawLine(x + 2, y + 8, x + 15, y + 8);
                        g2.drawLine(x + 8, y + 4, x + 8, y + 8);
                        break;
                    case "kardex":
                        g2.drawRect(x + 3, y + 2, 11, 13);
                        g2.drawLine(x + 6, y + 5, x + 11, y + 5);
                        g2.drawLine(x + 6, y + 8, x + 11, y + 8);
                        g2.drawLine(x + 6, y + 11, x + 9, y + 11);
                        break;
                    case "caja":
                        g2.drawRoundRect(x + 1, y + 3, 15, 10, 3, 3);
                        g2.drawOval(x + 6, y + 6, 4, 4);
                        break;
                    case "reporte":
                        g2.drawLine(x + 2, y + 14, x + 15, y + 14);
                        g2.fillRect(x + 4, y + 8, 2, 6);
                        g2.fillRect(x + 8, y + 5, 2, 9);
                        g2.fillRect(x + 12, y + 2, 2, 12);
                        break;
                    case "usuarios":
                    case "usuario":
                        g2.drawOval(x + 6, y + 2, 6, 6);
                        g2.drawArc(x + 3, y + 9, 12, 7, 0, 180);
                        break;
                    case "tienda":
                        g2.drawPolygon(new int[]{x + 2, x + 9, x + 16}, new int[]{y + 6, y + 1, y + 6}, 3);
                        g2.drawRect(x + 3, y + 6, 12, 9);
                        g2.drawRect(x + 7, y + 9, 4, 6);
                        break;
                    case "salir":
                        g2.drawRect(x + 2, y + 2, 7, 13);
                        g2.drawLine(x + 7, y + 9, x + 15, y + 9);
                        g2.drawLine(x + 12, y + 6, x + 15, y + 9);
                        g2.drawLine(x + 12, y + 12, x + 15, y + 9);
                        break;
                }
                g2.dispose();
            }
        };
    }

    private JPanel crearPanelPlaceholder(String titulo, String subtitulo) {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(new Color(248, 250, 252));

        JPanel tarjeta = new JPanel(new GridLayout(2, 1, 8, 8));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(30, 40, 30, 40)
        ));

        JLabel lblTit = new JLabel(titulo, JLabel.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTit.setForeground(COLOR_PRIMARIO);

        JLabel lblSub = new JLabel(subtitulo, JLabel.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(100, 116, 139));

        tarjeta.add(lblTit);
        tarjeta.add(lblSub);
        pnl.add(tarjeta);
        return pnl;
    }
}
