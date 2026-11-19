package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CrearMateriaRequest(
    @NotBlank(message = "La sigla es obligatoria (ej. INF110)")
    String sigla,

    @NotBlank(message = "El nombre de la materia es obligatorio")
    String nombre,

    @NotNull(message = "El nivel de semestre es obligatorio (1 a 10)")
    Integer semestreNivel,

    @NotNull(message = "Los créditos son obligatorios")
    Integer creditos,

    List<Long> prerrequisitosIds
) {}

