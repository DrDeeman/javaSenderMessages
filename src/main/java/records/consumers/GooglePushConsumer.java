package records.consumers;

import java.util.HashSet;

public record GooglePushConsumer(HashSet<String> tokens) {
}
