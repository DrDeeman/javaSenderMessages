package records;

import java.util.Map;

public record StructMessage(int id, String message, Map<String,Record> consumers) {}
