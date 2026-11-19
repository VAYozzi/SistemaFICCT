package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.DocumentoDigital;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoDigitalRepository extends JpaRepository<DocumentoDigital, Long> {
    Optional<DocumentoDigital> findByHashSha256(String hashSha256);
}

