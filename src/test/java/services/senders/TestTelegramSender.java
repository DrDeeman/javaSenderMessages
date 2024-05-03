package services.senders;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import records.StatusMessage;
import records.StructMessage;
import records.consumers.TelegramConsumer;

import java.io.IOException;
import java.util.HashMap;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class TestTelegramSender {


    @Test
    @DisplayName("SenderMessageInTelegram Test")
     void TestSendMessage() throws IOException {
       TelegramSender sender = new TelegramSender(new TelegramConsumer(6288237005L));
       StatusMessage status = sender.sendMessage(new StructMessage(1,"test","test__test", new HashMap<>()));
       assertTrue(status.status());
       assertEquals(200,status.bodyResponse().get("code").getAsInt());
    }
}
