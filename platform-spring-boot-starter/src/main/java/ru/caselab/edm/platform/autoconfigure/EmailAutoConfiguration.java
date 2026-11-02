package ru.caselab.edm.platform.autoconfigure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import ru.caselab.edm.platform.mail.EmailSender;
import ru.caselab.edm.platform.mail.SmtpEmailSender;

/**
 * SMTP email integration, activated only when Spring Boot has configured a
 * {@link JavaMailSender}.
 */
@AutoConfiguration
@ConditionalOnClass(JavaMailSender.class)
@ConditionalOnBean(JavaMailSender.class)
public class EmailAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EmailSender.class)
    public EmailSender emailSender(
            JavaMailSender mailSender,
            @Value("${platform.mail.from:${spring.mail.username:}}") String from) {
        return new SmtpEmailSender(mailSender, from);
    }
}
