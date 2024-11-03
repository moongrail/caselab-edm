package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class ApprovementProcessCreateDTO {

    private Long documentVersionId;
    @NotNull
    private LocalDateTime deadline;

    private float agreementProcent;
    @NotNull
    private Set<UUID> usersIds;
}
