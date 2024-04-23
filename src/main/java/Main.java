import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;


import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.util.*;




public class Main {

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
            DB_USERNAME = props.getProperty("database.username");;
            DB_PASSWORD = props.getProperty("database.password");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {




        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaProps.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG,false);
        kafkaProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,5000);
        kafkaProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,1);
        kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        kafkaProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"1000");

        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG,"senders3");
        try(
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaProps);
                Connection conn = DriverManager.getConnection(DB_URL,DB_USERNAME,DB_PASSWORD);
                ){
            consumer.subscribe(Collections.singletonList("postgres.public.test"));
            System.out.println("Connect..");


            while(true){
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(2000));

                for(ConsumerRecord<String, String> record : records){
                       System.out.println(record.value());
                         JSONObject t =  new JSONObject(record.value())
                            .getJSONObject("payload")
                            .getJSONObject("after");
                         System.out.println("Get object with id "+t.get("id"));
                        try(PreparedStatement sql = conn.prepareStatement("UPDATE test SET status_consumers='{\"status\":true}' WHERE id = ?")){
                            sql.setInt(1,t.getInt("id"));
                            sql.executeUpdate();
                        }



                }

                //consumer.commitAsync();

                System.out.println("Searching..");
                Thread.sleep(2000);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }



/*
        ExecutorService pool = Executors.newFixedThreadPool(5);
        List<Future> test_pool = new ArrayList<>();
        long start = System.currentTimeMillis() / 1000;
        for(int i = 0; i < 1; i++){
            Map<String,Record> map1 = new HashMap<>();
            map1.put("telegram", new TelegramConsumer(6288237005L));
            map1.put("whatsapp", new WhatsappConsumer("79023789604"));
            map1.put("mail", new MailConsumer("stalkerdrdeeman@gmail.com"));
            StructMessage m = new StructMessage("user"+i,map1);
            test_pool.add(pool.submit(new SenderThread(m)));
            //FutureTask<String> goal = new FutureTask<String>(new SenderThread(m));
          //  test_pool.add(goal);
           // new Thread(goal).start();
        }

        while(test_pool.stream().filter(g->!g.isDone()).count()!=0){
            //for tests
        }
        long end = System.currentTimeMillis() / 1000;
        System.out.println("End:"+(end-start));
        pool.shutdown();

 */
    }

}