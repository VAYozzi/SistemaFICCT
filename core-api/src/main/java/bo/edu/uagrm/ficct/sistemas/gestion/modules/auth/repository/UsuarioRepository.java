package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoInstitucional(String correoInstitucional);
    boolean existsByCorreoInstitucional(String correoInstitucional);
    Page<Usuario> findAll(Pageable pageable);
}

