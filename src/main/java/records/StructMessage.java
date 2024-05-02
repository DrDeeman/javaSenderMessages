package records;

import java.util.Map;

public record StructMessage(int id, String user_name, String message, Map<String,Record> consumers) {}
