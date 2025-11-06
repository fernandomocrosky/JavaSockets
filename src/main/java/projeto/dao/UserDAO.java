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

    public static User findById(String id) {
        String sql = "SELECT usuario, senha FROM usuarios WHERE id = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(id));

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
        String sql = "UPDATE usuarios SET senha = ? WHERE id = ?";

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getSenha());
            stmt.setInt(2, Integer.parseInt(user.getId()));
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception ex) {
            System.err.println("Erro ao atualizar usuário: " + ex.getMessage());
            return false;
        }
    }

    public static boolean delete(String id) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            
            // 1. Obter os IDs dos filmes que têm reviews deste usuário (antes de deletar)
            String sqlObterFilmes = """
                SELECT DISTINCT id_filme 
                FROM filmes_reviews 
                WHERE id_usuario = ?
                """;
            
            List<Integer> filmesIds = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlObterFilmes)) {
                stmt.setInt(1, Integer.parseInt(id));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        filmesIds.add(rs.getInt("id_filme"));
                    }
                }
            }
            
            // 2. Deletar o usuário (isso vai deletar as reviews automaticamente por causa do ON DELETE CASCADE)
            String sqlDelete = "DELETE FROM usuarios WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDelete)) {
                stmt.setInt(1, Integer.parseInt(id));
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }
            }
            
            // 3. Atualizar a nota média de cada filme afetado
            String sqlAtualizarNota = """
                UPDATE filmes
                SET nota = (
                    SELECT CASE 
                        WHEN COUNT(*) > 0 THEN CAST(AVG(CAST(nota AS REAL)) AS TEXT)
                        ELSE '0.0'
                    END
                    FROM filmes_reviews 
                    WHERE id_filme = ?
                )
                WHERE id = ?
                """;
            
            for (Integer filmeId : filmesIds) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlAtualizarNota)) {
                    stmt.setInt(1, filmeId);
                    stmt.setInt(2, filmeId);
                    stmt.executeUpdate();
                }
            }
            
            conn.commit();
            return true;
        } catch (Exception ex) {
            System.err.println("Erro ao excluir usuário: " + ex.getMessage());
            ex.printStackTrace();
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
