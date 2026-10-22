package dao;

import conexion.ConexionBD;
import modelo.DetalleVenta;
import modelo.Venta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VentaDAO {

    // Registra la venta y descuenta stock en una sola transacción atómica (RF01 y RF02)
    public boolean registrarVenta(Venta venta) {
        String sqlVenta = "INSERT INTO ventas (numero_ticket, id_caja, id_usuario, cliente, subtotal, igv, total, metodo_pago, monto_recibido, vuelto) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?::metodo_pago_tipo, ?, ?)";

        String sqlDetalle = "INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?)";

        String sqlStock = "UPDATE productos SET stock = stock - ? WHERE id_producto = ?";

        String sqlMovimiento = "INSERT INTO movimientos_inventario (id_producto, id_usuario, tipo, cantidad, stock_anterior, stock_posterior, motivo) "
                + "VALUES (?, ?, 'Venta'::tipo_movimiento, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false); // INICIA LA TRANSACCIÓN

            int idVentaGenerada = 0;
            try (PreparedStatement psVenta = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                psVenta.setString(1, venta.getNumeroTicket());
                psVenta.setInt(2, venta.getIdCaja());
                psVenta.setInt(3, venta.getIdUsuario());
                psVenta.setString(4, venta.getCliente());
                psVenta.setDouble(5, venta.getSubtotal());
                psVenta.setDouble(6, venta.getIgv());
                psVenta.setDouble(7, venta.getTotal());
                psVenta.setString(8, venta.getMetodoPago());
                psVenta.setDouble(9, venta.getMontoRecibido());
                psVenta.setDouble(10, venta.getVuelto());
                psVenta.executeUpdate();

                try (ResultSet rs = psVenta.getGeneratedKeys()) {
                    if (rs.next()) {
                        idVentaGenerada = rs.getInt(1);
                    }
                }
            }

            // Insertar detalles y actualizar stock
            String sqlConsultarStock
                    = "SELECT stock FROM productos WHERE id_producto = ?";

            try (
                    PreparedStatement psDetalle = con.prepareStatement(sqlDetalle); PreparedStatement psStock = con.prepareStatement(sqlStock); PreparedStatement psConsultarStock = con.prepareStatement(sqlConsultarStock); PreparedStatement psMovimiento = con.prepareStatement(sqlMovimiento)) {

                for (DetalleVenta item : venta.getDetalles()) {

                    // 1. Consultar stock actual
                    psConsultarStock.setInt(1, item.getIdProducto());

                    int stockAnterior = 0;

                    try (ResultSet rs = psConsultarStock.executeQuery()) {
                        if (rs.next()) {
                            stockAnterior = rs.getInt("stock");
                        }
                    }

                    int stockPosterior = stockAnterior - item.getCantidad();

                    // 2. Registrar detalle de venta
                    psDetalle.setInt(1, idVentaGenerada);
                    psDetalle.setInt(2, item.getIdProducto());
                    psDetalle.setInt(3, item.getCantidad());
                    psDetalle.setDouble(4, item.getPrecioUnitario());
                    psDetalle.setDouble(5, item.getSubtotal());
                    psDetalle.executeUpdate();

                    // 3. Descontar stock
                    psStock.setInt(1, item.getCantidad());
                    psStock.setInt(2, item.getIdProducto());
                    psStock.executeUpdate();

                    // 4. Registrar movimiento de inventario
                    psMovimiento.setInt(1, item.getIdProducto());
                    psMovimiento.setInt(2, venta.getIdUsuario());
                    psMovimiento.setInt(3, item.getCantidad());
                    psMovimiento.setInt(4, stockAnterior);
                    psMovimiento.setInt(5, stockPosterior);
                    psMovimiento.setString(
                            6,
                            "Venta " + venta.getNumeroTicket()
                    );

                                       psMovimiento.executeUpdate();
                }
                     }

            con.commit(); // CONFIRMA LA TRANSACCIÓN
            return true;  // INDICA QUE TODO SALIÓ BIEN

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en rollback: " + ex.getMessage());
                }
            }

            e.printStackTrace();
            System.err.println("Error al procesar la venta: " + e.getMessage());
            return false;

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }
}