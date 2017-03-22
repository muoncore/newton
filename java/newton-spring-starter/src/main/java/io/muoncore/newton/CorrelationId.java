package io.muoncore.newton;

import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@ToString
public class CorrelationId {
    @Getter
    private UUID id = UUID.randomUUID();
    @Getter
    private CorrelationType correlationType;

    public static CorrelationId recreateFrom(String id, CorrelationType correlationType) {
        CorrelationId ret = new CorrelationId(correlationType);
        ret.id = UUID.fromString(id);
        return ret;
    }

    public CorrelationId(CorrelationType correlationType) {
        id = UUID.randomUUID();
        this.correlationType = correlationType;
    }

    public enum CorrelationType {
        SAGA(0), INDEPENDENT(1);

        CorrelationType(long type) {this.type = type;}

        @Getter
        private long type;

        public static CorrelationType from(long value) {
            if (value == 0) return SAGA;
            return INDEPENDENT;
        }
    }
}
