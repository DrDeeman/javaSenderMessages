package services.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import records.MailConsumer;

import java.lang.reflect.Type;

public class MailConsumerDeserializer implements JsonDeserializer<MailConsumer> {

    @Override
    public MailConsumer deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        return new MailConsumer(jsonObject.get("mail").getAsString());
    }
}
