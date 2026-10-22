package dao;

import conexion.ConexionBD;
import modelo.MovimientoInventario;
import modelo.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class InventarioDAO {

    // 1. LISTAR PRODUCTOS CON DATOS DE STOCK
    public List<Producto> listarStock() {

        List<Producto> lista = new ArrayList<>();

        String sql = "SELECT "
                + "p.id_producto, "
                + "p.nombre, "
                + "c.nombre AS categoria, "
                + "p.stock, "
                + "p.stock_minimo, "
                + "p.unidad_medida, "
                + "p.precio_venta "
                + "FROM productos p "
                + "INNER JOIN categorias c "
                + "ON p.id_categoria = c.id_categoria "
                + "ORDER BY p.id_producto";

        try (
            Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                Producto p = new Producto();

                p.setIdProducto(
                        rs.getInt("id_producto")
                );

                p.setNombre(
                        rs.getString("nombre")
                );

                p.setCategoriaNombre(
                        rs.getString("categoria")
                );

                p.setStock(
                        rs.getInt("stock")
                );

                p.setStockMinimo(
                        rs.getInt("stock_minimo")
                );

                p.setUnidadMedida(
                        rs.getString("unidad_medida")
                );

                p.setPrecioVenta(
                        rs.getDouble("precio_venta")
                );

                lista.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }


    // 2. LISTAR HISTORIAL DE MOVIMIENTOS
    public List<MovimientoInventario> listarMovimientos() {

        List<MovimientoInventario> lista =
                new ArrayList<>();

        String sql = "SELECT "
                + "m.id_movimiento, "
                + "m.id_producto, "
                + "m.id_usuario, "
                + "m.tipo::text AS tipo, "
                + "m.cantidad, "
                + "m.stock_anterior, "
                + "m.stock_posterior, "
                + "m.motivo, "
                + "m.fecha_hora, "
                + "p.nombre AS producto, "
                + "u.nombre_completo AS usuario "
                + "FROM movimientos_inventario m "
                + "INNER JOIN productos p "
                + "ON m.id_producto = p.id_producto "
                + "INNER JOIN usuarios u "
                + "ON m.id_usuario = u.id_usuario "
                + "ORDER BY m.fecha_hora DESC";

        try (
            Connection con = ConexionBD.getConexion();
            PreparedStatement ps =
                    con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                MovimientoInventario m =
                        new MovimientoInventario();

                m.setIdMovimiento(
                        rs.getInt("id_movimiento")
                );

                m.setIdProducto(
                        rs.getInt("id_producto")
                );

                m.setIdUsuario(
                        rs.getInt("id_usuario")
                );

                m.setTipo(
                        rs.getString("tipo")
                );

                m.setCantidad(
                        rs.getInt("cantidad")
                );

                m.setStockAnterior(
                        rs.getInt("stock_anterior")
                );

                m.setStockPosterior(
                        rs.getInt("stock_posterior")
                );

                m.setMotivo(
                        rs.getString("motivo")
                );

                m.setFechaHora(
                        rs.getTimestamp("fecha_hora")
                );

                m.setProductoNombre(
                        rs.getString("producto")
                );

                m.setUsuarioNombre(
                        rs.getString("usuario")
                );

                lista.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }


    // 3. TOTAL DE UNIDADES EN STOCK
    public int obtenerStockTotal() {

        String sql =
                "SELECT COALESCE(SUM(stock),0) AS total "
                + "FROM productos";

        try (
            Connection con = ConexionBD.getConexion();
            PreparedStatement ps =
                    con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }


    // 4. CONTAR PRODUCTOS CON STOCK BAJO
    public int contarStockBajo() {

        String sql =
                "SELECT COUNT(*) AS total "
                + "FROM productos "
                + "WHERE stock <= stock_minimo";

        try (
            Connection con = ConexionBD.getConexion();
            PreparedStatement ps =
                    con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }


    // 5. VALOR ESTIMADO DEL INVENTARIO
    public double obtenerValorInventario() {

        String sql =
                "SELECT COALESCE("
                + "SUM(stock * precio_venta), 0"
                + ") AS total "
                + "FROM productos";

        try (
            Connection con = ConexionBD.getConexion();
            PreparedStatement ps =
                    con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }


    // 6. INGRESAR STOCK
    public boolean ingresarStock(
            int idProducto,
            int idUsuario,
            int cantidad,
            String motivo
    ) {

        String sqlConsultar =
                "SELECT stock "
                + "FROM productos "
                + "WHERE id_producto = ?";

        String sqlActualizar =
                "UPDATE productos "
                + "SET stock = stock + ? "
                + "WHERE id_producto = ?";

        String sqlMovimiento =
                "INSERT INTO movimientos_inventario "
                + "(id_producto, id_usuario, tipo, cantidad, "
                + "stock_anterior, stock_posterior, motivo) "
                + "VALUES (?, ?, 'Entrada'::tipo_movimiento, ?, ?, ?, ?)";

        Connection con = null;

        try {

            con = ConexionBD.getConexion();
            con.setAutoCommit(false);

            int stockAnterior = 0;

            // Consultar stock actual
            try (
                PreparedStatement ps =
                        con.prepareStatement(sqlConsultar)
            ) {

                ps.setInt(1, idProducto);

                try (
                    ResultSet rs = ps.executeQuery()
                ) {

                    if (rs.next()) {
                        stockAnterior =
                                rs.getInt("stock");
                    } else {
                        con.rollback();
                        return false;
                    }
                }
            }

            int stockPosterior =
                    stockAnterior + cantidad;

            // Actualizar stock
            try (
                PreparedStatement ps =
                        con.prepareStatement(sqlActualizar)
            ) {

                ps.setInt(1, cantidad);
                ps.setInt(2, idProducto);

                ps.executeUpdate();
            }

            // Registrar movimiento
            try (
                PreparedStatement ps =
                        con.prepareStatement(sqlMovimiento)
            ) {

                ps.setInt(1, idProducto);
                ps.setInt(2, idUsuario);
                ps.setInt(3, cantidad);
                ps.setInt(4, stockAnterior);
                ps.setInt(5, stockPosterior);
                ps.setString(6, motivo);

                ps.executeUpdate();
            }

            con.commit();

            return true;

        } catch (SQLException e) {

            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            e.printStackTrace();

            return false;

        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}