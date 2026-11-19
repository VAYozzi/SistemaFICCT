package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.model.PoaGestion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoaGestionRepository extends JpaRepository<PoaGestion, Long> {
    Optional<PoaGestion> findByAnioGestion(Integer anioGestion);
}

