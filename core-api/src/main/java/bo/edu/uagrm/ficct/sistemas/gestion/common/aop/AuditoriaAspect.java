package bo.edu.uagrm.ficct.sistemas.gestion.common.aop;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.BitacoraAuditoria;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.repository.BitacoraAuditoriaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditoriaAspect {

    private final BitacoraAuditoriaRepository bitacoraRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object auditarOperacion(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String direccionIp = "127.0.0.1";
        HttpServletRequest request = obtenerRequestActual();
        if (request != null) {
            String remoteAddr = request.getHeader("X-Forwarded-For");
            direccionIp = (remoteAddr != null && !remoteAddr.isBlank()) ? remoteAddr.split(",")[0].trim() : request.getRemoteAddr();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = null;
        String correoActor = "ANONIMO";

        if (auth != null && auth.getPrincipal() instanceof Usuario u) {
            usuario = u;
            correoActor = u.getCorreoInstitucional();
        }

        String datosAnteriores = null;
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            try {
                datosAnteriores = objectMapper.writeValueAsString(args[0]);
            } catch (Exception ignored) {}
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable t) {
            String errorJson = null;
            try {
                errorJson = objectMapper.writeValueAsString(Map.of("error", t.getMessage() != null ? t.getMessage() : "Excepción en operación"));
            } catch (Exception ignored) {}
            registrarEnBitacora(usuario, correoActor, direccionIp, auditable.modulo(), auditable.accion() + "_FAIL",
                auditable.entidadAfectada(), null, datosAnteriores, errorJson);
            throw t;
        }

        String datosNuevos = null;
        try {
            datosNuevos = objectMapper.writeValueAsString(result);
        } catch (Exception ignored) {}

        registrarEnBitacora(usuario, correoActor, direccionIp, auditable.modulo(), auditable.accion(),
            auditable.entidadAfectada(), null, datosAnteriores, datosNuevos);

        return result;
    }

    public void registrarEnBitacora(
        Usuario usuario, String correoActor, String direccionIp,
        String modulo, String accion, String entidadAfectada,
        String entidadId, String datosAnteriores, String datosNuevos
    ) {
        try {
            BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuario(usuario)
                .correoActor(correoActor)
                .direccionIp(direccionIp)
                .modulo(modulo)
                .accion(accion)
                .entidadAfectada(entidadAfectada)
                .entidadId(entidadId)
                .datosAnteriores(datosAnteriores)
                .datosNuevos(datosNuevos)
                .fechaHora(OffsetDateTime.now())
                .build();

            bitacoraRepository.save(bitacora);
        } catch (Exception e) {
            log.error("Error al registrar auditoría en bitacora: {}", e.getMessage());
        }
    }

    private HttpServletRequest obtenerRequestActual() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }
}
