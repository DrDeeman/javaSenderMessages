package services.senders;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import records.StatusMessage;
import records.StructMessage;
import records.consumers.GooglePushConsumer;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class TestGooglePushSender {


    @Test
    @DisplayName("SenderMessageInFirebase Test")
    void TestSendMessage() throws FirebaseMessagingException {
        HashSet<String> tokens = new HashSet<>();
        tokens.add("dKUkyZm9RRqkZvvlUWptxr:APA91bFrR1-336a6Vj03JonnuqdMi3SlYsuObmZhb6qCUD4ZVmUX22ZP3ueqbaWF15bU8ZgAjW9uJxgRCAKGs24kbw02dvwOxq-ojQ2OXgtK72bJ8CjiDcHYABf-YXcyqCxE0cpM8O33");
        //add google token in tokens for test
        GooglePushSender sender = new GooglePushSender(new GooglePushConsumer(tokens));
        StatusMessage status = sender.sendMessage(new StructMessage(1,"test","test", new HashMap<>()));
        assertTrue(status.status());
    }
}
