package br.com.wti.creditorural.domain.specification;

import br.com.wti.creditorural.controller.dto.request.FiltroOperacaoCreditoRuralRequestDTO;
import br.com.wti.creditorural.domain.entity.OperacaoCreditoRural;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class OperacaoCreditoRuralSpecification {

    private OperacaoCreditoRuralSpecification() {}

    public static Specification<OperacaoCreditoRural> porFiltro(FiltroOperacaoCreditoRuralRequestDTO filtro) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getStatus() != null) {
                predicates.add(builder.equal(root.get("status"), filtro.getStatus()));
            }
            if (StringUtils.isNotBlank(filtro.getCooperadoNome())) {
                predicates.add(builder.like(
                        builder.lower(root.join("cooperado").get("nome")),
                        "%" + filtro.getCooperadoNome().toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(filtro.getCooperativaNome())) {
                predicates.add(builder.like(
                        builder.lower(root.join("cooperativa").get("nome")),
                        "%" + filtro.getCooperativaNome().toLowerCase() + "%"));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
