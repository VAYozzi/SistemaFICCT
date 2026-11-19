package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.repository;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.DocumentoPaginaOcr;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoPaginaOcrRepository extends JpaRepository<DocumentoPaginaOcr, Long> {

    List<DocumentoPaginaOcr> findByDocumentoIdOrderByNumeroPaginaAsc(Long documentoId);

    Optional<DocumentoPaginaOcr> findByDocumentoIdAndNumeroPagina(Long documentoId, Integer numeroPagina);

    @Query(value = """
        SELECT p.id, p.documento_id, p.numero_pagina, p.confianza_pagina,
               ts_headline('spanish', COALESCE(p.texto_corregido, ''), plainto_tsquery('spanish', :termino), 
                           'StartSel=<mark>, StopSel=</mark>') AS fragmento,
               d.nombre_original, d.ruta_almacenamiento, d.tipo_mime
        FROM documento_paginas_ocr p
        JOIN documentos_digitales d ON d.id = p.documento_id
        WHERE p.vector_busqueda @@ plainto_tsquery('spanish', :termino)
        ORDER BY ts_rank(p.vector_busqueda, plainto_tsquery('spanish', :termino)) DESC
        """, nativeQuery = true)
    List<Object[]> buscarTextoCompletoConResaltado(@Param("termino") String termino);
}

