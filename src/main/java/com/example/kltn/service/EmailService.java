package com.example.kltn.service;

public interface EmailService {
    
    void sendVerificationEmail(String toEmail, String verificationCode);
    
    void sendResetPasswordEmail(String toEmail, String resetToken);
    
    void sendWelcomeEmail(String toEmail, String fullName);
    
    void sendEmail(String toEmail, String subject, String content);
}