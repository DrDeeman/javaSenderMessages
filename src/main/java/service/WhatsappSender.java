package service;

import lombok.AllArgsConstructor;
import org.json.JSONObject;
import records.StatusMessage;
import records.WhatsappConsumer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
public class WhatsappSender {

    private static final String whatsapp_token = "087d73f6c5114306b0eaac216df5c99a554667262e26454bae";
    private WhatsappConsumer consumer;

    public StatusMessage sendMessage(String message)throws MalformedURLException, IOException {
        URL url = new URL(
                "https://api.green-api.com/waInstance1101731151/SendMessage/" +
                        this.whatsapp_token
        );
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        conn.setDoOutput(true);
        conn.setConnectTimeout(2000);
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        JSONObject params = new JSONObject();
        params.put("chatId",consumer.phone() + "@c.us");
        params.put("message",message);
        out.write((params.toString()).getBytes());
        out.flush();
        out.close();

        if (conn.getResponseCode() == 200) {
            return new StatusMessage(true, "Send message in whatsapp successful");

        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream()
            ));
            String all_output = "";
            String output;
            while ((output = br.readLine()) != null) {
                all_output +=output;
            }
            conn.disconnect();
            return new StatusMessage(false, "Send message in whatsapp failure. Detail:" + all_output);
        }
    }
}
