package io.muoncore.newton.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;

//@Document // Todo - not sure what to do with this
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventStreamIndex {

	@Id
	private String stream;
	private long lastSeen;
}
