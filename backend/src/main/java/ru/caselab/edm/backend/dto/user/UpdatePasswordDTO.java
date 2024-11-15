package ru.caselab.edm.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import ru.caselab.edm.backend.validation.password.PasswordMatcher;
import ru.caselab.edm.backend.validation.password.PasswordValidatable;

@Schema(description = "DTO for update password")
@PasswordMatcher
public record UpdatePasswordDTO(

        @Schema(description = "Old password", example = "oldPassword")
        @NotBlank String oldPassword,

        @Schema(description = "New password", example = "newPassword")
        @NotBlank String newPassword,
        @Schema(description = "New password confirmation", example = "newPassword")
        @NotBlank String newPasswordConfirmation


) implements PasswordValidatable {
        @Override
        public String getPassword() {
                return this.newPassword;
        }

        @Override
        public String getPasswordConfirmation() {
                return this.newPasswordConfirmation;
        }
}
