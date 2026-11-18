package br.com.wti.creditorural.controller;

import br.com.wti.core.security.FuncionalidadeAutorizada;
import br.com.wti.core.tenant.TenantContext;
import br.com.wti.creditorural.controller.dto.request.ContratarCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.request.DesclassificarCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.request.FiltroOperacaoCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.request.NegarCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.response.OperacaoCreditoRuralResponseDTO;
import br.com.wti.creditorural.controller.dto.response.PageResponseDTO;
import br.com.wti.creditorural.controller.openapi.OperacaoCreditoRuralControllerApi;
import br.com.wti.creditorural.service.OperacaoCreditoRuralService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operacoes-credito-rural")
@RequiredArgsConstructor
public class OperacaoCreditoRuralController implements OperacaoCreditoRuralControllerApi {

    private final OperacaoCreditoRuralService operacaoCreditoRuralService;
    private final TenantContext tenant;

    @Override
    @FuncionalidadeAutorizada({"CREDITO_RURAL_PAGINADO_CENTRAL", "CREDITO_RURAL_PAGINADO_COOPERATIVA"})
    @GetMapping(value = "/paginado", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponseDTO<OperacaoCreditoRuralResponseDTO> pesquisarPaginado(
            @Valid @ModelAttribute FiltroOperacaoCreditoRuralRequestDTO filtro,
            @RequestParam(required = false) Long cooperativaId,
            Pageable pageable) {
        return this.operacaoCreditoRuralService.pesquisarPaginado(tenant.escopo(cooperativaId), filtro, pageable);
    }

    @Override
    @FuncionalidadeAutorizada({"CREDITO_RURAL_BUSCAR_CENTRAL", "CREDITO_RURAL_BUSCAR_COOPERATIVA"})
    @GetMapping(value = "/{id}/buscar", produces = MediaType.APPLICATION_JSON_VALUE)
    public OperacaoCreditoRuralResponseDTO buscar(@PathVariable Long id) {
        return this.operacaoCreditoRuralService.buscar(tenant.escopo(null), id);
    }

    @Override
    @FuncionalidadeAutorizada("CREDITO_RURAL_CONTRATAR")
    @PostMapping(value = "/contratar", produces = MediaType.APPLICATION_JSON_VALUE)
    public OperacaoCreditoRuralResponseDTO contratar(@RequestBody @Valid ContratarCreditoRuralRequestDTO dto) {
        return this.operacaoCreditoRuralService.contratar(tenant.escopo(null), dto);
    }

    @Override
    @FuncionalidadeAutorizada("CREDITO_RURAL_APROVAR")
    @PutMapping(value = "/{id}/aprovar", produces = MediaType.APPLICATION_JSON_VALUE)
    public void aprovar(@PathVariable Long id) {
        this.operacaoCreditoRuralService.aprovar(tenant.escopo(null), id);
    }

    @Override
    @FuncionalidadeAutorizada("CREDITO_RURAL_NEGAR")
    @PutMapping(value = "/{id}/negar", produces = MediaType.APPLICATION_JSON_VALUE)
    public void negar(@PathVariable Long id, @RequestBody @Valid NegarCreditoRuralRequestDTO dto) {
        this.operacaoCreditoRuralService.negar(tenant.escopo(null), id, dto);
    }

    @Override
    @FuncionalidadeAutorizada("CREDITO_RURAL_DESCLASSIFICAR")
    @PutMapping(value = "/{id}/desclassificar", produces = MediaType.APPLICATION_JSON_VALUE)
    public void desclassificar(@PathVariable Long id, @RequestBody @Valid DesclassificarCreditoRuralRequestDTO dto) {
        this.operacaoCreditoRuralService.desclassificar(tenant.escopo(null), id, dto);
    }
}
