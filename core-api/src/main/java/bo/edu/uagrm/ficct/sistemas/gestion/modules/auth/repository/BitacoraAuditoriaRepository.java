package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.BitacoraAuditoria;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraAuditoriaRepository extends JpaRepository<BitacoraAuditoria, Long> {

    @Query("SELECT b FROM BitacoraAuditoria b WHERE " +
           "(:modulo IS NULL OR b.modulo = :modulo) AND " +
           "(:accion IS NULL OR b.accion = :accion) AND " +
           "(:correoActor IS NULL OR b.correoActor = :correoActor) AND " +
           "(CAST(:fechaDesde AS java.time.OffsetDateTime) IS NULL OR b.fechaHora >= :fechaDesde) AND " +
           "(CAST(:fechaHasta AS java.time.OffsetDateTime) IS NULL OR b.fechaHora <= :fechaHasta) " +
           "ORDER BY b.fechaHora DESC")
    Page<BitacoraAuditoria> findConFiltros(
        @Param("modulo") String modulo,
        @Param("accion") String accion,
        @Param("correoActor") String correoActor,
        @Param("fechaDesde") OffsetDateTime fechaDesde,
        @Param("fechaHasta") OffsetDateTime fechaHasta,
        Pageable pageable
    );
}
