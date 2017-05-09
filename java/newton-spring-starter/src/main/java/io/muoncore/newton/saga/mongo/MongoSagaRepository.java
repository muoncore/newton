package io.muoncore.newton.saga.mongo;

import com.mongodb.BulkWriteResult;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.saga.Saga;
import io.muoncore.newton.saga.SagaCreated;
import io.muoncore.newton.saga.SagaInterest;
import io.muoncore.newton.saga.SagaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Slf4j
public class MongoSagaRepository implements SagaRepository {

	private MongoTemplate mongoTemplate;

	public MongoSagaRepository(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

  @Override
  public <T extends Saga> Optional<T> load(String sagaIdentifier, Class<T> type) {
		return Optional.ofNullable(
				mongoTemplate.findById(sagaIdentifier, type, "sagas"));
	}

	@Override
	public void save(Saga saga) {
		if (saga.isComplete()) {
			clearInterests(saga);
		}
		saga.getNewSagaInterests().forEach(sagaInterest -> registerEventExpectation((SagaInterest) sagaInterest));
		mongoTemplate.save(saga, "sagas");
	}

  @Override
  public void saveNewSaga(Saga saga, NewtonEvent ev) {
	  Assert.notNull(saga, "Saga is Required");
	  Assert.notNull(saga.getId(), "Saga ID is Required");
    save(saga);
    updateSagaCreated(saga, ev);
  }

  private void updateSagaCreated(Saga saga, NewtonEvent event) {
	  Query query = new Query();
	  query.addCriteria(Criteria.where("sagaId").is(saga.getId()));

    Update update = new Update();
    update.set("sagaClassName", saga.getClass().getName());
    update.set("sagaId", saga.getId());
    update.set("eventId", event.getId());

    mongoTemplate.upsert(query, update, SagaCreated.class);
  }

  private void clearInterests(Saga saga) {
		Query ops = new Query();

		ops.addCriteria(
				Criteria.where("sagaId").is(saga.getId())
		);

		BulkWriteResult execute = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, SagaInterest.class).remove(ops).execute();
		log.debug("Saga is complete, removed {} interests", execute.getRemovedCount());
	}

	public void registerEventExpectation(SagaInterest sagaInterest) {
		log.debug("Persisting saga interest " + sagaInterest);
		mongoTemplate.save(sagaInterest);
	}

	@Override
	public List<SagaInterest> getSagasInterestedIn(Class<? extends NewtonEvent> eventClass) {
		Query query = new Query();
		query.addCriteria(
				Criteria.where("className").is(eventClass.getName())
		);
		return mongoTemplate.find(query, SagaInterest.class);
	}

  @Override
  public List<SagaCreated> getSagasCreatedByEventId(Object id) {

    Query ops = new Query();

    ops.addCriteria(
      Criteria.where("eventId").is(id)
    );

	  return mongoTemplate.find(ops, SagaCreated.class);
  }
}
