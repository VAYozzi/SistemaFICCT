package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.HistorialEstadoTramite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialEstadoTramiteRepository extends JpaRepository<HistorialEstadoTramite, Long> {
    List<HistorialEstadoTramite> findByTramiteIdOrderByFechaHoraAsc(Long tramiteId);
}

