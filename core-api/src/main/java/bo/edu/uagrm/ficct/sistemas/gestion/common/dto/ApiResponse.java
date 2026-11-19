package bo.edu.uagrm.ficct.sistemas.gestion.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    Instant timestamp,
    List<ErrorDetail> errors
) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now(), null);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now(), null);
    }

    public static <T> ApiResponse<T> error(String message, List<ErrorDetail> errors) {
        return new ApiResponse<>(false, message, null, Instant.now(), errors);
    }

    public record ErrorDetail(String field, String issue) {}
}

