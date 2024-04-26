package services.senders;

import services.CustomLogger;

import java.io.IOException;
import java.util.Properties;

public class GooglePushSender {

    private static final String push_token;

    static{
        Properties props = new Properties();
        try {
            props.load(GooglePushSender.class.getClassLoader().getResourceAsStream("application.properties"));
            push_token = props.getProperty("google_cloud.token");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
