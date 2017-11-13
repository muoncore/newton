package io.muoncore.newton.mongodb;


import io.muoncore.newton.*;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public abstract class MongoUniqueAggregateDomainService<V> implements UniqueAggregateDomainService<V> {

	private StreamSubscriptionManager streamSubscriptionManager;
	private MongoTemplate mongoTemplate;
	private DynamicInvokeEventAdaptor eventAdaptor = new DynamicInvokeEventAdaptor(this, EventHandler.class);

	public MongoUniqueAggregateDomainService(StreamSubscriptionManager streamSubscriptionManager, MongoTemplate mongoTemplate) throws IOException {
		this.streamSubscriptionManager = streamSubscriptionManager;
    this.mongoTemplate = mongoTemplate;
  }

	private void handleEvent(NewtonEvent event) {
		eventAdaptor.accept(event);
	}

  @org.springframework.context.event.EventListener
  public void onApplicationEvent(ApplicationReadyEvent onReadyEvent) {
    Arrays.stream(eventStreams()).forEach(stream-> streamSubscriptionManager.globallyUniqueSubscription(this.getClass().getSimpleName(),stream, this::handleEvent));
	}

  protected abstract String[] eventStreams();

  @Override
  public boolean isUnique(Object thisId, V value) {
		return !exists(thisId, value);
	}

  @Override
  public boolean exists(V value) {
	  return exists(null, value);
  }

  @Override
  public boolean exists(Object thisId, V value) {
		if (thisId == null) {
		  return this.mongoTemplate.exists(
		            new Query(Criteria.where("value").is(value).and("objType").is(this.getClass().getCanonicalName())),
                UniquenessConstraintEntry.class
      );
		} else {
      return this.mongoTemplate.exists(
                new Query(Criteria.where("value").is(value).and("objType").is(this.getClass().getCanonicalName()).and("objId").ne(thisId)),
                UniquenessConstraintEntry.class
      );
    }
	}

	@Override
  public void addValue(Object id, V value) {
  		this.mongoTemplate.save(new UniquenessConstraintEntry<>(id,this.getClass().getCanonicalName(),value));
	}

	@Override
  public void removeValue(Object id) {
		this.mongoTemplate.remove(new Query(Criteria.where("objId").is(id).and("objType").is(this.getClass().getCanonicalName())), UniquenessConstraintEntry.class);
	}

	@Override
  public void updateValue(Object id, V value) {
    if (exists(id,value)){
      throw new IllegalArgumentException("Unable to update uniqueness constraint with new value. Existing constraint with the same value exists");
    }
    Update update = new Update();
    update.set("value",value);
    this.mongoTemplate.updateFirst(new Query(Criteria.where("objId").is(id).and("objType").is(this.getClass().getCanonicalName())),update,UniquenessConstraintEntry.class);
	}

  @Override
  public Optional<Object> find(V value) {
    return Optional.ofNullable(
      this.mongoTemplate.findOne(new Query(Criteria.where("objValue").is(value).and("objType").is(this.getClass().getCanonicalName())), UniquenessConstraintEntry.class));
  }

  @Data
  @Document(collection = "_uniqueness_constraint")
	private static class UniquenessConstraintEntry<V> {

    @Indexed @NonNull private Object objId;
    @Indexed @NonNull private String objType;
    @NonNull private V value;

  }

}
