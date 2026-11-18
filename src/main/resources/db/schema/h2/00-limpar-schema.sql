-- ============================================
-- SISTEMA WPROCESSO-COOP - CRÉDITO RURAL
-- LIMPEZA DO SCHEMA DE TESTE H2
-- ============================================
-- O TestBase pode criar mais de um ApplicationContext
-- na mesma execução. Limpa o banco em memória antes de
-- reaplicar o schema inicial.

DROP ALL OBJECTS;
