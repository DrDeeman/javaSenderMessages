package records;

import java.util.Map;

public record StructMessage(String name, Map<String,Record> consumers) {}
