-- ============================================
-- SISTEMA WPROCESSO-COOP - MÓDULO DE CRÉDITO
-- SCHEMA INICIAL (MYSQL)
-- Execute após 00-criar-database.sql
-- ============================================

CREATE TABLE revinfo (
    id integer not null,
    timestamp bigint not null,
    usuario varchar(255),
    primary key (id)
) ENGINE=InnoDB;

CREATE TABLE revinfo_seq (
    next_val bigint
) ENGINE=InnoDB;

INSERT INTO revinfo_seq (next_val) VALUES (1);

CREATE TABLE cooperativa (
    id bigint not null auto_increment,
    id_externo bigint not null,
    nome varchar(255) not null,
    CONSTRAINT UK_COOPERATIVA_ID_EXTERNO UNIQUE (id_externo),
    primary key (id)
) ENGINE=InnoDB;

CREATE TABLE cooperado (
    id bigint not null auto_increment,
    id_externo bigint not null,
    cooperativa_id bigint not null,
    nome varchar(255) not null,
    CONSTRAINT UK_COOPERADO_ID_EXTERNO UNIQUE (id_externo),
    CONSTRAINT FK_COOPERADO_COOPERATIVA FOREIGN KEY (cooperativa_id) REFERENCES cooperativa (id_externo),
    primary key (id)
) ENGINE=InnoDB;

CREATE TABLE operacao_credito_rural (
    id bigint not null auto_increment,
    cooperativa_id bigint not null,
    cooperado_id bigint not null,
    produto_codigo varchar(64) not null,
    valor decimal(19,2) not null,
    finalidade varchar(1000) not null,
    status varchar(32) not null,
    tipo_desclassificacao varchar(16),
    valor_desclassificado decimal(19,2),
    motivo varchar(1000),
    CONSTRAINT FK_OPERACAO_CREDITO_RURAL_COOPERATIVA FOREIGN KEY (cooperativa_id) REFERENCES cooperativa (id_externo),
    CONSTRAINT FK_OPERACAO_CREDITO_RURAL_COOPERADO FOREIGN KEY (cooperado_id) REFERENCES cooperado (id_externo),
    primary key (id)
) ENGINE=InnoDB;

CREATE TABLE operacao_credito_rural_aud (
    rev integer not null,
    revtype tinyint,
    id bigint not null,
    cooperativa_id bigint,
    cooperado_id bigint,
    produto_codigo varchar(64),
    valor decimal(19,2),
    finalidade varchar(1000),
    status varchar(32),
    tipo_desclassificacao varchar(16),
    valor_desclassificado decimal(19,2),
    motivo varchar(1000),
    CONSTRAINT FK_OPERACAO_CREDITO_RURAL_AUD_REVINFO FOREIGN KEY (rev) REFERENCES revinfo (id),
    primary key (rev, id)
) ENGINE=InnoDB;
