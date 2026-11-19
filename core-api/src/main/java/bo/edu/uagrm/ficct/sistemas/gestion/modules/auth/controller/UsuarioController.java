package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.controller;

import bo.edu.uagrm.ficct.sistemas.gestion.common.aop.Auditable;
import bo.edu.uagrm.ficct.sistemas.gestion.common.dto.ApiResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.common.dto.PageResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.request.CrearInvitacionRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.request.TransferirDireccionRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.response.InvitacionResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.response.UsuarioResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Gestión de Usuarios", description = "Administración de personal, invitaciones y transferencia de Dirección")
public class UsuarioController {

    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('DIRECTOR')")
    @Operation(summary = "Listar personal administrativo", description = "Devuelve listado paginado del personal (solo ROLE_DIRECTOR)")
    public ResponseEntity<ApiResponse<PageResponse<UsuarioResponse>>> listar(
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var page = authService.listarUsuarios(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Listado de usuarios obtenido", PageResponse.from(page)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('DIRECTOR')")
    @Auditable(modulo = "USUARIOS", accion = "UPDATE_STATUS", entidadAfectada = "usuarios")
    @Operation(summary = "Activar o desactivar usuario", description = "Modifica el estado activo del funcionario")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarEstado(
        @PathVariable Long id,
        @RequestBody Map<String, Boolean> body
    ) {
        boolean activo = body.getOrDefault("activo", true);
        UsuarioResponse response = authService.cambiarEstadoUsuario(id, activo);
        return ResponseEntity.ok(ApiResponse.ok("Estado de usuario actualizado", response));
    }

    @PostMapping("/invitaciones")
    @PreAuthorize("hasRole('DIRECTOR')")
    @Auditable(modulo = "USUARIOS", accion = "CREATE_INVITATION", entidadAfectada = "invitaciones_acceso")
    @Operation(summary = "Emitir invitación de acceso", description = "Genera token UUID de invitación temporal para nuevo personal")
    public ResponseEntity<ApiResponse<InvitacionResponse>> crearInvitacion(
        @Valid @RequestBody CrearInvitacionRequest request,
        @AuthenticationPrincipal Usuario usuarioActual
    ) {
        InvitacionResponse response = authService.crearInvitacion(request, usuarioActual);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created("Invitación emitida exitosamente", response));
    }

    @PostMapping("/transferir-direccion")
    @PreAuthorize("hasRole('DIRECTOR')")
    @Auditable(modulo = "USUARIOS", accion = "TRANSFER_AUTHORITY", entidadAfectada = "usuarios")
    @Operation(summary = "Transferencia de autoridad de Dirección (CU-04)", description = "Transfiere el rol ROLE_DIRECTOR en transacción ACID")
    public ResponseEntity<ApiResponse<Void>> transferirDireccion(
        @Valid @RequestBody TransferirDireccionRequest request,
        @AuthenticationPrincipal Usuario directorActual
    ) {
        authService.transferirDireccion(request, directorActual);
        return ResponseEntity.ok(ApiResponse.ok("Transferencia de autoridad de Dirección completada exitosamente", null));
    }
}

