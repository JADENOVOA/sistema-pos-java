package dao;

import conexion.ConexionBD;
import modelo.Caja;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class CajaDAO {

    // 1. ABRIR CAJA
    public int abrirCaja(Caja caja) {

        String sql = "INSERT INTO cajas "
                + "(id_usuario, fecha_apertura, monto_inicial, estado) "
                + "VALUES (?, CURRENT_TIMESTAMP, ?, 'Abierta')";

        try (
                Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {

            ps.setInt(1, caja.getIdUsuario());
            ps.setDouble(2, caja.getMontoInicial());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al abrir caja: " + e.getMessage());
        }

        return 0;
    }

    // 2. BUSCAR CAJA ABIERTA DEL USUARIO
    public Caja buscarCajaAbierta(int idUsuario) {
        System.out.println("=== BUSQUEDA DE CAJA ===");
System.out.println("ID USUARIO RECIBIDO: " + idUsuario);

        System.out.println("BUSCANDO CAJA PARA USUARIO: " + idUsuario);

       String sql = "SELECT * FROM cajas "
        + "WHERE id_usuario = ? "
        + "AND estado::text = 'Abierta' "
        + "ORDER BY id_caja DESC "
        + "LIMIT 1";
        try (
                Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    System.out.println("CAJA ABIERTA ENCONTRADA: " + rs.getInt("id_caja"));

                    Caja caja = new Caja();

                    caja.setIdCaja(rs.getInt("id_caja"));
                    caja.setIdUsuario(rs.getInt("id_usuario"));
                    caja.setFechaApertura(rs.getTimestamp("fecha_apertura"));
                    caja.setMontoInicial(rs.getDouble("monto_inicial"));
                    caja.setEstado(rs.getString("estado"));
                    caja.setObservacion(rs.getString("observacion"));

                    return caja;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(
                    "Error al buscar caja abierta: " + e.getMessage()
            );
        }
        System.out.println("NO SE ENCONTRO CAJA ABIERTA PARA EL USUARIO: " + idUsuario);

        return null;
    }

    // 3. TOTAL DE VENTAS EN EFECTIVO
    public double obtenerTotalEfectivo(int idCaja) {

        String sql = "SELECT COALESCE(SUM(total), 0) AS total "
                + "FROM ventas "
                + "WHERE id_caja = ? "
                + "AND metodo_pago::text = 'Efectivo'";

        try (
                Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCaja);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    // 4. TOTAL DE VENTAS DIGITALES
    public double obtenerTotalDigital(int idCaja) {

        String sql = "SELECT COALESCE(SUM(total), 0) AS total "
                + "FROM ventas "
                + "WHERE id_caja = ? "
                + "AND metodo_pago::text <> 'Efectivo'";

        try (
                Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCaja);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    // 5. TOTAL GENERAL DE VENTAS
    public double obtenerTotalVentas(int idCaja) {

        String sql = "SELECT COALESCE(SUM(total), 0) AS total "
                + "FROM ventas "
                + "WHERE id_caja = ?";

        try (
                Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCaja);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    // 6. CERRAR CAJA
    public boolean cerrarCaja(
            int idCaja,
            double montoCierreReal,
            double totalEfectivo,
            double totalBilletera,
            String observacion
    ) {

        String sql = "UPDATE cajas "
                + "SET fecha_cierre = CURRENT_TIMESTAMP, "
                + "monto_cierre_real = ?, "
                + "total_efectivo = ?, "
                + "total_billetera = ?, "
                + "estado = 'Cerrada', "
                + "observacion = ? "
                + "WHERE id_caja = ?";

        try (
                Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, montoCierreReal);
            ps.setDouble(2, totalEfectivo);
            ps.setDouble(3, totalBilletera);
            ps.setString(4, observacion);
            ps.setInt(5, idCaja);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(
                    "Error al cerrar caja: " + e.getMessage()
            );
        }

        return false;
    }

    public void cargarVentasCaja(int idCaja, DefaultTableModel modelo) {

        String sql = "SELECT "
                + "v.numero_ticket, "
                + "TO_CHAR(v.fecha_hora, 'HH24:MI') AS hora, "
                + "v.cliente, "
                + "v.metodo_pago::text AS metodo_pago, "
                + "v.total, "
                + "u.nombre_completo "
                + "FROM ventas v "
                + "INNER JOIN usuarios u ON v.id_usuario = u.id_usuario "
                + "WHERE v.id_caja = ? "
                + "ORDER BY v.fecha_hora DESC";

        modelo.setRowCount(0);

        try (
                Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCaja);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    modelo.addRow(new Object[]{
                        rs.getString("numero_ticket"),
                        rs.getString("hora"),
                        rs.getString("cliente"),
                        rs.getString("metodo_pago"),
                        String.format("S/. %.2f", rs.getDouble("total")),
                        rs.getString("nombre_completo")
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
