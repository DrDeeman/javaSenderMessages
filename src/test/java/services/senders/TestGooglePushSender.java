package services.senders;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import records.StatusMessage;
import records.consumers.GooglePushConsumer;

import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class TestGooglePushSender {


    @Test
    @DisplayName("SenderMessageInFirebase Test")
    void TestSendMessage() throws FirebaseMessagingException {
        GooglePushSender sender = new GooglePushSender(new GooglePushConsumer("cma0NuNYTUWltR47L8a4DE:APA91bE9r4fP17IyanT_LQPcJBTjxcB3oxhx6-1igw6u8Qvll3vK6VUOnppSpulz1VIQdvxNZ2Hc52NA7zAbIDm5vnS6VL5sMlHNJMN0dsdrtp4cg0dkfGFqEOZg3P5cksZF021_5yUk"));
        StatusMessage status = sender.sendMessage("testing");
        assertTrue(status.status());
    }
}
