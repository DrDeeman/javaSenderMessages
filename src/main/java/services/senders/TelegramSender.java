package services.senders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exceptions.CustomException;
import lombok.AllArgsConstructor;
import records.StatusMessage;
import records.consumers.TelegramConsumer;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@AllArgsConstructor
public class TelegramSender implements SenderInterface{

    private static final String TELEGRAM_TOKEN;
    private TelegramConsumer consumer;

    static{
        Properties props = new Properties();
        try {
            props.load(TelegramSender.class.getClassLoader().getResourceAsStream("application.properties"));
            TELEGRAM_TOKEN = props.getProperty("telegram.bot_token");
        } catch (IOException e) {
            throw new CustomException(e);
        }
    }

    @Override
    public StatusMessage sendMessage(String message) throws IOException {

        URL url = new URL(
                "https://api.telegram.org/bot" +
                        TELEGRAM_TOKEN +
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

        final int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            JsonObject body = new JsonObject();
            body.addProperty("code",200);
            return new StatusMessage(true, body);

        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream()
            ));

            String output;
            StringBuilder builder = new StringBuilder();

            while ((output = br.readLine()) != null) {
                builder.append(output);
            }

            JsonObject responseObject = JsonParser.parseString(builder.toString()).getAsJsonObject();
            int errorCode = responseObject.get("error_code").getAsInt();
            boolean status = errorCode == 401 || errorCode == 400;
            return new StatusMessage(status, responseObject);
        }

    }
}
