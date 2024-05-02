package services.deserializers;

import com.google.gson.*;
import records.consumers.GooglePushConsumer;
import records.consumers.MailConsumer;
import records.StructMessage;
import records.consumers.TelegramConsumer;
import records.consumers.WhatsappConsumer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MessageDeserializer implements JsonDeserializer<StructMessage> {

    @Override
    public StructMessage deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context){
        JsonObject jsonObject = json.getAsJsonObject();

        JsonObject jsonConsumers = JsonParser
                .parseString(jsonObject.getAsJsonPrimitive("consumers").getAsString())
                .getAsJsonObject();

        Map<String,Class<?>> mapClass = new HashMap<>();
        mapClass.put("telegram",TelegramConsumer.class);
        mapClass.put("whatsapp",WhatsappConsumer.class);
        mapClass.put("mail", MailConsumer.class);
        mapClass.put("google_push", GooglePushConsumer.class);

        HashMap<String, Record> consumers = new HashMap<>();

        for(Map.Entry<String, JsonElement> entry: jsonConsumers.entrySet()){
            String key = entry.getKey();
            consumers.put(key,context.deserialize(entry.getValue(), mapClass.get(key)));
        }

        return new StructMessage(
                jsonObject.get("id").getAsInt(),
                jsonObject.get("user_name").getAsString(),
                jsonObject.get("message").getAsString(),
                consumers
                );
    }
}
