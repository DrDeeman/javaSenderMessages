
import com.google.gson.*;

import com.google.gson.internal.LinkedTreeMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import records.StructMessage;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import records.TelegramConsumer;
import records.WhatsappConsumer;
import records.MailConsumer;
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
    Future<Map<String, Boolean>> f;
    boolean isDone;

    public FutureThreadWithIdRecord(int idRow, Future<Map<String, Boolean>> f){
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

    static{
        Properties props = new Properties();
        try {
            props.load(ClassLoader.getSystemResourceAsStream("application.properties"));
            BOOTSTRAP_SERVERS = props.getProperty("kafka.bootstrap_servers");
            DB_URL = props.getProperty("database.url");
            DB_USERNAME = props.getProperty("database.username");
            DB_PASSWORD = props.getProperty("database.password");
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    public static void main(String[] args) throws ExecutionException, InterruptedException {


        ExecutorService pool = Executors.newFixedThreadPool(SIZE_POOL);//создаем фиксируемый пул потоков
        FutureThreadWithIdRecord[] FuturePool = new FutureThreadWithIdRecord[SIZE_POOL];
        //создаем массив фьючерсов для ожидания результата. Выполненные задачи замещаются новыми либо создаются до границы пула.

        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaProps.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG,false);
        kafkaProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,5000);
        kafkaProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,1);
        kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG,"senders4");
        //настраиваем подключение к топику debezium

        long start = System.currentTimeMillis() / 1000;

        try(
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaProps);
                Connection conn = DriverManager.getConnection(DB_URL,DB_USERNAME,DB_PASSWORD);
        ) {

            TopicPartition part = new TopicPartition("postgres.public.test", 0);
            consumer.assign(Collections.singletonList(part));//подключаемся к топику
            System.out.println("Connect..");

            while (true) {


                for (int ind = 0; ind < SIZE_POOL; ind++) {

                    FutureThreadWithIdRecord futureInPool = FuturePool[ind];
                    if (futureInPool != null) {
                        Future<Map<String, Boolean>> f = futureInPool.getF();
                        if (f.isDone() && !f.isCancelled() && !futureInPool.isDone()) {
                            try (PreparedStatement sql = conn.prepareStatement("UPDATE test SET status_consumers=? WHERE id = ?")) {
                                String res = f.get().toString();
                                System.out.println(res);
                                PGobject jsonObject = new PGobject();
                                jsonObject.setType("jsonb");
                                jsonObject.setValue(res);
                                sql.setObject(1,jsonObject);
                                sql.setInt(2, futureInPool.getIdRow());
                                sql.executeUpdate();
                            }
                            System.out.println(f.get());
                            futureInPool.setDone(true);
                        }
                    }

                    if(futureInPool == null || futureInPool.isDone()) {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                        for (ConsumerRecord<String, String> r : records) {

                            JsonObject payload = JsonParser
                                    .parseString(r.value())
                                    .getAsJsonObject()
                                    .get("payload")
                                    .getAsJsonObject();

                            if (payload.get("op").getAsString().equals("c")) {
                                StructMessage trow = gson.fromJson(payload.get("after").toString(), StructMessage.class);
                                FuturePool[ind] = new FutureThreadWithIdRecord(
                                        trow.id(),
                                        pool.submit(new SenderThread(trow))
                                );

                            }
                        }

                        consumer.commitSync();
                    }
                }

                System.out.println("Searching..");
               // Thread.sleep(2000);
                Thread.yield();

            }
        } catch(SQLException e){
            System.out.println(e.getMessage());
            long end = System.currentTimeMillis() / 1000;
            System.out.println("End:"+(end-start));
            pool.shutdown();
        }
/*
        while(Arrays.stream(FuturePool).filter(g->!g.f().isDone()).count()!=0){
            //for tests
        }

 */






    }

}