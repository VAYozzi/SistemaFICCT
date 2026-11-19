package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response;

import java.math.BigDecimal;

public record ResultadoBusquedaDocumentalDto(
    Long paginaId,
    Long documentoId,
    Integer numeroPagina,
    BigDecimal confianzaPagina,
    String fragmentoResaltado,
    String nombreOriginal,
    String rutaAlmacenamiento,
    String tipoMime
) {}

