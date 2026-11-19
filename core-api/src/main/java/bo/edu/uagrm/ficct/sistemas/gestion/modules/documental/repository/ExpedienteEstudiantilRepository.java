package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.ExpedienteEstudiantil;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpedienteEstudiantilRepository extends JpaRepository<ExpedienteEstudiantil, Long> {
    Optional<ExpedienteEstudiantil> findByRegistroEstudiantil(String registroEstudiantil);
    Optional<ExpedienteEstudiantil> findByCedulaIdentidad(String cedulaIdentidad);
}

