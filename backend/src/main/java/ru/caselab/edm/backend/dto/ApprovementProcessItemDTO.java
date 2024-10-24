package ru.caselab.edm.backend.dto;

import jakarta.persistence.*;
import lombok.Data;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.Signature;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ApprovementProcessItemDTO {

    private Long id;

    private Long approvementProcessId;

    private UUID userId;

    private LocalDateTime createdAt;

    private ApprovementProcessItemStatus status;

    private Long documentVersionId;

    private Long signatureId;
}
