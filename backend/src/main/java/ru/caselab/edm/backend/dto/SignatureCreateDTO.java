package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "DTO for create sign")
public class SignatureCreateDTO {
    @JsonProperty("userId")
    @NotNull
    @Schema(description = "User id", example = "0473e603-6144-4d1d-ba7b-e2fc475dcf89")
    private UUID userId;

}
