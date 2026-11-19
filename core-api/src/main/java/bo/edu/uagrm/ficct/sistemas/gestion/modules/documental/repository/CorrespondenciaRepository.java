package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.Correspondencia;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorrespondenciaRepository extends JpaRepository<Correspondencia, Long> {
    Optional<Correspondencia> findByCodigoCite(String codigoCite);
    Page<Correspondencia> findByTipo(String tipo, Pageable pageable);
}

