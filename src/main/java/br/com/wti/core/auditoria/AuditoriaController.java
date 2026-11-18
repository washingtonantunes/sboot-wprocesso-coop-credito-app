package br.com.wti.core.auditoria;

import br.com.wti.core.security.FuncionalidadeAutorizada;
import br.com.wti.core.tenant.TenantContext;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auditoria")
@RequiredArgsConstructor
public class AuditoriaController implements AuditoriaControllerApi {

    private final AuditoriaService auditoriaService;
    private final TenantContext tenant;

    @FuncionalidadeAutorizada({"AUDITORIA_LISTAR_CENTRAL", "AUDITORIA_LISTAR_COOPERATIVA"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditoriaResponseDTO> buscarAuditoria(@Valid @ModelAttribute FiltroAuditoriaRequestDTO dto) {
        return auditoriaService.buscarAuditoria(tenant.escopo(null), dto);
    }
}
