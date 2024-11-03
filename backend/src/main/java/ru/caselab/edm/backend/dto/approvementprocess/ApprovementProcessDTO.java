package ru.caselab.edm.backend.dto.approvementprocess;

import lombok.Data;
import ru.caselab.edm.backend.enums.ApprovementProcessStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApprovementProcessDTO {

    private Long id;

    private Long documentVersionId;

    private ApprovementProcessStatus status;

    private LocalDateTime deadline;

    private float agreementPercent;

    private List<Long> approvementProcessItemsIds;
}
