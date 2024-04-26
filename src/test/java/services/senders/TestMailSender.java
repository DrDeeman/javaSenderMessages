package services.senders;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import records.StatusMessage;
import records.consumers.MailConsumer;

import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class TestMailSender {


    @Test
    @DisplayName("SenderMessageInTelegram Test")
    void TestSendMessage() throws MessagingException {
        MailSender sender = new MailSender(new MailConsumer("stalkerdrdeeman@gmailx.com"));
        StatusMessage status = sender.sendMessage("testing");
        assertTrue(status.status());
    }
}
