package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model.PeriodoAcademico;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, Long> {
    Optional<PeriodoAcademico> findByCodigo(String codigo);
    Optional<PeriodoAcademico> findFirstByEstadoOrderByFechaInicioDesc(String estado);
}

