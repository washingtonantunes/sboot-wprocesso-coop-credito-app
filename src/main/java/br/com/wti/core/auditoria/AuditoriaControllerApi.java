package br.com.wti.core.auditoria;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Auditoria", description = "Histórico de alterações das entidades")
public interface AuditoriaControllerApi {

    @Operation(summary = "Buscar auditoria", description = "Retorna o histórico de alterações de uma entidade por ID")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Sucesso"),
                @ApiResponse(responseCode = "400", description = "Entidade não suportada ou parâmetros inválidos")
            })
    List<AuditoriaResponseDTO> buscarAuditoria(@Valid FiltroAuditoriaRequestDTO dto);
}
