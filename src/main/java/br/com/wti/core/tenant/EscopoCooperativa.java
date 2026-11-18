package br.com.wti.core.tenant;

import java.util.Objects;

/**
 * Recorte de dados de uma requisição: todas as cooperativas ou uma só. É montado
 * apenas pelo {@link TenantContext}; controllers e services o recebem pronto e o
 * repassam às consultas.
 */
public record EscopoCooperativa(Long cooperativaId) {

    static EscopoCooperativa todas() {
        return new EscopoCooperativa(null);
    }

    static EscopoCooperativa daCooperativa(Long cooperativaId) {
        return new EscopoCooperativa(Objects.requireNonNull(cooperativaId, "cooperativaId"));
    }

    public boolean isTodas() {
        return cooperativaId == null;
    }

    /**
     * Indica se um registro da cooperativa informada está dentro do escopo. Registro
     * sem cooperativa (da Central) só está dentro do escopo que abrange todas.
     */
    public boolean abrange(Long cooperativaIdDoRegistro) {
        return isTodas() || Objects.equals(cooperativaId, cooperativaIdDoRegistro);
    }
}
