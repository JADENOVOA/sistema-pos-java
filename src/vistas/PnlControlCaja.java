package vistas;

import dao.CajaDAO;
import modelo.Caja;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PnlControlCaja extends JPanel {

    private Usuario usuario;
    private CajaDAO cajaDAO;
    private Caja cajaAbierta;

    private JLabel lblEstadoCaja;
    private JLabel lblMontoInicial;
    private JLabel lblVentasEfectivo;
    private JLabel lblVentasDigitales;
    private JLabel lblTotalVentas;
    private JLabel lblEfectivoGaveta;

    private JButton btnAbrirCaja;
    private JButton btnCerrarCaja;

    private JTable tblVentasTurno;
    private DefaultTableModel modeloTabla;

    private final Color VERDE = new Color(21, 128, 61);
    private final Color VERDE_OSCURO = new Color(22, 101, 52);
    private final Color FONDO = new Color(241, 245, 249);

    public PnlControlCaja(Usuario usuario) {

        this.usuario = usuario;
        this.cajaDAO = new CajaDAO();

        setLayout(new BorderLayout(15, 15));
        setBackground(FONDO);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        crearInterfaz();

        cargarEstadoCaja();
    }

    private void crearInterfaz() {

        // =========================
        // CABECERA
        // =========================

        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setBackground(Color.WHITE);
        pnlCabecera.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlTitulo = new JPanel();
        pnlTitulo.setLayout(new BoxLayout(pnlTitulo, BoxLayout.Y_AXIS));
        pnlTitulo.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Control de Caja y Arqueo");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lblSubtitulo = new JLabel(
                "Apertura de turno, control de efectivo y cierre de caja."
        );

        lblSubtitulo.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        pnlTitulo.add(lblTitulo);
        pnlTitulo.add(Box.createRigidArea(new Dimension(0, 4)));
        pnlTitulo.add(lblSubtitulo);

        JPanel pnlAcciones = new JPanel(new FlowLayout());
        pnlAcciones.setBackground(Color.WHITE);

        lblEstadoCaja = new JLabel("CAJA CERRADA");
        lblEstadoCaja.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        btnAbrirCaja = new JButton("Abrir Caja");
        btnCerrarCaja = new JButton("Cerrar Caja");

        btnAbrirCaja.addActionListener(e -> abrirCaja());
        btnCerrarCaja.addActionListener(e -> cerrarCaja());

        pnlAcciones.add(lblEstadoCaja);
        pnlAcciones.add(btnAbrirCaja);
        pnlAcciones.add(btnCerrarCaja);

        pnlCabecera.add(pnlTitulo, BorderLayout.WEST);
        pnlCabecera.add(pnlAcciones, BorderLayout.EAST);

        add(pnlCabecera, BorderLayout.NORTH);

        // =========================
        // CONTENIDO CENTRAL
        // =========================

        JPanel pnlCentro = new JPanel(new BorderLayout(0, 15));
        pnlCentro.setBackground(FONDO);

        JPanel pnlTarjetas = new JPanel(
                new GridLayout(1, 5, 12, 0)
        );

        pnlTarjetas.setBackground(FONDO);

        lblMontoInicial = new JLabel("S/. 0.00");
        lblVentasEfectivo = new JLabel("S/. 0.00");
        lblVentasDigitales = new JLabel("S/. 0.00");
        lblTotalVentas = new JLabel("S/. 0.00");
        lblEfectivoGaveta = new JLabel("S/. 0.00");

        pnlTarjetas.add(
                crearTarjeta("MONTO INICIAL", lblMontoInicial)
        );

        pnlTarjetas.add(
                crearTarjeta("VENTAS EFECTIVO", lblVentasEfectivo)
        );

        pnlTarjetas.add(
                crearTarjeta("VENTAS DIGITALES", lblVentasDigitales)
        );

        pnlTarjetas.add(
                crearTarjeta("TOTAL EN VENTAS", lblTotalVentas)
        );

        pnlTarjetas.add(
                crearTarjeta("EFECTIVO EN GAVETA", lblEfectivoGaveta)
        );

        pnlCentro.add(pnlTarjetas, BorderLayout.NORTH);

        // =========================
        // TABLA
        // =========================

        String[] columnas = {
            "Ticket",
            "Hora",
            "Cliente",
            "Método Pago",
            "Total",
            "Cajero"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column
            ) {
                return false;
            }
        };

        tblVentasTurno = new JTable(modeloTabla);
        tblVentasTurno.setRowHeight(30);

        JScrollPane scroll =
                new JScrollPane(tblVentasTurno);

        JPanel pnlTabla = new JPanel(
                new BorderLayout()
        );

        pnlTabla.setBackground(Color.WHITE);
        pnlTabla.setBorder(
                new EmptyBorder(15, 15, 15, 15)
        );

        JLabel lblVentas =
                new JLabel(
                        "Ventas realizadas en el turno actual"
                );

        lblVentas.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        15
                )
        );

        pnlTabla.add(lblVentas, BorderLayout.NORTH);
        pnlTabla.add(scroll, BorderLayout.CENTER);

        pnlCentro.add(pnlTabla, BorderLayout.CENTER);

        add(pnlCentro, BorderLayout.CENTER);
    }

    private JPanel crearTarjeta(
            String titulo,
            JLabel valor
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
                new EmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        JLabel lblTitulo =
                new JLabel(titulo);

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
                        20
                )
        );

        valor.setForeground(VERDE_OSCURO);

        pnl.add(lblTitulo);
        pnl.add(
                Box.createRigidArea(
                        new Dimension(0, 8)
                )
        );
        pnl.add(valor);

        return pnl;
    }

    private void cargarEstadoCaja() {

        cajaAbierta =
                cajaDAO.buscarCajaAbierta(
                        usuario.getIdUsuario()
                );

        if (cajaAbierta == null) {

            lblEstadoCaja.setText(
                    "CAJA CERRADA"
            );

            lblEstadoCaja.setForeground(
                    Color.RED
            );

            btnAbrirCaja.setEnabled(true);
            btnCerrarCaja.setEnabled(false);

            limpiarDatos();

        } else {

            lblEstadoCaja.setText(
                    "CAJA ABIERTA"
            );

            lblEstadoCaja.setForeground(
                    VERDE
            );

            btnAbrirCaja.setEnabled(false);
            btnCerrarCaja.setEnabled(true);

            cargarTotales();
        }
    }

