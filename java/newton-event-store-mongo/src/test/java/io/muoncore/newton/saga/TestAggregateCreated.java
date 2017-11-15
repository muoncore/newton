package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TestAggregateCreated implements NewtonEvent {

	private String id;
}
