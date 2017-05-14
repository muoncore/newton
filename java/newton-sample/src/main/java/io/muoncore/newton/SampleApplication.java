package io.muoncore.newton;

import io.muoncore.newton.eventsource.muon.EventStreamProcessor;
import io.muoncore.newton.mongo.MongoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.function.Consumer;

@SpringBootApplication
@ComponentScan
@EnableAutoConfiguration
@EnableNewton
@Import(MongoConfiguration.class)
@Configuration
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

	@Bean
	public EventStreamProcessor eventStreamProcessor(){
	  return new EventStreamProcessor() {
      @Override
      public List<? extends NewtonEvent> processForPersistence(List<NewtonEvent> events) {
        return events;
      }

      @Override
      public List<? extends NewtonEvent> processForLoad(List<? extends NewtonEvent> events) {
        return events;
      }

      @Override
      public void executeWithinEventContext(NewtonEvent event, Consumer<NewtonEvent> exec) {
        exec.accept(event);
      }
    };
  }
}
