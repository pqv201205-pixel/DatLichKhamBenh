package com.example.demo.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetOtp(
            String toEmail,
            String username,
            String otp){

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(toEmail);

        message.setSubject(
                "Reset Password OTP"
        );

        message.setText(
                "Xin chào " + username +
                        "\n\nOTP của bạn là: " + otp +
                        "\nOTP có hiệu lực 15 phút."
        );

        mailSender.send(message);
    }
}