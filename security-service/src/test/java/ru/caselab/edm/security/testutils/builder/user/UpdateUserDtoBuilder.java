package ru.caselab.edm.security.testutils.builder.user;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.caselab.edm.security.api.dto.user.UpdateUserDTO;
import ru.caselab.edm.security.domain.RoleName;
import ru.caselab.edm.security.testutils.builder.BaseBuilder;

@AllArgsConstructor
@NoArgsConstructor(staticName = "builder")
@With
public class UpdateUserDtoBuilder implements BaseBuilder<UpdateUserDTO> {

    private String login = "default_user";
    private String email = "default@example.com";
    private String firstName = "DefaultFirstName";
    private String lastName = "DefaultLastName";
    private String patronymic = "DefaultPatronymic";
    private String position = "DefaultPosition";
    private RoleName[] roles = new RoleName[]{RoleName.USER};

    @Override
    public UpdateUserDTO build() {
        return new UpdateUserDTO(
                login,
                email,
                firstName,
                lastName,
                patronymic,
                position,
                roles
        );
    }
}
