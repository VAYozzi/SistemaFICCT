package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model.PlanEstudio;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanEstudioRepository extends JpaRepository<PlanEstudio, Long> {
    Optional<PlanEstudio> findByCodigoPlan(String codigoPlan);
}

