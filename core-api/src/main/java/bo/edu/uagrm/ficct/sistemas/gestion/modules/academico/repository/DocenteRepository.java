package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model.Docente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {
    Optional<Docente> findByCodigoDocente(String codigoDocente);
    List<Docente> findByActivoTrueOrderByNombreCompletoAsc();
}

