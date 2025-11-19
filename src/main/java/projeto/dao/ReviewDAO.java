package projeto.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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

    public static List<Review> findByUsuarioId(String idUsuario) {
        List<Review> reviews = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        System.out.println("Buscando reviews do usuário ID: " + idUsuario);
        String sql = """
                SELECT 
                    r.id,
                    r.id_filme,
                    r.titulo,
                    r.descricao,
                    r.nota,
                    r."data" AS data_review,
                    r.editado,
                    u.usuario AS nome_usuario
                FROM filmes_reviews r
                INNER JOIN usuarios u ON r.id_usuario = u.id
                WHERE r.id_usuario = ?
                ORDER BY r."data" DESC
                """;

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setInt(1, Integer.parseInt(idUsuario));
            System.out.println("SQL executado: " + sql.replace("?", idUsuario));
            
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("ResultSet obtido, iterando resultados...");
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("Processando review #" + count);
                    Review review = new Review();
                    review.id = rs.getString("id");
                    review.id_filme = rs.getString("id_filme");
                    review.titulo = rs.getString("titulo");
                    review.descricao = rs.getString("descricao");
                    
                    BigDecimal notaDecimal = rs.getBigDecimal("nota");
                    review.nota = notaDecimal != null ? notaDecimal.toString() : "0.0";
                    
                    // Formatar data como dd/mm/aaaa
                    Timestamp timestamp = rs.getTimestamp("data_review");
                    if (timestamp != null) {
                        review.data = dateFormat.format(timestamp);
                    } else {
                        review.data = "";
                    }
                    
                    review.editado = rs.getString("editado");
                    if (review.editado == null) {
                        review.editado = "false";
                    }
                    
                    review.nome_usuario = rs.getString("nome_usuario");
                    System.out.println("Review adicionada: " + review.titulo + " por " + review.nome_usuario);
                    reviews.add(review);
                }
                System.out.println("Total de reviews processadas: " + count);
            }
        } catch (Exception ex) {
            System.err.println("Erro ao buscar reviews do usuário ID " + idUsuario + ": " + ex.getMessage());
            ex.printStackTrace();
        }

        System.out.println("Reviews encontradas: " + reviews.size());
        
        return reviews;
    }

    public static Review findById(String id) {
        String sql = """
                SELECT 
                    r.id,
                    r.id_filme,
                    r.id_usuario,
                    r.titulo,
                    r.descricao,
                    r.nota,
                    r."data" AS data_review,
                    r.editado
                FROM filmes_reviews r
                WHERE r.id = ?
                """;

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setInt(1, Integer.parseInt(id));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Review review = new Review();
                    review.id = rs.getString("id");
                    review.id_filme = rs.getString("id_filme");
                    review.id_usuario = rs.getString("id_usuario");
                    review.titulo = rs.getString("titulo");
                    review.descricao = rs.getString("descricao");
                    
                    BigDecimal notaDecimal = rs.getBigDecimal("nota");
                    review.nota = notaDecimal != null ? notaDecimal.toString() : "0.0";
                    
                    review.editado = rs.getString("editado");
                    if (review.editado == null) {
                        review.editado = "false";
                    }
                    
                    return review;
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro ao buscar review ID " + id + ": " + ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }

    public static boolean update(Review review) {
        String sql = """
                UPDATE filmes_reviews 
                SET titulo = ?, descricao = ?, nota = ?, editado = 'true', atualizado_em = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            conn.setAutoCommit(false);
            
            stmt.setString(1, review.getTitulo());
            stmt.setString(2, review.getDescricao());
            
            // Garantir que a nota seja salva com exatamente 2 casas decimais
            String notaString = review.getNota();
            if (notaString == null || notaString.trim().isEmpty()) {
                System.err.println("Erro: nota não pode ser nula ou vazia");
                return false;
            }
            
            BigDecimal notaDecimal = new BigDecimal(notaString.trim());
            notaDecimal = notaDecimal.setScale(2, RoundingMode.HALF_UP);
            stmt.setBigDecimal(3, notaDecimal);
            stmt.setInt(4, Integer.parseInt(review.getId()));

            if (stmt.executeUpdate() > 0) {
                // Buscar o id_filme da review para atualizar a nota do filme
                Review reviewAtual = findById(review.getId());
                if (reviewAtual != null) {
                    String sqlFilme = """
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
                    try (
                        PreparedStatement stmt2 = conn.prepareStatement(sqlFilme);
                    ) {
                        stmt2.setInt(1, Integer.parseInt(reviewAtual.getFilme()));
                        stmt2.setInt(2, Integer.parseInt(reviewAtual.getFilme()));
                        int rowsAffected = stmt2.executeUpdate();
                        if(rowsAffected == 0) {
                            conn.rollback();
                            return false;
                        }
                        conn.commit();
                        return true;
                    } catch (Exception ex) {
                        System.out.println("Erro ao atualizar nota do filme: " + ex.getMessage());
                        conn.rollback();
                        return false;
                    }
                } else {
                    conn.rollback();
                    return false;
                }
            } else {
                conn.rollback();
                return false;
            }
        } catch (Exception ex) {
            System.out.println("Erro ao atualizar review: " + ex.getMessage());
            return false;
        }
    }

    public static boolean delete(String id) {
        // Primeiro, buscar o id_filme antes de deletar
        Review review = findById(id);
        if (review == null) {
            return false;
        }
        
        String idFilme = review.getFilme();
        
        String sql = "DELETE FROM filmes_reviews WHERE id = ?";

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            conn.setAutoCommit(false);
            stmt.setInt(1, Integer.parseInt(id));

            if (stmt.executeUpdate() > 0) {
                // Atualizar a nota média do filme
                String sqlFilme = """
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
                try (
                    PreparedStatement stmt2 = conn.prepareStatement(sqlFilme);
                ) {
                    stmt2.setInt(1, Integer.parseInt(idFilme));
                    stmt2.setInt(2, Integer.parseInt(idFilme));
                    int rowsAffected = stmt2.executeUpdate();
                    if(rowsAffected == 0) {
                        conn.rollback();
                        return false;
                    }
                    conn.commit();
                    return true;
                } catch (Exception ex) {
                    System.out.println("Erro ao atualizar nota do filme: " + ex.getMessage());
                    conn.rollback();
                    return false;
                }
            } else {
                conn.rollback();
                return false;
            }
        } catch (Exception ex) {
            System.out.println("Erro ao excluir review: " + ex.getMessage());
            return false;
        }
    }
}
