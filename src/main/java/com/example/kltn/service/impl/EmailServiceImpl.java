package com.example.kltn.service.impl;

import com.example.kltn.service.EmailService;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @Value("${app.backend.url}")
    private String backendUrl;
    
    @Override
    @Async
    public void sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            String verificationLink = frontendUrl + "/verify-email?token=" + verificationCode;
            
            Context context = new Context(Locale.getDefault());
            context.setVariable("verificationLink", verificationLink);
            context.setVariable("email", toEmail);
            
            String htmlContent = templateEngine.process("email/verify-email", context);
            
            sendEmail(toEmail, "Xác thực tài khoản Fashion Store", htmlContent);
            
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Không thể gửi email xác thực");
        }
    }
    
    @Override
    @Async
    public void sendResetPasswordEmail(String toEmail, String resetToken) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            
            Context context = new Context(Locale.getDefault());
            context.setVariable("resetLink", resetLink);
            context.setVariable("email", toEmail);
            
            String htmlContent = templateEngine.process("email/reset-password", context);
            
            sendEmail(toEmail, "Đặt lại mật khẩu Fashion Store", htmlContent);
            
            log.info("Reset password email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send reset password email to: {}", toEmail, e);
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu");
        }
    }
    
    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("fullName", fullName);
            context.setVariable("email", toEmail);
            context.setVariable("storeUrl", frontendUrl);
            
            String htmlContent = templateEngine.process("email/welcome", context);
            
            sendEmail(toEmail, "Chào mừng đến với Fashion Store", htmlContent);
            
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }
    
    @Override
    @Async
    public void sendEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, "Fashion Store");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(mimeMessage);

            log.info("Email sent successfully to: {}", toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("Không thể gửi email");
        }
    }

}