import lombok.AllArgsConstructor;
import lombok.NonNull;
import records.*;
import services.TelegramSender;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

@AllArgsConstructor
public class SenderThread implements Callable {

    @NonNull private final StructMessage message;



    @Override
    public Map<String, Boolean> call(){

        Logger logger = Logger.getLogger(SenderThread.class.getName()+"-"+message.id());
        logger.info("start messages sender");
        logger.setLevel(Level.INFO);

        Map<String,Boolean> senderStatus = new HashMap<>();
        Map<String, Record> consumers = message.consumers();
        long start_time = System.currentTimeMillis() / 1000;
        long now_time;

        consumers.keySet().forEach(k-> {
            if(consumers.get(k)!=null) {
                senderStatus.put(k, false);
            }
        });



        try{
            for (;;) {
                try {
                    for (String k : senderStatus.keySet()) {
                        if (!senderStatus.get(k)) {
                            Record consumer = consumers.get(k);

                            switch (consumer.getClass().getName()) {
                                case "records.TelegramConsumer":
                                    logger.info("start send in telegram");
                                    StatusMessage tresult = (new TelegramSender((TelegramConsumer) consumer)).sendMessage(message.message());
                                    logger.info(tresult.message());
                                    senderStatus.put(k, tresult.status());
                                    break;

                                case "records.WhatsappConsumer":
                                    /*
                                        logger.info("start send in whatsapp");
                                        StatusMessage wresult = (new WhatsappSender((WhatsappConsumer) consumer)).sendMessage(message.name());
                                        logger.info(wresult.message());
                                        senderStatus.put(k, wresult.status());

                                     */
                                    senderStatus.put(k,true);
                                    break;

                                case "records.MailConsumer":
                                    /*
                                    logger.info("start send in mail");
                                    StatusMessage mresult = (new MailSender((MailConsumer) consumer)).sendMessage(message.name());
                                    logger.info(mresult.message());
                                    senderStatus.put(k, mresult.status());

                                     */
                                    senderStatus.put(k,true);
                                    break;

                                case "records.GooglePushConsumer":
                                    /*
                                    logger.info("start send push in device");
                                    StatusMessage gpresult = (new GooglePushSender((GooglePushConsumer) consumer)).sendMessage(message.name());
                                    logger.info(gpresult.message());
                                    senderStatus.put(k, gpresult.status());

                                     */
                                    break;
                            }

                        }
                    }
                }
                /*
                catch(SocketTimeoutException e){
                    logger.severe(e.getMessage());
                }

                 */
                catch(NullPointerException e){
                    logger.severe(e.getMessage());
                }





                if(senderStatus.values().stream().filter(v->!v).count() == 0){
                    logger.info("all message send");
                    break;
                }

                now_time = System.currentTimeMillis() / 1000;
                if(now_time - start_time > 300) break;
                Thread.sleep(3000);
            }
        }catch(Exception e){
            logger.severe("Exception. Detail:"+e.getMessage()+".class:"+e.getClass().getName());
        }

        logger.info("end thread");

        return senderStatus;

    }
}
