package projeto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:data/app.db";

    public static void init() {
        try (
                Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {

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
            System.out.println("Banco inicializado em: " + DB_URL);

        } catch (Exception ex) {
            System.err.println("Erro ao inicializar o banco de dados: " + ex.getMessage());
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(DB_URL);
    }
}
