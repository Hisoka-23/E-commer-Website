package com.ecom.util;

import com.ecom.model.ProductOrder;
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

    String msg = null;

    public Boolean sendMailForProductOrder(ProductOrder order, String status) throws MessagingException, UnsupportedEncodingException {
        msg = "<p>Hello  [[name]],</p>"+"<p>Thank you order <b>[[orderStatus]]</b>.</p>"
                +"<p>Product Details : </p>"
                +"<p>Name : [[productName]]</p>"
                +"<p>Category : [[category]]</p>"
                +"<p>Quantity : [[quantity]]</p>"
                +"<p>Price : [[price]]</p>"
                +"<p>Payment Type :[[paymentType]] </p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("prakash.chaurasiya214522@gmail.com", "Shopping Cart");
        helper.setTo(order.getOrderAddress().getEmail());

        msg = msg.replace("[[name]]", order.getOrderAddress().getFirstName());
        msg = msg.replace("[[orderStatus]]", status);
        msg = msg.replace("[[productName]]", order.getProduct().getTitle());
        msg = msg.replace("[[category]]", order.getProduct().getCategory());
        msg = msg.replace("[[quantity]]", order.getQuantity().toString());
        msg = msg.replace("[[price]]", order.getProduct().getPrice().toString());
        msg = msg.replace("[[paymentType]]", order.getPaymentType());

        helper.setSubject("Product Order Status");
        helper.setText(msg, true);
        mailSender.send(message);
        return  true;
    }

}
