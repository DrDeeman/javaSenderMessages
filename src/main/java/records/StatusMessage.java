package records;

import com.google.gson.JsonObject;

public record StatusMessage(Boolean status, JsonObject bodyResponse) {
}
