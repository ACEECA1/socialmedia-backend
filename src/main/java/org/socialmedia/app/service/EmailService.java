package org.socialmedia.app.service;

import org.socialmedia.app.util.EmailUtil;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final EmailUtil emailUtil;

    private final String frontendUrl = "http://localhost:3000";
    public EmailService(EmailUtil emailUtil) {
        this.emailUtil = emailUtil;
    }

    public void sendVerificationEmail(String toEmail, String code) {
        String subject = "Verify your email address";
        
        String verifyLink = frontendUrl + "/verify?email=" + toEmail + "&code=" + code;

        String htmlContent = "<h2>Welcome to SocialMedia!</h2>"
                + "<p>Your 6-digit verification code is: <strong>" + code + "</strong></p>"
                + "<p>Or, you can just click the link below to verify automatically:</p>"
                + "<a href=\"" + verifyLink + "\">Verify Email</a>"
                + "<p>This code will expire in 15 minutes.</p>";

        emailUtil.sendEmail(toEmail, subject, htmlContent);
    }

    public void sendPasswordResetEmail(String toEmail, String code) {
        String subject = "Reset your password";
        
        String resetLink = frontendUrl + "/reset-password?email=" + toEmail + "&code=" + code;

        String htmlContent = "<h2>Password Reset</h2>"
                + "<p>Your 6-digit password reset code is: <strong>" + code + "</strong></p>"
                + "<p>Or click the link below to reset it automatically:</p>"
                + "<a href=\"" + resetLink + "\">Reset Password</a>"
                + "<p>This code will expire in 15 minutes. If you did not request this, please ignore this email.</p>";

        emailUtil.sendEmail(toEmail, subject, htmlContent);
    }
}
