package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.service.impl;

import bo.edu.uagrm.ficct.sistemas.gestion.common.aop.AuditoriaAspect;
import bo.edu.uagrm.ficct.sistemas.gestion.common.exception.BusinessRuleException;
import bo.edu.uagrm.ficct.sistemas.gestion.config.security.JwtService;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.request.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.response.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.repository.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.service.AuthService;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final InvitacionAccesoRepository invitacionRepository;
    private final HistorialTransferenciaDireccionRepository transferenciaRepository;
    private final BitacoraAuditoriaRepository bitacoraRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditoriaAspect auditoriaAspect;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String direccionIp) {
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(request.correoInstitucional())
            .orElse(null);

        if (usuario == null || !passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            auditoriaAspect.registrarEnBitacora(
                usuario, request.correoInstitucional(), direccionIp,
                "AUTH", "LOGIN_FAIL", "usuarios", null, null, "{\"error\":\"Credenciales inválidas\"}"
            );
            throw new BadCredentialsException("Credenciales inválidas");
        }

        if (!usuario.getActivo()) {
            auditoriaAspect.registrarEnBitacora(
                usuario, request.correoInstitucional(), direccionIp,
                "AUTH", "LOGIN_FAIL", "usuarios", usuario.getId().toString(), null, "{\"error\":\"Usuario inactivo\"}"
            );
            throw new BusinessRuleException("La cuenta de usuario está desactivada. Contacte a Dirección.", HttpStatus.FORBIDDEN);
        }

        usuario.setUltimoAcceso(OffsetDateTime.now());
        usuarioRepository.save(usuario);

        String token = jwtService.generarToken(usuario.getId(), usuario.getCorreoInstitucional(), usuario.getRol().getCodigo());

        auditoriaAspect.registrarEnBitacora(
            usuario, usuario.getCorreoInstitucional(), direccionIp,
            "AUTH", "LOGIN_OK", "usuarios", usuario.getId().toString(), null, "{\"sesionIniciada\":true}"
        );

        return new LoginResponse(
            token,
            "Bearer",
            usuario.getId(),
            usuario.getNombreCompleto(),
            usuario.getCorreoInstitucional(),
            usuario.getCargo(),
            usuario.getRol().getCodigo(),
            usuario.getRol().getNombre()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(Usuario usuarioActual) {
        return mapToUsuarioResponse(usuarioActual);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::mapToUsuarioResponse);
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarEstadoUsuario(Long usuarioId, boolean activo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new BusinessRuleException("Usuario no encontrado con ID: " + usuarioId, HttpStatus.NOT_FOUND));

        if ("ROLE_DIRECTOR".equals(usuario.getRol().getCodigo()) && !activo) {
            throw new BusinessRuleException("No se puede desactivar al Director de Carrera activo. Debe utilizar Transferencia de Dirección.", HttpStatus.CONFLICT);
        }

        usuario.setActivo(activo);
        Usuario guardado = usuarioRepository.save(usuario);
        return mapToUsuarioResponse(guardado);
    }

    @Override
    @Transactional
    public InvitacionResponse crearInvitacion(CrearInvitacionRequest request, Usuario usuarioActual) {
        if (usuarioRepository.existsByCorreoInstitucional(request.correoDestino())) {
            throw new BusinessRuleException("Ya existe una cuenta con el correo: " + request.correoDestino(), HttpStatus.CONFLICT);
        }

        Role rol = roleRepository.findById(request.rolId())
            .orElseThrow(() -> new BusinessRuleException("Rol no encontrado con ID: " + request.rolId(), HttpStatus.NOT_FOUND));

        if ("ROLE_DIRECTOR".equals(rol.getCodigo())) {
            throw new BusinessRuleException("No se pueden emitir invitaciones para el rol ROLE_DIRECTOR. Solo se asigna por Transferencia.", HttpStatus.CONFLICT);
        }

        InvitacionAcceso invitacion = InvitacionAcceso.builder()
            .correoDestino(request.correoDestino())
            .rolAsignado(rol)
            .tokenInvitacion(UUID.randomUUID())
            .creadoPorUsuario(usuarioActual)
            .fechaExpiracion(OffsetDateTime.now().plusHours(48))
            .estado("PENDIENTE")
            .build();

        InvitacionAcceso guardada = invitacionRepository.save(invitacion);

        return new InvitacionResponse(
            guardada.getId(),
            guardada.getCorreoDestino(),
            guardada.getRolAsignado().getId(),
            guardada.getRolAsignado().getNombre(),
            guardada.getTokenInvitacion(),
            usuarioActual.getNombreCompleto(),
            guardada.getFechaExpiracion(),
            guardada.getEstado(),
            guardada.getCreadoEn()
        );
    }

    @Override
    @Transactional
    public UsuarioResponse aceptarInvitacion(AceptarInvitacionRequest request) {
        InvitacionAcceso invitacion = invitacionRepository.findByTokenInvitacion(request.tokenInvitacion())
            .orElseThrow(() -> new BusinessRuleException("Invitación no encontrada o token inválido", HttpStatus.NOT_FOUND));

        if (!"PENDIENTE".equals(invitacion.getEstado())) {
            throw new BusinessRuleException("La invitación ya no está pendiente (estado actual: " + invitacion.getEstado() + ")", HttpStatus.CONFLICT);
        }

        if (OffsetDateTime.now().isAfter(invitacion.getFechaExpiracion())) {
            invitacion.setEstado("EXPIRADA");
            invitacionRepository.save(invitacion);
            throw new BusinessRuleException("La invitación ha expirado. Solicite una nueva a Dirección.", HttpStatus.GONE);
        }

        if (usuarioRepository.existsByCorreoInstitucional(invitacion.getCorreoDestino())) {
            throw new BusinessRuleException("El correo ya se encuentra registrado", HttpStatus.CONFLICT);
        }

        Usuario nuevoUsuario = Usuario.builder()
            .rol(invitacion.getRolAsignado())
            .nombreCompleto(request.nombreCompleto())
            .correoInstitucional(invitacion.getCorreoDestino())
            .passwordHash(passwordEncoder.encode(request.password()))
            .cargo(request.cargo())
            .activo(true)
            .build();

        Usuario guardado = usuarioRepository.save(nuevoUsuario);

        invitacion.setEstado("ACEPTADA");
        invitacionRepository.save(invitacion);

        return mapToUsuarioResponse(guardado);
    }

    @Override
    @Transactional
    public void transferirDireccion(TransferirDireccionRequest request, Usuario directorActual) {
        // Recargar entidad del director actual dentro de la transacción activa
        Usuario actualEnDb = usuarioRepository.findById(directorActual.getId())
            .orElseThrow(() -> new BusinessRuleException("Director actual no encontrado", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.passwordConfirmacionActual(), actualEnDb.getPasswordHash())) {
            throw new BusinessRuleException("Contraseña de confirmación incorrecta. No se puede transferir la Dirección.", HttpStatus.UNAUTHORIZED);
        }

        Usuario nuevoDirector = usuarioRepository.findById(request.nuevoDirectorUsuarioId())
            .orElseThrow(() -> new BusinessRuleException("El usuario designado para nuevo Director no existe", HttpStatus.NOT_FOUND));

        if (!nuevoDirector.getActivo()) {
            throw new BusinessRuleException("El usuario entrante está inactivo", HttpStatus.CONFLICT);
        }

        Role rolDirector = roleRepository.findByCodigo("ROLE_DIRECTOR")
            .orElseThrow(() -> new BusinessRuleException("Rol ROLE_DIRECTOR no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        Role rolCoordinador = roleRepository.findByCodigo("ROLE_COORDINADOR")
            .orElseThrow(() -> new BusinessRuleException("Rol ROLE_COORDINADOR no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        // PASO 1: Degradar inmediatamente al Director saliente para liberar la restricción única (idx_unico_director_activo)
        actualEnDb.setRol(rolCoordinador);
        actualEnDb.setCargo("Ex-Director / Docente");
        usuarioRepository.saveAndFlush(actualEnDb);

        // PASO 2: Ascender al nuevo Director
        nuevoDirector.setRol(rolDirector);
        nuevoDirector.setCargo("Director de Carrera");
        usuarioRepository.saveAndFlush(nuevoDirector);

        // PASO 3: Registrar en historial inmutable de transferencia de autoridad
        HistorialTransferenciaDireccion transferencia = HistorialTransferenciaDireccion.builder()
            .directorSaliente(actualEnDb)
            .directorEntrante(nuevoDirector)
            .resolucionRespaldo(request.resolucionRespaldo())
            .observaciones(request.observaciones())
            .fechaTransferencia(OffsetDateTime.now())
            .build();

        transferenciaRepository.save(transferencia);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BitacoraAuditoriaResponse> consultarBitacora(
        String modulo, String accion, String correoActor,
        OffsetDateTime fechaDesde, OffsetDateTime fechaHasta, Pageable pageable
    ) {
        return bitacoraRepository.findConFiltros(modulo, accion, correoActor, fechaDesde, fechaHasta, pageable)
            .map(b -> new BitacoraAuditoriaResponse(
                b.getId(),
                b.getUsuario() != null ? b.getUsuario().getId() : null,
                b.getCorreoActor(),
                b.getDireccionIp(),
                b.getModulo(),
                b.getAccion(),
                b.getEntidadAfectada(),
                b.getEntidadId(),
                b.getDatosAnteriores(),
                b.getDatosNuevos(),
                b.getFechaHora()
            ));
    }

    private UsuarioResponse mapToUsuarioResponse(Usuario u) {
        return new UsuarioResponse(
            u.getId(),
            u.getNombreCompleto(),
            u.getCorreoInstitucional(),
            u.getCargo(),
            u.getRol().getCodigo(),
            u.getRol().getNombre(),
            u.getActivo(),
            u.getUltimoAcceso(),
            u.getCreadoEn()
        );
    }
}
