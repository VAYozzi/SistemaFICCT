package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.controller;

import bo.edu.uagrm.ficct.sistemas.gestion.common.aop.Auditable;
import bo.edu.uagrm.ficct.sistemas.gestion.common.dto.ApiResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.common.dto.PageResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.request.CrearCorrespondenciaRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.request.TransicionTramiteRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response.CorrespondenciaResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response.TramiteEstudiantilResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.DocumentoDigital;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.service.DocumentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Gestión Documental y Trámites", description = "Correspondencia oficial, carga de archivos y Máquina de Estados de Trámites")
public class DocumentalController {

    private final DocumentalService documentalService;

    @PostMapping(value = "/documentos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Auditable(modulo = "DOCUMENTAL", accion = "UPLOAD_DOCUMENT", entidadAfectada = "documentos_digitales")
    @Operation(summary = "Subir archivo digital con cálculo SHA-256", description = "Almacena el binario y registra metadatos e integridad")
    public ResponseEntity<ApiResponse<DocumentoDigital>> upload(
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal Usuario usuarioActual
    ) {
        DocumentoDigital doc = documentalService.subirDocumento(file, usuarioActual);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created("Archivo digital subido exitosamente", doc));
    }

    @PostMapping("/correspondencia")
    @Auditable(modulo = "CORRESPONDENCIA", accion = "CREATE_CITE", entidadAfectada = "correspondencia")
    @Operation(summary = "Registrar correspondencia", description = "Crea registro oficial con código CITE")
    public ResponseEntity<ApiResponse<CorrespondenciaResponse>> registrarCorrespondencia(
        @Valid @RequestBody CrearCorrespondenciaRequest request,
        @AuthenticationPrincipal Usuario usuarioActual
    ) {
        CorrespondenciaResponse response = documentalService.registrarCorrespondencia(request, usuarioActual);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created("Correspondencia registrada exitosamente", response));
    }

    @GetMapping("/correspondencia")
    @Operation(summary = "Listar correspondencia", description = "Bandeja de correspondencia institucional paginada")
    public ResponseEntity<ApiResponse<PageResponse<CorrespondenciaResponse>>> listarCorrespondencia(
        @RequestParam(required = false) String tipo,
        @PageableDefault(size = 15, sort = "fechaRecepcionEmision", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var page = documentalService.listarCorrespondencia(tipo, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Correspondencia obtenida", PageResponse.from(page)));
    }

    @GetMapping("/tramites/{id}")
    @Operation(summary = "Detalle de trámite con historial", description = "Consulta estado actual y auditoría de transiciones")
    public ResponseEntity<ApiResponse<TramiteEstudiantilResponse>> obtenerTramite(@PathVariable Long id) {
        TramiteEstudiantilResponse response = documentalService.obtenerTramitePorId(id);
        return ResponseEntity.ok(ApiResponse.ok("Trámite obtenido", response));
    }

    @PostMapping("/tramites/{id}/transicion")
    @Auditable(modulo = "TRAMITES", accion = "STATE_MACHINE_TRANSITION", entidadAfectada = "tramites_estudiantiles")
    @Operation(summary = "Transición de estado de trámite (State Machine)", description = "Ejecuta paso de estado gobernado por reglas y roles")
    public ResponseEntity<ApiResponse<TramiteEstudiantilResponse>> transicion(
        @PathVariable Long id,
        @Valid @RequestBody TransicionTramiteRequest request,
        @AuthenticationPrincipal Usuario usuarioActual
    ) {
        TramiteEstudiantilResponse response = documentalService.realizarTransicion(id, request, usuarioActual);
        return ResponseEntity.ok(ApiResponse.ok("Transición de trámite ejecutada exitosamente", response));
    }
}

