package io.muoncore.newton.todo;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.query.SharedDatastoreView;
import io.muoncore.newton.support.DocumentId;
import io.muoncore.newton.support.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class TaskViewStore extends SharedDatastoreView {

  private List<TaskView> views = new ArrayList<>();

//  private MongoTemplate mongoTemplate;

  @Autowired
  public TaskViewStore(StreamSubscriptionManager streamSubscriptionManager/*, MongoTemplate mongoTemplate1*/) throws IOException {
    super(streamSubscriptionManager);
//    this.mongoTemplate = mongoTemplate1;
  }

//  @Override
//  protected Collection<Class<? extends AggregateRoot>> aggregateRoots() {
//    return Collections.singletonList(Task.class);
//  }


  @Override
  protected String[] getStreams() {
    return new String[]{"newton-sample/Task"};
  }

  @Override
  protected Collection<Class<? extends AggregateRoot>> aggregateRoots() {
    return Collections.singletonList(Task.class);
  }

  public TaskView findById(DocumentId id) {
    //return mongoTemplate.findById(id, TaskView.class);
    return views.stream().filter(taskView -> taskView.getId().equals(id)).findFirst().orElse(null);
  }

  public List<TaskView> listAll() {
    //return mongoTemplate.findAll(TaskView.class);
    return views;
  }

  @EventHandler
  public void handle(TaskCreatedEvent event) {
//    if (TenantContextHolder.getTenantId() == null){
//      throw new IllegalStateException("Tenant context unavailable!!!!");
//    }
//    mongoTemplate.save(new TaskView(event.getId(), event.getDescription(), TenantContextHolder.getTenantId()));

    views.add(new TaskView(event.getId(), event.getDescription(), TenantContextHolder.getTenantId()));
  }

  @EventHandler
  public void handle(TaskDescriptionChangedEvent event) {
    log.info("Processing description update into view " + event);
//    Update update = new Update();
//    update.set("description", event.getDescription());
//    mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(event.getId().getObjectId())), update, Task.class);
    findById(event.getId()).setDescription(event.getDescription());
  }
}
