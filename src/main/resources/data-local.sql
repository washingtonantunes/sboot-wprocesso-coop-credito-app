-- ============================================
-- SISTEMA WPROCESSO-COOP - MÓDULO DE CRÉDITO
-- Dados locais para demonstração
-- ============================================

INSERT INTO cooperativa (id, id_externo, nome)
VALUES
    (1, 1, 'Cooperativa 1'),
    (2, 2, 'Cooperativa 2');

INSERT INTO cooperado (id, id_externo, cooperativa_id, nome)
VALUES
    (1, 10, 1, 'Ana Souza'),
    (2, 20, 2, 'Bruno Lima');

INSERT INTO operacao_credito_rural (
    id, cooperativa_id, cooperado_id, produto_codigo, valor, finalidade, status,
    tipo_desclassificacao, valor_desclassificado, motivo
)
VALUES
    (1, 1, 10, 'CREDITO_RURAL', 100000.00, 'Custeio de safra', 'SOLICITADA', null, null, null),
    (2, 2, 20, 'CREDITO_RURAL', 50000.00, 'Aquisição de insumos', 'SOLICITADA', null, null, null);
