package projeto.dao;

import projeto.Database;
import projeto.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static User findByUsernameAndPassword(String username, String password) {
        String sql = "SELECT id ,usuario, senha FROM usuarios WHERE usuario = ? AND senha = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsuario(rs.getString("usuario"));
                user.setRole(rs.getString("usuario").equals("admin") ? "admin" : "user");
                
                return user;
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

    public static List<User> findAll() {
        String sql = "SELECT id, usuario FROM usuarios";

        List<User> usuarios = new ArrayList<>();

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsuario(rs.getString("usuario"));

                usuarios.add(user);
            }
        } catch (Exception ex) {
            System.err.println("Erro ao buscar usuários: " + ex.getMessage());
        }
        return usuarios;
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

    public static boolean update(User user) {
        String sql = "UPDATE usuarios SET usuario = ? WHERE id = ?";

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsuario());
            stmt.setInt(2, Integer.parseInt(user.getId()));
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception ex) {
            System.err.println("Erro ao atualizar usuário: " + ex.getMessage());
            return false;
        }
    }

    public static boolean delete(String id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(id));
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception ex) {
            System.err.println("Erro ao excluir usuário: " + ex.getMessage());
            return false;
        }
    }

    public static void addTokenToBlacklist(String token, long expirationMillis) {
        String sql = "INSERT INTO token_blacklist (token, expiracao) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            stmt.setTimestamp(2, new Timestamp(expirationMillis));
            stmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("Erro ao adicionar token na blacklist: " + e.getMessage());
        }
    }

    public static boolean isTokenBlacklisted(String token) {
        String sql = "SELECT 1 FROM token_blacklist WHERE token = ? AND expiracao > ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            stmt.setTimestamp(2, Timestamp.from(Instant.now()));

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true se encontrou

        } catch (Exception e) {
            System.err.println("Erro ao verificar token na blacklist: " + e.getMessage());
            return false;
        }
    }

    public static void cleanupExpiredTokens() {
        String sql = "DELETE FROM token_blacklist WHERE expiracao <= ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.from(Instant.now()));
            stmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("Erro ao limpar tokens expirados: " + e.getMessage());
        }
    }

}
