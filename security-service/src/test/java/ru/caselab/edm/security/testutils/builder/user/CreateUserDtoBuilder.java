package ru.caselab.edm.security.testutils.builder.user;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.caselab.edm.security.api.dto.user.CreateUserDTO;
import ru.caselab.edm.security.domain.RoleName;
import ru.caselab.edm.security.testutils.builder.BaseBuilder;

@AllArgsConstructor
@NoArgsConstructor(staticName = "builder")
@With
public class CreateUserDtoBuilder implements BaseBuilder<CreateUserDTO> {

    private Long departmentId = 1L;
    private String login = "default_user";
    private String email = "default@example.com";
    private String password = "Password123!";
    private String passwordConfirmation = "Password123!";
    private String firstName = "DefaultFirstName";
    private String lastName = "DefaultLastName";
    private String patronymic = "DefaultPatronymic";
    private String position = "DefaultPosition";
    private RoleName[] roles = new RoleName[]{RoleName.USER};

    @Override
    public CreateUserDTO build() {
        return new CreateUserDTO(
                departmentId,
                login,
                email,
                password,
                passwordConfirmation,
                firstName,
                lastName,
                patronymic,
                position,
                roles
        );
    }

}
