package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCodigo(String codigo);
}

