package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DocumentoOcrResponse(
    Long documentoId,
    String nombreOriginal,
    String rutaAlmacenamiento,
    String tipoMime,
    String estadoOcr,
    BigDecimal confianzaPromedio,
    List<PaginaOcrDto> paginas
) {
    public record PaginaOcrDto(
        Long id,
        Integer numeroPagina,
        String textoExtraidoBruto,
        String textoCorregido,
        BigDecimal confianzaPagina,
        Integer tiempoProcesamientoMs
    ) {}
}

