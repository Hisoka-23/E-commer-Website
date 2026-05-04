package com.ecom.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class CommonUtil {

    @Autowired
    private JavaMailSender mailSender;

    public Boolean sendMail(String url, String reciepentEmail) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("prakash.chaurasiya214522@gmail.com", "Shopping Cart");
        helper.setTo(reciepentEmail);

        String content = "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>"
                + "<p><a href='" + url + "'>Reset Password</a></p>"
                + "<br>"
                + "<p>If you did not request this, please ignore this email.</p>"
                + "<p>Thank you,<br>Shopping Cart Team</p>";

        helper.setSubject("Password Reset");
        helper.setText(content, true);
        mailSender.send(message);
        return  true;
    }

    public static String genrateUrl(HttpServletRequest request) {
        String siteUrl =  request.getRequestURL().toString();

        return  siteUrl.replace(request.getServletPath(), "");
    }
}
