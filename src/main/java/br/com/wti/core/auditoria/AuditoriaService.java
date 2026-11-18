package br.com.wti.core.auditoria;

import static java.time.LocalTime.MAX;
import static java.time.LocalTime.MIN;

import br.com.wti.core.tenant.EscopoCooperativa;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class AuditoriaService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper;

    private final Map<String, EntidadeAuditada> entidadesAuditadas;

    @Autowired
    public AuditoriaService(ObjectMapper objectMapper, CatalogoAuditoria catalogoAuditoria) {
        this.objectMapper = objectMapper;
        this.entidadesAuditadas = catalogoAuditoria.entidades().stream()
                .collect(Collectors.toMap(EntidadeAuditada::nome, Function.identity()));
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponseDTO> buscarAuditoria(EscopoCooperativa escopo, FiltroAuditoriaRequestDTO dto) {
        EntidadeAuditada entidadeAuditada =
                entidadesAuditadas.get(dto.getEntidade().toLowerCase());
        if (entidadeAuditada == null) {
            throw new IllegalArgumentException("Entidade não suportada para auditoria: " + dto.getEntidade());
        }

        Class<?> clazz = entidadeAuditada.classe();

        if (!registroNoEscopo(entidadeAuditada, dto.getId(), escopo)) {
            return Collections.emptyList();
        }

        AuditQuery query = AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(clazz, false, true)
                .add(AuditEntity.id().eq(dto.getId()));

        if (dto.getData() != null) {
            Date dataInicio = Date.from(
                    dto.getData().atTime(MIN).atZone(ZoneId.systemDefault()).toInstant());
            Date dataFim = Date.from(
                    dto.getData().atTime(MAX).atZone(ZoneId.systemDefault()).toInstant());
            query.add(AuditEntity.revisionProperty("timestamp").ge(dataInicio.getTime()));
            query.add(AuditEntity.revisionProperty("timestamp").le(dataFim.getTime()));
        }

        @SuppressWarnings("unchecked")
        List<Object[]> resultList = query.getResultList();

        return resultList.stream()
                .map(arr -> {
                    Object entity = arr[0];
                    CustomRevEntity customRev = (CustomRevEntity) arr[1];
                    RevisionType revisionType = (RevisionType) arr[2];
                    LocalDateTime dataHora =
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(customRev.getTimestamp()), ZoneOffset.UTC);

                    Map<String, Object> objeto =
                            objectMapper.convertValue(entity, new TypeReference<Map<String, Object>>() {});
                    return new AuditoriaResponseDTO(dataHora, revisionType, customRev.getUsuario(), objeto);
                })
                .toList();
    }

    // O registro só é carregado para entidade que pertence a uma cooperativa; nas
    // globais o alcance do histórico não depende dele.
    private boolean registroNoEscopo(EntidadeAuditada entidadeAuditada, Long id, EscopoCooperativa escopo) {
        Object registro = entidadeAuditada.pertenceACooperativa() && !escopo.isTodas()
                ? entityManager.find(entidadeAuditada.classe(), id)
                : null;
        return entidadeAuditada.visivelNoEscopo(escopo, registro);
    }
}
