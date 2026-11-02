package ru.caselab.edm.platform.mail;

/**
 * Generic HTML email message.
 */
public record EmailMessage(String to, String subject, String html) {
}
