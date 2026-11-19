package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.RequisitoTramite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequisitoTramiteRepository extends JpaRepository<RequisitoTramite, Long> {
    List<RequisitoTramite> findByTramiteId(Long tramiteId);
}

