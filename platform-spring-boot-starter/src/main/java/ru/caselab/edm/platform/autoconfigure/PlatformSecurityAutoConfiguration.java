package ru.caselab.edm.platform.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.caselab.edm.platform.security.JwtProperties;
import ru.caselab.edm.platform.security.JwtTokenService;
import ru.caselab.edm.platform.security.JwtTokenServiceImpl;
import ru.caselab.edm.platform.security.SecurityProperties;

/**
 * Shared stateless JWT resource-server configuration.
 *
 * <p>Every service validates the same access token and receives an
 * {@link ru.caselab.edm.platform.security.AuthenticatedUser} principal without
 * calling the identity service on each request.</p>
 */
@AutoConfiguration(before = UserDetailsServiceAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties({JwtProperties.class, SecurityProperties.class})
@Import(PlatformSecurityWebConfiguration.class)
public class PlatformSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JwtTokenService.class)
    public JwtTokenService jwtTokenService(JwtProperties properties) {
        return new JwtTokenServiceImpl(properties);
    }

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Suppresses Spring Boot's generated in-memory user. Resource servers have
     * no login endpoint; the identity service supplies its own database-backed
     * {@link UserDetailsService}.
     */
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService disabledDefaultUserDetailsService() {
        return username -> {
            throw new UsernameNotFoundException(username);
        };
    }
}
