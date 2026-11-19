package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.service;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.request.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.response.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthService {
    LoginResponse login(LoginRequest request, String direccionIp);
    UsuarioResponse obtenerPerfil(Usuario usuarioActual);
    Page<UsuarioResponse> listarUsuarios(Pageable pageable);
    UsuarioResponse cambiarEstadoUsuario(Long usuarioId, boolean activo);
    InvitacionResponse crearInvitacion(CrearInvitacionRequest request, Usuario usuarioActual);
    UsuarioResponse aceptarInvitacion(AceptarInvitacionRequest request);
    void transferirDireccion(TransferirDireccionRequest request, Usuario directorActual);
    Page<BitacoraAuditoriaResponse> consultarBitacora(
        String modulo, String accion, String correoActor,
        OffsetDateTime fechaDesde, OffsetDateTime fechaHasta, Pageable pageable
    );
}

