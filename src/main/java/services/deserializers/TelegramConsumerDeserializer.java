package services.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import records.TelegramConsumer;

import java.lang.reflect.Type;

public class TelegramConsumerDeserializer implements JsonDeserializer<TelegramConsumer> {

    @Override
    public TelegramConsumer deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        return new TelegramConsumer(jsonObject.get("chat").getAsLong());
    }
}
