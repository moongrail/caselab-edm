package ru.caselab.edm.backend.dto.replacementmanagement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.caselab.edm.backend.dto.user.UserDTO;
import ru.caselab.edm.backend.entity.User;

import java.util.List;

@Data
@Schema(description = "DTO for representing user for replacement")
public class UsersForReplacementDTO {
    @Schema(description = "Department ID", example = "1")
    private Long departmentId;

    @Schema(description = "Login", example = "login")
    private List<UserDTO> users;
}
