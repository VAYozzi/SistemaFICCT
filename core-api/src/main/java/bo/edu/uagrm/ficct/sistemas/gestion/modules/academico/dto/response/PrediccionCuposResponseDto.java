package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.dto.response;

import java.math.BigDecimal;

public record PrediccionCuposResponseDto(
    String materiaSigla,
    BigDecimal probabilidadSobredemanda,
    String nivelAlerta, // 'ROJO', 'AMARILLO', 'VERDE'
    Integer cuposOfertados,
    Integer demandaProyectada,
    Integer deficitEstimado,
    Boolean esDatosPrueba,
    String avisoInstitucional
) {}

