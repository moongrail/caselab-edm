package ru.caselab.edm.backend.testutils.builder.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.caselab.edm.backend.testutils.builder.BaseBuilder;
import ru.caselab.edm.backend.dto.user.UpdateUserDTO;
import ru.caselab.edm.backend.enums.RoleName;

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
