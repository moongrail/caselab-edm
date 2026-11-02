package ru.caselab.edm.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.caselab.edm.security.jwt.RefreshTokenService;
import ru.caselab.edm.security.jwt.RefreshTokenServiceImpl;
import ru.caselab.edm.security.jwt.UserDetailsServiceImpl;
import ru.caselab.edm.security.internal.InternalApiProperties;
import ru.caselab.edm.security.internal.InternalTokenFilter;
import ru.caselab.edm.security.mapper.department.DepartmentMapper;
import ru.caselab.edm.security.mapper.user.UserMapper;
import ru.caselab.edm.security.persistence.DepartmentRepository;
import ru.caselab.edm.security.persistence.RefreshTokenRepository;
import ru.caselab.edm.security.persistence.RoleRepository;
import ru.caselab.edm.security.persistence.UserRepository;
import ru.caselab.edm.security.service.AuthService;
import ru.caselab.edm.security.service.DepartmentService;
import ru.caselab.edm.security.service.UserService;
import ru.caselab.edm.security.service.impl.AuthServiceImpl;
import ru.caselab.edm.security.service.impl.DepartmentServiceImpl;
import ru.caselab.edm.security.service.impl.UserServiceImpl;
import ru.caselab.edm.platform.security.JwtProperties;
import ru.caselab.edm.platform.security.JwtTokenService;

/**
 * Application services and authentication wiring for the identity service.
 */
@Configuration(proxyBeanMethods = false)
public class SecurityServiceConfiguration {

    @Bean
    public InternalTokenFilter internalTokenFilter(InternalApiProperties properties) {
        return new InternalTokenFilter(properties);
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new UserDetailsServiceImpl(userRepository);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public RefreshTokenService refreshTokenService(UserRepository userRepository,
                                                   RefreshTokenRepository refreshTokenRepository,
                                                   JwtProperties properties) {
        return new RefreshTokenServiceImpl(userRepository, refreshTokenRepository, properties);
    }

    @Bean
    public AuthService authService(AuthenticationManager authenticationManager,
                                   JwtTokenService jwtTokenService,
                                   RefreshTokenService refreshTokenService,
                                   JwtClaimsFactory jwtClaimsFactory) {
        return new AuthServiceImpl(authenticationManager, jwtTokenService, refreshTokenService,
                jwtClaimsFactory);
    }

    @Bean
    public UserService userService(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   DepartmentRepository departmentRepository,
                                   UserMapper userMapper,
                                   PasswordEncoder passwordEncoder,
                                   RefreshTokenRepository refreshTokenRepository) {
        return new UserServiceImpl(userRepository, roleRepository, departmentRepository,
                userMapper, passwordEncoder, refreshTokenRepository);
    }

    @Bean
    public DepartmentService departmentService(DepartmentRepository departmentRepository,
                                               UserRepository userRepository,
                                               DepartmentMapper departmentMapper,
                                               UserMapper userMapper) {
        return new DepartmentServiceImpl(departmentRepository, userRepository,
                departmentMapper, userMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.bootstrap.admin", name = "enabled",
            havingValue = "true")
    public AdminBootstrapRunner adminBootstrapRunner(AdminBootstrapProperties properties,
                                                     UserRepository userRepository,
                                                     RoleRepository roleRepository,
                                                     PasswordEncoder passwordEncoder) {
        return new AdminBootstrapRunner(properties, userRepository, roleRepository, passwordEncoder);
    }
}
