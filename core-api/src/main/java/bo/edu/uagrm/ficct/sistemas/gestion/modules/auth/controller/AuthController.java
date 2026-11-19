package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.controller;

import bo.edu.uagrm.ficct.sistemas.gestion.common.dto.ApiResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.request.AceptarInvitacionRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.request.LoginRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.response.LoginResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.response.UsuarioResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de sesión, perfil institucional y registro de invitados")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Inicio de sesión institucional", description = "Autentica credenciales y emite JWT")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        String direccionIp = httpRequest.getHeader("X-Forwarded-For");
        if (direccionIp == null || direccionIp.isBlank()) {
            direccionIp = httpRequest.getRemoteAddr();
        } else {
            direccionIp = direccionIp.split(",")[0].trim();
        }

        LoginResponse response = authService.login(request, direccionIp);
        return ResponseEntity.ok(ApiResponse.ok("Inicio de sesión exitoso", response));
    }

    @GetMapping("/perfil")
    @Operation(summary = "Perfil del usuario en sesión", description = "Devuelve información del usuario autenticado")
    public ResponseEntity<ApiResponse<UsuarioResponse>> perfil(@AuthenticationPrincipal Usuario usuarioActual) {
        UsuarioResponse response = authService.obtenerPerfil(usuarioActual);
        return ResponseEntity.ok(ApiResponse.ok("Perfil recuperado exitosamente", response));
    }

    @PostMapping("/invitaciones/aceptar")
    @Operation(summary = "Aceptar invitación y registrar cuenta", description = "Completa el registro con el token UUID recibido")
    public ResponseEntity<ApiResponse<UsuarioResponse>> aceptarInvitacion(
        @Valid @RequestBody AceptarInvitacionRequest request
    ) {
        UsuarioResponse response = authService.aceptarInvitacion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created("Cuenta creada exitosamente a partir de la invitación", response));
    }
}

