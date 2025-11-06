package projeto.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import projeto.Database;
import projeto.handlers.JsonHandler;
import projeto.models.Filme;
import projeto.models.Review;

public class FilmeDAO {

    public static boolean insert(Filme filme) {
        String filmeSql = "INSERT INTO filmes (titulo, diretor, ano, sinopse) VALUES (?, ?, ?, ?)";
        String generoSql = "INSERT INTO filmes_generos (id_filme, genero) VALUES (?, ?)";

        try (
                Connection conn = Database.getConnection();) {
            conn.setAutoCommit(false);

            int filmeId = 0;

            try (
                    PreparedStatement stmt = conn.prepareStatement(filmeSql,
                            PreparedStatement.RETURN_GENERATED_KEYS);) {
                stmt.setString(1, filme.titulo);
                stmt.setString(2, filme.diretor);
                stmt.setString(3, filme.ano);
                stmt.setString(4, filme.sinopse);

                if (stmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                try (
                        ResultSet rs = stmt.getGeneratedKeys();) {
                    if (rs.next()) {
                        filmeId = rs.getInt(1);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Erro ao inserir filme: " + ex.getMessage());
                return false;
            }

            if (filme.genero.size() > 0 && filmeId > 0) {
                try (
                        PreparedStatement stmt = conn.prepareStatement(generoSql);) {
                    for (String genero : filme.genero) {
                        stmt.setInt(1, filmeId);
                        stmt.setString(2, genero);
                        if (stmt.executeUpdate() == 0) {
                            conn.rollback();
                            return false;
                        }
                    }

                    conn.commit();
                    return true;
                } catch (Exception ex) {
                    System.err.println("Erro ao inserir filme: " + ex.getMessage());
                    return false;
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro ao inserir filme: " + ex.getMessage());
            return false;
        }

        return true;
    }

    public static boolean editFilme(Filme filme) {
        String filmeSql = "UPDATE filmes SET titulo = ?, diretor = ?, ano = ?, sinopse = ? WHERE id = ?";
        String generoSql = "DELETE FROM filmes_generos WHERE id_filme = ?";
        String insertGeneroSql = "INSERT INTO filmes_generos (id_filme, genero) VALUES (?, ?)";

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(filmeSql);) {

            conn.setAutoCommit(false);
            stmt.setString(1, filme.titulo);
            stmt.setString(2, filme.diretor);
            stmt.setString(3, filme.ano);
            stmt.setString(4, filme.sinopse);
            stmt.setInt(5, Integer.parseInt(filme.id));

            if (stmt.executeUpdate() == 0) {
                return false;
            }

            try (
                    PreparedStatement stmt2 = conn.prepareStatement(generoSql);
                    PreparedStatement stmt3 = conn.prepareStatement(insertGeneroSql);) {

                stmt2.setInt(1, Integer.parseInt(filme.id));
                if (stmt2.executeUpdate() == 0) {
                    return false;
                }
                ;

                for (String genero : filme.genero) {
                    stmt3.setInt(1, Integer.parseInt(filme.id));
                    stmt3.setString(2, genero);
                    if (stmt3.executeUpdate() == 0) {
                        return false;
                    }
                }

                conn.commit();
                return true;
            } catch (Exception ex) {
                System.err.println("Erro ao editar filme: " + ex.getMessage());
                return false;
            }
        } catch (Exception ex) {
            System.err.println("Erro ao editar filme: " + ex.getMessage());
            return false;
        }
    }

    public static List<Filme> findAll() {
        List<Filme> filmes = new ArrayList<>();

        String sql = """
                        SELECT
                        f.id,
                        f.titulo,
                        f.diretor,
                        f.ano,
                        f.nota,
                        (
                            SELECT GROUP_CONCAT(g.genero, '|')
                            FROM (
                                SELECT DISTINCT genero
                                FROM filmes_generos
                                WHERE id_filme = f.id
                            ) g
                        ) AS generos,
                        f.sinopse,
                        (
                            SELECT COUNT(*)
                            FROM filmes_reviews r
                            WHERE r.id_filme = f.id
                        ) AS qtdAvaliacoes
                    FROM filmes f;
                """;

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();) {
            while (rs.next()) {
                Filme filme = new Filme();
                filme.id = rs.getString("id");
                filme.titulo = rs.getString("titulo");
                filme.diretor = rs.getString("diretor");
                filme.ano = rs.getString("ano");
                filme.sinopse = rs.getString("sinopse");
                filme.nota = rs.getString("nota");
                filme.genero = Arrays.asList(rs.getString("generos").split("\\|"));
                filme.qtd_avaliacoes = rs.getString("qtdAvaliacoes");
                filmes.add(filme);
            }
        } catch (Exception ex) {
            System.err.println("Erro ao buscar filmes: " + ex.getMessage());
        }

        return filmes;
    }

    public static Filme findFilme(String titulo, String diretor) {
        JsonObject filmeDb = new JsonObject();
        String sql = """
                SELECT f.id, f.titulo, f.diretor, f.ano, f.sinopse, GROUP_CONCAT(fg.genero, '|') as generos
                FROM filmes f
                LEFT JOIN filmes_generos fg ON fg.id_filme = f.id
                WHERE f.titulo = ? AND f.diretor = ?
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, titulo);
            stmt.setString(2, diretor);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;

                filmeDb.addProperty("id", rs.getString("id"));
                filmeDb.addProperty("titulo", rs.getString("titulo"));
                filmeDb.addProperty("diretor", rs.getString("diretor"));
                filmeDb.addProperty("ano", rs.getString("ano"));
                filmeDb.addProperty("sinopse", rs.getString("sinopse"));

                String generos = rs.getString("generos");
                if (generos != null) {
                    JsonArray arr = new JsonArray();

                    for (String g : generos.split("\\|")) {
                        arr.add(g);
                    }
                    filmeDb.add("generos", arr);
                } else {
                    filmeDb.add("generos", new JsonArray());
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro ao buscar filme: " + ex.getMessage());
        }

        return JsonHandler.jsonToModel(filmeDb, Filme.class);
    }

    public static Filme findFilmeById(Filme filme) {
        JsonObject filmeDb = new JsonObject();
        String sql = """
                SELECT f.id, f.titulo, f.diretor, f.ano, f.sinopse, GROUP_CONCAT(fg.genero, '|') as generos
                FROM filmes f
                LEFT JOIN filmes_generos fg ON fg.id_filme = f.id
                WHERE f.id = ?
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(filme.id));

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;

                filmeDb.addProperty("id", rs.getString("id"));
                filmeDb.addProperty("titulo", rs.getString("titulo"));
                filmeDb.addProperty("diretor", rs.getString("diretor"));
                filmeDb.addProperty("ano", rs.getString("ano"));
                filmeDb.addProperty("sinopse", rs.getString("sinopse"));

                String generos = rs.getString("generos");
                if (generos != null) {
                    JsonArray arr = new JsonArray();

                    for (String g : generos.split("\\|")) {
                        arr.add(g);
                    }
                    filmeDb.add("generos", arr);
                } else {
                    filmeDb.add("generos", new JsonArray());
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro ao buscar filme: " + ex.getMessage());
        }

        return JsonHandler.jsonToModel(filmeDb, Filme.class);
    }

    public static Filme findById(String id) {
        Filme filme = new Filme();
        
        String sql = """
                        SELECT
                        f.id,
                        f.titulo,
                        f.diretor,
                        f.ano,
                        f.nota,
                        (
                            SELECT GROUP_CONCAT(g.genero, '|')
                            FROM (
                                SELECT DISTINCT genero
                                FROM filmes_generos
                                WHERE id_filme = f.id
                            ) g
                        ) AS generos,
                        f.sinopse,
                        (
                            SELECT COUNT(*)
                            FROM filmes_reviews r
                            WHERE r.id_filme = f.id
                        ) AS qtdAvaliacoes
                    FROM filmes f
                    WHERE f.id = ?;
                """;

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setInt(1, Integer.parseInt(id));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    filme.id = rs.getString("id");
                    filme.titulo = rs.getString("titulo");
                    filme.diretor = rs.getString("diretor");
                    filme.ano = rs.getString("ano");
                    filme.sinopse = rs.getString("sinopse");
                    filme.nota = rs.getString("nota");
                    String generos = rs.getString("generos");
                    if (generos != null && !generos.isEmpty()) {
                        filme.genero = Arrays.asList(generos.split("\\|"));
                    } else {
                        filme.genero = new ArrayList<>();
                    }
                    filme.qtd_avaliacoes = rs.getString("qtdAvaliacoes");
                } else {
                    return null;
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro ao buscar filme por ID: " + ex.getMessage());
            return null;
        }

        return filme;
    }

    public static List<Review> findReviewsByFilmeId(String id) {
        List<Review> reviews = new ArrayList<>();

        System.out.println("Buscando reviews do filme ID: " + id);
        String sql = """
                SELECT 
                    r.id,
                    r.id_filme,
                    r.id_usuario,
                    r.titulo,
                    r.descricao,
                    r.nota,
                    r."data" AS data_review,
                    u.usuario AS nome_usuario
                FROM filmes_reviews r
                INNER JOIN usuarios u ON r.id_usuario = u.id
                WHERE r.id_filme = ?
                ORDER BY r."data" DESC
                """;

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setInt(1, Integer.parseInt(id));
            System.out.println("SQL executado: " + sql.replace("?", id));
            
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("ResultSet obtido, iterando resultados...");
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("Processando review #" + count);
                    Review review = new Review();
                    review.id = rs.getString("id");
                    review.id_filme = rs.getString("id_filme");
                    review.id_usuario = rs.getString("id_usuario");
                    review.titulo = rs.getString("titulo");
                    review.descricao = rs.getString("descricao");
                    
                    BigDecimal notaDecimal = rs.getBigDecimal("nota");
                    review.nota = notaDecimal != null ? notaDecimal.toString() : "0.0";
                    
                    review.data = rs.getString("data_review");
                    review.nome_usuario = rs.getString("nome_usuario");
                    System.out.println("Review adicionada: " + review.titulo + " por " + review.nome_usuario);
                    reviews.add(review);
                }
                System.out.println("Total de reviews processadas: " + count);
            }
        } catch (Exception ex) {
            System.err.println("Erro ao buscar reviews do filme ID " + id + ": " + ex.getMessage());
            ex.printStackTrace();
        }

        System.out.println("Reviews encontradas: " + reviews.size());
        
        return reviews;
    }

    public static boolean update(Filme filme) {
        String sql = "UPDATE filmes SET titulo = ?, diretor = ?, ano = ?, sinopse = ? WHERE id = ?";
        String deleteCategorias = "DELETE FROM filmes_generos WHERE id_filme = ?";
        String insertCategorias = "INSERT INTO filmes_generos (id_filme, genero) VALUES (?, ?)";

        try (
                Connection conn = Database.getConnection();) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement updateStatement = conn.prepareStatement(sql);
                    PreparedStatement deleteStatement = conn.prepareStatement(deleteCategorias);
                    PreparedStatement insertStatement = conn.prepareStatement(insertCategorias);) {
                updateStatement.setString(1, filme.titulo);
                updateStatement.setString(2, filme.diretor);
                updateStatement.setString(3, filme.ano);
                updateStatement.setString(4, filme.sinopse);
                updateStatement.setInt(5, Integer.parseInt(filme.id));

                if (updateStatement.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                deleteStatement.setInt(1, Integer.parseInt(filme.id));

                if (deleteStatement.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                for (String genero : filme.genero) {
                    insertStatement.setInt(1, Integer.parseInt(filme.id));
                    insertStatement.setString(2, genero);
                    insertStatement.addBatch();
                }

                if (insertStatement.executeBatch().length == 0) {
                    conn.rollback();
                    return false;
                }

                conn.commit();
                return true;
            } catch (Exception ex) {
                System.err.println("Erro ao editar filme: " + ex.getMessage());
                return false;
            }
        } catch (Exception ex) {
            System.err.println("Erro ao editar filme: " + ex.getMessage());
            return false;
        }
    }

    public static boolean delete(String id) {
        String sql = "DELETE FROM filmes WHERE id = ?";

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(id));
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception ex) {
            System.err.println("Erro ao excluir filme: " + ex.getMessage());
            return false;
        }
    }
}
