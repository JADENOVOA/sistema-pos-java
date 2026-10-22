
package vistas;

import dao.UsuarioDAO;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FrmLogin extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;

    private final Color VERDE = new Color(21, 128, 61);
    private final Color VERDE_OSCURO = new Color(22, 101, 52);
    private final Color FONDO = new Color(241, 245, 249);
    private final Color TEXTO = new Color(30, 41, 59);
    private final Color GRIS = new Color(100, 116, 139);
    private final Color BORDE = new Color(203, 213, 225);

    public FrmLogin() {

        setTitle("Bodega La Peruanita - Acceso al Sistema POS");
        setSize(460, 490);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout());

        crearInterfaz();
        configurarEventos();
    }

    private void crearInterfaz() {

        // ==============================
        // ENCABEZADO
        // ==============================

        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(
                new BoxLayout(pnlHeader, BoxLayout.Y_AXIS)
        );

        pnlHeader.setBackground(VERDE);
        pnlHeader.setBorder(
                new EmptyBorder(22, 20, 22, 20)
        );

        JLabel lblIcono = new JLabel("▣");
        lblIcono.setFont(
                new Font("Segoe UI Symbol", Font.PLAIN, 27)
        );
        lblIcono.setForeground(Color.WHITE);
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel(
                "BODEGA LA PERUANITA"
        );

        lblTitulo.setFont(
                new Font("Segoe UI", Font.BOLD, 22)
        );
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel(
                "SISTEMA DE PUNTO DE VENTA"
        );

        lblSubtitulo.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );
        lblSubtitulo.setForeground(
                new Color(220, 252, 231)
        );
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlHeader.add(lblIcono);
        pnlHeader.add(Box.createVerticalStrut(5));
        pnlHeader.add(lblTitulo);
        pnlHeader.add(Box.createVerticalStrut(5));
        pnlHeader.add(lblSubtitulo);

        add(pnlHeader, BorderLayout.NORTH);

        // ==============================
        // FORMULARIO
        // ==============================

        JPanel pnlContenedor = new JPanel(
                new GridBagLayout()
        );

        pnlContenedor.setBackground(FONDO);
        pnlContenedor.setBorder(
                new EmptyBorder(18, 35, 20, 35)
        );

        JPanel pnlFormulario = new JPanel(
                new GridBagLayout()
        );

        pnlFormulario.setBackground(Color.WHITE);
        pnlFormulario.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDE, 1, true),
                        new EmptyBorder(22, 24, 22, 24)
                )
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 0, 4, 0);

        // Título del formulario
        JLabel lblBienvenida = new JLabel(
                "¡Bienvenido!"
        );

        lblBienvenida.setFont(
                new Font("Segoe UI", Font.BOLD, 21)
        );
        lblBienvenida.setForeground(TEXTO);

        gbc.gridy = 0;
        pnlFormulario.add(lblBienvenida, gbc);

        JLabel lblIndicacion = new JLabel(
                "Ingresa tus credenciales para continuar."
        );

        lblIndicacion.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );
        lblIndicacion.setForeground(GRIS);

        gbc.gridy = 1;
        gbc.insets = new Insets(2, 0, 18, 0);
        pnlFormulario.add(lblIndicacion, gbc);

        // Etiqueta usuario
        JLabel lblUsuario = new JLabel("Usuario");

        lblUsuario.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        lblUsuario.setForeground(TEXTO);

        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 6, 0);
        pnlFormulario.add(lblUsuario, gbc);

        // Campo usuario
        txtUsuario = new JTextField();
        txtUsuario.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );
        txtUsuario.setForeground(TEXTO);
        txtUsuario.setBackground(Color.WHITE);
        txtUsuario.setCaretColor(VERDE);
        txtUsuario.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDE, 1, true),
                        new EmptyBorder(10, 12, 10, 12)
                )
        );
        txtUsuario.setPreferredSize(
                new Dimension(280, 43)
        );

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        pnlFormulario.add(txtUsuario, gbc);

        // Etiqueta contraseña
        JLabel lblPassword = new JLabel("Contraseña");

        lblPassword.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        lblPassword.setForeground(TEXTO);

        gbc.gridy = 4;
        gbc.insets = new Insets(4, 0, 6, 0);
        pnlFormulario.add(lblPassword, gbc);

        // Campo contraseña
        txtPassword = new JPasswordField();
        txtPassword.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );
        txtPassword.setForeground(TEXTO);
        txtPassword.setBackground(Color.WHITE);
        txtPassword.setCaretColor(VERDE);
        txtPassword.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDE, 1, true),
                        new EmptyBorder(10, 12, 10, 12)
                )
        );
        txtPassword.setPreferredSize(
                new Dimension(280, 43)
        );

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 20, 0);
        pnlFormulario.add(txtPassword, gbc);

        // Botón ingresar
        btnIngresar = new JButton("INICIAR SESIÓN");

        btnIngresar.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setBackground(VERDE);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setOpaque(true);
        btnIngresar.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );
        btnIngresar.setPreferredSize(
                new Dimension(280, 45)
        );

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 8, 0);
        pnlFormulario.add(btnIngresar, gbc);

        JLabel lblSeguridad = new JLabel(
                "Acceso exclusivo para usuarios autorizados"
        );

        lblSeguridad.setFont(
                new Font("Segoe UI", Font.PLAIN, 10)
        );
        lblSeguridad.setForeground(GRIS);
        lblSeguridad.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        gbc.gridy = 7;
        gbc.insets = new Insets(8, 0, 0, 0);
        pnlFormulario.add(lblSeguridad, gbc);

        pnlContenedor.add(pnlFormulario);

        add(pnlContenedor, BorderLayout.CENTER);

        // ==============================
        // PIE DE VENTANA
        // ==============================

        JPanel pnlFooter = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 5, 8)
        );

        pnlFooter.setBackground(FONDO);

        JLabel lblFooter = new JLabel(
                "Bodega La Peruanita  |  Sistema POS"
        );

        lblFooter.setFont(
                new Font("Segoe UI", Font.PLAIN, 10)
        );
        lblFooter.setForeground(GRIS);

        pnlFooter.add(lblFooter);

        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void configurarEventos() {

        // Efecto al pasar el cursor
        btnIngresar.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                btnIngresar.setBackground(VERDE_OSCURO);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnIngresar.setBackground(VERDE);
            }
        });

        // Iniciar sesión al hacer clic
        btnIngresar.addActionListener(
                (ActionEvent e) -> iniciarSesion()
        );

        // También permite iniciar sesión con Enter
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void iniciarSesion() {

        String usuario = txtUsuario.getText().trim();

        String password = new String(
                txtPassword.getPassword()
        ).trim();

        if (usuario.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, completa el usuario y la contraseña.",
                    "Campos obligatorios",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        btnIngresar.setEnabled(false);
        btnIngresar.setText("VERIFICANDO...");

        try {

            UsuarioDAO dao = new UsuarioDAO();

            Usuario u = dao.autenticar(
                    usuario,
                    password
            );

            if (u != null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Bienvenido(a) "
                        + u.getNombreCompleto()
                        + "\nRol: "
                        + u.getRol(),
                        "Ingreso correcto",
                        JOptionPane.INFORMATION_MESSAGE
                );

                new FrmPrincipal(u).setVisible(true);
                dispose();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Usuario o contraseña incorrectos.",
                        "Acceso denegado",
                        JOptionPane.ERROR_MESSAGE
                );

                txtPassword.setText("");
                txtPassword.requestFocus();
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo iniciar sesión.\n"
                    + "Detalle: " + ex.getMessage(),
                    "Error de acceso",
                    JOptionPane.ERROR_MESSAGE
            );

        } finally {

            if (isDisplayable()) {
                btnIngresar.setEnabled(true);
                btnIngresar.setText("INICIAR SESIÓN");
            }
        }
    }

    // MÉTODO MAIN
    public static void main(String args[]) {

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception ignored) {
        }

        EventQueue.invokeLater(() -> {
            new FrmLogin().setVisible(true);
        });
    }
}
