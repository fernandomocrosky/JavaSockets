CREATE TABLE
    IF NOT EXISTS usuarios (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        usuario VARCHAR(255) NOT NULL UNIQUE,
        senha VARCHAR(255) NOT NULL,
        criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE
    IF NOT EXISTS filmes (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        titulo VARCHAR(255) NOT NULL,
        diretor VARCHAR(6) NOT NULL,
        ano VARCHAR(255) NOT NULL,
        nota VARCHAR(255) NOT NULL DEFAULT 0.0,
        sinopse TEXT NOT NULL,
        criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE
    IF NOT EXISTS filmes_generos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        genero VARCHAR(255) NOT NULL,
        filme_id INTEGER NOT NULL,
        CONSTRAINT fk_generos_filme FOREIGN KEY (filme_id) REFERENCES filmes (id) ON DELETE CASCADE
    );

CREATE TABLE
    IF NOT EXISTS filmes_reviews (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        filme_id INTEGER NOT NULL,
        titulo VARCHAR(255) NOT NULL,
        nota DECIMAL(1, 1) NOT NULL,
        descricao TEXT NOT NULL,
        "data" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_reviews_filme FOREIGN KEY (filme_id) REFERENCES filmes (id) ON DELETE CASCADE
    );


CREATE INDEX IF NOT EXISTS ix_reviews_filmes ON filmes_reviews (filme_id);

CREATE INDEX IF NOT EXISTS ix_generos_filmes ON filmes_generos (filme_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_generos_filme_genero ON filmes_generos(filme_id, genero);

CREATE TABLE
    IF NOT EXISTS token_blacklist (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        token TEXT NOT NULL,
        expiracao TIMESTAMP NOT NULL
    );

INSERT
OR IGNORE INTO usuarios (usuario, senha)
VALUES
    ("admin", "admin");