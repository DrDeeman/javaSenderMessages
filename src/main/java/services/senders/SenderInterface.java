package services.senders;

import records.StatusMessage;
import records.StructMessage;

public interface SenderInterface {

    StatusMessage sendMessage(StructMessage message) throws Exception;
}
