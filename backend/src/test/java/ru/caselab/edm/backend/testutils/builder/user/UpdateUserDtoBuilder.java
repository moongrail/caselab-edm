package ru.caselab.edm.backend.testutils.builder.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.caselab.edm.backend.testutils.builder.BaseBuilder;
import ru.caselab.edm.backend.dto.user.UpdateUserDTO;
import ru.caselab.edm.backend.enums.RoleName;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateUserDtoBuilder implements BaseBuilder<UpdateUserDTO> {

    private String login = "default_user";
    private String email = "default@example.com";
    private String firstName = "DefaultFirstName";
    private String lastName = "DefaultLastName";
    private String patronymic = "DefaultPatronymic";
    private String position = "DefaultPosition";
    private RoleName[] roles = new RoleName[]{RoleName.USER};

    public static UpdateUserDtoBuilder builder() {
        return new UpdateUserDtoBuilder();
    }

    public UpdateUserDtoBuilder withLogin(String login) {
        this.login = login;
        return this;
    }

    public UpdateUserDtoBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UpdateUserDtoBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UpdateUserDtoBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UpdateUserDtoBuilder withPatronymic(String patronymic) {
        this.patronymic = patronymic;
        return this;
    }

    public UpdateUserDtoBuilder withPosition(String position) {
        this.position = position;
        return this;
    }

    public UpdateUserDtoBuilder withRoles(RoleName[] roles) {
        this.roles = roles;
        return this;
    }

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
