package services.senders;

import records.StatusMessage;

public interface SenderInterface {

    StatusMessage sendMessage(String message) throws Exception;
}
