package io.muoncore.newton.todo.composite;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.query.SharedDatastoreView;
import io.muoncore.newton.support.DocumentId;
import io.muoncore.newton.todo.Task;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@Component
@Slf4j
public class TaskViewSummaryStore extends SharedDatastoreView {

  private MongoTemplate mongoTemplate;

  @Autowired
  public TaskViewSummaryStore(StreamSubscriptionManager streamSubscriptionManager, MongoTemplate mongoTemplate1) throws IOException {
    super(streamSubscriptionManager);
    this.mongoTemplate = mongoTemplate1;
  }

  @Override
  protected Collection<Class<? extends AggregateRoot>> aggregateRoots() {
    return Collections.singletonList(Task.class);
  }


  @EventHandler
  public void handle(TaskCompositeEvent event) {
    TaskSummary summary = new TaskSummary();
    summary.setId(event.getId());
    summary.setDesc(event.getDescription());
    this.mongoTemplate.save(summary);
    log.info("Saving task summary");
  }


  @Document
  @Data
  public static class TaskSummary{

    private DocumentId id;
    private String desc;
  }
}
