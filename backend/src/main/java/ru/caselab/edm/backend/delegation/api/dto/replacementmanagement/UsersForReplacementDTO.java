package ru.caselab.edm.backend.delegation.api.dto.replacementmanagement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.caselab.edm.backend.shared.directory.api.UserInfoDTO;

import java.util.List;

@Data
@Schema(description = "DTO for representing user for replacement")
public class UsersForReplacementDTO {
    @Schema(description = "Department ID", example = "1")
    private Long departmentId;

    @Schema(description = "Users")
    private List<UserInfoDTO> users;
}
