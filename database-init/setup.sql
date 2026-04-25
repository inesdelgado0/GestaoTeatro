CREATE TABLE TipoUtilizador (
    id SERIAL PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL
);

CREATE TABLE Sala (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    capacidade_Total INTEGER NOT NULL
);

CREATE TABLE Evento (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    duracaoMin INTEGER,
    classificacaoEtaria VARCHAR(20),
    genero VARCHAR(50)
);

CREATE TABLE TipoBilhete (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    percentagemDesconto DECIMAL(5,2) NOT NULL DEFAULT 0
);

CREATE TABLE Pagamento (
    id SERIAL PRIMARY KEY,
    valorTotal DECIMAL(10,2) NOT NULL,
    dataPagamento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metodoPagamento VARCHAR(50)
);


CREATE TABLE Utilizador (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    telemovel VARCHAR(20),
    morada TEXT,
    nif VARCHAR(15),
    id_tipo_utilizador INTEGER NOT NULL REFERENCES TipoUtilizador(id)
);

CREATE TABLE Zona (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    taxaAdicional DECIMAL(10,2) DEFAULT 0,
    id_sala INTEGER NOT NULL REFERENCES Sala(id) ON DELETE CASCADE
);

CREATE TABLE Lugar (
    id SERIAL PRIMARY KEY,
    fila VARCHAR(5) NOT NULL,
    numero INTEGER NOT NULL,
    id_zona INTEGER NOT NULL REFERENCES Zona(id) ON DELETE CASCADE,
    UNIQUE (fila, numero, id_zona)
);

CREATE TABLE Sessao (
    id SERIAL PRIMARY KEY,
    dataHora TIMESTAMP NOT NULL,
    precoBase DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'Aberta',
    id_evento INTEGER NOT NULL REFERENCES Evento(id),
    id_sala INTEGER NOT NULL REFERENCES Sala(id)
);

CREATE TABLE Bilhete (
    id SERIAL PRIMARY KEY,
    precoFinal DECIMAL(10,2),
    estado VARCHAR(20) NOT NULL DEFAULT 'Reservado',
    id_utilizador INTEGER NOT NULL REFERENCES Utilizador(id),
    id_sessao INTEGER NOT NULL REFERENCES Sessao(id),
    id_pagamento INTEGER REFERENCES Pagamento(id)
);

CREATE TABLE Lugar_Bilhete (
    id SERIAL PRIMARY KEY,
    precoUnitario DECIMAL(10,2),
    id_bilhete INTEGER NOT NULL REFERENCES Bilhete(id) ON DELETE CASCADE,
    id_lugar INTEGER NOT NULL REFERENCES Lugar(id),
    id_tipo_bilhete INTEGER NOT NULL REFERENCES TipoBilhete(id),
    UNIQUE (id_lugar, id_bilhete)
);

INSERT INTO TipoUtilizador (tipo) VALUES
    ('Administrador'),
    ('Cliente');

INSERT INTO Utilizador (
    nome,
    email,
    password,
    telemovel,
    morada,
    nif,
    id_tipo_utilizador
) VALUES (
    'Administrador Principal',
    'admin@teatro.pt',
    '$2a$10$w1suLT79mBxablCCMlXodeRBM8iIeZnK2bt1Z8YX.Is4kB77HNy5u',
    NULL,
    NULL,
    NULL,
    1
);
