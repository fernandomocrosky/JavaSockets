package projeto.dao;

import projeto.Database;
import projeto.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

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
