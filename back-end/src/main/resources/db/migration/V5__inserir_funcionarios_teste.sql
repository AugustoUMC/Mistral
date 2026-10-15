INSERT INTO funcionario (
    nome,
    matricula,
    cargo,
    salario,
    ativo,
    acesso_sistema,
    senha,
    perfil
)
VALUES
    ('Ana Souza', '1', 'Operadora de Producao',
     2350.00, TRUE, TRUE,
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'USUARIO'),

    ('Bruno Lima', '2', 'Auxiliar de Producao',
     2180.00, TRUE, FALSE, NULL, 'USUARIO'),

    ('Camila Santos', '3', 'Inspetora de Qualidade',
     2850.00, TRUE, TRUE,
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'USUARIO'),

    ('Diego Oliveira', '4', 'Operador de Maquinas',
     2600.00, TRUE, FALSE, NULL, 'USUARIO'),

    ('Eduarda Costa', '5', 'Supervisora de Producao',
     3650.00, TRUE, TRUE,
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'USUARIO'),

    ('Felipe Rodrigues', '6', 'Auxiliar de Producao',
     2200.00, TRUE, FALSE, NULL, 'USUARIO'),

    ('Gabriela Almeida', '7', 'Analista de Qualidade',
     3100.00, TRUE, TRUE,
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'USUARIO'),

    ('Heitor Ferreira', '8', 'Operador de Producao',
     2400.00, TRUE, FALSE, NULL, 'USUARIO'),

    ('Isabela Martins', '9', 'Assistente Administrativa',
     2750.00, TRUE, TRUE,
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'USUARIO'),

    ('Joao Pereira', '10', 'Auxiliar de Estoque',
     2250.00, TRUE, FALSE, NULL, 'USUARIO');