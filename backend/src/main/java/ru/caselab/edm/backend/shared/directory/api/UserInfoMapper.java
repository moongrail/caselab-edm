package ru.caselab.edm.backend.shared.directory.api;

import ru.caselab.edm.backend.shared.directory.UserPage;
import ru.caselab.edm.backend.shared.directory.UserSummary;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class UserInfoMapper {

    private UserInfoMapper() {
    }

    public static UserInfoDTO toDto(UserSummary user) {
        Set<RoleInfoDTO> roles = user.roles() == null
                ? Set.of()
                : user.roles().stream()
                .map(name -> new RoleInfoDTO(null, name))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        return new UserInfoDTO(user.id(), user.login(), user.email(), user.firstName(),
                user.lastName(), user.patronymic(), user.position(), roles);
    }

    public static List<UserInfoDTO> toDto(List<UserSummary> users) {
        return users.stream().map(UserInfoMapper::toDto).toList();
    }

    public static UserInfoPageDTO toPage(UserPage page) {
        return new UserInfoPageDTO(page.number(), page.size(), page.totalPages(),
                (int) Math.min(Integer.MAX_VALUE, page.totalElements()),
                toDto(page.content()));
    }

    public static UserInfoPageDTO toPage(org.springframework.data.domain.Page<UserSummary> page) {
        return new UserInfoPageDTO(page.getNumber(), page.getSize(), page.getTotalPages(),
                (int) Math.min(Integer.MAX_VALUE, page.getTotalElements()),
                toDto(page.getContent()));
    }
}
