-- ============================================
-- SISTEMA WPROCESSO-COOP - CRÉDITO RURAL
-- COMPLEMENTO H2 PARA HIBERNATE ENVERS
-- ============================================
-- O schema MySQL usa a tabela revinfo_seq. Em H2, o
-- Hibernate usa uma sequência homônima para gerar a revisão.

CREATE SEQUENCE IF NOT EXISTS revinfo_seq
    START WITH 1
    INCREMENT BY 50;
