package ru.caselab.edm.backend.testutils.builder.user;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.caselab.edm.backend.testutils.builder.BaseBuilder;
import ru.caselab.edm.backend.entity.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor(staticName = "builder")
@With
public class UserEntityBuilder implements BaseBuilder<User> {

    private String login = "default_user";
    private String email = "default@example.com";
    private String password = "Password123!";
    private String firstName = "DefaultFirstName";
    private String lastName = "DefaultLastName";
    private String patronymic = "DefaultPatronymic";
    private String position = "DefaultPosition";

    private Set<Role> roles = new HashSet<>();
    private Set<RefreshToken> refreshTokens = new HashSet<>();
    private Set<Document> documents = new HashSet<>();
    private Set<ReplacementManager> replacementManagers = new HashSet<>();
    private Set<ReplacementManager> tempReplacementManagers = new HashSet<>();
    private List<ApprovementProcessItem> approvementProcessItems = new ArrayList<>();

    @Override
    public User build() {
        return User.builder()
                .login(login)
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .patronymic(patronymic)
                .position(position)
                .roles(roles)
                .refreshTokens(refreshTokens)
                .documents(documents)
                .replacementManagers(replacementManagers)
                .tempReplacementManagers(tempReplacementManagers)
                .approvementProcessItems(approvementProcessItems)
                .build();
    }
}
