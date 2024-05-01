package app;


import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import records.*;
import records.consumers.GooglePushConsumer;
import records.consumers.MailConsumer;
import records.consumers.TelegramConsumer;
import records.consumers.WhatsappConsumer;
import services.CustomLogger;
import services.senders.GooglePushSender;
import services.senders.MailSender;
import services.senders.TelegramSender;
import services.senders.WhatsappSender;


import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;


@AllArgsConstructor
public class SenderThread implements Callable<Map<String, StatusMessage>> {

    private final StructMessage message;



    @Override
    public Map<String, StatusMessage> call()throws IOException {

       CustomLogger logger = new CustomLogger(SenderThread.class.getName()+"-"+message.id());
       logger.log(Level.INFO,"start messages sender");


        Map<String,StatusMessage> senderStatus = new HashMap<>();
        Map<String, Record> consumers = message.consumers();
        long startTime = System.currentTimeMillis() / 1000;
        long nowTime;

        consumers.keySet().forEach(k-> {
            if(consumers.get(k)!=null) {
                senderStatus.put(k, new StatusMessage(false, new JsonObject()));
            }
        });



        try{
            for (;;) {
              try {
                  for (Map.Entry<String, StatusMessage> entry : senderStatus.entrySet()) {

                      String k = entry.getKey();
                      StatusMessage statusObject = entry.getValue();
                      boolean status = statusObject.status();

                      if (!status) {
                          Record consumer = consumers.get(k);

                          switch (consumer.getClass().getName()) {
                              case "records.consumers.TelegramConsumer":
                                  logger.log(Level.INFO, "start send in telegram");
                                  StatusMessage tresult = (new TelegramSender((TelegramConsumer) consumer)).sendMessage(message.message());
                                  logger.log(Level.INFO, tresult.bodyResponse().toString());
                                  senderStatus.put(k, tresult);
                                  break;

                              case "records.consumers.WhatsappConsumer":

                                  logger.log(Level.INFO, "start send in whatsapp");
                                  StatusMessage wresult = (new WhatsappSender((WhatsappConsumer) consumer)).sendMessage(message.message());
                                  logger.log(Level.INFO, wresult.bodyResponse().toString());
                                  senderStatus.put(k, wresult);
                                  break;

                              case "records.consumers.MailConsumer":

                                  logger.log(Level.INFO, "start send in mail");
                                  StatusMessage mresult = (new MailSender((MailConsumer) consumer)).sendMessage(message.message());
                                  logger.log(Level.INFO, mresult.bodyResponse().toString());
                                  senderStatus.put(k, mresult);
                                  break;

                              case "records.consumers.GooglePushConsumer":
                                    logger.log(Level.INFO,"start send google push in device");
                                    StatusMessage gpresult = (new GooglePushSender((GooglePushConsumer) consumer)).sendMessage(message.message());
                                    logger.log(Level.INFO,gpresult.bodyResponse().toString());
                                    senderStatus.put(k, gpresult);
                                  break;

                              default:
                                  senderStatus.put(k, new StatusMessage(true, new JsonObject()));
                                  break;
                          }

                      }
                  }


                  boolean needStoppedSending = false;

                  if (senderStatus.values().stream().allMatch(StatusMessage::status)) {
                      logger.log(Level.INFO, "all message send");
                      needStoppedSending = true;
                  }

                  nowTime = System.currentTimeMillis() / 1000;
                  if (nowTime - startTime > 300) {
                      logger.log(Level.WARNING, "Timeout senders send message with id " + message.id());
                      needStoppedSending = true;
                  }

                  if (needStoppedSending) {
                      break;
                  }

              }catch(SocketTimeoutException e){
                  logger.log(Level.WARNING,"Connect timeout.");
              }

                Thread.yield();
            }
        }catch(Exception e){
            logger.log(Level.SEVERE,"Exception. Detail:"+e.getMessage()+".class:"+e.getClass().getName());
        }

        logger.log(Level.INFO,"end thread");

        return senderStatus;

    }
}
