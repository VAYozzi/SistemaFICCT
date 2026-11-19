package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.InvitacionAcceso;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitacionAccesoRepository extends JpaRepository<InvitacionAcceso, Long> {
    Optional<InvitacionAcceso> findByTokenInvitacion(UUID tokenInvitacion);
    Page<InvitacionAcceso> findAll(Pageable pageable);
}

