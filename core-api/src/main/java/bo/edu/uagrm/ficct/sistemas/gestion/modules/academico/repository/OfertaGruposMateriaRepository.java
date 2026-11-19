package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model.OfertaGruposMateria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfertaGruposMateriaRepository extends JpaRepository<OfertaGruposMateria, Long> {
    List<OfertaGruposMateria> findByPeriodoId(Long periodoId);
    List<OfertaGruposMateria> findByPeriodoIdAndMateriaId(Long periodoId, Long materiaId);
    Optional<OfertaGruposMateria> findByPeriodoIdAndMateriaIdAndCodigoGrupo(Long periodoId, Long materiaId, String codigoGrupo);
}

