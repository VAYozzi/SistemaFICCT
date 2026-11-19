package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.model.AtencionAlerta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtencionAlertaRepository extends JpaRepository<AtencionAlerta, Long> {
    List<AtencionAlerta> findByAlertaIdOrderByFechaAtencionDesc(Long alertaId);
}

