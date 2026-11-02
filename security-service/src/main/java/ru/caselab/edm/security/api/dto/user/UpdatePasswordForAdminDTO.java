package ru.caselab.edm.security.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import ru.caselab.edm.security.validation.annotations.Password;
import ru.caselab.edm.security.validation.annotations.PasswordMatcher;
import ru.caselab.edm.security.validation.interfaces.PasswordValidatable;

@Schema(description = "DTO for update password")
@PasswordMatcher
public record UpdatePasswordForAdminDTO(

        @Schema(description = "New password", example = "newPassword")
        @NotBlank
        @Password
        String newPassword,
        @Schema(description = "New password confirmation", example = "newPassword")
        @NotBlank
        String newPasswordConfirmation


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
