package dao;

import conexion.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    // ============================================================
    // CONDICIÓN SEGÚN EL PERIODO
    // ============================================================

    private String condicionFecha(String periodo) {

        switch (periodo) {

            case "SEMANA":
                return "DATE(v.fecha_hora) >= DATE_TRUNC('week', CURRENT_DATE) ";

            case "MES":
                return "DATE(v.fecha_hora) >= DATE_TRUNC('month', CURRENT_DATE) ";

            case "HOY":
            default:
                return "DATE(v.fecha_hora) = CURRENT_DATE ";
        }
    }

    // ============================================================
    // 1. INGRESOS TOTALES
    // ============================================================

    public double obtenerIngresos(String periodo) {

        String sql =
                "SELECT COALESCE(SUM(v.total), 0) AS total "
                + "FROM ventas v "
                + "WHERE "
                + condicionFecha(periodo);

        try (
            Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
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

    // ============================================================
    // 2. CANTIDAD DE VENTAS
    // ============================================================

    public int obtenerCantidadVentas(String periodo) {

        String sql =
                "SELECT COUNT(*) AS total "
                + "FROM ventas v "
                + "WHERE "
                + condicionFecha(periodo);

        try (
            Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
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

    // ============================================================
    // 3. TICKET PROMEDIO
    // ============================================================

    public double obtenerTicketPromedio(String periodo) {

        String sql =
                "SELECT COALESCE(AVG(v.total), 0) AS promedio "
                + "FROM ventas v "
                + "WHERE "
                + condicionFecha(periodo);

        try (
            Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getDouble("promedio");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    // ============================================================
    // 4. PRODUCTOS MÁS VENDIDOS
    // ============================================================

    public List<Object[]> listarProductosMasVendidos(String periodo) {

        List<Object[]> lista = new ArrayList<>();

        String sql =
                "SELECT "
                + "p.nombre AS producto, "
                + "c.nombre AS categoria, "
                + "SUM(d.cantidad) AS cantidad, "
                + "SUM(d.subtotal) AS recaudado "
                + "FROM detalle_ventas d "
                + "INNER JOIN ventas v "
                + "ON d.id_venta = v.id_venta "
                + "INNER JOIN productos p "
                + "ON d.id_producto = p.id_producto "
                + "INNER JOIN categorias c "
                + "ON p.id_categoria = c.id_categoria "
                + "WHERE "
                + condicionFecha(periodo)
                + "GROUP BY p.id_producto, p.nombre, c.nombre "
                + "ORDER BY cantidad DESC, recaudado DESC";

        try (
            Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                lista.add(new Object[]{
                    rs.getString("producto"),
                    rs.getString("categoria"),
                    rs.getInt("cantidad"),
                    rs.getDouble("recaudado")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // ============================================================
    // 5. TOTALES POR MÉTODO DE PAGO
    // ============================================================

    public List<Object[]> listarMetodosPago(String periodo) {

        List<Object[]> lista = new ArrayList<>();

        String sql =
                "SELECT "
                + "v.metodo_pago::text AS metodo, "
                + "COUNT(*) AS cantidad, "
                + "COALESCE(SUM(v.total), 0) AS total "
                + "FROM ventas v "
                + "WHERE "
                + condicionFecha(periodo)
                + "GROUP BY v.metodo_pago "
                + "ORDER BY total DESC";

        try (
            Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                lista.add(new Object[]{
                    rs.getString("metodo"),
                    rs.getInt("cantidad"),
                    rs.getDouble("total")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
}