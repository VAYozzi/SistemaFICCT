package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model.Materia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Long> {
    List<Materia> findByPlanEstudioIdOrderBySemestreNivelAscSiglaAsc(Long planEstudioId);
    Optional<Materia> findByPlanEstudioIdAndSigla(Long planEstudioId, String sigla);
    Optional<Materia> findBySigla(String sigla);
}

