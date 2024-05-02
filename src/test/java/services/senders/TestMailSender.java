package services.senders;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import records.StatusMessage;
import records.StructMessage;
import records.consumers.MailConsumer;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class TestMailSender {


    @Test
    @DisplayName("SenderMessageInTelegram Test")
    void TestSendMessage() throws MessagingException {
        MailSender sender = new MailSender(new MailConsumer("stalkerdrdeeman@gmailx.com"));
        StatusMessage status = sender.sendMessage(new StructMessage(1,"test","test", new HashMap<>()));
        assertTrue(status.status());
    }
}
