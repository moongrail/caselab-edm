package ru.caselab.edm.backend.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.service.EmailService;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value(value = "${spring.mail.username}")
    private String senderEmail;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendEmailForSign(ApprovementProcessItem processItem) {
        try {
            log.info("Preparing email to send to user: {}", processItem.getUser().getEmail());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            User user = processItem.getUser();

            helper.setFrom(senderEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Подписание документа");

            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("lastName", user.getLastName());
            context.setVariable("documentName", processItem.getDocumentVersion().getDocumentName());

            helper.setText(templateEngine.process("email/sign", context), true);

            message.setHeader("X-Priority", "1");
            message.setHeader("Importance", "High");

            mailSender.send(message);
            log.info("Email sent successfully to user: {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Error while sending email to %s: %s".formatted(processItem.getUser().getEmail(), e.getMessage()));
        }
    }
}
