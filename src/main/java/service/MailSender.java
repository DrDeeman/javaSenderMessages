package service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.AllArgsConstructor;
import records.MailConsumer;
import records.StatusMessage;
import records.TelegramConsumer;

import java.util.Properties;

@AllArgsConstructor
public class MailSender {

    private final MailConsumer consumer;
    private static final  String smtp_host = "mail.ruglonass.ru";
    private static final  String smtp_port = "587";
    private static final  String smtp_sender = "monitoring@ruglonass.ru";


    public StatusMessage sendMessage(String message) throws MessagingException {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", smtp_host);
        prop.put("mail.smtp.port", smtp_port);
        prop.put("mail.sender", smtp_sender);

        Session session = Session.getInstance(prop, new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(smtp_sender,"wtf1mkl8Q1");
            }
        });

        Message m = new MimeMessage(session);
        m.setFrom(new InternetAddress(smtp_sender));
        m.setRecipients(Message.RecipientType.TO,InternetAddress.parse(consumer.mail()));
        m.setSubject("sub");
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(message, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        m.setContent(multipart);

        Transport.send(m);

        return new StatusMessage(true, "Send message in mail successful");
    }
}
