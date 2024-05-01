package services.senders;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.gson.JsonObject;
import exceptions.CustomException;
import lombok.AllArgsConstructor;
import records.StatusMessage;
import records.consumers.GooglePushConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@AllArgsConstructor
public class GooglePushSender implements SenderInterface{

    private final GooglePushConsumer consumer;
    private static final FirebaseMessaging FCM;

    static{

        try {
            ClassLoader loader = GooglePushSender.class.getClassLoader();
            InputStream stream = loader.getResourceAsStream("firebase-account.json");
            if(stream == null) throw new IOException("Not found file");

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream);
            FirebaseOptions firebaseOptions = FirebaseOptions
                    .builder()
                    .setCredentials(googleCredentials)
                    .build();

            FCM = FirebaseMessaging.getInstance(FirebaseApp.initializeApp(firebaseOptions, "sender"));

        } catch (IOException e) {
            throw new CustomException(e);
        }

    }



    @Override
    public StatusMessage sendMessage(String message) {

        List<Boolean> results = new ArrayList<>();

        Notification note = Notification.builder()
                .setTitle("Sputnic.tech")
                .setBody(message)
                .build();

        for(String token : consumer.tokens()) {

         try{

         Message googleMessage = Message.builder()
                 .setToken(token)
                 .setNotification(note)
                 .build();

         FCM.send(googleMessage);

         results.add(true);

      } catch(FirebaseMessagingException e){
             results.add(false);
         }
     }


     String description = results.stream().map(status->status?"Send":"Not send").collect(Collectors.joining(","));

     JsonObject statusObject = new JsonObject();
     statusObject.addProperty("description",description);

        return new StatusMessage(true, statusObject);
    }
}
