package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.TramiteEstudiantil;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TramiteEstudiantilRepository extends JpaRepository<TramiteEstudiantil, Long> {
    Optional<TramiteEstudiantil> findByCodigoTramite(String codigoTramite);
    Page<TramiteEstudiantil> findByEstadoActual(String estadoActual, Pageable pageable);
    List<TramiteEstudiantil> findByExpedienteId(Long expedienteId);
}

