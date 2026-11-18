package br.com.wti.creditorural.domain.repository;

import br.com.wti.creditorural.domain.entity.OperacaoCreditoRural;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OperacaoCreditoRuralRepository
        extends JpaRepository<OperacaoCreditoRural, Long>, JpaSpecificationExecutor<OperacaoCreditoRural> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT operacao FROM OperacaoCreditoRural operacao WHERE operacao.id = :id")
    Optional<OperacaoCreditoRural> findByIdForUpdate(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"cooperado", "cooperativa"})
    Page<OperacaoCreditoRural> findAll(Specification<OperacaoCreditoRural> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"cooperado", "cooperativa"})
    Optional<OperacaoCreditoRural> findById(Long id);
}
