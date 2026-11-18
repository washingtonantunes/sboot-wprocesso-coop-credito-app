package br.com.wti.creditorural.domain.entity;

import br.com.wti.creditorural.domain.enums.StatusOperacaoCreditoRuralEnum;
import br.com.wti.creditorural.domain.enums.TipoDesclassificacaoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@Audited
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "operacao_credito_rural")
public class OperacaoCreditoRural {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "cooperativa_id", nullable = false, updatable = false)
    private Long cooperativaId;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooperativa_id", referencedColumnName = "id_externo", insertable = false, updatable = false)
    private Cooperativa cooperativa;

    @Column(name = "cooperado_id", nullable = false, updatable = false)
    private Long cooperadoId;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooperado_id", referencedColumnName = "id_externo", insertable = false, updatable = false)
    private Cooperado cooperado;

    @Column(name = "produto_codigo", nullable = false, updatable = false, length = 64)
    private String produtoCodigo;

    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "finalidade", nullable = false, length = 1000)
    private String finalidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private StatusOperacaoCreditoRuralEnum status;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_desclassificacao", length = 16)
    private TipoDesclassificacaoEnum tipoDesclassificacao;

    @Column(name = "valor_desclassificado", precision = 19, scale = 2)
    private BigDecimal valorDesclassificado;

    @Column(name = "motivo", length = 1000)
    private String motivo;
}
