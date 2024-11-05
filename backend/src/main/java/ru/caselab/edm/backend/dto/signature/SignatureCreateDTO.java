package ru.caselab.edm.backend.dto.signature;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class SignatureCreateDTO {

    @JsonProperty("status")
    @NotNull
    @Pattern(regexp = "APPROVED|REJECTED|REWORK_REQUIRED", message = "Status must be either APPROVED or REJECTED or REWORK_REQUIRED")
    @Schema(description = "Status of sign (does user approved/rejected/rework_required", example = "APPROVED")
    private String status;

}
