package projeto.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Locale;

import projeto.Database;
import projeto.models.Review;

public class ReviewDAO {
    public static boolean insert(Review review) {

        String sql = """
                INSERT INTO filmes_reviews (id_filme, id_usuario, titulo, descricao, nota)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            conn.setAutoCommit(false);
            stmt.setInt(1, Integer.parseInt(review.getFilme()));
            stmt.setInt(2, Integer.parseInt(review.getIdUsuario()));
            stmt.setString(3, review.getTitulo());
            stmt.setString(4, review.getDescricao());
            stmt.setFloat(5, Float.parseFloat(review.getNota()));

            if (stmt.executeUpdate() > 0) {
                String sqlFilme = """
                        UPDATE filmes
                        SET nota = 
                        (
                            ((SELECT AVG(nota) from filmes_reviews WHERE id_filme = ?) * (SELECT COUNT(*) from filmes_reviews where id_filme = ?) + ?)
                            / ((SELECT COUNT(*) from filmes_reviews where id_filme = ?) + 1)
                        )
                        WHERE id = ?
                        """;
                try (
                    PreparedStatement stmt2 = conn.prepareStatement(sqlFilme);
                ) {
                    String nota = String.format(Locale.US, "%.2f", Float.parseFloat(review.getNota()));
                    stmt2.setInt(1, Integer.parseInt(review.getFilme()));
                    stmt2.setInt(2, Integer.parseInt(review.getFilme()));
                    stmt2.setFloat(3, Float.parseFloat(nota));
                    stmt2.setInt(4, Integer.parseInt(review.getFilme()));
                    stmt2.setInt(5, Integer.parseInt(review.getFilme()));
                    int rowsAffected = stmt2.executeUpdate();
                    if(rowsAffected == 0) {
                        conn.rollback();
                        return false;
                    }
                    conn.commit();
                    return rowsAffected > 0;
                } catch (Exception ex) {
                    System.out.println("Erro ao atualizar nota do filme: " + ex.getMessage());
                    return false;
                }
            } else {
                conn.rollback();
                return false;
            }
        } catch (Exception ex) {
            System.out.println("Erro ao inserir review: " + ex.getMessage());
            return false;
        }
    }
}
