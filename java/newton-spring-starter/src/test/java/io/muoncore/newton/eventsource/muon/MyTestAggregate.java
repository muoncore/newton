package io.muoncore.newton.eventsource.muon;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.eventsource.AggregateConfiguration;
import lombok.Getter;

@AggregateConfiguration(context = "user")
public class MyTestAggregate extends AggregateRoot<String> {

  @Getter
  String id;

}
