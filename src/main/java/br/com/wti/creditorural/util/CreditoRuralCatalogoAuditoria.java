package br.com.wti.creditorural.util;

import br.com.wti.core.auditoria.CatalogoAuditoria;
import br.com.wti.core.auditoria.EntidadeAuditada;
import br.com.wti.creditorural.domain.entity.OperacaoCreditoRural;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CreditoRuralCatalogoAuditoria implements CatalogoAuditoria {

    @Override
    public List<EntidadeAuditada> entidades() {
        return List.of(EntidadeAuditada.porCooperativa(
                "operacao_credito_rural", OperacaoCreditoRural.class, OperacaoCreditoRural::getCooperativaId));
    }
}
