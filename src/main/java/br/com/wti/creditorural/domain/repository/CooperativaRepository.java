package br.com.wti.creditorural.domain.repository;

import br.com.wti.creditorural.domain.entity.Cooperativa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CooperativaRepository extends JpaRepository<Cooperativa, Long> {

    Optional<Cooperativa> findByIdExterno(Long idExterno);
}
