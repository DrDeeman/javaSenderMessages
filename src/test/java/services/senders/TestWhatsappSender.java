package services.senders;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import records.StatusMessage;
import records.StructMessage;
import records.consumers.TelegramConsumer;
import records.consumers.WhatsappConsumer;

import java.io.IOException;
import java.util.HashMap;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
 class TestWhatsappSender {


    @Test
    @DisplayName("SenderMessageInTelegram Test")
    void TestSendMessage() throws IOException {
        WhatsappSender sender = new WhatsappSender(new WhatsappConsumer("79023789604"));
        StatusMessage status = sender.sendMessage(new StructMessage(1,"test","*test*__test", new HashMap<>()));
        assertTrue(status.status());
        assertEquals(200,status.bodyResponse().get("code").getAsInt());
    }
}