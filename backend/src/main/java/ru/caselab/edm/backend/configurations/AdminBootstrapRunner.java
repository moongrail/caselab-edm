package ru.caselab.edm.backend.configurations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.configurations.props.AdminBootstrapProperties;
import ru.caselab.edm.backend.entity.Role;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.enums.RoleName;
import ru.caselab.edm.backend.repository.RoleRepository;
import ru.caselab.edm.backend.repository.UserRepository;

import java.util.Set;

/**
 * Создаёт первого администратора при пустой базе.
 * Включается только флагом BOOTSTRAP_ADMIN_ENABLED=true.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AdminBootstrapProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }

        validateConfiguration();

        if (userRepository.findUserByLogin(properties.login()).isPresent()) {
            log.info("Bootstrap admin '{}' already exists, skipping", properties.login());
            return;
        }
        if (userRepository.existsByEmail(properties.email())) {
            throw new IllegalStateException(
                    "Cannot bootstrap admin: email %s is already used by another user"
                            .formatted(properties.email()));
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot bootstrap admin: role ADMIN is missing. Check Liquibase migrations."));

        User admin = User.builder()
                .login(properties.login())
                .email(properties.email())
                .password(passwordEncoder.encode(properties.password()))
                .firstName(properties.firstName())
                .lastName(properties.lastName())
                .position(properties.position())
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
        log.info("Bootstrap admin '{}' created", admin.getLogin());
    }

    private void validateConfiguration() {
        if (isBlank(properties.login()) || isBlank(properties.email()) || isBlank(properties.password())) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_LOGIN, BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD are required "
                            + "when BOOTSTRAP_ADMIN_ENABLED=true");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
