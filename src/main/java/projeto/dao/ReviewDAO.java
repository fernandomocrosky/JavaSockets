package projeto.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            
            // Garantir que a nota seja salva com exatamente 2 casas decimais
            String notaString = review.getNota();
            if (notaString == null || notaString.trim().isEmpty()) {
                System.err.println("Erro: nota não pode ser nula ou vazia");
                return false;
            }
            
            BigDecimal notaDecimal = new BigDecimal(notaString.trim());
            notaDecimal = notaDecimal.setScale(2, RoundingMode.HALF_UP);
            stmt.setBigDecimal(5, notaDecimal);

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
                    // Usar a mesma nota formatada com 2 casas decimais
                    BigDecimal notaFormatada = notaDecimal;
                    stmt2.setInt(1, Integer.parseInt(review.getFilme()));
                    stmt2.setInt(2, Integer.parseInt(review.getFilme()));
                    stmt2.setBigDecimal(3, notaFormatada);
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
