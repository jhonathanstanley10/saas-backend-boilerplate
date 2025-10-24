package br.com.stanleydev.backendboilerplate.email;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;


    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        log.info("Attempting to send password reset email to {}", toEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Password Reset Request");

            String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;

            message.setText(
                    "Hello,\n\n" +
                            "You are receiving this email because a password reset request was made for your account.\n\n" +
                            "Please click the link below to reset your password:\n" +
                            resetUrl + "\n\n" +
                            "If you did not request this, please ignore this email.\n\n" +
                            "Thanks,\n" +
                            "The Team"
            );

            mailSender.send(message);
            log.info("Password reset email sent successfully to {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }


}