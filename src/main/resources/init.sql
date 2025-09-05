CREATE TABLE IF NOT EXISTS users(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        usuario VARCHAR(255) NOT NULL,
                        senha VARCHAR(255) NOT NULL,
                        criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAP,
                        atualizado_em TIMESTAP DEFAULT CURRENT_TIMESTAMP
                    )