private void cargarTotales() {

    if (cajaAbierta == null) {
        return;
    }

    int idCaja = cajaAbierta.getIdCaja();

    double inicial =
            cajaAbierta.getMontoInicial();

    double efectivo =
            cajaDAO.obtenerTotalEfectivo(idCaja);

    double digital =
            cajaDAO.obtenerTotalDigital(idCaja);

    double total =
            cajaDAO.obtenerTotalVentas(idCaja);

    double gaveta =
            inicial + efectivo;

    lblMontoInicial.setText(
            String.format("S/. %.2f", inicial)
    );

    lblVentasEfectivo.setText(
            String.format("S/. %.2f", efectivo)
    );

    lblVentasDigitales.setText(
            String.format("S/. %.2f", digital)
    );

    lblTotalVentas.setText(
            String.format("S/. %.2f", total)
    );

    lblEfectivoGaveta.setText(
            String.format("S/. %.2f", gaveta)
    );

    cajaDAO.cargarVentasCaja(
            idCaja,
            modeloTabla
    );
}

    private void abrirCaja() {

        String entrada =
                JOptionPane.showInputDialog(
                        this,
                        "Ingrese el monto inicial de caja:",
                        "Abrir Caja",
                        JOptionPane.QUESTION_MESSAGE
                );

        if (entrada == null) {
            return;
        }

        try {

            double montoInicial =
                    Double.parseDouble(
                            entrada.replace(",", ".")
                    );

            if (montoInicial < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "El monto no puede ser negativo."
                );

                return;
            }

            Caja nuevaCaja =
                    new Caja(
                            usuario.getIdUsuario(),
                            montoInicial
                    );

            int idCaja =
                    cajaDAO.abrirCaja(
                            nuevaCaja
                    );

            if (idCaja > 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Caja abierta correctamente."
                );

                cargarEstadoCaja();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo abrir la caja."
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ingrese un monto válido."
            );
        }
    }

    private void cerrarCaja() {

        if (cajaAbierta == null) {
            return;
        }

        double efectivo =
                cajaDAO.obtenerTotalEfectivo(
                        cajaAbierta.getIdCaja()
                );

        double digital =
                cajaDAO.obtenerTotalDigital(
                        cajaAbierta.getIdCaja()
                );

        double esperado =
                cajaAbierta.getMontoInicial()
                + efectivo;

        String entrada =
                JOptionPane.showInputDialog(
                        this,
                        "Efectivo esperado: S/. "
                                + String.format(
                                        "%.2f",
                                        esperado
                                )
                                + "\n\nIngrese el efectivo real:",
                        "Cerrar Caja",
                        JOptionPane.QUESTION_MESSAGE
                );

        if (entrada == null) {
            return;
        }

        try {

            double montoReal =
                    Double.parseDouble(
                            entrada.replace(",", ".")
                    );

            double diferencia =
                    montoReal - esperado;

            String observacion =
                    "Diferencia de caja: S/. "
                    + String.format(
                            "%.2f",
                            diferencia
                    );

            boolean exito =
                    cajaDAO.cerrarCaja(
                            cajaAbierta.getIdCaja(),
                            montoReal,
                            efectivo,
                            digital,
                            observacion
                    );

            if (exito) {

                JOptionPane.showMessageDialog(
                        this,
                        "Caja cerrada correctamente.\n"
                                + "Diferencia: S/. "
                                + String.format(
                                        "%.2f",
                                        diferencia
                                )
                );

                cargarEstadoCaja();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo cerrar la caja."
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ingrese un monto válido."
            );
        }
    }

    private void limpiarDatos() {

        lblMontoInicial.setText("S/. 0.00");
        lblVentasEfectivo.setText("S/. 0.00");
        lblVentasDigitales.setText("S/. 0.00");
        lblTotalVentas.setText("S/. 0.00");
        lblEfectivoGaveta.setText("S/. 0.00");

        modeloTabla.setRowCount(0);
    }
    
    public void refrescarDatos() {

    cajaAbierta = cajaDAO.buscarCajaAbierta(
            usuario.getIdUsuario()
    );

    cargarEstadoCaja();

    if (cajaAbierta != null) {
        cargarTotales();

        cajaDAO.cargarVentasCaja(
                cajaAbierta.getIdCaja(),
                modeloTabla
        );
    }
}
}