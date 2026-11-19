package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.controller;

import bo.edu.uagrm.ficct.sistemas.gestion.common.dto.ApiResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.common.dto.PageResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.response.BitacoraAuditoriaResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auditoria")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Consulta de bitácora inmutable de eventos institucionales")
public class AuditoriaController {

    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('DIRECTOR')")
    @Operation(summary = "Consultar bitácora de auditoría", description = "Consulta paginada y filtrada de eventos inmutables (solo ROLE_DIRECTOR)")
    public ResponseEntity<ApiResponse<PageResponse<BitacoraAuditoriaResponse>>> consultar(
        @RequestParam(required = false) String modulo,
        @RequestParam(required = false) String accion,
        @RequestParam(required = false) String correoActor,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaDesde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaHasta,
        @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var page = authService.consultarBitacora(modulo, accion, correoActor, fechaDesde, fechaHasta, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Eventos de auditoría obtenidos", PageResponse.from(page)));
    }
}

