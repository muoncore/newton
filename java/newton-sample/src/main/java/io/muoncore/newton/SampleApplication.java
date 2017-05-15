package io.muoncore.newton;

import io.muoncore.newton.todo.TenantEvent;
import io.muoncore.newton.eventsource.muon.EventStreamProcessor;
import io.muoncore.newton.mongo.MongoConfiguration;
import io.muoncore.newton.support.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        if (event instanceof TenantEvent){
          final String tenantId = ((TenantEvent) event).getTenantId();
          log.info("Setting tenant context: '{}'", tenantId);
          TenantContextHolder.setTenantId(tenantId);
        }
        exec.accept(event);
      }
    };
  }
}
