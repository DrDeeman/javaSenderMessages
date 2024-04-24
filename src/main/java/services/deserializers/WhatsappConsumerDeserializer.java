package services.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import records.WhatsappConsumer;

import java.lang.reflect.Type;

public class WhatsappConsumerDeserializer implements JsonDeserializer<WhatsappConsumer> {

    @Override
    public WhatsappConsumer deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        return new WhatsappConsumer(jsonObject.get("phone").getAsString());
    }
}