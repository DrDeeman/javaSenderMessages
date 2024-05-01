package services.senders;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import records.StatusMessage;
import records.consumers.GooglePushConsumer;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class TestGooglePushSender {


    @Test
    @DisplayName("SenderMessageInFirebase Test")
    void TestSendMessage() throws FirebaseMessagingException {
        HashSet<String> tokens = new HashSet<>();
        //add google token in tokens for test
        GooglePushSender sender = new GooglePushSender(new GooglePushConsumer(tokens));
        StatusMessage status = sender.sendMessage("testing");
        assertTrue(status.status());
    }
}
