package ru.caselab.edm.backend.dto;

import jakarta.persistence.*;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.enums.ApprovementProcessStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ApprovementProcessDTO {

    private Long id;

    private Long documentVersionId;

    private ApprovementProcessStatus status;

    private LocalDateTime deadline;

    private float agreementProcent;

    private List<Long> approvementProcessItemsIds;
}
