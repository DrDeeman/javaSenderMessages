package services.deserializers;

import com.google.gson.*;
import records.consumers.GooglePushConsumer;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;


public class GooglePushConsumerDeserializer implements JsonDeserializer<GooglePushConsumer> {

    @Override
    public GooglePushConsumer deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context) {
        HashSet<String> tokens = new HashSet<>();

        Iterator<JsonElement> iterator = json.getAsJsonObject().getAsJsonArray("tokens").iterator();
        while(iterator.hasNext()){
            tokens.add(iterator.next().getAsString());
        }

        return new GooglePushConsumer(tokens);
    }
}
