package projeto.dao;

import projeto.Database;
import projeto.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public static User findByUsernameAndPassword(String username, String password) {
        String sql = "SELECT usuario, senha FROM usuarios WHERE usuario = ? AND senha = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("usuario"),
                        rs.getString("senha"));
            }

        } catch (Exception e) {
            System.err.println("Erro ao buscar usuário: " + e.getMessage());
        }

        return null;
    }

    public static User findByUsername(String username) {
        String sql = "SELECT usuario, senha FROM usuarios WHERE usuario = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("usuario"),
                        null);
            }
        } catch (Exception ex) {
            System.err.println("Erro ao buscar usuário: " + ex.getMessage());
        }
        
        return null;
    }

    public static boolean insert(User user) {
        String sql = "INSERT INTO usuarios (usuario, senha) VALUES (?, ?)";

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsuario());
            stmt.setString(2, user.getSenha());
            stmt.executeUpdate();
            return true;
        } catch (Exception ex) {
            System.err.println("Erro ao inserir usuário: " + ex.getMessage());
            return false;
        }
    }

}
