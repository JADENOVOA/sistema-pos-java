package dao;

import conexion.ConexionBD;
import modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

public Usuario autenticar(String usuario, String password) {

    Usuario u = null;

    String sql = "SELECT "
            + "id_usuario, "
            + "nombre_completo, "
            + "usuario, "
            + "password, "
            + "rol::text AS rol, "
            + "estado::text AS estado "
            + "FROM usuarios "
            + "WHERE usuario = ? "
            + "AND password = ? "
            + "AND estado::text = 'Activo'";

    Connection con = ConexionBD.getConexion();

    if (con == null) {
        JOptionPane.showMessageDialog(
                null,
                "No se pudo conectar a PostgreSQL.",
                "Error de conexión",
                JOptionPane.ERROR_MESSAGE
        );

        return null;
    }

    try (PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, usuario.trim());
        ps.setString(2, password);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                u = new Usuario();

                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setUsuario(rs.getString("usuario"));
                u.setPassword(rs.getString("password"));
                u.setRol(rs.getString("rol"));
                u.setEstado(rs.getString("estado"));

                System.out.println(
                        "LOGIN CORRECTO: "
                        + u.getUsuario()
                        + " | "
                        + u.getRol()
                );

            } else {

                System.out.println(
                        "NO SE ENCONTRO EL USUARIO CON ESAS CREDENCIALES"
                );
            }
        }

    } catch (SQLException e) {

        e.printStackTrace();

        System.err.println(
                "Error al ejecutar login: "
                + e.getMessage()
        );

    } finally {

        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException ignored) {
        }
    }

    return u;
}
    
    // LISTAR USUARIOS
public List<Usuario> listarTodos() {

    List<Usuario> lista = new ArrayList<>();

    String sql = "SELECT u.id_usuario, u.nombre_completo, u.usuario, "
            + "u.password, r.nombre AS rol, u.estado::text AS estado "
            + "FROM usuarios u "
            + "INNER JOIN roles r ON u.id_rol = r.id_rol "
            + "ORDER BY u.id_usuario";

    try (
        Connection con = ConexionBD.getConexion();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()
    ) {

        while (rs.next()) {

            Usuario u = new Usuario();

            u.setIdUsuario(rs.getInt("id_usuario"));
            u.setNombreCompleto(rs.getString("nombre_completo"));
            u.setUsuario(rs.getString("usuario"));
            u.setPassword(rs.getString("password"));
            u.setRol(rs.getString("rol"));
            u.setEstado(rs.getString("estado"));

            lista.add(u);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return lista;
}
public boolean insertar(Usuario u) {

    String sql = "INSERT INTO usuarios "
            + "(nombre_completo, usuario, password, rol, estado, id_rol) "
            + "VALUES (?, ?, ?, ?::rol_usuario, ?::estado_general, "
            + "(SELECT id_rol FROM roles WHERE LOWER(nombre) = LOWER(?)))";

    try (
        Connection con = ConexionBD.getConexion();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {

        ps.setString(1, u.getNombreCompleto());
        ps.setString(2, u.getUsuario());
        ps.setString(3, u.getPassword());

        ps.setString(4, u.getRol());

        ps.setString(5, u.getEstado());

        ps.setString(6, u.getRol());

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println("Error al insertar usuario: " + e.getMessage());
        return false;
    }
}
public boolean actualizar(Usuario u) {

    String sql = "UPDATE usuarios SET "
            + "nombre_completo = ?, "
            + "usuario = ?, "
            + "password = ?, "
            + "rol = ?::rol_usuario, "
            + "estado = ?::estado_general, "
            + "id_rol = (SELECT id_rol FROM roles WHERE LOWER(nombre) = LOWER(?)) "
            + "WHERE id_usuario = ?";

    try (
        Connection con = ConexionBD.getConexion();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {

        ps.setString(1, u.getNombreCompleto());
        ps.setString(2, u.getUsuario());
        ps.setString(3, u.getPassword());
        ps.setString(4, u.getRol());
        ps.setString(5, u.getEstado());
        ps.setString(6, u.getRol());
        ps.setInt(7, u.getIdUsuario());

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println("Error al actualizar usuario: " + e.getMessage());
        return false;
    }
}
public boolean cambiarEstado(int idUsuario, String estado) {

    String sql = "UPDATE usuarios "
            + "SET estado = ?::estado_general "
            + "WHERE id_usuario = ?";

    try (
        Connection con = ConexionBD.getConexion();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {

        ps.setString(1, estado);
        ps.setInt(2, idUsuario);

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

public boolean existeUsuario(String usuario) {

    String sql = "SELECT COUNT(*) "
            + "FROM usuarios "
            + "WHERE LOWER(usuario) = LOWER(?)";

    try (
        Connection con = ConexionBD.getConexion();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {

        ps.setString(1, usuario);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return false;
}
}