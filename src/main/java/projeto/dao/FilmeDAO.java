package projeto.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
                if (!rs.next()) return null;

                filmeDb.addProperty("id", rs.getString("id"));
                filmeDb.addProperty("titulo", rs.getString("titulo"));
                filmeDb.addProperty("diretor", rs.getString("diretor"));
                filmeDb.addProperty("ano", rs.getString("ano"));
                filmeDb.addProperty("sinopse", rs.getString("sinopse"));
                
                String generos = rs.getString("generos");
                if (generos != null) {
                   JsonArray arr = new JsonArray();

                   for(String g : generos.split("\\|")) {
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
}
