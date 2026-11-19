package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ValidarOcrRequest(
    @NotNull(message = "El ID del documento es obligatorio")
    Long documentoId,

    List<PaginaOcrValidada> paginas
) {
    public record PaginaOcrValidada(
        @NotNull(message = "El número de página es obligatorio")
        Integer numeroPagina,

        String textoCorregido
    ) {}
}

