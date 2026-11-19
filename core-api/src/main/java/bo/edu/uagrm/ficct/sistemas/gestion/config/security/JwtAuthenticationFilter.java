package bo.edu.uagrm.ficct.sistemas.gestion.config.security;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        if (jwtService.esTokenValido(jwt)) {
            String correo = jwtService.extraerCorreo(jwt);
            String rol = jwtService.extraerRol(jwt);

            if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Usuario usuario = usuarioRepository.findByCorreoInstitucional(correo).orElse(null);
                if (usuario != null && usuario.getActivo()) {
                    var authority = new SimpleGrantedAuthority(rol.startsWith("ROLE_") ? rol : "ROLE_" + rol);
                    var authToken = new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        Collections.singletonList(authority)
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

