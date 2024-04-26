package app;

import com.google.gson.*;

import exceptions.CustomException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.postgresql.util.PGobject;
import records.StatusMessage;
import records.StructMessage;


import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;


import records.consumers.TelegramConsumer;
import records.consumers.WhatsappConsumer;
import records.consumers.MailConsumer;
import services.CustomLogger;
import services.deserializers.MailConsumerDeserializer;
import services.deserializers.MessageDeserializer;
import services.deserializers.TelegramConsumerDeserializer;
import services.deserializers.WhatsappConsumerDeserializer;

@Getter
@Setter
class FutureThreadWithIdRecord{
    @Setter(AccessLevel.PRIVATE)
    int idRow;
    @Setter(AccessLevel.PRIVATE)
    Future<Map<String, StatusMessage>> f;
    boolean isDone;

    public FutureThreadWithIdRecord(int idRow, Future<Map<String, StatusMessage>> f){
        this.idRow = idRow;
        this.f = f;
        this.isDone =false;
    }
}


public class Main {

    private static final int SIZE_POOL = 5;
    private static final String BOOTSTRAP_SERVERS;
    private static final String DB_URL;
    private static final String DB_USERNAME;
    private static final String DB_PASSWORD;
    private static final String TOPIC_MESSAGES;

    static{
        Properties props = new Properties();
        try {
            props.load(Main.class.getClassLoader().getResourceAsStream("application.properties"));
            BOOTSTRAP_SERVERS = props.getProperty("kafka.bootstrap_servers");
            DB_URL = props.getProperty("database.url");
            DB_USERNAME = props.getProperty("database.username");
            DB_PASSWORD = props.getProperty("database.password");
            TOPIC_MESSAGES = props.getProperty("kafka.topic_messages");
        } catch (IOException e) {
            throw new CustomException(e);
        }
    }


    private static final Gson gson;

    static{
        gson = new GsonBuilder()
                .registerTypeAdapter(StructMessage.class, new MessageDeserializer())
                .registerTypeAdapter(TelegramConsumer.class, new TelegramConsumerDeserializer())
                .registerTypeAdapter(WhatsappConsumer.class, new WhatsappConsumerDeserializer())
                .registerTypeAdapter(MailConsumer.class, new MailConsumerDeserializer())
                .create();
    }


    private static final Properties kafkaProps;

    static{
        kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaProps.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG,false);
        kafkaProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,5000);
        kafkaProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,1);
        kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG,"senders");
        //настраиваем подключение к топику debezium
    }


    private static final Properties DB_PROPS;

    static{
        DB_PROPS = new Properties();
        DB_PROPS.put("user",DB_USERNAME);
        DB_PROPS.put("password",DB_PASSWORD);
        DB_PROPS.put("connectTimeout",1);

    }


    private static boolean checkGoal(FutureThreadWithIdRecord futureInPool){
        Future<Map<String, StatusMessage>> f = futureInPool.getF();
        return f.isDone() && !f.isCancelled() && !futureInPool.isDone();
    }


    public static void main(String[] args) throws IOException {

        CustomLogger logger = new CustomLogger("app.Main");
        ExecutorService pool = Executors.newFixedThreadPool(SIZE_POOL);//создаем фиксируемый пул потоков
        FutureThreadWithIdRecord[] futurePool = new FutureThreadWithIdRecord[SIZE_POOL];
        //создаем массив фьючерсов для ожидания результата. Выполненные задачи замещаются новыми либо создаются до границы пула.

        KafkaConsumer<String, String> tconsumer = null;

        long timeFirstConnect = System.currentTimeMillis() / 1000;
        int  countReconectJDBC = 0;

        for(;;) {

            logger.log(Level.WARNING,"Reconnect...");
            boolean needLeave = false;

            try (
                    KafkaConsumer<String, String> consumer = tconsumer = (tconsumer == null?new KafkaConsumer<>(kafkaProps):tconsumer);
                    Connection conn = DriverManager.getConnection(DB_URL, DB_PROPS);
            ) {


                TopicPartition part = new TopicPartition(TOPIC_MESSAGES, 0);
                consumer.assign(Collections.singletonList(part));//подключаемся к топику
                logger.log(Level.INFO, "Connect..");

                while (true) {


                    for (int ind = 0; ind < SIZE_POOL; ind++) {//постоянно проходимся циклом по пулу

                        FutureThreadWithIdRecord futureInPool = futurePool[ind];
                        if (futureInPool != null) { //если задача была назначена по этому индексу
                            Future<Map<String, StatusMessage>> f = futureInPool.getF();
                            if (checkGoal(futureInPool)) {
                                //если поток завершил работу и не был прерван и лог в базу записан не был
                                try (PreparedStatement sql = conn.prepareStatement("UPDATE events_messages SET datetime=NOW(), status_consumers=? WHERE id = ?")) {

                                    PGobject jsonObject = new PGobject();
                                    jsonObject.setType("jsonb");
                                    jsonObject.setValue(gson.toJson(f.get()));
                                    sql.setObject(1, jsonObject);
                                    sql.setInt(2, futureInPool.getIdRow());
                                    sql.executeUpdate();
                                }

                                futureInPool.setDone(true);//устанавливаем что данная задача полностью завершила работу
                            }
                        }

                        if (futureInPool == null || futureInPool.isDone()) {//если объект задачи не был назначен или работу завершил
                            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));//делаем опрос в течении секунды

                            if (records.count() > 0) {
                                ConsumerRecord<String, String> r = records.iterator().next();

                                JsonObject payload = JsonParser
                                        .parseString(r.value())
                                        .getAsJsonObject()
                                        .get("payload")
                                        .getAsJsonObject();

                                if (payload.get("op").getAsString().equals("c")) {//фильтруем события таблицы и работаем только с INSERT
                                    StructMessage trow = gson.fromJson(payload.get("after").toString(), StructMessage.class);
                                    //преобразовываем payload json в POJO
                                    futurePool[ind] = new FutureThreadWithIdRecord(
                                            trow.id(),
                                            pool.submit(new SenderThread(trow))
                                    );//создаем объект задачи и сразу запускаем поток

                                }
                            }

                            consumer.commitSync();//синхронно фиксируем смещение
                        }
                    }

                    Thread.yield();//после проходки по циклу отдаем процессорное время другим потокам

                }
            }
            catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage());

                if(countReconectJDBC>5){
                    long timeEndConnect = System.currentTimeMillis() / 1000;

                    if(timeEndConnect  - timeFirstConnect < 60) {
                        logger.log(Level.SEVERE, "Connection sets too many... shutdown");
                        needLeave = true;
                    } else {
                        countReconectJDBC = 0;
                        timeFirstConnect = timeEndConnect;
                    }
                }

                countReconectJDBC++;
            }

            catch(Exception e){
                logger.log(Level.SEVERE, e.getMessage());
                needLeave = true;
            }

            if(needLeave){
                break;
            }

        }

        pool.shutdown();//при возникновении исключения завершаем работу всех потоков
/*
        while(Arrays.stream(FuturePool).filter(g->!g.f().isDone()).count()!=0){
            //for tests
        }

 */

    }

}