package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.HistorialTransferenciaDireccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialTransferenciaDireccionRepository extends JpaRepository<HistorialTransferenciaDireccion, Long> {
}

