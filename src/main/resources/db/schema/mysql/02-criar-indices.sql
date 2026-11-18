-- ============================================
-- SISTEMA WPROCESSO-COOP - MÓDULO DE CRÉDITO
-- ÍNDICES DE CONSULTA (MYSQL)
-- Execute após 01-criar-schema.sql
-- ============================================

CREATE INDEX IDX_OPERACAO_CREDITO_RURAL_STATUS
    ON operacao_credito_rural (status);

CREATE INDEX IDX_OPERACAO_CREDITO_RURAL_COOPERADO
    ON operacao_credito_rural (cooperado_id);

CREATE INDEX IDX_OPERACAO_CREDITO_RURAL_COOPERATIVA
    ON operacao_credito_rural (cooperativa_id);
