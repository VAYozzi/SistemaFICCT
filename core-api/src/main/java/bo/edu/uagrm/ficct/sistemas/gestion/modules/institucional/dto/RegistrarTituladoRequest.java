package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarTituladoRequest(
    @NotNull(message = "El ID del expediente es obligatorio")
    Long expedienteId,

    @NotBlank(message = "La modalidad de graduación es obligatoria (TESIS, PROYECTO_GRADO, etc.)")
    String modalidadGraduacion,

    @NotNull(message = "La fecha de defensa es obligatoria")
    LocalDate fechaDefensa,

    @NotNull(message = "La nota final es obligatoria")
    BigDecimal notaFinal,

    String numeroTituloProvision,
    Long documentoActaId
) {}

