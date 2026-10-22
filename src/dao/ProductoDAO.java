package dao;

import conexion.ConexionBD;
import modelo.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // 1. LISTAR TODOS
    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.id_producto, p.nombre, p.id_categoria, c.nombre AS categoria, "
                   + "p.precio_costo, p.precio_venta, p.stock, p.stock_minimo, p.unidad_medida "
                   + "FROM productos p "
                   + "INNER JOIN categorias c ON p.id_categoria = c.id_categoria "
                   + "WHERE p.estado = 'Activo' "
                   + "ORDER BY p.id_producto ASC";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setNombre(rs.getString("nombre"));
                p.setIdCategoria(rs.getInt("id_categoria"));
                p.setCategoriaNombre(rs.getString("categoria"));
                p.setPrecioCosto(rs.getDouble("precio_costo"));
                p.setPrecioVenta(rs.getDouble("precio_venta"));
                p.setStock(rs.getInt("stock"));
                p.setStockMinimo(rs.getInt("stock_minimo"));
                p.setUnidadMedida(rs.getString("unidad_medida"));
                lista.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar: " + e.getMessage());
        }
        return lista;
    }

// 2. INSERTAR (REGISTRAR)
public boolean insertar(Producto p) {

    String sql = "INSERT INTO productos "
            + "(nombre, id_categoria, precio_costo, precio_venta, "
            + "stock, stock_minimo, unidad_medida, id_unidad_medida, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, "
            + "(SELECT id_unidad_medida FROM unidades_medida "
            + " WHERE LOWER(abreviatura) = LOWER(?)), "
            + "'Activo')";

    try (
        Connection con = ConexionBD.getConexion();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {

        ps.setString(1, p.getNombre());
        ps.setInt(2, p.getIdCategoria());
        ps.setDouble(3, p.getPrecioCosto());
        ps.setDouble(4, p.getPrecioVenta());
        ps.setInt(5, p.getStock());
        ps.setInt(6, p.getStockMinimo());

        // columna antigua
        ps.setString(7, p.getUnidadMedida());

        // para buscar id_unidad_medida
        ps.setString(8, p.getUnidadMedida());

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println("Error al insertar: " + e.getMessage());
        return false;
    }
}

   // 3. ACTUALIZAR (MODIFICAR)
public boolean actualizar(Producto p) {

    String sql = "UPDATE productos SET "
            + "nombre = ?, "
            + "id_categoria = ?, "
            + "precio_costo = ?, "
            + "precio_venta = ?, "
            + "stock = ?, "
            + "stock_minimo = ?, "
            + "unidad_medida = ?, "
            + "id_unidad_medida = ("
            + "SELECT id_unidad_medida "
            + "FROM unidades_medida "
            + "WHERE LOWER(abreviatura) = LOWER(?)"
            + ") "
            + "WHERE id_producto = ?";

    try (
        Connection con = ConexionBD.getConexion();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {

        ps.setString(1, p.getNombre());
        ps.setInt(2, p.getIdCategoria());
        ps.setDouble(3, p.getPrecioCosto());
        ps.setDouble(4, p.getPrecioVenta());
        ps.setInt(5, p.getStock());
        ps.setInt(6, p.getStockMinimo());

        ps.setString(7, p.getUnidadMedida());

        // buscar unidad
        ps.setString(8, p.getUnidadMedida());

        ps.setInt(9, p.getIdProducto());

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println("Error al actualizar: " + e.getMessage());
        return false;
    }
}

    // 4. ELIMINAR (Lógico: cambia estado a 'Inactivo')
    public boolean eliminar(int idProducto) {
        String sql = "UPDATE productos SET estado = 'Inactivo' WHERE id_producto = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar: " + e.getMessage());
            return false;
        }
    }
}