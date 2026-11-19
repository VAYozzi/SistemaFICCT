package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.model.Titulado;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TituladoRepository extends JpaRepository<Titulado, Long> {
    Optional<Titulado> findByExpedienteId(Long expedienteId);
    List<Titulado> findByModalidadGraduacion(String modalidadGraduacion);

    @Query("SELECT t.modalidadGraduacion, COUNT(t) FROM Titulado t GROUP BY t.modalidadGraduacion")
    List<Object[]> contarPorModalidad();
}

