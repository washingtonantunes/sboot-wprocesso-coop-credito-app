package br.com.wti.creditorural.controller.openapi;

import br.com.wti.creditorural.controller.dto.request.ContratarCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.request.DesclassificarCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.request.FiltroOperacaoCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.request.NegarCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.response.OperacaoCreditoRuralResponseDTO;
import br.com.wti.creditorural.controller.dto.response.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Operação de Crédito Rural")
public interface OperacaoCreditoRuralControllerApi {

    @Operation(summary = "Pesquisar operações de crédito rural")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Sucesso")})
    PageResponseDTO<OperacaoCreditoRuralResponseDTO> pesquisarPaginado(
            @Valid @ModelAttribute FiltroOperacaoCreditoRuralRequestDTO filtro,
            @RequestParam(required = false) Long cooperativaId,
            Pageable pageable);

    @Operation(summary = "Buscar operação de crédito rural")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Sucesso")})
    OperacaoCreditoRuralResponseDTO buscar(@PathVariable Long id);

    @Operation(summary = "Contratar crédito rural")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Sucesso")})
    OperacaoCreditoRuralResponseDTO contratar(@RequestBody @Valid ContratarCreditoRuralRequestDTO dto);

    @Operation(summary = "Aprovar operação de crédito rural")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Sucesso")})
    void aprovar(@PathVariable Long id);

    @Operation(summary = "Negar operação de crédito rural")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Sucesso")})
    void negar(@PathVariable Long id, @RequestBody @Valid NegarCreditoRuralRequestDTO dto);

    @Operation(summary = "Desclassificar operação de crédito rural")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Sucesso")})
    void desclassificar(@PathVariable Long id, @RequestBody @Valid DesclassificarCreditoRuralRequestDTO dto);
}
