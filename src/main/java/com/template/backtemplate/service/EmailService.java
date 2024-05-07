package com.template.backtemplate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailService {

  @Value("${mail.enabled}")
  private boolean mailEnabled;

  private final JavaMailSender emailSender;

  @Autowired
  public EmailService(JavaMailSender emailSender) {
    this.emailSender = emailSender;
  }

  public void sendSimpleMessage(
      String to, String subject, String text) throws MailException, MessagingException {
    if (!mailEnabled) {
      return;
    }
    MimeMessage mimeMessage = emailSender.createMimeMessage();
    MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
    messageHelper.setFrom(new InternetAddress("noreply@test.com"));
    messageHelper.setTo(to);
    messageHelper.setSubject(subject);
    messageHelper.setText(text);

    emailSender.send(mimeMessage);
  }
}