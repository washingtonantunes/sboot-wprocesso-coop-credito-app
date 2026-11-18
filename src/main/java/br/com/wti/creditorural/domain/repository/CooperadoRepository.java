package br.com.wti.creditorural.domain.repository;

import br.com.wti.creditorural.domain.entity.Cooperado;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CooperadoRepository extends JpaRepository<Cooperado, Long> {

    Optional<Cooperado> findByIdExterno(Long idExterno);
}
