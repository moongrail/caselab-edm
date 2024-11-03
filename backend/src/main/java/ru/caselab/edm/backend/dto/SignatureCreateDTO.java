package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
public class SignatureCreateDTO {
    @JsonProperty("userId")
    @NotNull
    @Schema(description = "User ID", format = "uuid", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @JsonProperty("status")
    @NotNull
    @Pattern(regexp = "APPROVED|REJECTED|REWORK_REQUIRED", message = "Status must be either APPROVED or REJECTED or REWORK_REQUIRED")
    @Schema(description = "Status of sign (does user approved/rejected/rework_required", example = "APPROVED")
    private String status;

}
