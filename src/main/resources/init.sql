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
        id_filme INTEGER NOT NULL,
        CONSTRAINT fk_generos_filme FOREIGN KEY (id_filme) REFERENCES filmes (id) ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE TABLE
    IF NOT EXISTS filmes_reviews (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        id_filme INTEGER NOT NULL,
        id_usuario INTEGER NOT NULL,
        titulo VARCHAR(255) NOT NULL,
        nota DECIMAL(1, 1) NOT NULL,
        descricao TEXT NOT NULL,
        "data" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        -- Se um filme for deletado, todas suas reviews também serão deletadas
        CONSTRAINT fk_reviews_filme FOREIGN KEY (id_filme) REFERENCES filmes (id) ON DELETE CASCADE ON UPDATE CASCADE,
        -- Se um usuário for deletado, todas suas reviews também serão deletadas automaticamente
        CONSTRAINT fk_reviews_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios (id) ON DELETE CASCADE ON UPDATE CASCADE
    );


CREATE INDEX IF NOT EXISTS ix_reviews_filmes ON filmes_reviews (id_filme);
CREATE INDEX IF NOT EXISTS ix_reviews_usuarios ON filmes_reviews (id_usuario);

CREATE INDEX IF NOT EXISTS ix_generos_filmes ON filmes_generos (filme_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_generos_filme_genero ON filmes_generos(id_filme, genero);

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