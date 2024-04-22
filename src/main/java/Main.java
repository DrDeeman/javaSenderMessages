import records.MailConsumer;
import records.StructMessage;
import records.TelegramConsumer;
import records.WhatsappConsumer;


import java.util.*;
import java.util.concurrent.*;


public class Main {


    public static void main(String[] args) {

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
    }
}