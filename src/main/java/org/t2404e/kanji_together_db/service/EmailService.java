package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.from}")
    private String fromAddress;

    @Value("${otp.email.subject:Kanji Together OTP}")
    private String otpSubject;

    public void sendOtpEmail(String to, String otpCode, int ttlMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(otpSubject);
        message.setText("Your OTP code is: " + otpCode + "\nThis code expires in " + ttlMinutes + " minutes.");
        mailSender.send(message);
    }
}
