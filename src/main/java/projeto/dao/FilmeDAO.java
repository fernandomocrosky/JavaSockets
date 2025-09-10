package projeto.dao;

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

public class FilmeDAO {

    public static boolean insert(Filme filme) {
        String filmeSql = "INSERT INTO filmes (titulo, diretor, ano, sinopse) VALUES (?, ?, ?, ?)";
        String generoSql = "INSERT INTO filmes_generos (filme_id, genero) VALUES (?, ?)";

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

            if (filme.generos.size() > 0 && filmeId > 0) {
                try (
                        PreparedStatement stmt = conn.prepareStatement(generoSql);) {
                    for (String genero : filme.generos) {
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
        String generoSql = "DELETE FROM filmes_generos WHERE filme_id = ?";
        String insertGeneroSql = "INSERT INTO filmes_generos (filme_id, genero) VALUES (?, ?)";

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

                for (String genero : filme.generos) {
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
                    GROUP_CONCAT(fg.genero, '|') as generos,
                    f.sinopse,
                    COUNT(fr.filme_id) as qtdAvaliacoes
                FROM filmes f
                LEFT JOIN filmes_generos fg ON fg.filme_id = f.id
                LEFT JOIN filmes_reviews fr ON fr.filme_id = f.id
                GROUP BY f.id, f.titulo, f.diretor, f.ano, f.nota, f.sinopse
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
                filme.generos = Arrays.asList(rs.getString("generos").split("\\|"));
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
                LEFT JOIN filmes_generos fg ON fg.filme_id = f.id
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
                LEFT JOIN filmes_generos fg ON fg.filme_id = f.id
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

    public static boolean update(Filme filme) {
        String sql = "UPDATE filmes SET titulo = ?, diretor = ?, ano = ?, sinopse = ? WHERE id = ?";
        String deleteCategorias = "DELETE FROM filmes_generos WHERE filme_id = ?";
        String insertCategorias = "INSERT INTO filmes_generos (filme_id, genero) VALUES (?, ?)";

        System.out.println(filme.toString());

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

                for (String genero : filme.generos) {
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
