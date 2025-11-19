package projeto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:data/app.db";

    public static void init() {
        try (
                Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {

            // Habilita foreign keys no SQLite
            stmt.execute("PRAGMA foreign_keys = ON");

            String sql;
            try (
                BufferedReader br = new BufferedReader(new InputStreamReader(Database.class.getResourceAsStream("/init.sql")))
            ) {
                sql = br.lines().collect(Collectors.joining("\n"));

                for (String query : sql.split(";")) {
                    if(!query.trim().isEmpty()) {
                        stmt.execute(query);
                    }
                }
            }
            
            // Adicionar coluna editado se não existir
            try {
                boolean colunaExiste = false;
                try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(filmes_reviews)")) {
                    while (rs.next()) {
                        if ("editado".equals(rs.getString("name"))) {
                            colunaExiste = true;
                            break;
                        }
                    }
                }
                if (!colunaExiste) {
                    stmt.execute("ALTER TABLE filmes_reviews ADD COLUMN editado VARCHAR(255) NOT NULL DEFAULT 'false'");
                    System.out.println("Coluna 'editado' adicionada à tabela filmes_reviews");
                }
            } catch (Exception ex) {
                System.err.println("Erro ao verificar/adicionar coluna editado: " + ex.getMessage());
            }
            
            System.out.println("Banco inicializado em: " + DB_URL);

        } catch (Exception ex) {
            System.err.println("Erro ao inicializar o banco de dados: " + ex.getMessage());
        }
    }

    public static Connection getConnection() throws Exception {
        Connection conn = DriverManager.getConnection(DB_URL);
        // Habilita foreign keys para cada conexão
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }
}
