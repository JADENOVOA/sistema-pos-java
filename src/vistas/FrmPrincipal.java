package vistas;

import conexion.ConexionBD;
import dao.InventarioDAO;
import dao.ReporteDAO;
import modelo.Usuario;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FrmPrincipal extends javax.swing.JFrame {

    private final Usuario usuarioLogueado;
    private final boolean esCajero;

    // Logo (src/img/logo.png). Si no se encuentra, se usa el ícono dibujado.
    private final BufferedImage logoImg = cargarLogo();

    // Contenido central
    private JPanel pnlContenido;
    private CardLayout cardLayout;
    private JLabel kSaludo, kIngresos, kVentas, kTicket, kStockBajo;
    private JLabel kSemana, kMes, kUnidades, kValorInv;
    private PanelBarras pnlBarras;
    private PanelDona pnlDona;
    private PnlInventario pnlInventario;
    private PnlControlCaja pnlControlCaja;
    private PnlReportes pnlReportes;
    private PnlUsuarios pnlUsuarios;

    // Menú
    private final Map<String, BotonMenu> botones = new LinkedHashMap<>();
    private BotonMenu btnCerrarSesion;

    // Cabecera y pie
    private JLabel lblTituloModulo;
    private JLabel lblSubtituloModulo;
    private JLabel lblHora;
    private JLabel lblFecha;
    private JLabel lblEstadoBD;
    private Timer timerReloj;
    private Timer timerBD;

    private final SimpleDateFormat fmtHora = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat fmtFecha
            = new SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Locale.forLanguageTag("es-PE"));

    // Paleta de colores
    private static final Color COLOR_PRIMARIO = new Color(21, 128, 61);      // Verde esmeralda
    private static final Color COLOR_PRIMARIO_OSC = new Color(22, 101, 52);  // Verde oscuro
    private static final Color COLOR_VERDE_CLARO = new Color(187, 247, 208);
    private static final Color COLOR_NAV_BG = new Color(15, 23, 42);         // Slate 900
    private static final Color COLOR_BTN_HOVER = new Color(30, 41, 59);      // Slate 800
    private static final Color COLOR_FONDO = new Color(241, 245, 249);       // Slate 100
    private static final Color COLOR_GRIS = new Color(100, 116, 139);        // Slate 500
    private static final Color COLOR_TEXTO = new Color(15, 23, 42);          // Slate 900
    private static final Color COLOR_BORDE = new Color(226, 232, 240);       // Slate 200

    // Colores para gráficos
    private static final Color[] PALETA = {
        new Color(21, 128, 61), new Color(37, 99, 235), new Color(217, 119, 6),
        new Color(147, 51, 234), new Color(220, 38, 38), new Color(14, 165, 233)
    };

    // Título y subtítulo de cada módulo (se muestran en la cabecera)
    private static final Map<String, String[]> INFO = new HashMap<>();

    static {
        INFO.put("inicio", new String[]{"Dashboard", "Resumen general del día en la bodega"});
        INFO.put("ventas", new String[]{"Nueva Venta (POS)", "Registra ventas y emite el comprobante"});
        INFO.put("productos", new String[]{"Catálogo de Productos", "Registro, edición y consulta de productos"});
        INFO.put("inventario", new String[]{"Inventario / Stock", "Control de existencias y movimientos"});
        INFO.put("caja", new String[]{"Control de Caja", "Apertura, arqueo y cierre de caja"});
        INFO.put("reportes", new String[]{"Reportes y PDF", "Indicadores de ventas y exportación"});
        INFO.put("usuarios", new String[]{"Usuarios", "Administración de usuarios y accesos"});
    }

    public FrmPrincipal(Usuario usuario) {
        this.usuarioLogueado = usuario;
        this.esCajero = usuario != null && "Cajero".equalsIgnoreCase(usuario.getRol());

        setTitle("Bodega La Peruanita - Sistema POS");
        if (logoImg != null) {
            setIconImage(escalar(logoImg, 64));
        }
        setSize(1280, 720);
        setMinimumSize(new Dimension(1024, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(crearCabecera(), BorderLayout.NORTH);
        add(crearMenuLateral(), BorderLayout.WEST);
        add(crearContenido(), BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        iniciarTimers();

        // Módulo inicial (abre en Nueva Venta)
        irA("ventas");
    }

    private BufferedImage cargarLogo() {
    String[] recursos = {"/imagen/logo.png", "/img/logo.png"};
    String[] rutas = {"src/imagen/logo.png", "src/img/logo.png", "imagen/logo.png"};
    try {
        // 1) Desde el classpath
        for (String r : recursos) {
            URL u = FrmPrincipal.class.getResource(r);
            if (u != null) {
                System.out.println("[LOGO] Cargado desde classpath: " + u);
                return ImageIO.read(u);
            }
        }
        // 2) Desde el disco (carpeta del proyecto)
        for (String r : rutas) {
            File f = new File(r);
            if (f.exists()) {
                System.out.println("[LOGO] Cargado desde: " + f.getAbsolutePath());
                return ImageIO.read(f);
            }
        }
        System.out.println("[LOGO] NO ENCONTRADO. Carpeta de trabajo: "
                + new File(".").getAbsolutePath());
    } catch (Exception e) {
        System.out.println("[LOGO] Error al leer la imagen: " + e);
    }
    return null;
}

    /** Reduce la imagen por pasos (mejor calidad que un solo salto). */
    private static BufferedImage escalar(BufferedImage src, int lado) {
        BufferedImage actual = src;
        int w = src.getWidth();
        int h = src.getHeight();
        while (w > lado * 2) {
            w /= 2;
            h /= 2;
            actual = pasoEscala(actual, w, h);
        }
        return pasoEscala(actual, lado, lado);
    }

    private static BufferedImage pasoEscala(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }

    /** Ícono circular con el logo (fondo blanco, para que resalte sobre fondos oscuros o verdes). */
    private Icon crearLogoCircular(final int lado) {
        final BufferedImage img = escalar(logoImg, lado);
        return new Icon() {
            @Override
            public int getIconWidth() {
                return lado;
            }

            @Override
            public int getIconHeight() {
                return lado;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Float(x, y, lado, lado));
                g2.setClip(new Ellipse2D.Float(x, y, lado, lado));
                g2.drawImage(img, x, y, null);
                g2.dispose();
            }
        };
    }

    private JPanel crearCabecera() {
        JPanel pnl = new PanelDegradado(COLOR_PRIMARIO_OSC, COLOR_PRIMARIO);
        pnl.setLayout(new BorderLayout());
        pnl.setPreferredSize(new Dimension(1280, 68));
        pnl.setBorder(new EmptyBorder(0, 26, 0, 24));

  
        JPanel izq = new JPanel();
        izq.setOpaque(false);
        izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));

        lblTituloModulo = new JLabel(" ");
        lblTituloModulo.setForeground(Color.WHITE);
        lblTituloModulo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        lblSubtituloModulo = new JLabel(" ");
        lblSubtituloModulo.setForeground(COLOR_VERDE_CLARO);
        lblSubtituloModulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        izq.add(Box.createVerticalGlue());
        izq.add(lblTituloModulo);
        izq.add(Box.createRigidArea(new Dimension(0, 2)));
        izq.add(lblSubtituloModulo);
        izq.add(Box.createVerticalGlue());
        pnl.add(izq, BorderLayout.WEST);

        JPanel der = new JPanel(new GridBagLayout());
        der.setOpaque(false);

        JPanel reloj = new JPanel();
        reloj.setOpaque(false);
        reloj.setLayout(new BoxLayout(reloj, BoxLayout.Y_AXIS));

        lblHora = new JLabel("00:00:00");
        lblHora.setForeground(Color.WHITE);
        lblHora.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHora.setAlignmentX(Component.RIGHT_ALIGNMENT);

        lblFecha = new JLabel(" ");
        lblFecha.setForeground(COLOR_VERDE_CLARO);
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFecha.setAlignmentX(Component.RIGHT_ALIGNMENT);

        reloj.add(lblHora);
        reloj.add(lblFecha);

        String rol = (usuarioLogueado != null && usuarioLogueado.getRol() != null)
                ? usuarioLogueado.getRol().toUpperCase()
                : "ADMINISTRADOR";
        Pastilla pastilla = new Pastilla(rol);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        der.add(reloj, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 18, 0, 0);
        der.add(pastilla, gbc);

        pnl.add(der, BorderLayout.EAST);
        return pnl;
    }


    private JPanel crearMenuLateral() {
        JPanel pnlMenu = new JPanel();
        pnlMenu.setBackground(COLOR_NAV_BG);
        pnlMenu.setPreferredSize(new Dimension(250, 600));
        pnlMenu.setLayout(new BoxLayout(pnlMenu, BoxLayout.Y_AXIS));
        pnlMenu.setBorder(new EmptyBorder(18, 12, 14, 12));

        pnlMenu.add(crearMarca());
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 22)));

        JLabel lblSeccion = new JLabel("MENÚ PRINCIPAL");
        lblSeccion.setForeground(new Color(100, 116, 139));
        lblSeccion.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSeccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSeccion.setBorder(new EmptyBorder(0, 6, 0, 0));
        pnlMenu.add(lblSeccion);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 10)));

        agregarBoton(pnlMenu, "inicio", "Dashboard", "dashboard", true);
        agregarBoton(pnlMenu, "ventas", "Nueva Venta (POS)", "carrito", true);
        agregarBoton(pnlMenu, "productos", "Catálogo Productos", "paquete", true);
        agregarBoton(pnlMenu, "inventario", "Inventario / Stock", "kardex", true);
        agregarBoton(pnlMenu, "caja", "Control de Caja", "caja", true);
        agregarBoton(pnlMenu, "reportes", "Reportes y PDF", "reporte", !esCajero);
        agregarBoton(pnlMenu, "usuarios", "Usuarios", "usuarios", !esCajero);

        pnlMenu.add(Box.createVerticalGlue());
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 12)));
        pnlMenu.add(crearTarjetaUsuario());
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 10)));

        btnCerrarSesion = new BotonMenu("Cerrar Sesión", "salir",
                new Color(127, 29, 29), new Color(153, 27, 27));
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        pnlMenu.add(btnCerrarSesion);

        return pnlMenu;
    }

    private void agregarBoton(JPanel pnl, String clave, String texto, String icono, boolean permitido) {
        BotonMenu b = new BotonMenu(texto, icono, null, null);
        if (permitido) {
            b.addActionListener(e -> irA(clave));
        } else {
            b.setEnabled(false);
            b.setCursor(Cursor.getDefaultCursor());
            b.setToolTipText("Disponible solo para el rol Administrador");
        }
        botones.put(clave, b);
        pnl.add(b);
        pnl.add(Box.createRigidArea(new Dimension(0, 6)));
    }

    /** Logo + nombre de la bodega, en la parte superior del menú. */
    private JPanel crearMarca() {
        JPanel pnl = new JPanel(new BorderLayout(12, 0));
        pnl.setOpaque(false);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setBorder(new EmptyBorder(0, 2, 0, 0));
        pnl.setMaximumSize(new Dimension(Short.MAX_VALUE, 66));
        pnl.setPreferredSize(new Dimension(226, 66));

        // Logo real (más grande); si no existe el archivo, usa la insignia dibujada
        Icon icono = (logoImg != null) ? crearLogoCircular(62) : crearInsignia();
        pnl.add(new JLabel(icono), BorderLayout.WEST);

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 0));
        textos.setOpaque(false);

        JLabel lblNombre = new JLabel("La Peruanita");
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel lblTipo = new JLabel("BODEGA · POS");
        lblTipo.setForeground(new Color(134, 239, 172));
        lblTipo.setFont(new Font("Segoe UI", Font.BOLD, 11));

        textos.add(lblNombre);
        textos.add(lblTipo);
        pnl.add(textos, BorderLayout.CENTER);
        return pnl;
    }

    /** Tarjeta con el avatar, nombre y rol del usuario logueado. */
    private JPanel crearTarjetaUsuario() {
        String nombre = (usuarioLogueado != null && usuarioLogueado.getNombreCompleto() != null)
                ? usuarioLogueado.getNombreCompleto()
                : "Administrador";
        String rol = (usuarioLogueado != null && usuarioLogueado.getRol() != null)
                ? usuarioLogueado.getRol()
                : "Administrador";

        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        card.setPreferredSize(new Dimension(226, 62));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 62));
        card.setToolTipText(nombre + " (" + rol + ")");

        card.add(new JLabel(crearAvatar(iniciales(nombre))), BorderLayout.WEST);

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 0));
        textos.setOpaque(false);

        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lblRol = new JLabel(rol);
        lblRol.setForeground(new Color(134, 239, 172));
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        textos.add(lblNombre);
        textos.add(lblRol);
        card.add(textos, BorderLayout.CENTER);
        return card;
    }

    private String iniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "U";
        }
        String[] p = nombre.trim().split("\\s+");
        String r = p[0].substring(0, 1);
        if (p.length > 1) {
            r += p[1].substring(0, 1);
        }
        return r.toUpperCase();
    }

  
    private JPanel crearContenido() {
        cardLayout = new CardLayout();
        pnlContenido = new JPanel(cardLayout);
        pnlContenido.setBackground(COLOR_FONDO);

        JPanel pnlDashboard = crearDashboard();
        pnlInventario = new PnlInventario(usuarioLogueado);
        pnlControlCaja = new PnlControlCaja(usuarioLogueado);
        pnlReportes = new PnlReportes();
        pnlUsuarios = new PnlUsuarios(usuarioLogueado);

        pnlContenido.add(pnlDashboard, "inicio");
        pnlContenido.add(new PnlNuevaVenta(usuarioLogueado), "ventas");
        pnlContenido.add(new PnlProductos(usuarioLogueado), "productos");
        pnlContenido.add(pnlInventario, "inventario");
        pnlContenido.add(pnlControlCaja, "caja");
        pnlContenido.add(pnlReportes, "reportes");
        pnlContenido.add(pnlUsuarios, "usuarios");

        return pnlContenido;
    }

  
    private JPanel crearPie() {
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setPreferredSize(new Dimension(1280, 30));
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE),
                new EmptyBorder(4, 16, 4, 16)
        ));

        JLabel lblIzq = new JLabel("Sistema POS Bodega La Peruanita v1.0  |  Integrador I (UTP)");
        lblIzq.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblIzq.setForeground(COLOR_GRIS);

        lblEstadoBD = new JLabel("● Base de datos: verificando... ");
        lblEstadoBD.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEstadoBD.setForeground(COLOR_GRIS);

        pnlFooter.add(lblIzq, BorderLayout.WEST);
        pnlFooter.add(lblEstadoBD, BorderLayout.EAST);
        return pnlFooter;
    }

  
    private void irA(String clave) {
        BotonMenu b = botones.get(clave);
        if (b == null || !b.isEnabled()) {
            return;
        }

        switch (clave) {
            case "inicio":
                refrescarDashboard();
                break;
            case "inventario":
                pnlInventario.refrescarDatos();
                break;
            case "caja":
                pnlControlCaja.refrescarDatos();
                break;
            case "reportes":
                pnlReportes.refrescarDatos();
                break;
            case "usuarios":
                pnlUsuarios.refrescarUsuarios();
                break;
            default:
                break;
        }

        seleccionarModulo(clave);
    }

    private void seleccionarModulo(String clave) {
        cardLayout.show(pnlContenido, clave);

        for (Map.Entry<String, BotonMenu> en : botones.entrySet()) {
            en.getValue().setActivo(en.getKey().equals(clave));
        }

        String[] info = INFO.get(clave);
        if (info != null) {
            lblTituloModulo.setText(info[0]);
            lblSubtituloModulo.setText(info[1]);
        }
    }

    private void cerrarSesion() {
        int opc = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de cerrar sesión?", "Salir", JOptionPane.YES_NO_OPTION);
        if (opc == JOptionPane.YES_OPTION) {
            new FrmLogin().setVisible(true);
            this.dispose();
        }
    }

    private JPanel crearDashboard() {
        JPanel pnl = new JPanel(new BorderLayout(0, 16));
        pnl.setBackground(COLOR_FONDO);
        pnl.setBorder(new EmptyBorder(20, 26, 20, 26));

        kSaludo = new JLabel(" ");
        kIngresos = new JLabel("—");
        kVentas = new JLabel("—");
        kTicket = new JLabel("—");
        kStockBajo = new JLabel("—");
        kSemana = new JLabel("—");
        kMes = new JLabel("—");
        kUnidades = new JLabel("—");
        kValorInv = new JLabel("—");

        // ---- Arriba: banner de bienvenida + fila de KPI ----
        JPanel arriba = new JPanel();
        arriba.setOpaque(false);
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));

        JPanel banner = crearBanner();
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setPreferredSize(new Dimension(100, 112));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));

        JPanel fila = new JPanel(new GridLayout(1, 4, 14, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setPreferredSize(new Dimension(100, 104));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 104));
        fila.add(crearKpi("VENTAS DE HOY", kIngresos, "Ingresos del día", COLOR_PRIMARIO));
        fila.add(crearKpi("N° DE VENTAS", kVentas, "Transacciones de hoy", new Color(37, 99, 235)));
        fila.add(crearKpi("TICKET PROMEDIO", kTicket, "Monto promedio por venta", new Color(217, 119, 6)));
        fila.add(crearKpi("STOCK BAJO", kStockBajo, "Productos por reponer", new Color(220, 38, 38)));

        arriba.add(banner);
        arriba.add(Box.createRigidArea(new Dimension(0, 16)));
        arriba.add(fila);
        pnl.add(arriba, BorderLayout.NORTH);

        // ---- Centro: gráficos y resumen ----
        pnlBarras = new PanelBarras();
        pnlDona = new PanelDona();

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        gc.gridy = 0;

        gc.gridx = 0;
        gc.weightx = 1.3;
        gc.insets = new Insets(0, 0, 0, 14);
        centro.add(crearTarjeta("Productos más vendidos", "Top 5 del mes (unidades)", pnlBarras), gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        centro.add(crearTarjeta("Métodos de pago", "Distribución del mes", pnlDona), gc);

        gc.gridx = 2;
        gc.weightx = 0.9;
        gc.insets = new Insets(0, 0, 0, 0);
        centro.add(crearTarjeta("Resumen y accesos", "Atajos a los módulos", crearResumen()), gc);

        pnl.add(centro, BorderLayout.CENTER);

        refrescarDashboard();
        return pnl;
    }

    /** Banner verde de bienvenida con el logo grande a la derecha. */
    private JPanel crearBanner() {
        JPanel b = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Shape forma = new RoundRectangle2D.Float(0, 0, w, h, 22, 22);
                g2.setClip(forma);
                g2.setPaint(new GradientPaint(0, 0, COLOR_PRIMARIO_OSC, w, h, new Color(34, 160, 84)));
                g2.fillRect(0, 0, w, h);
                // Círculos decorativos
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillOval(w - 330, -110, 330, 330);
                g2.setColor(new Color(255, 255, 255, 16));
                g2.fillOval(w - 190, 30, 230, 230);
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(-50, h - 60, 170, 170);
                g2.fillOval(w / 2 - 60, -70, 120, 120);
                g2.dispose();
            }
        };
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(10, 28, 10, 28));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        kSaludo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        kSaludo.setForeground(Color.WHITE);
        kSaludo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Resumen de ventas e inventario del día");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(COLOR_VERDE_CLARO);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lema = new Pastilla("TU BODEGA DE CONFIANZA");
        ((Pastilla) lema).setAlignmentX(Component.LEFT_ALIGNMENT);

        textos.add(Box.createVerticalGlue());
        textos.add(kSaludo);
        textos.add(Box.createRigidArea(new Dimension(0, 4)));
        textos.add(sub);
        textos.add(Box.createRigidArea(new Dimension(0, 10)));
        textos.add(lema);
        textos.add(Box.createVerticalGlue());
        b.add(textos, BorderLayout.CENTER);

        if (logoImg != null) {
            JLabel lbl = new JLabel(crearLogoCircular(92));
            b.add(lbl, BorderLayout.EAST);
        }
        return b;
    }

    private JPanel crearKpi(String titulo, JLabel valor, String detalle, final Color acento) {
        JPanel t = new JPanel(new BorderLayout(0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth() - 1;
                int h = getHeight() - 1;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, 16, 16);
                g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));
                g2.setColor(acento);
                g2.fillRect(0, 0, w + 1, 4);
                g2.setClip(null);
                g2.setColor(COLOR_BORDE);
                g2.drawRoundRect(0, 0, w, h, 16, 16);
                g2.dispose();
            }
        };
        t.setOpaque(false);
        t.setBorder(new EmptyBorder(14, 18, 12, 14));

        JLabel lt = new JLabel(titulo);
        lt.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lt.setForeground(COLOR_GRIS);
        valor.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valor.setForeground(COLOR_TEXTO);
        JLabel ld = new JLabel(detalle);
        ld.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ld.setForeground(COLOR_GRIS);

        t.add(lt, BorderLayout.NORTH);
        t.add(valor, BorderLayout.CENTER);
        t.add(ld, BorderLayout.SOUTH);
        return t;
    }

    /** Tarjeta blanca redondeada con título, subtítulo y un cuerpo. */
    private JPanel crearTarjeta(String titulo, String subtitulo, JComponent cuerpo) {
        JPanel t = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth() - 1;
                int h = getHeight() - 1;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, 16, 16);
                g2.setColor(COLOR_BORDE);
                g2.drawRoundRect(0, 0, w, h, 16, 16);
                g2.dispose();
            }
        };
        t.setOpaque(false);
        t.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel cab = new JPanel(new GridLayout(2, 1, 0, 0));
        cab.setOpaque(false);
        JLabel lt = new JLabel(titulo);
        lt.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lt.setForeground(COLOR_TEXTO);
        JLabel ls = new JLabel(subtitulo);
        ls.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ls.setForeground(COLOR_GRIS);
        cab.add(lt);
        cab.add(ls);

        t.add(cab, BorderLayout.NORTH);
        t.add(cuerpo, BorderLayout.CENTER);
        return t;
    }

    /** Filas de resumen + botones de acceso rápido. */
    private JPanel crearResumen() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(filaResumen("Ventas de la semana", kSemana));
        p.add(filaResumen("Ventas del mes", kMes));
        p.add(filaResumen("Unidades en stock", kUnidades));
        p.add(filaResumen("Valor del inventario", kValorInv));
        p.add(Box.createVerticalGlue());

        JPanel grid = new JPanel(new GridLayout(2, 2, 8, 8));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setPreferredSize(new Dimension(100, 96));
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        grid.add(botonRapido("Nueva venta", PALETA[0], "ventas"));
        grid.add(botonRapido("Productos", PALETA[1], "productos"));
        grid.add(botonRapido("Inventario", PALETA[2], "inventario"));
        grid.add(botonRapido("Caja", PALETA[3], "caja"));
        p.add(grid);
        return p;
    }

    private JPanel filaResumen(String texto, JLabel valor) {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setPreferredSize(new Dimension(100, 30));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        f.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));

        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(COLOR_GRIS);
        valor.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valor.setForeground(COLOR_TEXTO);
        valor.setHorizontalAlignment(SwingConstants.RIGHT);

        f.add(l, BorderLayout.WEST);
        f.add(valor, BorderLayout.EAST);
        return f;
    }

    private JButton botonRapido(String texto, final Color color, final String modulo) {
        BotonRapido b = new BotonRapido(texto, color);
        b.addActionListener(e -> irA(modulo));
        return b;
    }

    /** Datos del dashboard que se cargan en segundo plano. */
    private static class DatosDash {

        double ingresos, ticket, semana, mes, valorInv;
        int ventas, stockBajo, unidades;
        List<Object[]> top = new ArrayList<>();
        List<Object[]> pagos = new ArrayList<>();
    }

    /** Carga los indicadores del día en segundo plano. */
    private void refrescarDashboard() {
        int hora = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String saludo = hora < 12 ? "¡Buenos días" : (hora < 19 ? "¡Buenas tardes" : "¡Buenas noches");
        String nombre = "";
        if (usuarioLogueado != null && usuarioLogueado.getNombreCompleto() != null
                && !usuarioLogueado.getNombreCompleto().trim().isEmpty()) {
            nombre = ", " + usuarioLogueado.getNombreCompleto().trim().split("\\s+")[0];
        }
        kSaludo.setText(saludo + nombre + "!");

        new SwingWorker<DatosDash, Void>() {
            @Override
            protected DatosDash doInBackground() {
                ReporteDAO r = new ReporteDAO();
                InventarioDAO i = new InventarioDAO();
                DatosDash d = new DatosDash();
                d.ingresos = r.obtenerIngresos("HOY");
                d.ventas = r.obtenerCantidadVentas("HOY");
                d.ticket = r.obtenerTicketPromedio("HOY");
                d.stockBajo = i.contarStockBajo();
                d.semana = r.obtenerIngresos("SEMANA");
                d.mes = r.obtenerIngresos("MES");
                d.unidades = i.obtenerStockTotal();
                d.valorInv = i.obtenerValorInventario();
                d.top = r.listarProductosMasVendidos("MES");
                d.pagos = r.listarMetodosPago("MES");
                return d;
            }

            @Override
            protected void done() {
                try {
                    DatosDash d = get();
                    kIngresos.setText(String.format(Locale.US, "S/ %,.2f", d.ingresos));
                    kVentas.setText(String.valueOf(d.ventas));
                    kTicket.setText(String.format(Locale.US, "S/ %,.2f", d.ticket));
                    kStockBajo.setText(String.valueOf(d.stockBajo));
                    kStockBajo.setForeground(d.stockBajo > 0 ? new Color(220, 38, 38) : COLOR_PRIMARIO);
                    kSemana.setText(String.format(Locale.US, "S/ %,.2f", d.semana));
                    kMes.setText(String.format(Locale.US, "S/ %,.2f", d.mes));
                    kUnidades.setText(String.format(Locale.US, "%,d", d.unidades));
                    kValorInv.setText(String.format(Locale.US, "S/ %,.2f", d.valorInv));
                    pnlBarras.setDatos(d.top);
                    pnlDona.setDatos(d.pagos);
                } catch (Exception e) {
                    kIngresos.setText("—");
                    kVentas.setText("—");
                    kTicket.setText("—");
                    kStockBajo.setText("—");
                    kSemana.setText("—");
                    kMes.setText("—");
                    kUnidades.setText("—");
                    kValorInv.setText("—");
                }
            }
        }.execute();
    }

    private void iniciarTimers() {
        actualizarReloj();
        timerReloj = new Timer(1000, e -> actualizarReloj());
        timerReloj.start();

        verificarConexion();
        timerBD = new Timer(30000, e -> verificarConexion());
        timerBD.start();
    }

    private void actualizarReloj() {
        Date ahora = new Date();
        lblHora.setText(fmtHora.format(ahora));
        String f = fmtFecha.format(ahora);
        if (!f.isEmpty()) {
            f = Character.toUpperCase(f.charAt(0)) + f.substring(1);
        }
        lblFecha.setText(f);
    }

    private void verificarConexion() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try (Connection c = ConexionBD.getConexion()) {
                    return c != null && c.isValid(2);
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void done() {
                boolean ok = false;
                try {
                    ok = get();
                } catch (Exception ignored) {
                }
                lblEstadoBD.setText(ok
                        ? "● Base de datos: PostgreSQL conectado "
                        : "● Base de datos: sin conexión ");
                lblEstadoBD.setForeground(ok ? new Color(22, 163, 74) : new Color(220, 38, 38));
            }
        }.execute();
    }

    @Override
    public void dispose() {
        if (timerReloj != null) {
            timerReloj.stop();
        }
        if (timerBD != null) {
            timerBD.stop();
        }
        super.dispose();
    }

    private static String recortar(String s, FontMetrics fm, int ancho) {
        if (s == null) {
            return "";
        }
        if (fm.stringWidth(s) <= ancho) {
            return s;
        }
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "…") > ancho) {
            t = t.substring(0, t.length() - 1);
        }
        return t + "…";
    }

    /** "TARJETA_CREDITO" -> "Tarjeta credito". */
    private static String bonito(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        String t = s.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }


    private static class PanelBarras extends JPanel {

        private List<Object[]> datos = new ArrayList<>();

        PanelBarras() {
            setOpaque(false);
        }

        void setDatos(List<Object[]> d) {
            this.datos = (d != null) ? d : new ArrayList<Object[]>();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (datos.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(COLOR_GRIS);
                String msg = "Aún no hay ventas registradas este mes";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
                g2.dispose();
                return;
            }

            int n = Math.min(5, datos.size());
            int max = Math.max(1, ((Number) datos.get(0)[2]).intValue());
            int filaH = Math.min(h / n, 58);
            Font fNom = new Font("Segoe UI", Font.BOLD, 12);
            Font fVal = new Font("Segoe UI", Font.BOLD, 12);

            for (int i = 0; i < n; i++) {
                Object[] fila = datos.get(i);
                String nombre = String.valueOf(fila[0]);
                int cant = ((Number) fila[2]).intValue();
                int y = i * filaH + 4;

                String val = cant + " uds";
                g2.setFont(fVal);
                FontMetrics fmV = g2.getFontMetrics();
                int anchoVal = fmV.stringWidth(val);
                g2.setColor(COLOR_TEXTO);
                g2.drawString(val, w - anchoVal, y + 12);

                g2.setFont(fNom);
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(new Color(51, 65, 85));
                g2.drawString((i + 1) + ". " + recortar(nombre, fm, w - anchoVal - 30), 0, y + 12);

                int by = y + 20;
                int bh = 10;
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, by, w, bh, bh, bh);
                int bw = Math.max(bh, (int) ((w) * (cant / (double) max)));
                Color c = PALETA[i % PALETA.length];
                g2.setPaint(new GradientPaint(0, 0, c, w, 0, c.brighter()));
                g2.fillRoundRect(0, by, bw, bh, bh, bh);
            }
            g2.dispose();
        }
    }

    /** Gráfico de dona: métodos de pago. Datos: {metodo, cantidad, total}. */
    private static class PanelDona extends JPanel {

        private List<Object[]> datos = new ArrayList<>();

        PanelDona() {
            setOpaque(false);
        }

        void setDatos(List<Object[]> d) {
            this.datos = (d != null) ? d : new ArrayList<Object[]>();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            double total = 0;
            for (Object[] f : datos) {
                total += ((Number) f[2]).doubleValue();
            }

            if (datos.isEmpty() || total <= 0) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(COLOR_GRIS);
                String msg = "Sin ventas registradas este mes";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
                g2.dispose();
                return;
            }

            // Dona a la izquierda
            int d = Math.max(60, Math.min(h - 8, (int) (w * 0.52)));
            int cx = 0;
            int cy = (h - d) / 2;
            double inicio = 90;
            for (int i = 0; i < datos.size(); i++) {
                double v = ((Number) datos.get(i)[2]).doubleValue();
                double ext = -360.0 * (v / total);
                g2.setColor(PALETA[i % PALETA.length]);
                g2.fill(new Arc2D.Double(cx, cy, d, d, inicio, ext, Arc2D.PIE));
                inicio += ext;
            }
            int hueco = (int) (d * 0.62);
            g2.setColor(Color.WHITE);
            g2.fillOval(cx + (d - hueco) / 2, cy + (d - hueco) / 2, hueco, hueco);

            // Texto central
            g2.setColor(COLOR_TEXTO);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            String t = String.format(Locale.US, "S/ %,.0f", total);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(t, cx + (d - fm.stringWidth(t)) / 2, cy + d / 2 + 2);
            g2.setColor(COLOR_GRIS);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            fm = g2.getFontMetrics();
            String s = "Total mes";
            g2.drawString(s, cx + (d - fm.stringWidth(s)) / 2, cy + d / 2 + 16);

            // Leyenda a la derecha
            int lx = d + 16;
            int filaH = 38;
            int ly = Math.max(0, (h - Math.min(datos.size(), 5) * filaH) / 2);
            for (int i = 0; i < Math.min(datos.size(), 5); i++) {
                Object[] f = datos.get(i);
                double v = ((Number) f[2]).doubleValue();
                int y = ly + i * filaH;
                g2.setColor(PALETA[i % PALETA.length]);
                g2.fillRoundRect(lx, y + 3, 10, 10, 4, 4);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                fm = g2.getFontMetrics();
                g2.setColor(COLOR_TEXTO);
                g2.drawString(recortar(bonito(String.valueOf(f[0])), fm, w - lx - 18), lx + 16, y + 13);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(COLOR_GRIS);
                String det = String.format(Locale.US, "%.0f%%  ·  S/ %,.2f", 100.0 * v / total, v);
                fm = g2.getFontMetrics();
                g2.drawString(recortar(det, fm, w - lx - 18), lx + 16, y + 28);
            }
            g2.dispose();
        }
    }

    private static class BotonRapido extends JButton {

        private final Color color;
        private boolean hover = false;

        BotonRapido(String texto, Color color) {
            super(texto);
            this.color = color;
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hover ? color.darker() : color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }
    private class BotonMenu extends JButton {

        private boolean activo = false;
        private boolean hover = false;
        private final Icon icoNormal;
        private final Icon icoGris;
        private final Color fondoBase;
        private final Color fondoHover;

        BotonMenu(String texto, String tipoIcono, Color fondoBase, Color fondoHover) {
            super(texto);
            this.icoNormal = crearIcono(tipoIcono, Color.WHITE);
            this.icoGris = crearIcono(tipoIcono, new Color(71, 85, 105));
            this.fondoBase = fondoBase;
            this.fondoHover = fondoHover;

            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setContentAreaFilled(false);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(226, 44));
            setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setActivo(boolean activo) {
            this.activo = activo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (activo) {
                g2.setPaint(new GradientPaint(0, 0, COLOR_PRIMARIO, w, 0, COLOR_PRIMARIO_OSC));
                g2.fillRoundRect(0, 0, w, h, 14, 14);
                g2.setColor(COLOR_VERDE_CLARO);
                g2.fillRoundRect(0, 10, 4, h - 20, 4, 4);
            } else if (isEnabled() && hover) {
                g2.setColor(fondoHover != null ? fondoHover : COLOR_BTN_HOVER);
                g2.fillRoundRect(0, 0, w, h, 14, 14);
            } else if (isEnabled() && fondoBase != null) {
                g2.setColor(fondoBase);
                g2.fillRoundRect(0, 0, w, h, 14, 14);
            }

            Icon ic = isEnabled() ? icoNormal : icoGris;
            int x = 16;
            ic.paintIcon(this, g2, x, (h - ic.getIconHeight()) / 2);

            g2.setFont(getFont());
            g2.setColor(isEnabled() ? Color.WHITE : new Color(100, 116, 139));
            FontMetrics fm = g2.getFontMetrics();
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x + ic.getIconWidth() + 12, ty);

            g2.dispose();
        }
    }

    private static class PanelDegradado extends JPanel {

        private final Color c1;
        private final Color c2;

        PanelDegradado(Color c1, Color c2) {
            this.c1 = c1;
            this.c2 = c2;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), 0, c2));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
    private static class Pastilla extends JLabel {

        Pastilla(String texto) {
            super(texto);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setBorder(new EmptyBorder(7, 16, 7, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 40));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g2.setColor(new Color(255, 255, 255, 100));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private Icon crearInsignia() {
        final Icon tienda = crearIcono("tienda", Color.WHITE);
        return new Icon() {
            @Override
            public int getIconWidth() {
                return 42;
            }

            @Override
            public int getIconHeight() {
                return 42;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(x, y, new Color(34, 197, 94), x + 42, y + 42, COLOR_PRIMARIO_OSC));
                g2.fillRoundRect(x, y, 42, 42, 14, 14);
                tienda.paintIcon(c, g2, x + 12, y + 12);
                g2.dispose();
            }
        };
    }

    private Icon crearAvatar(final String iniciales) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return 38;
            }

            @Override
            public int getIconHeight() {
                return 38;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(x, y, new Color(34, 197, 94), x + 38, y + 38, COLOR_PRIMARIO_OSC));
                g2.fillOval(x, y, 38, 38);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (38 - fm.stringWidth(iniciales)) / 2;
                int ty = y + (38 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(iniciales, tx, ty);
                g2.dispose();
            }
        };
    }

    private Icon crearIcono(final String tipo, final Color color) {
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
                    default:
                        break;
                }
                g2.dispose();
            }
        };
    }
}
