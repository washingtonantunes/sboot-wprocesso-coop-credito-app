package br.com.wti.core.auditoria;

import java.util.List;

/**
 * Entidades do back cujo histórico de alterações pode ser consultado. Cada back
 * implementa esta interface no próprio domínio, em um único bean.
 */
public interface CatalogoAuditoria {

    List<EntidadeAuditada> entidades();
}
