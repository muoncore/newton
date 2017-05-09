package io.muoncore.newton.todo;

import io.muoncore.newton.EventHandler;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.eventsource.muon.EventStreamProcessor;
import io.muoncore.newton.query.NewtonView;
import io.muoncore.newton.query.SharedDatastoreView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
//@NewtonView(streams = "newton-sample/Todo")
@NewtonView(aggregateRoot = {Todo.class})
public class TodoViewStore extends SharedDatastoreView {

  private MongoTemplate mongoTemplate;

  @Autowired
  public TodoViewStore(StreamSubscriptionManager streamSubscriptionManager, EventStreamProcessor eventStreamProcessor, MongoTemplate mongoTemplate1) throws IOException {
    super(streamSubscriptionManager, eventStreamProcessor);
    this.mongoTemplate = mongoTemplate1;
  }

  @EventHandler
  public void handle(TodoCreatedEvent event){
      //todo: remove once bug resolved
//      if (event.getDescription().equals("exception")){
//        throw new RuntimeException("Test exception");
//      }
      mongoTemplate.save(new TodoListView(event.getId(),event.getDescription()));
  }

  @EventHandler
  public void handle(TodoDescriptionChangedEvent event){
    Update update = new Update();
    update.set("description",event.getDescription());
    mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(event.getId().getObjectId())), update, Todo.class);
  }

}
