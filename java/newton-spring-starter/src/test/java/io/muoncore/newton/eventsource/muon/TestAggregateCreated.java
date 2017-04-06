package io.muoncore.newton.eventsource.muon;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TestAggregateCreated implements NewtonEvent {

	private DocumentId id;
}
