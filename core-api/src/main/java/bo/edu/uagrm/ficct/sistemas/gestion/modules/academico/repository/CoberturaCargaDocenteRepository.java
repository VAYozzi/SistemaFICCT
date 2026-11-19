package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model.CoberturaCargaDocente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoberturaCargaDocenteRepository extends JpaRepository<CoberturaCargaDocente, Long> {
    List<CoberturaCargaDocente> findByOfertaGrupoPeriodoId(Long periodoId);

    @Query("""
        SELECT o FROM OfertaGruposMateria o
        WHERE o.periodo.id = :periodoId
        AND NOT EXISTS (
            SELECT c FROM CoberturaCargaDocente c
            WHERE c.ofertaGrupo.id = o.id AND c.estado = 'VIGENTE'
        )
        """)
    List<Object> encontrarAcefaliasPorPeriodo(@Param("periodoId") Long periodoId);
}

