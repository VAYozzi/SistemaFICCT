package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.controller;

import bo.edu.uagrm.ficct.sistemas.gestion.common.aop.Auditable;
import bo.edu.uagrm.ficct.sistemas.gestion.common.dto.ApiResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.dto.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.model.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.service.InstitucionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/institucional")
@RequiredArgsConstructor
@Tag(name = "Seguimiento Institucional, Dashboard y Alertas", description = "Titulados, KPIs del Dashboard, atención de alertas predictivas y Dossier")
public class InstitucionalController {

    private final InstitucionalService institucionalService;

    // --- KPIs Dashboard ---
    @GetMapping("/dashboard/kpis")
    @Operation(summary = "Obtener KPIs del Dashboard Directivo", description = "Consolida contadores de estudiantes, trámites, alertas y titulados")
    public ResponseEntity<ApiResponse<DashboardKpisDto>> obtenerKpis() {
        return ResponseEntity.ok(ApiResponse.ok("KPIs del Dashboard recuperados", institucionalService.obtenerKpisDashboard()));
    }

    // --- Titulados ---
    @PostMapping("/titulados")
    @Auditable(modulo = "INSTITUCIONAL", accion = "REGISTRAR_TITULADO", entidadAfectada = "titulados")
    @Operation(summary = "Registrar titulado", description = "Registra defensa y modalidad de titulación")
    public ResponseEntity<ApiResponse<Titulado>> registrarTitulado(@Valid @RequestBody RegistrarTituladoRequest request) {
        Titulado t = institucionalService.registrarTitulado(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Titulado registrado exitosamente", t));
    }

    @GetMapping("/titulados")
    @Operation(summary = "Listar directorio de titulados")
    public ResponseEntity<ApiResponse<List<Titulado>>> listarTitulados() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de titulados", institucionalService.listarTitulados()));
    }

    // --- Alertas y Semáforos ---
    @GetMapping("/alertas")
    @Operation(summary = "Listar alertas predictivas y operativas", description = "Filtro por estado (PENDIENTE/ATENDIDA) o criticidad (ROJO/AMARILLO/VERDE)")
    public ResponseEntity<ApiResponse<List<Alerta>>> listarAlertas(
        @RequestParam(value = "estado", required = false) String estado,
        @RequestParam(value = "nivelCriticidad", required = false) String nivelCriticidad
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Listado de alertas", institucionalService.listarAlertas(estado, nivelCriticidad)));
    }

    @PostMapping("/alertas")
    @Auditable(modulo = "ALERTAS", accion = "CREATE_ALERTA", entidadAfectada = "alertas")
    @Operation(summary = "Crear alerta institucional manual o predictiva")
    public ResponseEntity<ApiResponse<Alerta>> crearAlerta(@Valid @RequestBody Alerta alerta) {
        Alerta creada = institucionalService.crearAlerta(alerta);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Alerta registrada", creada));
    }

    @PostMapping("/alertas/{id}/atender")
    @Auditable(modulo = "ALERTAS", accion = "RESOLVER_ALERTA", entidadAfectada = "alertas")
    @Operation(summary = "Atender alerta institucional (Soberanía Humana)", description = "Exige registro de resolución humana tomada")
    public ResponseEntity<ApiResponse<Alerta>> atenderAlerta(
        @PathVariable Long id,
        @Valid @RequestBody AtenderAlertaRequest request,
        @AuthenticationPrincipal Usuario usuarioActual
    ) {
        Alerta atendida = institucionalService.atenderAlerta(id, request, usuarioActual);
        return ResponseEntity.ok(ApiResponse.ok("Alerta atendida con resolución formal registrada", atendida));
    }

    // --- Dossier Institucional Oficial (RPA / Compilador) ---
    @PostMapping("/dossier/generar")
    @Auditable(modulo = "RPA", accion = "GENERATE_DOSSIER", entidadAfectada = "documentos_digitales")
    @Operation(summary = "Generar Dossier Institucional en PDF", description = "Compila informe oficial con hash SHA-256")
    public ResponseEntity<ApiResponse<DossierGeneradoResponse>> generarDossier(
        @RequestBody SolicitudDossierRequest request,
        @AuthenticationPrincipal Usuario usuarioActual
    ) {
        DossierGeneradoResponse dossier = institucionalService.generarDossierInstitucionalRpa(request, usuarioActual);
        return ResponseEntity.ok(ApiResponse.ok("Dossier generado con éxito", dossier));
    }
}

