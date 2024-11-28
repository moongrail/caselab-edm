package ru.caselab.edm.backend.testutils.builder.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.caselab.edm.backend.testutils.builder.BaseBuilder;
import ru.caselab.edm.backend.dto.user.CreateUserDTO;
import ru.caselab.edm.backend.enums.RoleName;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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


    public static CreateUserDtoBuilder builder() {
        return new CreateUserDtoBuilder();
    }

    public CreateUserDtoBuilder withDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
        return this;
    }

    public CreateUserDtoBuilder withLogin(String login) {
        this.login = login;
        return this;
    }

    public CreateUserDtoBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public CreateUserDtoBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public CreateUserDtoBuilder withPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
        return this;
    }

    public CreateUserDtoBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public CreateUserDtoBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public CreateUserDtoBuilder withPatronymic(String patronymic) {
        this.patronymic = patronymic;
        return this;
    }

    public CreateUserDtoBuilder withPosition(String position) {
        this.position = position;
        return this;
    }

    public CreateUserDtoBuilder withRoles(RoleName[] roles) {
        this.roles = roles;
        return this;
    }

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
