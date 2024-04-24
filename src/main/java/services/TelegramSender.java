package services;

import lombok.AllArgsConstructor;
import records.StatusMessage;
import records.TelegramConsumer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@AllArgsConstructor
public class TelegramSender {

    private static final String telegram_token;
    private TelegramConsumer consumer;

    static{
        Properties props = new Properties();
        try {
            props.load(ClassLoader.getSystemResourceAsStream("application.properties"));
            telegram_token = props.getProperty("telegram.bot_token");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public StatusMessage sendMessage(String message) throws MalformedURLException, IOException {
        URL url = new URL(
                "https://api.telegram.org/bot" +
                        this.telegram_token +
                        "/sendMessage");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setConnectTimeout(2000);
        conn.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        out.write(("chat_id=" +
                consumer.chat() +
                "&is_bot=true&text=" +
                URLEncoder.encode(message, StandardCharsets.UTF_8))
                .getBytes());
        out.flush();
        out.close();

        if (conn.getResponseCode() == 200) {
            return new StatusMessage(true, "Send message in telegram successful");

        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream()
            ));
            String all_output = "";
            String output;
            while ((output = br.readLine()) != null) {
                all_output +=output;
            }
            return new StatusMessage(false, "Send message in telegram failure. Detail:" + all_output);
        }
    }
}
