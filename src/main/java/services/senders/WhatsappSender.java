package services.senders;

import com.google.gson.JsonObject;
import exceptions.CustomException;
import lombok.AllArgsConstructor;
import records.StatusMessage;
import records.StructMessage;
import records.consumers.WhatsappConsumer;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@AllArgsConstructor
public class WhatsappSender implements SenderInterface{

    private static final String WHATSAPP_TOKEN;
    private WhatsappConsumer consumer;

    static{
        Properties props = new Properties();
        try {
            props.load(WhatsappSender.class.getClassLoader().getResourceAsStream("application.properties"));
            WHATSAPP_TOKEN = props.getProperty("whatsapp.token");
        } catch (IOException e) {
            throw new CustomException(e);
        }
    }

    private static final Map<Integer, String> ERRORS_MESSAGES;

    static{
        ERRORS_MESSAGES = new HashMap<>();
        ERRORS_MESSAGES.put(400,"instance account not authorized or instance in starting process try later or bad request data");
        ERRORS_MESSAGES.put(401,"Unauthorized");
        ERRORS_MESSAGES.put(403,"Forbidden");
        ERRORS_MESSAGES.put(429,"Too Many Requests");
        ERRORS_MESSAGES.put(466,"correspondentsStatus");
        ERRORS_MESSAGES.put(499,"Client Closed Request");
        ERRORS_MESSAGES.put(500,"File from url exceeded max upload size. Size: XXXXmb Limit: 100mb Url");
        ERRORS_MESSAGES.put(502,"Bad Gateway");
    }

    @Override
    public StatusMessage sendMessage(StructMessage message)throws IOException {
        URL url = new URL(
                "https://api.green-api.com/waInstance1101731151/SendMessage/" +
                        WHATSAPP_TOKEN
        );
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        conn.setDoOutput(true);
        conn.setConnectTimeout(2000);
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        JsonObject params = new JsonObject();
        params.addProperty("chatId",consumer.phone() + "@c.us");
        params.addProperty("message",message.message().replace("__","\n"));
        out.write((params.toString()).getBytes());
        out.flush();
        out.close();

        final int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            JsonObject body = new JsonObject();
            body.addProperty("code",responseCode);
            return new StatusMessage(true, body);

        } else {
            conn.disconnect();

            JsonObject responseObject = new JsonObject();
            responseObject.addProperty("error_code",responseCode);
            responseObject.addProperty("description",ERRORS_MESSAGES.getOrDefault(responseCode,"Unknown"));
            return new StatusMessage(false, responseObject);
        }
    }
}
