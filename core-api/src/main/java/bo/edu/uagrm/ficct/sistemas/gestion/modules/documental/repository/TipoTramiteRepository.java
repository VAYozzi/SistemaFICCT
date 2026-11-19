package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.TipoTramite;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoTramiteRepository extends JpaRepository<TipoTramite, Long> {
    Optional<TipoTramite> findByCodigo(String codigo);
}

