package services.senders;

import com.google.gson.JsonObject;
import exceptions.CustomException;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.AllArgsConstructor;
import records.StructMessage;
import records.consumers.MailConsumer;
import records.StatusMessage;


import java.io.IOException;
import java.util.Properties;

@AllArgsConstructor
public class MailSender implements SenderInterface{

    private final MailConsumer consumer;
    private static final  String SMTP_HOST;
    private static final  String SMTP_PORT;
    private static final  String SMTP_SENDER;
    private static final  String AUTH_PASS;

    static{
        try {
            Properties props = new Properties();
            props.load(MailSender.class.getClassLoader().getResourceAsStream("application.properties"));
            SMTP_HOST = props.getProperty("mail.smtp_host");
            SMTP_PORT = props.getProperty("mail.smtp_port");
            SMTP_SENDER = props.getProperty("mail.smtp_sender");
            AUTH_PASS = props.getProperty("mail.auth_pass");
        } catch (IOException e) {
            throw new CustomException(e);
        }
    }

    @Override
    public StatusMessage sendMessage(StructMessage message) {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", SMTP_HOST);
        prop.put("mail.smtp.port", SMTP_PORT);
        prop.put("mail.sender", SMTP_SENDER);

        Session session = Session.getInstance(prop, new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(SMTP_SENDER,AUTH_PASS);
            }
        });

        JsonObject obj = new JsonObject();

        try {
            Message m = new MimeMessage(session);
            m.setFrom(new InternetAddress(SMTP_SENDER));
            m.setRecipients(Message.RecipientType.TO, InternetAddress.parse(consumer.mail()));
            m.setSubject("sub");
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(message.message(), "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            m.setContent(multipart);

            Transport.send(m);
        }catch(MessagingException e){
            obj.addProperty("description", e.getMessage());
        }

        return new StatusMessage(true, obj);
    }
}
