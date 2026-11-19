package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.model.Alerta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByEstadoOrderByFechaGeneracionDesc(String estado);
    List<Alerta> findByNivelCriticidadOrderByFechaGeneracionDesc(String nivelCriticidad);
    long countByEstado(String estado);
}

